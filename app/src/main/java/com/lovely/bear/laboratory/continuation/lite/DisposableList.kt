package com.lovely.bear.laboratory.continuation.lite

import io.reactivex.rxjava3.disposables.Disposable

/**
 * 不可变的链表结构
 * 链表在节点修改上有优势，只需要替换首尾引用即可
 */
sealed class DisposableList {
    object Nil : DisposableList()
    class Cons(
        val head: Disposable,
        val tail: DisposableList
    ) : DisposableList()
}

fun DisposableList.remove(disposable: Disposable): DisposableList {
    return when (this) {
        DisposableList.Nil -> this
        is DisposableList.Cons -> {
            if (head == disposable) {
                return tail
            } else {
                DisposableList.Cons(head, tail.remove(disposable))
            }
        }
    }
}


tailrec fun DisposableList.forEach(action: (Disposable) -> Unit): Unit =
    when (this) {
        DisposableList.Nil -> Unit
        is DisposableList.Cons -> {
            action(this.head)
            this.tail.forEach(action)
        }
    }

//使用了内联函数的可捕获泛型
inline fun <reified T : Disposable> DisposableList.loopOn(crossinline action: (T) -> Unit) =
    forEach {
        if (it is T) {
            action(it)
        }
    }