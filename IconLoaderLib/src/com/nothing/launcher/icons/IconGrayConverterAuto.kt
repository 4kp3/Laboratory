package com.android.launcher3.icons

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.graphics.drawable.Drawable
import com.nothing.launcher.icons.IconPackManager
import com.nothing.launcher.util.BitmapUtils
import com.nothing.launcher.util.MonoUtil

/*
Copyright (C), 2023, Nothing Technology
FileName: IconGrayConverter
Author: stephen.bi
Date: 2023/2/27 15:40
Description: Created for themed icons. Used to convert the colorized icon to a themed icon.
History:
<author> <time> <version> <desc>
 */

class IconGrayConverterAuto() {

    private lateinit var pixels: IntArray
    private lateinit var grayArray: IntArray
    private lateinit var alphaArray: Array<IntArray>
    var outScale = 1f
    var isBadForeground = false

    private var monoSize: Int=10

    fun grayAndDrawCircle(icon: Drawable,monoSize:Int ): Bitmap {
        val gray = gray(icon, monoSize)
        return gray
        return BitmapUtils.scaleBitmap(
            gray,
            monoSize,
            1F,
            1F,
            Config.ALPHA_8
        )
    }

    private fun gray(icon: Drawable, size: Int): Bitmap {
        // 获取最大色块的灰度值
        val mostGray = getMostColorGray(icon)

        // 统一处理为方形
        val bitmap = BitmapUtils.toSquareBitmap(icon, size)
        // 获取灰度和透明度
        readData(bitmap, size)

        var temp: Int
        var minGray = MAX_GRAY_VALUE
        for (i in grayArray.indices) {
            val alpha = alphaArray[i / size][i % size]
            // 透明的点不需要处理
            if (alpha == 0) {
                continue
            }

            // 最大色块转变成目标颜色
            temp = convertMostColorToDes(grayArray[i], mostGray)
            // 获取最大或者最小的颜色值
            minGray = getMinGray(alpha, temp, minGray)

            grayArray[i] = temp
        }

        // 调整alpha值
        adjustAlpha(size, minGray)
        // 重新设置图片像素点
        bitmap.setPixels(pixels, 0, size, 0, 0, size, size)

        // 截取图片中的非空白部分，并转为谷歌默认的ALPHA_8格式
        // return MonoUtil.clipBitmap(bitmap, alphaArray).extractAlpha()
        // 直接返回，因为已经进行了边缘识别
        // todo 确认图像转换后是否透明度发生变化导致需要重新裁剪
        return bitmap.extractAlpha()
    }

    private fun readData(bitmap: Bitmap, size: Int) {
        pixels = IntArray(size * size)
        grayArray = IntArray(size * size)
        alphaArray = Array(size) { IntArray(size) }
        MonoUtil.readGrayAndAlpha(bitmap, size, pixels, grayArray, alphaArray)
    }

    private fun getMostColorGray(icon: Drawable): Int {
        val size = SAMPLE_PICTURE_SIZE_50
        val bitmap = BitmapUtils.toSquareBitmap(icon, size)
        val pixels = IntArray(size * size)
        val grayArray = IntArray(size * size)
        val alphaArray = Array(size) { IntArray(size) }
        // 获取图片所有像素点
        MonoUtil.readGrayAndAlpha(bitmap, size, pixels, grayArray, alphaArray)

        val counter = IntArray(GRAY_DIVIDED_COUNT)
        // 灰度值可划分为多个等级，把每一个点都归入一个等级
        for (i in pixels.indices step PICTURE_SAMPLE_STEP_4) {
            // 半透明的点，不计入统计
            if (alphaArray[i / size][i % size] > MIN_CONTRAST_ALPHA) {
                // 灰度值除以一个等级的大小，由这个结果判断灰度值的等级
                counter[grayArray[i] / GRAY_DIVIDED_WIDTH]++
            }
        }
        // 获取点数最多的灰度等级
        var max = 0
        var maxRank = 0
        for (i in 0 until GRAY_DIVIDED_COUNT) {
            if (counter[i] > max) {
                max = counter[i]
                maxRank = i
            }
        }

        // 从坐标轴可知，等级 0 对应坐标轴左起第 1 段，该等级中最大的值为 15，等级 1 对应第 2 段，最大为 31，以此类推
        // 那么，等级 n 对应第 n + 1 段，最大为 (n + 1) * 256 / 16 - 1，总结得如下公式
        return (maxRank + 1) * GRAY_VALUE_TOTAL / GRAY_DIVIDED_COUNT - 1
    }

