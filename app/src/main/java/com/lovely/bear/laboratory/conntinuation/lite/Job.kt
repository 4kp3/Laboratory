package com.lovely.bear.laboratory.conntinuation.lite

import io.reactivex.rxjava3.disposables.Disposable
import java.lang.IllegalStateException
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.*
import kotlin.coroutines.cancellation.CancellationException

interface Job : CoroutineContext.Element {
    companion object Key : CoroutineContext.Key<Job>

    override val key: CoroutineContext.Key<*>
        get() = Job

    val isActive: Boolean

    fun invokeOnCancel(onCancel: OnCancel): Disposable
    fun invokeOnCompletion(onComplete: OnComplete): Disposable
    fun cancel()
    fun remove(disposable: Disposable)
    suspend fun join()
}

/**
 * 技术点：
 * 并发中状态的更新使用cas
 * 同时每次增改回调时采用新建状态对象方式，在原有回调基础上增删回调
 */
abstract class AbstractCoroutine<T>(context: CoroutineContext) : Job, Continuation<T> {
    protected val state = AtomicReference<CoroutineState>(CoroutineState.Incomplete())

    override val context: CoroutineContext

    init {
        this.context = context + this
    }

    val isCompleted: Boolean
        get() = state.get() is CoroutineState.Incomplete

    override val isActive: Boolean
        get() = when (state.get()) {
            is CoroutineState.Complete<*>,
            is CoroutineState.Cancelling -> false
            else -> true
        }

    override fun invokeOnCancel(onCancel: OnCancel): Disposable {
        val disposable = CancelHandlerDisposable(this) { onCancel() }
        val newState = state.updateAndGet {
            when (it) {
                is CoroutineState.Incomplete -> CoroutineState.Incomplete().from(it)
                    .with(disposable)
                is CoroutineState.Cancelling,
                is CoroutineState.Complete<*> -> it
            }
        }
        //已经被取消，立即执行回调
        (newState as? CoroutineState.Cancelling)?.let {
            onCancel()
        }
        return disposable
    }

    override fun invokeOnCompletion(onComplete: OnComplete): Disposable {
        return doOnCompleted { onComplete() }
    }

    protected fun doOnCompleted(block: (Result<T>) -> Unit): Disposable {
        val disposable = CompletionHandlerDisposable(this, block)

        /**
         * 并发安全的关键在于，这里不会在原有状态上修改（增加disposable），而是创建新的状态
         * updateAndGet 方法副作用是并发情况下，块可能会被多次执行
         */
        val newState = state.updateAndGet { pre ->
            when (pre) {
                is CoroutineState.Incomplete -> CoroutineState.Incomplete().from(pre)
                    .with(disposable)
                is CoroutineState.Cancelling -> CoroutineState.Cancelling().from(pre)
                    .with(disposable)
                is CoroutineState.Complete<*> -> pre
            }
        }

        //新状态如果是 [CoroutineState.Complete] 立即执行回调
        if (newState is CoroutineState.Complete<*>) {
            block(
                when {
                    newState.value != null -> Result.success(newState.value as T)
                    newState.exception != null -> Result.failure(newState.exception)
                    else -> throw IllegalStateException("")
                }
            )
        }

        return disposable
    }

    override fun resumeWith(result: Result<T>) {
        val newState = state.updateAndGet {
            when (it) {
                is CoroutineState.Complete<*> -> throw IllegalStateException("Job 已结束，但 resumeWith 方法被重复调用")
                is CoroutineState.Cancelling,
                is CoroutineState.Incomplete,
                -> CoroutineState.Complete(
                    value = result.getOrNull(),
                    exception = result.exceptionOrNull()
                ).from(it)
            }
        }

        newState.notifyCompletion(result)
        newState.clear()
    }

    override fun cancel() {
        val pre = state.getAndUpdate {
            when (it) {
                //只有当下是未完成状态时，才有状态流转
                is CoroutineState.Incomplete -> CoroutineState.Cancelling().from(it)
                else -> it
            }
        }
        //只会执行一次
        if (pre is CoroutineState.Incomplete) {
            //创建一个取消异常
            pre.notifyCancel()
            pre.clear()
        }
    }

    override fun remove(disposable: Disposable) {
        state.updateAndGet {
            when (it) {
                is CoroutineState.Incomplete -> CoroutineState.Incomplete().from(it)
                    .with(disposable)
                is CoroutineState.Cancelling -> CoroutineState.Cancelling().from(it)
                    .with(disposable)
                is CoroutineState.Complete<*> -> it
            }
        }
    }

    /**
     * 外部协程等待自己完成状态
     * 如果自己已经结束[CoroutineState.Complete]，则立即恢复，否则增加一个回调，挂起当前协程。
     */
    override suspend fun join() {

        //挂起后注册一个完成时的回调，在当前协程完成时恢复外部协程
        suspend fun suspendJoin() {
            suspendCancellableCoroutine<Unit> { continuation ->
                val disposable = doOnCompleted { continuation.resume(Unit) }
                //外部协程取消时，内部即使完成也不会再回调监听
                continuation.invokeOnCancellation {
                    disposable.dispose()
                }
            }
        }

        when (state.get()) {
            is CoroutineState.Incomplete,
            is CoroutineState.Cancelling -> suspendJoin()//未完成，挂起外部协程，并注册完成时的回调
            is CoroutineState.Complete<*> -> {
                /**
                 * 如果外部协程已取消，则抛出取消异常
                 * 注意此处的 [coroutineContext] 取得的是调用join方法的外部协程的上下文
                 */
                val currentCallingJobState = coroutineContext[Job]?.isActive?:return
                if (!currentCallingJobState) {
                    throw CancellationException("Coroutine is cancelled")
                }
                return//已完成直接返回
            }
        }

    }
}

typealias OnComplete = () -> Unit
typealias OnCancel = () -> Unit