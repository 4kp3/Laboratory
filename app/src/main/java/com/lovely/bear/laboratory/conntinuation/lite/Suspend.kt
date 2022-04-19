package com.lovely.bear.laboratory.conntinuation.lite

import java.util.concurrent.CompletableFuture
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn

/**
 * 支持协程取消的挂起方法
 *
 * 挂起是协程的关键。
 * 官方的 suspendCoroutine 方法中，通过创建SafeContinuation，随后执行 safe.getOrThrow() ，如果返回挂起标志则
 * 协程实现真正挂起。
 *
 * 挂起后的协程取消需要内外两个协程协作。内部协程监听外部协程取消，然后改变自身的状态，同时内部协程提供取消标志，
 * 供自己的子协程协作取消。
 *
 * @author guoyixiong
 */
suspend inline fun <T> suspendCancellableCoroutine(
    crossinline block: (CancellableContinuation<T>) -> Unit
): T {
    return suspendCoroutineUninterceptedOrReturn { c: Continuation<T> ->
        val cancellable = CancellableContinuation(c.intercepted())
        block(cancellable)
        cancellable.getOrThrow()
    }
}

private suspend fun suspend(block:()->Unit) {
    suspendCoroutine<Unit> { continuation->
        val future= CompletableFuture.supplyAsync {
            Thread.sleep(2222)
            1
        }
        future.thenApply {
            continuation.resume(Unit)
        }.exceptionally {
            continuation.resumeWithException(it)
        }
    }
}

fun <T> Continuation<T>.intercepted(): Continuation<T> {
    return context[ContinuationInterceptor]?.interceptContinuation(this) ?: this
}