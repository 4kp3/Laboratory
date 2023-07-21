package com.lovely.bear.laboratory.bitmap

import android.graphics.Color

/**
 * todo 改为使用传递的配置参数获取结果，不是写死在这里
 * 方法聚合，把颜色值获取和判断逻辑分离，不是每个方法都有颜色提取
 */
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


    fun isAlmostWhitePixelStrict(pixel: Int): Boolean {
        val red = Color.red(pixel)
        val green = Color.green(pixel)
        val blue = Color.blue(pixel)

        // 定义浅白色的颜色范围
        val minThreshold = 200
        val maxThreshold = 255
        return red >= minThreshold && green >= minThreshold && blue >= minThreshold
    }

    // todo 使用配置常量决定
    fun isAlmostBlackPixelStrict(pixel: Int): Boolean {
        val minThreshold = 10
        return colorTest(pixel) { alpha, red, green, blue ->
            // 定义黑的颜色范围
            return red <= minThreshold && green <= minThreshold && blue <= minThreshold && alpha > minThreshold
        }
    }

    inline fun colorTest(color: Int, test: Test): Boolean {
        return test(Color.alpha(color), Color.red(color), Color.green(color), Color.blue(color))
    }

}
typealias Test = (alpha: Int, red: Int, green: Int, blue: Int) -> Boolean
