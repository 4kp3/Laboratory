package com.lovely.bear.laboratory.util

import android.app.Activity
import android.content.Context
import android.content.Intent

/**
 * 工具方法
 * @author guoyixiong
 */

/**
 * 实化类型参数特性运用
 */
inline fun <reified T> startActivity(context: Context) where T : Activity {
    context.startActivity(Intent(context, T::class.java))
}