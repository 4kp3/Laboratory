package com.lovely.bear.laboratory

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

//private val complete = object : Continuation<Int> {
//    override val context: CoroutineContext
//        get() = EmptyCoroutineContext
//
//    override fun resumeWith(result: Result<Int>) {
//        if (result.isSuccess)
//            println("协程成功结束：${result.getOrNull()}")
//    }
//}

//suspend fun f2(): Int {
//    println("内部协程 f2")
//    delay(100)
//    return 1
//}

//suspend fun f1(): Int {
//    val v = f2()
//    return v + 1
//}

//suspend fun f3(): Int {
//    delay(1000)
//    return 2;
//}

suspend fun f4(): Int {
    delay(2000)
    return 2;
}

suspend fun f5(): Int {
    delay(2000)
    return 2;
}

fun launchCoroutine() {
    GlobalScope.launch(start = CoroutineStart.UNDISPATCHED) {
        println("1")
        f4()
        println("2")
        f5()
        println("3")
    }
}

fun main() {
    val t = thread {
        launchCoroutine()
    }
    t.start()
    t.join()
}
/**
 * launch.resume{
 * launch.invokeSuspend{
 * f3(launch).resume{
 *  return
 * f3.invokeSuspend{
 *  return SUSPEND
 * }
 * }
 * }
 * }
 *
 */