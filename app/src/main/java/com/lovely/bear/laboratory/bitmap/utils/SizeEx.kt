package com.lovely.bear.laboratory.bitmap.utils

import android.graphics.Bitmap
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.VectorDrawable
import android.util.Size
import com.lovely.bear.laboratory.util.pxToDp

/*
* Copyright (C), 2023, Nothing Technology
* FileName: SizeEx
* Author: yixiong.guo
* Date: 2023/7/18 20:36
* Description:  
* History:
* <author> <time> <version> <desc>
*/
fun Bitmap.toSize(): Size {
    return Size(this.width,this.height)
}
fun Drawable.toSize():Size{
    return Size(this.intrinsicWidth,this.intrinsicHeight)
}

fun Drawable.typeDesc():String{
    return when (this) {
        is AdaptiveIconDrawable -> "Adaptive"
        is BitmapDrawable -> "Bitmap"
        is VectorDrawable -> "Vector"
        is ColorDrawable -> "Color"
        is GradientDrawable -> "Gradient"
        else -> this::class.simpleName ?: ""
    }
}

fun Size.dpSize():String {
    return "dpSize[${pxToDp(width)},${pxToDp(height)}]"
}