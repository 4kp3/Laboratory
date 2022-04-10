package com.lovely.bear.laboratory.conntinuation

import kotlinx.coroutines.delay
import kotlin.coroutines.*

suspend fun main() {
    val producter = yieldAble<Unit, Int> {
        println("make 1")
        yield(1)
        println("make 2")
        yield(2)
        println("make 3")
        yield(3)
        1
    }

    val resumer = yieldAble<Int, Unit> {
        //初始消费
        val v1 = yield(Unit)
        println("receive :$v1")
        val v2 = yield(Unit)
        println("receive :$v2")
        val v3 = yield(Unit)
        println("receive :$v3")
    }

    var i = 0
    while (i++ < 3) {
        val pV = producter.resume(Unit)
        resumer.resume(pV)
    }
    producter.resumeWith(Result.success(1))
    resumer.resumeWith(Result.success(Unit))
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
fun <T, R> yieldAble(block: suspend YieldScope<T, R>.() -> R): YieldAndResumeContinuation<T, R> {
    return YieldAndResumeContinuation(
        block = block
    )
}

class YieldScope<T, R> {

    var next: Continuation<T>? = null
    var parent: Continuation<R>? = null
    var initValue: T? = null

    /**
     * 暂停当前协程，恢复上级协程
     * @param value 要传递的值
     */
    suspend fun yield(value: R): T {
        return suspendCoroutine<T> {
            //不能直接resume，需要挂起
            //it.resume(value)
            val initT = initValue
            initValue = null
            if (initT == null||initT==Unit) {
                next = it
                parent?.resume(value)
            } else {
                it.resume(initT)
            }
        }
    }
}

/**
 * 注意Yield方法不能声明在这里，因为这是给用户返回的对象，如果用户拿到了，将有权限调用。所以委托给另外的对象。
 *
 * @param T 调用resume方法的传入值
 * @param R 调用yield方法的传出值
 */
class YieldAndResumeContinuation<T, R>(block: suspend YieldScope<T, R>.() -> R) :
    Continuation<R> {

    private val scope: YieldScope<T, R> = YieldScope()

    override val context: CoroutineContext
        get() = EmptyCoroutineContext

    private val initBlockContinuation =
        block.createCoroutine(scope, object : Continuation<R> {
            override val context: CoroutineContext
                get() = EmptyCoroutineContext

            override fun resumeWith(result: Result<R>) {
                println("块协程终止")
            }
        }
        )

    private var continuation: Continuation<R>? = null

    override fun resumeWith(result: Result<R>) {
        println("协程终止")
    }

    /**
     * 恢复协程并等待一次结果返回
     * 由于需要返回异步结果，所以必须使用挂起函数
     * 需要获得下游延续才能恢复，下游延续必须在 yield 的时候创建（获取），然后保存以供现在使用。
     */
    suspend fun resume(value: T): R {
        return suspendCoroutine<R> {
            scope.parent = it
            //启动目标协程
            if (scope.next == null) {
                //初始协程
                scope.initValue = value
                initBlockContinuation.resume(Unit)
            } else {
                //yield的协程
                scope.next?.resume(value)
            }
        }
    }

}
