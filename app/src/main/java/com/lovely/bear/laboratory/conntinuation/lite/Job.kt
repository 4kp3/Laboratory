package com.lovely.bear.laboratory.conntinuation.lite

import io.reactivex.rxjava3.disposables.Disposable
import java.lang.IllegalStateException
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.*
import kotlin.coroutines.cancellation.CancellationException

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