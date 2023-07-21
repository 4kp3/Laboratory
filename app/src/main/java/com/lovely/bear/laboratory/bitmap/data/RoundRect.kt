package com.lovely.bear.laboratory.bitmap.data

import android.graphics.Rect

data class RoundRect(val content: Rect, val corners: Corners) {
    val width = content.width()
    val height = content.height()
}

data class Corners(
    val leftTop: Int = 0,
    val rightTop: Int = 0,
    val leftBottom: Int = 0,
    val rightBottom: Int = 0
) {
    val isNoCorners: Boolean = this == NoCorners
    val normalCorner:Int=leftTop
}

val NoCorners = Corners()