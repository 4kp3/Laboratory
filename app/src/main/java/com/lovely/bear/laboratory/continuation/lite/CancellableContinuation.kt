package com.lovely.bear.laboratory.continuation.lite

import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.Continuation
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resumeWithException

/**
 * 可取消的协程，同时有SafeContinuation的同步功能。
 * 可以注册一个取消时的回调，供外部响应取消。
 *
 * @author guoyixiong
 */
class CancellableContinuation<T>(
    val delegate: Continuation<T>
) : Continuation<T> by delegate {

    private val state: AtomicReference<CancelState> = AtomicReference(CancelState.Incomplete)
    private val decision: AtomicReference<CancelDecision> =
        AtomicReference(CancelDecision.UNDECIDED)

    /**
     * 协程取消的关键，内部协程需要检查此状态，在true的时候，取消自己。
     */
    val isCancelled: Boolean = false

    val isCompleted: Boolean
        get() = when (state.get()) {
            CancelState.Incomplete, is CancelState.CancelHandler -> false
            is CancelState.Complete<*>, CancelState.Cancelled -> true
        }

    /**
     * 协程完结，修改状态
     */
    override fun resumeWith(result: Result<T>) {
        when {
            //这个分支是同步调用，稍后外部会调用get方法取值
            decision.compareAndSet(CancelDecision.UNDECIDED, CancelDecision.RESUMED) -> {
                state.set(CancelState.Complete(result.getOrNull(), result.exceptionOrNull()))
            }
            //已经挂起，异步调用
            decision.compareAndSet(CancelDecision.SUSPENDED, CancelDecision.RESUMED) -> {
                state.updateAndGet { pre ->
                    when (pre) {
                        is CancelState.Complete<*> -> {
                            throw IllegalStateException("Already completed.")
                        }
                        else -> CancelState.Complete(result.getOrNull(), result.exceptionOrNull())
                    }
                }
                //恢复协程，继续延续
                delegate.resumeWith(result)
            }

        }
    }

    /**
     * 取得结果的方法
     */
    fun getOrThrow(): Any? {
        installCancelHandler()

        val cor_suspended = kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED

        //在执行完 block(safe)，后该方法会立即被调用以判断内部协程是否设置了结果，如果未设置结果，这里会立即返回，实现挂起
        if (decision.compareAndSet(CancelDecision.UNDECIDED, CancelDecision.SUSPENDED)) {
            //SafeContinuation
            return cor_suspended
        }
        return when (val curr = state.get()) {
            is CancelState.CancelHandler, CancelState.Incomplete -> cor_suspended
            CancelState.Cancelled -> throw CancellationException("协程已被取消")
            is CoroutineState.Complete<*> -> {
                (curr as CancelState.Complete<T>).let {
                    it.value ?: it.e
                }
            }
            else -> throw CancellationException("not come")
        }
    }

    //供外部注册一个取消时的回调，注意，此种类型的回调只能注册一个
    fun invokeOnCancellation(onCancel: OnCancel) {
        val newState = state.updateAndGet {
            when (it) {
                is CancelState.Incomplete -> CancelState.CancelHandler(onCancel)
                is CancelState.CancelHandler -> throw IllegalStateException("CancelHandler不能重复注册")
                else -> it
            }
        }
        if (newState is CancelState.Cancelled) {
            onCancel()
        }
    }

    /**
     * 向Job中注册取消的回调
     */
    private fun installCancelHandler() {
        if (isCompleted) return
        context[Job]?.invokeOnCancel {
            //回调时立即改变自己的状态为
            doCancel()
        }
    }

    /**
     * 最终需要把当前状态设置为 [CancelState.Cancelled]
     */
    private fun doCancel() {
        if (isCompleted || isCancelled) return
        val pre = state.getAndUpdate {
            when (it) {
                is CancelState.Incomplete,
                is CancelState.CancelHandler -> CancelState.Cancelled
                else -> it
            }
        }

        if (pre is CancelState.CancelHandler) {
            pre.onCancel()
            resumeWithException(CancellationException("Cancelled"))
        }
    }
}

sealed class CancelState {
    object Incomplete : CancelState()
    class CancelHandler(val onCancel: OnCancel) : CancelState()//等于Cancelling
    object Cancelled : CancelState()
    class Complete<T>(val value: T? = null, val e: Throwable? = null) : CancelState()
}

enum class CancelDecision {
    UNDECIDED, SUSPENDED, RESUMED
}