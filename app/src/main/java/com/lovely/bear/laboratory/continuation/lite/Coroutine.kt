package com.lovely.bear.laboratory.continuation.lite

import java.lang.IllegalArgumentException
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn

fun main() {
    GlobalScope.launch(Dispatchers.DEFAULT + object : CoroutineExceptionHandler {
        override val key: CoroutineContext.Key<*>
            get() = CoroutineExceptionHandler.Key

        override fun handleException(context: CoroutineContext, e: Throwable): Boolean {
            println("异常处理器捕获到异常：$e")
            return true
        }
    }) {
        println("1")
        justSuspend()
        throw IllegalArgumentException("ces ")
        println("2")
    }
}

private suspend fun justSuspend() {
    coroutineScope<Unit> {
        launch {

        }
    }
}

/**
 * 启动一个无返回值的协程
 * @author guoyixiong
 */
fun CoroutineScope.launch(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> Unit
): Job {
    val completion = StandaloneCoroutine(newCoroutineContext(context))
    block.startCoroutine(completion, completion)
    return completion
}

fun <T> CoroutineScope.async(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> T
): Deferred<T> {
    val completion = DeferredCoroutine<T>(newCoroutineContext(context))
    block.startCoroutine(completion, completion)
    return completion
}

/**
 * 获取当前协程中的作用域
 */
suspend fun <T> coroutineScope(block: CoroutineScope.() -> Unit) =
    suspendCoroutine<T> {
        val scope = ScopeCoroutine(it.context, it)
        block.invoke(scope)
    }

/**
 * 获取当前协程中的监督者作用域
 */
suspend fun <T> supervisorCoroutineScope(block: CoroutineScope.() -> Unit) =
    suspendCoroutine<T> {
        val scope = SupervisorCoroutine(it.context, it)
        block.invoke(scope)
    }

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

fun <T> Continuation<T>.intercepted(): Continuation<T> {
    return context[ContinuationInterceptor]?.interceptContinuation(this) ?: this
}

private suspend fun suspend(block: () -> Unit) {
    suspendCoroutine<Unit> { continuation ->
        val future = CompletableFuture.supplyAsync {
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

/**
 * 把当前作用域的上下文添加到协程中
 */
fun CoroutineScope.newCoroutineContext(context: CoroutineContext): CoroutineContext {
    return scopeContext + context
}