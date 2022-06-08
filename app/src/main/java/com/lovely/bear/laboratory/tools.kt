package com.lovely.bear.laboratory

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import kotlin.math.ceil

/**
 * 工具方法
 * @author guoyixiong
 */

inline fun <reified T> startActivity(context: Context) where T : Activity {
    context.startActivity(Intent(context, T::class.java))
}

fun dpToPx(value: Float, resources: Resources): Int {
    return ceil(value * resources.displayMetrics.density).toInt()
}

fun dpToPx(value: Float, context: Context): Int {
    return dpToPx(value, context.resources)
}