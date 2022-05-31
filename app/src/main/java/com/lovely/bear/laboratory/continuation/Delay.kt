package com.lovely.bear.laboratory.continuation

import com.lovely.bear.laboratory.continuation.lite.suspendCancellableCoroutine
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.*

suspend fun main() {
    suspend {
        println("start")
        delayCustom(1000)
        println("end")
    }.startCoroutine(object : Continuation<Unit> {
        override val context: CoroutineContext
            get() = EmptyCoroutineContext

        override fun resumeWith(result: Result<Unit>) {
            println("result:$result")
        }
    })
}

/**
 * 协程的延迟功能
 * @author guoyixiong
 */
suspend fun delayCustom(timeMs: Long) {
    if (timeMs <= 0) return
    suspendCancellableCoroutine<Unit> {
        val future = delay_executor.schedule({
            it.resume(Unit)
        }, timeMs, TimeUnit.MILLISECONDS)
        it.invokeOnCancellation { future.cancel(true) }
    }
}

private val delay_executor =
    Executors.newSingleThreadScheduledExecutor() { r -> Thread(r, "delay-executor") }