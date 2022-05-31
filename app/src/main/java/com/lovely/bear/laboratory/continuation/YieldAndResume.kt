package com.lovely.bear.laboratory.continuation

import java.lang.IllegalStateException
import kotlin.coroutines.*

suspend fun main() {
    val product = yieldAble<Unit, Int> {
        println("make 1")
        yield(1)
        println("make 2")
        yield(2)
        println("make 3")
        yield(3)
    }

    val resume = yieldAble<Int, Unit> { initValue ->
        println("receive :$initValue")
        val v2 = yield(Unit)
        println("receive :$v2")
        val v3 = yield(Unit)
        println("receive :$v3")
    }

    var i = 0
    while (i++ < 3) {
        val pV = product.resume(Unit)
        resume.resume(pV)
    }
    product.resumeWith(Result.success(1))
    resume.resumeWith(Result.success(Unit))
}


/**
 * yield / resume 方式封装协程， yield 方法将使得当前协程让出执行权，传递的参数可以作为该协程的返回值
 * resume 方法恢复协程执行，直到内部调用了 yield 方法再次挂起
 *
 * 要想挂起一个协程，必须使用挂起函数，因为只有挂起函数可以实现同步或者异步结果的返回。
 *
 * 要想在多个协程之间切换，必须持有这几个协程（的Continuation）。同样要想为一个协程增加恢复、暂停功能，
 * 就必须有这个协程的状态保存。
 * 状态包含两个：
 * 一、暂停时的协程，即调用 yield 方法时的协程。我们知道这个方法一定是 [suspendCoroutine] ，在这里可以拿到当前的协程对象。
 * 二、恢复时的协程，调用 resume 方法时要继续执行的协程。有两种情况，一是没有暂停过的协程，这时候应该启动初始协程；
 * 二是已经调用 yield 方法暂停过的协程，此时恢复协程即可。
 *
 * yield 方法需要作用域保护，以防在外部被调用。
 */

/**
 * 创建一个拥有yield/resume能力的协程（延续）
 */
fun <T, R> yieldAble(block: suspend YieldScope<T, R>.(T) -> Unit): YieldAndResumeContinuation<T, R> {
    return YieldAndResumeContinuation(
        block = block
    )
}

sealed class YieldAndResumeStatus {
    // R resume方法返回值和yield方法传入参数，T是block初始参数和yield返回值
    class Created(
        val continuation: Continuation<Unit>
    ) :
        YieldAndResumeStatus() {

        fun resume() {
            continuation.resume(Unit)
        }
    }

    class Resumed<R>(private val continuation: Continuation<R>) : YieldAndResumeStatus() {
        fun resume(value: R) {
            continuation.resume(value)
        }
    }

    class Yield<T>(private val continuation: Continuation<T>) : YieldAndResumeStatus() {
        fun resume(value: T) {
            continuation.resume(value)
        }
    }

    object Dead : YieldAndResumeStatus()
}

/**
 * Yield方法声明在这里为了屏蔽用户拿到 YieldAndResumeContinuation 后在外部调用
 */
interface YieldScope<T, R> {

    /**
     * 暂停当前协程，并返回入参
     * @param value 要传递出去的值
     */
   public suspend fun yield(value: R): T
}

/**
 * 代表可以恢复暂停的协程对象。
 * 控制状态流转。
 * @param T block初始参数和yield方法的返回值类型
 * @param R resume返回和yield方法传入参数类型
 */
class YieldAndResumeContinuation<T, R>(
    override val context: CoroutineContext = EmptyCoroutineContext,
    block: suspend YieldScope<T, R>.(T) -> Unit
) :
    Continuation<R>,YieldScope<T, R> {

    /*为了能在Transfer 中挂起，这里开放了yield方法*/
//    private val scope: YieldScope<T, R> = object : YieldScope<T, R> {
        override suspend fun yield(value: R): T {
            return suspendCoroutine<T> { continuation ->
                //读取并更新，如果读取时状态异常，则抛出异常
                val pre = statusUpdater.getAndUpdate(this@YieldAndResumeContinuation) {
                    if (it is YieldAndResumeStatus.Resumed<*>) {
                        YieldAndResumeStatus.Yield<T>(continuation)
                    } else {
                        throw IllegalStateException("required status is Resumed,but now is $status")
                    }
                }

                if (pre is YieldAndResumeStatus.Resumed<*>) {
                    (pre as YieldAndResumeStatus.Resumed<R>).resume(value)
                }
            }
        }
//    }

    private var initValue: T? = null

    @Volatile
    private var status: YieldAndResumeStatus =
        YieldAndResumeStatus.Created(continuation = suspend {
//            scope.block(initValue!!)
            block(initValue!!)
        }.createCoroutine(
            object : Continuation<Unit> {
                override val context: CoroutineContext
                    get() = EmptyCoroutineContext

                override fun resumeWith(result: Result<Unit>) {
                    println("块协程终止")
                }
            }
        ))

    override fun resumeWith(result: Result<R>) {
        status = YieldAndResumeStatus.Dead
        println("协程终止")
    }

    /**
     * 恢复协程并等待一次结果返回
     * 由于需要返回异步结果，所以必须使用挂起函数
     * 需要获得下游延续才能恢复，下游延续必须在 yield 的时候创建（获取），然后保存以供现在使用。
     */
    suspend fun resume(value: T): R {
        return suspendCoroutine<R> { continuation ->
            //先更新状态，再执行
            val pre = statusUpdater.getAndUpdate(this) {
                if (it is YieldAndResumeStatus.Created || it is YieldAndResumeStatus.Yield<*>) {
                    YieldAndResumeStatus.Resumed(continuation)
                } else {
                    throw  IllegalStateException("resume时要求状态为 Created or Yield, current is $status")
                }
            }
            if (pre is YieldAndResumeStatus.Created) {
                //初始执行
                initValue = value
                pre.resume()
            } else if (pre is YieldAndResumeStatus.Yield<*>) {
                //yield恢复执行
                (pre as YieldAndResumeStatus.Yield<T>).resume(value)
            }
        }
    }

    companion object {
        private val statusUpdater =
            java.util.concurrent.atomic.AtomicReferenceFieldUpdater.newUpdater(
                YieldAndResumeContinuation::class.java,
                YieldAndResumeStatus::class.java,
                "status"
            )
    }

}
