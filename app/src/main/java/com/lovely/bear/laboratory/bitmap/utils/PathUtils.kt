package com.lovely.bear.laboratory.bitmap.utils

import android.graphics.Path
import android.graphics.RectF

object PathUtils {
    fun createRoundedRectPath(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        radius: Float
    ): Path {
        // Create a Path object to hold the rounded rectangle.
        val path = Path()

        // Create a RectF object to define the bounds of the rounded rectangle.
        val rectF = RectF(left, top, right, bottom)

        // Add the rounded rectangle to the path with the specified radius.
        // The corners will be rounded with the given radius.
        path.addRoundRect(rectF, radius, radius, Path.Direction.CW)
        return path
    }
}