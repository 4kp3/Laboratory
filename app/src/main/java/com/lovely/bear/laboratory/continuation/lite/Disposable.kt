package com.lovely.bear.laboratory.continuation.lite

import io.reactivex.rxjava3.disposables.Disposable
import java.util.*
import java.util.concurrent.atomic.AtomicReference

class CompletionHandlerDisposable<T>(
    job: Job,
    action: (Result<T>) -> Unit
) : ReferenceDisposable<Pair<Job, (Result<T>) -> Unit>>(Pair(job, action)) {

    override fun onDisposed(value: Pair<Job, (Result<T>) -> Unit>) {
        value.first.remove(this)
    }

    fun run(result: Result<T>) {
        get().second.invoke(result)
    }
}

class CancelHandlerDisposable(
    job: Job,
    action: () -> Unit
) : ReferenceDisposable<Pair<Job, () -> Unit>>(Pair(job, action)) {

    override fun onDisposed(value: Pair<Job, () -> Unit>) {
        value.first.remove(this)
    }

    fun run() {
        get().second.invoke()
    }
}

abstract class ReferenceDisposable<T>(value: T) :
    AtomicReference<T>(Objects.requireNonNull(value, "value is null")),
    Disposable {
    protected abstract fun onDisposed(value: T)
    override fun dispose() {
        var value = get()
        if (value != null) {
            value = getAndSet(null)
            value?.let { onDisposed(it) }
        }
    }

    override fun isDisposed(): Boolean {
        return get() == null
    }

    companion object {
        private const val serialVersionUID = 6537757548749041217L
    }
}