package com.lovely.bear.laboratory.conntinuation

import java.lang.Exception
import kotlin.concurrent.thread
import kotlin.coroutines.*


/**
 * 实现一个 async/await 协程
 *
 * async接受一个协程体
 * await可以在该协程体内返回异步结果（suspend 挂起函数）
 */
fun <T> async(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend AsyncScope.() -> T
) {
    return block.startCoroutine(AsyncScope(), object : Continuation<T> {
        override val context: CoroutineContext
            get() = context

        override fun resumeWith(result: Result<T>) {
            //忽略结果
        }
    })
}

/**
 * async block的接收者，提供 await 方法的作用域
 * @param context 向块中传递数据，可以实现拦截器等逻辑。现在未继承 [Continuation] ，只是作为展位
 */
class AsyncScope(val context: CoroutineContext = EmptyCoroutineContext)

/**
 * 等待直到返回异步执行结果
 */
suspend fun <T> AsyncScope.await(block: () -> T): T {
    //这里需要处理上游和下游，把上游异步结果返回给下游
    //上游即本方法参数block，下游即当前协程
    return suspendCoroutine {
        //异步执行block
        thread {
            val result = try {
                block()
            } catch (e: Exception) {
                it.resumeWithException(e)
                return@thread
            }
            it.resume(result)
        }
    }
}