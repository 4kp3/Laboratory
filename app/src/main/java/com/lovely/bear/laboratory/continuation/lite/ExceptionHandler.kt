package com.lovely.bear.laboratory.continuation.lite

import kotlin.coroutines.CoroutineContext

/**
 * 异常处理器
 * @author guoyixiong
 */
interface CoroutineExceptionHandler:CoroutineContext.Element {
    companion object Key : CoroutineContext.Key<CoroutineExceptionHandler>
    fun handleException(context: CoroutineContext,e: Throwable): Boolean
}