package com.lovely.bear.laboratory.bitmap.data

import android.graphics.Color

object PixelUtils {

    // 包含半透明像素
    fun isBlackPixel(pixel: Int): Boolean {
        val red = Color.red(pixel)
        val green = Color.green(pixel)
        val blue = Color.blue(pixel)
        val alpha = Color.alpha(pixel)

        // 定义黑的颜色范围
        val minThreshold = 0
        return red == 0 && green == 0 && blue == 0 && alpha > minThreshold
    }

     fun isAlmostBlackPixel(pixel: Int): Boolean {
        val red = Color.red(pixel)
        val green = Color.green(pixel)
        val blue = Color.blue(pixel)
        val alpha = Color.alpha(pixel)

        // 定义黑的颜色范围
        val minThreshold = 10
        return red <= minThreshold && green <= minThreshold && blue <= minThreshold && alpha > minThreshold
    }

     fun isAlmostWhitePixel(pixel: Int): Boolean {
        val red = Color.red(pixel)
        val green = Color.green(pixel)
        val blue = Color.blue(pixel)

        // 定义浅白色的颜色范围
        val minThreshold = 50
        val maxThreshold = 255
        return red >= minThreshold && green >= minThreshold && blue >= minThreshold
    }
}