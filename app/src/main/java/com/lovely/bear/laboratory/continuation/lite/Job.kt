package com.lovely.bear.laboratory.continuation.lite

import io.reactivex.rxjava3.disposables.Disposable
import kotlin.coroutines.*

interface Job : CoroutineContext.Element {
    companion object Key : CoroutineContext.Key<Job>

    override val key: CoroutineContext.Key<*>
        get() = Job

    val isActive: Boolean

    fun invokeOnCancel(onCancel: OnCancel): Disposable
    fun invokeOnCompletion(onComplete: OnComplete): Disposable
    fun cancel()
    fun remove(disposable: Disposable)
    suspend fun join()

}
typealias OnComplete = () -> Unit
typealias OnCancel = () -> Unit