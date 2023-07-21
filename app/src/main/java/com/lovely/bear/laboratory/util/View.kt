package com.lovely.bear.laboratory.util

import android.util.TypedValue
import com.lovely.bear.laboratory.MyApplication
import kotlin.math.ceil


fun pxToDp(value: Int): Int {
    return ceil(value / MyApplication.APP.resources.displayMetrics.density).toInt()
}

fun dpToPx(value: Float): Int {
    return ceil(value * MyApplication.APP.resources.displayMetrics.density).toInt()
}

fun spToPx(value: Float): Int {
    return ceil(
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            value,
            MyApplication.APP.resources.displayMetrics
        )
    ).toInt()
}