package com.lovely.bear.laboratory

import com.lovely.bear.laboratory.continuation.YieldAndResumeContinuation
import kotlin.coroutines.*

fun main() {
//    suspend {
//        println("执行暂停函数")
//        1
//    }.startCoroutine(object : Continuation<Int> {
//        override val context: CoroutineContext
//            get() = EmptyCoroutineContext + LogInterceptor()
//
//        override fun resumeWith(result: Result<Int>) {
//            println("result:$result")
//        }
//    })

//    async {
//        println("协程体start")
//        val result=await {
//            Thread.sleep(2000)
//            5
//        }
//        println("await结果=$result")
//        println("协程体end")
//    }
    suspend {

        val a = YieldAndResumeContinuation<Unit, Int> {
            for (i in 0..10) {
                println("do $i")
                yield(i)
            }
            1
        }

        val b = YieldAndResumeContinuation<Int, Unit> {
            for (i in 0..10) {
                val v = yield(Unit)
                println("receive $v")
            }
            1
        }
        Unit

        var i = 0
        while (i++ < 11) {
            val v=a.resume(Unit)
            b.resume(v)
        }

    }.startCoroutine(object : Continuation<Unit> {
        override val context: CoroutineContext
            get() = EmptyCoroutineContext

        override fun resumeWith(result: Result<Unit>) {
        }
    })
}

class LogInterceptor : ContinuationInterceptor {

    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> {
        return LogContinuation(continuation)
    }

    override val key: CoroutineContext.Key<*>
        get() = KEY

    companion object KEY : CoroutineContext.Key<LogInterceptor>
}

class LogContinuation<T>(val next: Continuation<T>) : Continuation<T> {
    override val context: CoroutineContext
        get() = EmptyCoroutineContext

    override fun resumeWith(result: Result<T>) {
        println("恢复之前")
        next.resumeWith(result)
        println("恢复之后")
    }
}