package com.lovely.bear.laboratory.conntinuation.view

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.lovely.bear.laboratory.conntinuation.lite.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * 利用协程，把异步代码同步化。
 * 该方法可以同步返回用户的选择结果，无需回调式写法，大大增加了可读性，对协程在异步场景下的应用有很大的启发性。
 */
suspend fun Context.alert(title: CharSequence, message: String): Boolean? {
    return suspendCancellableCoroutine { continuation ->

        val listener = DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> continuation.resume(true)
                DialogInterface.BUTTON_NEGATIVE -> continuation.resume(false)
                else -> continuation.resume(null)
            }
        }

        val dialog = AlertDialog.Builder(this).setTitle(title).setMessage(message)
            .setPositiveButton("确认", listener)
            .setNegativeButton("取消", listener)
            .setNeutralButton("不管", listener)
            .show()

        continuation.invokeOnCancellation {
            dialog.cancel()
        }


    }
}