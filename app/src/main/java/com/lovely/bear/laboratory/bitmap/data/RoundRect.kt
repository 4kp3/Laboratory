package com.lovely.bear.laboratory.bitmap.data

import android.graphics.Rect

data class RoundRect(val content: Rect, val corners: Corners, val safeRadius: Int = 0) {
    val width = content.width()
    val height = content.height()

    fun scaleAndMove(
        factor: Float,
        centerX: Int,
        centerY: Int
    ): RoundRect {
        return RoundRect(
            content = content.scaleAndMove(factor, centerX = centerX, centerY = centerY),
            corners.scale(factor = factor),
            safeRadius = (factor * safeRadius).toInt()
        )
    }

    override fun toString(): String {
        return "RoundRect(content=$content, corners=$corners, safeRadius=$safeRadius, width=$width, height=$height)"
    }


}

data class Corners(
    val leftTop: Int = 0,
    val rightTop: Int = 0,
    val rightBottom: Int = 0,
    val leftBottom: Int = 0
) {
    val isNoCorners: Boolean = this == NoCorners
    val normalCorner: Int = leftTop

    fun scale(factor: Float): Corners {
        return Corners(
            leftTop = leftTop * factor.toInt(),
            rightTop = rightTop * factor.toInt(),
            rightBottom = rightBottom * factor.toInt(),
            leftBottom = leftBottom * factor.toInt(),
        )
    }

    override fun toString(): String {
        return "Corners(leftTop=$leftTop, rightTop=$rightTop, rightBottom=$rightBottom, leftBottom=$leftBottom, isNoCorners=$isNoCorners, normalCorner=$normalCorner)"
    }

}

val NoCorners = Corners()

fun Rect.scaleAndMove(
    factor: Float,
    centerX: Int,
    centerY: Int,
): Rect {
    return Rect(
        (left * factor).toInt(),
        (top * factor).toInt(),
        (right * factor).toInt(),
        (bottom * factor).toInt()
    ).apply { moveToCenter(x = centerX, y = centerY) }
}

fun Rect.scale(
    factor: Float,
    centerX: Int,
    centerY: Int,
): Rect {
    val originalRect = this

    // Step 1: Translate the center of the Rect to the origin (0, 0).
    val centerOffsetX = centerX - (originalRect.left + originalRect.right) / 2.0f
    val centerOffsetY = centerY - (originalRect.top + originalRect.bottom) / 2.0f

    // Step 2: Scale the Rect coordinates.
    val scaledLeft = centerOffsetX + (originalRect.left - centerX) * factor
    val scaledTop = centerOffsetY + (originalRect.top - centerY) * factor
    val scaledRight = centerOffsetX + (originalRect.right - centerX) * factor
    val scaledBottom =
        centerOffsetY + (originalRect.bottom - centerY) * factor

    // Create the scaled Rect.
    return Rect(
        scaledLeft.toInt(),
        scaledTop.toInt(),
        scaledRight.toInt(),
        scaledBottom.toInt()
    )
}

fun Rect.moveToCenter(
    x: Int,
    y: Int
) {
    val offsetX: Int = x - this.centerX()
    val offsetY: Int = y - this.centerY()

    set(left + offsetX, top + offsetY, right + offsetX, bottom + offsetY)
}
