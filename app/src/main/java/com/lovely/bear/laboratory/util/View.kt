package com.lovely.bear.laboratory.util

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import kotlin.math.ceil


fun dpToPx(value: Float, resources: Resources): Int {
    return ceil(value * resources.displayMetrics.density).toInt()
}

fun dpToPx(value: Float, context: Context): Int {
    return dpToPx(value, context.resources)
}

fun spToPx(value: Float, context: Context): Int {
    return ceil(
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            value,
            context.resources.displayMetrics
        )
    ).toInt()
}