    private fun getMinGray(alpha: Int, gray: Int, boundaryGray: Int): Int {
        // 半透明的点，不计入统计
        if (alpha > MIN_CONTRAST_ALPHA) {
            // 找到最小灰度值
            if (gray < boundaryGray) {
                return gray
            }
        }
        return boundaryGray
    }

    private fun convertMostColorToDes(origin: Int, offset: Int): Int {
        // 加上255与最大色块灰度的差值，这样多数点的灰度值会增加到255左右，就是白色
        // 其他点也有变化，颜色上限是255，如果超过，则超过多少就向黑色回调多少
        // 点的灰度会变成255或接近255，呈白色
        var result = origin + MAX_GRAY_VALUE - offset
        if (result > MAX_GRAY_VALUE) {
            result = MAX_GRAY_VALUE * 2 - result
        }
        return result
    }

    private fun adjustAlpha(size: Int, minGray: Int) {
        // 点数最多的灰度等级的最小值
        val mostGrayMinValue = MAX_GRAY_VALUE - GRAY_VALUE_TOTAL / GRAY_DIVIDED_COUNT
        val mostGraySquare = mostGrayMinValue * mostGrayMinValue
        val minGraySquare = minGray * minGray
        val k = (CONTRAST_ALPHA_MAX - CONTRAST_ALPHA_MIN).toFloat() / (mostGraySquare - minGraySquare)
        for (i in grayArray.indices) {
            var alpha = alphaArray[i / size][i % size]
            val gray = grayArray[i]
            // 调整alpha值
            if (alpha > MIN_CONTRAST_ALPHA && mostGrayMinValue > minGray) {
                alpha = (k * (gray * gray - mostGraySquare)).toInt() + CONTRAST_ALPHA_MAX
                if (alpha > MAX_GRAY_VALUE) {
                    alpha = MAX_GRAY_VALUE
                }
            }
            pixels[i] = alpha shl ALPHA_OFFSET
        }
    }

    companion object {
        // 设计师要求，图标缩放后占背景的比例
        const val THEMED_ICON_SCALE_RATIO = 7f / 18f
        // 正常尺寸下的最大边距
        const val MAX_BLANK_RATIO = THEMED_ICON_SCALE_RATIO + 0.04
        // 正常尺寸下的最小边距
        const val MIN_BLANK_RATIO = THEMED_ICON_SCALE_RATIO - 0.04
        // ARGB的偏移量
        private const val ALPHA_OFFSET = 24
        // 经验值，0-255的灰度值平分后的等级个数
        private const val GRAY_DIVIDED_COUNT = 32
        // 0-255的灰度值平分后每个等级的大小
        private const val GRAY_DIVIDED_WIDTH = 256 / GRAY_DIVIDED_COUNT
        // 最大灰度值
        private const val MAX_GRAY_VALUE = 255
        // 灰度数量
        private const val GRAY_VALUE_TOTAL = 256
        // 对图片的像素点进行抽样处理，所设置的跨度
        private const val PICTURE_SAMPLE_STEP_4 = 4
        private const val RATIO_0_5 = 0.5f
        // 对于本身就半透明的像素点，进行对比度优化会有显示异常
        private const val MIN_CONTRAST_ALPHA = 110
        // 增加对比度时，图标主要部分的透明度
        private const val CONTRAST_ALPHA_MAX = 255
        // 增加对比度时，图标次要部分的透明度
        private const val CONTRAST_ALPHA_MIN = 45
        // 样本图片尺寸
        private const val SAMPLE_PICTURE_SIZE_50 = 50

        fun getMonoFgColor(context: Context, isLight: Boolean): Int {
            val isNothingMonoStyle = IconPackManager.instance.isNothingThemedIconSelected()
            return context.getColor(if (isLight) {
                if (isNothingMonoStyle) R.color.mono_nothing_foreground_color else R.color.mono_color_foreground_color
            } else {
                if (isNothingMonoStyle) R.color.mono_nothing_foreground_color_night else R.color.mono_color_foreground_color_night
            })
        }

        fun getMonoBgColor(context: Context, isLight: Boolean): Int {
            val isNothingMonoStyle = IconPackManager.instance.isNothingThemedIconSelected()
            return context.getColor(if (isLight) {
                if (isNothingMonoStyle) R.color.mono_nothing_background_color else R.color.mono_color_background_color
            } else {
                if (isNothingMonoStyle) R.color.mono_nothing_background_color_night else R.color.mono_color_background_color_night
            })
        }
    }
}