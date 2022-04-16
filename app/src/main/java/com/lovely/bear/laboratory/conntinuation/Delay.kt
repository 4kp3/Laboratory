package com.lovely.bear.laboratory.conntinuation

import java.util.concurrent.Executors
import kotlin.coroutines.*

suspend fun main(){



    suspend {
        println("start")
        delay(1000)
        println("end")
    }.startCoroutine(object:Continuation<Unit>{
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
suspend fun delay(timeMs: Long) {
    suspendCoroutine<Unit> {
        delay_executor.submit {
            Thread.sleep(timeMs)
            it.resume(Unit)
        }
    }
}

private val delay_executor = Executors.newSingleThreadExecutor { r -> Thread(r, "delay-executor") }