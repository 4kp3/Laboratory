package com.lovely.bear.laboratory.conntinuation.lite

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

fun main() {
    launch(Dispatchers.DEFAULT) {
        println("1")
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