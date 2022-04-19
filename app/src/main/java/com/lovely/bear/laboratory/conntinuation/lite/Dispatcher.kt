package com.lovely.bear.laboratory.conntinuation.lite

import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

/**
 * 调度器
 * @author guoyixiong
 */
interface Dispatcher {
    fun dispatch(block: () -> Unit)
}

open class DispatcherContext(private val dispatcher: Dispatcher) :
    AbstractCoroutineContextElement(ContinuationInterceptor.Key), ContinuationInterceptor {
    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> {
        //println("拦截协程 创建代理延续")
        return DispatchedContinuation(continuation, dispatcher)
    }
}

private class DispatchedContinuation<T>(
    val delegate: Continuation<T>,
    val dispatcher: Dispatcher,
) : Continuation<T> {
    override val context: CoroutineContext = delegate.context

    override fun resumeWith(result: Result<T>) {
        //println("拦截协程 resumeWith")
        dispatcher.dispatch {
//            println("发起拦截 resumeWith")
            delegate.resumeWith(result)
        }
    }
}

/**
 * 默认调度器
 */
object DefaultDispatcher : Dispatcher {

    private val threadGroup = ThreadGroup("DefaultDispatcher")
    private val threadIndex = AtomicInteger(0)

    //CPU密集型计算，线程最大数量和核心数等同。设置为守护线程，如果JVM中只剩下守护线程，则虚拟机直接退出
    private val executor =
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1) {
            Thread(threadGroup, it, "${threadGroup.name}-worker-${threadIndex.getAndIncrement()}")
                .apply { isDaemon = false }//调试代码中，如果守护开启，将会很快死亡，暂时关闭。生产代码中应该开启
        }

    override fun dispatch(block: () -> Unit) {
        executor.submit(block)
    }
}

object Dispatchers {
    val DEFAULT = DispatcherContext(DefaultDispatcher)
}