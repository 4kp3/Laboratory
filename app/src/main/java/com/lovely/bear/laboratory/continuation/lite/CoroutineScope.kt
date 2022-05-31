package com.lovely.bear.laboratory.continuation.lite

import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * 协程作用域
 * @author guoyixiong
 */
interface CoroutineScope {
    val scopeContext: CoroutineContext
}

object GlobalScope : CoroutineScope {
    override val scopeContext: CoroutineContext
        get() = EmptyCoroutineContext
}

internal open class ScopeCoroutine<T>(context: CoroutineContext, val continuation: Continuation<T>):AbstractCoroutine<T>(context){
    override fun resumeWith(result: Result<T>) {
        super.resumeWith(result)
        continuation.resumeWith(result)
    }
}

internal class SupervisorCoroutine<T>(context: CoroutineContext, continuation: Continuation<T>):ScopeCoroutine<T>(context,continuation){
    override fun handleChildException(e: Throwable): Boolean {
        return false
    }
}
