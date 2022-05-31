package com.lovely.bear.laboratory.continuation.lite

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * 无返回值类型的Job
 * @author guoyixiong
 */
class StandaloneCoroutine(context: CoroutineContext = EmptyCoroutineContext) :
    AbstractCoroutine<Unit>(context) {
    override fun handleJobException(e: Throwable): Boolean {
        super.handleJobException(e)
        //发现未捕获异常时，调用自己的异常处理器处理，无则抛给线程默认的异常处理器
        context[CoroutineExceptionHandler.Key]?.handleException(context, e) ?: run {
            val ue = Thread.currentThread().uncaughtExceptionHandler
            ue?.uncaughtException(Thread.currentThread(), e)
            Unit
        }
        return true
    }
}