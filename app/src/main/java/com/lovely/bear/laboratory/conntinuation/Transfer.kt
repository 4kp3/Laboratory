package com.lovely.bear.laboratory.conntinuation

import kotlin.coroutines.*

suspend fun main() {
    TransferredCoroutine.main {
        println("start transfer main")
        val result = transfer(Transfer.a, 1)
        println("end transfer main,result=$result")
    }
}

object Transfer {
    val a: TransferredCoroutine<Int> = TransferredCoroutine<Int> {
        println("this is A TransferredCoroutine")
        val result = transfer(b, 2)
        println("this is A TransferredCoroutine, after a transfer,result=$result")
        val result2 = transfer(c, 4)
        println("this is A TransferredCoroutine, after a transfer,result=$result2")
    }

    val b: TransferredCoroutine<Int> = TransferredCoroutine<Int> {
        println("this is B TransferredCoroutine")
        val result = transfer(a, 8)
        println("this is B TransferredCoroutine,after a transfer,result=$result")
    }

    val c: TransferredCoroutine<Int> = TransferredCoroutine<Int> {
        println("this is C TransferredCoroutine,END")
    }
}

/**
 * 可以任意转移控制权。依靠调度协程实现，它会在协程需要被挂起的时候恢复，然后唤醒目标协程。
 * @author guoyixiong
 */
interface CoroutineTransferScope<T> {
    /**
     * 挂起当前协程，唤醒目标协程
     *
     *
     * 暂停函数中会处理三个协程，一是调度协程，二是当前暂停的协程，三是目标协程。
     * T是调度协程的数据类型，因为它是暂停函数的返回值，也就是捕获了。
     *
     * transfer会捕获当前协程，这属于被调度的协程，同时恢复调度协程，由调度协程处理后续调度。
     * 1 存储捕获的当前协程的延续（当前的暂停状态）
     * 2 读取调度协程并恢复
     * 3 调度协程拿到目标协程和参数后执行恢复
     *
     * @param T 当前协程参数类型
     * @param P 目标协程数据类型
     */
    suspend fun <P> transfer(continuation: TransferredCoroutine<P>, p: P): T

}

/**
 * 调度器的参数类型
 * @param coroutine 目标协程
 * @param value 目标协程的参数
 */
class Parameter<T>(val coroutine: TransferredCoroutine<T>, val value: T)

/**
 * 把执行权转移给目标协程
 *
 * 这里涉及三个角色，控制器、调用者、接受者。
 * 由于要求传递角色本身，所以三者的数据类型也必须是 [Parameter] ，携带了 continuation.resume(T) 信息，
 * 即要恢复的延续和恢复时传递的参数值
 *
 */
class TransferredCoroutine<T>(
    override val context: CoroutineContext = EmptyCoroutineContext,
    private val block: suspend CoroutineTransferScope<T>.(T) -> Unit
) : Continuation<T> {

    val isMain: Boolean
        get() = this == main

    private var scope: CoroutineTransferScope<T> = object : CoroutineTransferScope<T> {

        /**
         * 核心逻辑
         *
         */
        private tailrec suspend fun <P> transferInner(
            continuation: TransferredCoroutine<P>,
            value: Any?
        ): T {
            if (this@TransferredCoroutine.isMain) { //这是在调度协程内执行

                return if (continuation.isMain) {//目标协程是调度协程
                    value as T
                } else {//目标协程是普通协程
                    //启动
                    val parameter = continuation.coroutine.resume(value as P)
                    //目标协程暂停
                    transferInner(parameter.coroutine, parameter.value)
                }

            } else {
                /**
                 * 普通协程调用 transfer时，挂起自己即可，这里会把控制权还给上层(隐含的调度协程block，
                 * 即 [TransferredCoroutine.main] 代码)。
                 * 挂起后只有等别人调用 [YieldAndResumeContinuation.resume] 才会恢复
                 */
                return coroutine.yield(Parameter(continuation, value as P))
            }
        }

        override suspend fun <P> transfer(continuation: TransferredCoroutine<P>, p: P): T {
            return transferInner(continuation, p)
        }
    }

    /**
     * [CoroutineTransferScope.transfer] 会导致调用者协程挂起，随后恢复目标协程，所以
     * 依赖 [YieldAndResumeContinuation] 的暂停和恢复功能
     */
    val coroutine = YieldAndResumeContinuation<T, Parameter<*>>(context)
    { v ->
        Parameter(
            this@TransferredCoroutine,
            suspend {
                block(scope, v)
                if (this@TransferredCoroutine.isMain) Unit else {
                    throw IllegalStateException("TransferredCoroutine cannot be dead.")
                }
            }() as T
        )
    }

    override fun resumeWith(result: Result<T>) {
        //设置为结束状态并把移交调度权

    }

    suspend fun resume(value: T) {
        coroutine.resume(value)
    }

    companion object {
        lateinit var main: TransferredCoroutine<Any?>

        /**
         * 启动一个对称协程
         * 先创建一个协程作为调度协程，然后启动它
         */
        suspend fun main(
            block: suspend CoroutineTransferScope<Any?>.() -> Unit
        ) {
            TransferredCoroutine<Any?>(block = { block() }).also {
                main = it
            }.resume(Unit)
        }

    }


}