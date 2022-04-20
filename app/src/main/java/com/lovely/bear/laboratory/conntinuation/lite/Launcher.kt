package com.lovely.bear.laboratory.conntinuation.lite

import java.lang.IllegalArgumentException
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

fun main() {
    launch(Dispatchers.DEFAULT + object : CoroutineExceptionHandler {
        override val key: CoroutineContext.Key<*>
            get() = CoroutineExceptionHandler.Key

        override fun handleException(context: CoroutineContext, e: Throwable): Boolean {
            println("异常处理器捕获到异常：$e")
            return true
        }
    }) {
        println("1")
        throw IllegalArgumentException("ces ")
        println("2")
    }
}

/**
 * 启动一个无返回值的协程
 * @author guoyixiong
 */
fun launch(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend () -> Unit
): Job {
    val completion = StandaloneCoroutine(context)
    block.startCoroutine(completion)
    return completion
}

fun <T> async(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend () -> T
): Deferred<T> {
    val completion = DeferredCoroutine<T>(context)
    block.startCoroutine(completion)
    return completion
}