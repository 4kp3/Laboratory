package com.lovely.bear.laboratory

import android.app.Activity
import android.content.Context
import android.content.Intent

/**
 * 工具方法
 * @author guoyixiong
 */

inline fun <reified T> startActivity(context: Context) where T : Activity {
    context.startActivity(Intent(context, T::class.java))
}