package com.lovely.bear.laboratory.continuation.lite

import kotlin.coroutines.*
import kotlin.coroutines.cancellation.CancellationException

/**
 * 有返回值的Job
 * @author guoyixiong
 */
interface Deferred<T> : Job {

    /**
     * 协程已完成时
     */
    suspend fun await(): T
}

class DeferredCoroutine<T>(context: CoroutineContext = EmptyCoroutineContext) :
    AbstractCoroutine<T>(context), Deferred<T> {

    /**
     * 如果协程已经结束，立即返回结果；如果有异常，则抛出（await只返回结果，异常作为await方法本身产生的重新抛出符合逻辑）
     * 如果协程未完成，则挂起直到完成
     */
    override suspend fun await(): T {
        return when (val current = state.get()) {
            is CoroutineState.Complete<*> ->{//内部协程已完成，直接返回结果
                //首先检查外部协程的取消状态
                if (coroutineContext[Job]?.isActive == false) {
                    throw CancellationException("协程已取消")
                }
                (current as CoroutineState.Complete<T>).value
                    ?: throw current.exception!!
            }
            is CoroutineState.Cancelling,//内部协程未完成，挂起
            is CoroutineState.Incomplete -> doAwait()
        }
    }

    private suspend fun doAwait(): T {
        return suspendCancellableCoroutine { continuation ->
            val disposable = doOnCompleted {
                continuation.resume(it.getOrThrow())
            }
            continuation.invokeOnCancellation { disposable.dispose() }
        }
    }

    override fun resumeWith(result: Result<T>) {
        super.resumeWith(result)
        //由于调用者通过await方法获取返回值，所以在那里再抛出异常
    }
}