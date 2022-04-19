package com.lovely.bear.laboratory.conntinuation.lite

import io.reactivex.rxjava3.disposables.Disposable

/**
 *
 * @author guoyixiong
 */
sealed class CoroutineState {
    class Incomplete : CoroutineState()
    class Cancelling : CoroutineState()
    class Complete<T>(val value: T? = null, val exception: Throwable? = null) : CoroutineState()

    private var disposableList: DisposableList = DisposableList.Nil


    fun from(state: CoroutineState): CoroutineState {
        this.disposableList = state.disposableList
        return this
    }

    fun with(disposable: Disposable): CoroutineState {
        this.disposableList = DisposableList.Cons(disposable, this.disposableList)
        return this
    }

    fun withOut(disposable: Disposable): CoroutineState {
        this.disposableList = this.disposableList.remove(disposable)
        return this
    }

    fun clear() {
        this.disposableList = DisposableList.Nil
    }

    fun <T> notifyCompletion(result: Result<T>) {
        this.disposableList.loopOn<CompletionHandlerDisposable<T>> {
            it.run(result = result)
        }
    }

    fun notifyCancel() {
        this.disposableList.loopOn<CancelHandlerDisposable> {
            it.run()
        }
    }
}
