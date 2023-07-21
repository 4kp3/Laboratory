/*
Copyright (C), 2023, Nothing Technology
FileName: ThemeUtil
Author: stephen.bi
Date: 2023/03/08 16:02
Description: A tool class about the phone theme.
History:
<author> <time> <version> <desc>
 */
package com.nothing.launcher.util

import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.drawable.Drawable

object MonoUtil {

    // RGB计算灰度值所乘比例
    private const val RED_RATIO = 0.3
    private const val GREEN_RATIO = 0.59
    private const val BLUE_RATIO = 0.11
    // ARGB的偏移量
    private const val ALPHA_OFFSET = 24
    private const val RED_OFFSET = 16
    private const val GREEN_OFFSET = 8
    // 对图片的像素点进行抽样处理，所设置的跨度
    private const val PICTURE_SAMPLE_STEP_2 = 2
    // 经验值，不保留图片中特别难看清的部分
    private const val MIN_ALPHA = 50
    // 经验值，边缘看起来太锐利了，多少保留一点空白
    private const val BOUNDS_PADDING = 2
    // 样本图片尺寸
    private const val SAMPLE_PICTURE_SIZE_100 = 100

    fun readGrayAndAlpha(
        bitmap: Bitmap,
        size: Int,
        pixels: IntArray,
        grayArray: IntArray,
        alphaArray: Array<IntArray>
    ) {
        // 获取图片所有像素点
        bitmap.getPixels(pixels, 0, size, 0, 0, size, size)
        var alpha: Int
        for (i in pixels.indices) {
            // 提取颜色的Alpha部分
            alpha = ((pixels[i].toLong() and 0xFF000000) shr ALPHA_OFFSET).toInt()
            if (alpha > 0) {
                // 分别提取颜色的R、G、B部分
                val r = (pixels[i] and 0x00FF0000) shr RED_OFFSET
                val g = (pixels[i] and 0x0000FF00) shr GREEN_OFFSET
                val b = pixels[i] and 0x000000FF
                // 计算灰度值
                grayArray[i] = (RED_RATIO * r + GREEN_RATIO * g + BLUE_RATIO * b).toInt()
            }
            alphaArray[i / size][i % size] = alpha
        }
    }

    fun readAlpha(
        bitmap: Bitmap,
        size: Int,
        alphaArray: Array<IntArray>
    ) {
        val pixels = IntArray(size * size)
        // 获取图片所有像素点
        bitmap.getPixels(pixels, 0, size, 0, 0, size, size)
        var alpha: Int
        for (i in pixels.indices) {
            // 提取颜色的Alpha部分
            alpha = ((pixels[i].toLong() and 0xFF000000) shr ALPHA_OFFSET).toInt()
            alphaArray[i / size][i % size] = alpha
        }
    }

    fun getContentRatio(drawable: Drawable): Float {
        val size = SAMPLE_PICTURE_SIZE_100
        val bitmap = BitmapUtils.toSquareBitmap(drawable, size, 1f, Bitmap.Config.ALPHA_8, true)
        val alphaArray = Array(size) { IntArray(size) }
        readAlpha(bitmap, size, alphaArray)
        val bounds = getBounds(size, alphaArray)
        val widthRatio = (bounds.right.toFloat() - bounds.left.toFloat()) / size
        val heightRatio = (bounds.bottom.toFloat() - bounds.top.toFloat()) / size
        return if (widthRatio <= 0 || heightRatio <= 0) {
            -1f
        } else {
            kotlin.math.max(widthRatio, heightRatio)
        }
    }

    fun clipBitmap(bitmap: Bitmap, alphaArray: Array<IntArray>): Bitmap {
        val size = bitmap.width
        getBounds(size, alphaArray).run {
            if ((left >= right) || (top >= bottom)) {
                return bitmap
            }
            return Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top)
        }
    }

    fun getBounds(size: Int, alphaArray: Array<IntArray>): Rect {
        val left = getLeft(size, alphaArray)
        val top = getTop(size, alphaArray)
        val right = getRight(size, alphaArray)
        val bottom = getBottom(size, alphaArray)
        return Rect(left, top, right, bottom)
    }

    private fun getLeft(size: Int, alphaArray: Array<IntArray>): Int {
        for (i in 0 until size step PICTURE_SAMPLE_STEP_2) {
            for (j in 0 until size step PICTURE_SAMPLE_STEP_2) {
                if (alphaArray[j][i] > MIN_ALPHA) {
                    var result = i - BOUNDS_PADDING
                    if (result < 0) result = 0
                    return result
                }
            }
        }
        return size
    }

    private fun getTop(size: Int, alphaArray: Array<IntArray>): Int {
        for (i in 0 until size step PICTURE_SAMPLE_STEP_2) {
            for (j in 0 until size step PICTURE_SAMPLE_STEP_2) {
                if (alphaArray[i][j] > MIN_ALPHA) {
                    var result = i - BOUNDS_PADDING
                    if (result < 0) result = 0
                    return result
                }
            }
        }
        return size
    }

    private fun getRight(size: Int, alphaArray: Array<IntArray>): Int {
        for (i in size - 1 downTo 0 step PICTURE_SAMPLE_STEP_2) {
            for (j in 0 until size step PICTURE_SAMPLE_STEP_2) {
                if (alphaArray[j][i] > MIN_ALPHA) {
                    var result = i + BOUNDS_PADDING
                    if (result > size) result = size
                    return result
                }
            }
        }
        return size
    }

    private fun getBottom(size: Int, alphaArray: Array<IntArray>): Int {
        for (i in size - 1 downTo 0 step PICTURE_SAMPLE_STEP_2) {
            for (j in 0 until size step PICTURE_SAMPLE_STEP_2) {
                if (alphaArray[i][j] > MIN_ALPHA) {
                    var result = i + BOUNDS_PADDING
                    if (result > size) result = size
                    return result
                }
            }
        }
        return size
    }
}