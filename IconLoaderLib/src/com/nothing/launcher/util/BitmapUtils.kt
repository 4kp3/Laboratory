/*
Copyright (C), 2022, Nothing Technology
FileName: BitmapUtils
Author: benny.fang
Date: 2022/11/21 16:09
Description: Bitmap tools class
History:
<author> <time> <version> <desc>
 */
package com.nothing.launcher.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Picture
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.provider.MediaStore
import android.util.Log

object BitmapUtils {
    private const val TAG = "BitmapUtils"
    private const val DEBUG = false
    private const val MIN_VISIBLE_ALPHA = 40
    private const val PIXEL_DIFF_PERCENTAGE_THRESHOLD = 0.005f
    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

    fun drawableToBitmap(drawable: Drawable, iconSize: Int): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        var w = iconSize
        var h = iconSize
        if (drawable.intrinsicWidth > 0 && drawable.intrinsicHeight > 0) {
            w = drawable.intrinsicWidth
            h = drawable.intrinsicHeight
        }
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, w, h)
        drawable.draw(canvas)
        return bitmap
    }

    fun toSquareBitmap(
        drawable: Drawable,
        size: Int,
        ratio: Float = 1f,
        config: Config = Config.ARGB_8888,
        isSizeForced: Boolean = true
    ): Bitmap {
        val iconWidth: Int
        val iconHeight: Int
        val newSize: Int
        if (!isSizeForced && (kotlin.math.max(drawable.intrinsicWidth, drawable.intrinsicHeight) > size)) {
            iconWidth = drawable.intrinsicWidth
            iconHeight = drawable.intrinsicHeight
            newSize = if (drawable.intrinsicWidth > drawable.intrinsicHeight) {
                drawable.intrinsicWidth
            } else {
                drawable.intrinsicHeight
            }
        } else {
            newSize = size
            if (drawable.intrinsicWidth > drawable.intrinsicHeight) {
                iconWidth = (newSize * ratio).toInt()
                iconHeight = iconWidth * drawable.intrinsicHeight / drawable.intrinsicWidth
            } else {
                iconHeight = (newSize * ratio).toInt()
                iconWidth = iconHeight * drawable.intrinsicWidth / drawable.intrinsicHeight
            }
        }
        val bitmap = Bitmap.createBitmap(newSize, newSize, config)
        val canvas = Canvas(bitmap)
        drawable.setBounds(
            newSize / 2 - iconWidth / 2,
            newSize / 2 - iconHeight / 2,
            newSize / 2 + iconWidth / 2,
            newSize / 2 + iconHeight / 2
        )
        drawable.draw(canvas)
        return bitmap
    }

    fun scaleBitmap(bitmap: Bitmap, size: Int, scale: Float, outScale: Float, config: Config): Bitmap {
        val result = Bitmap.createBitmap(size, size, config)
        val canvas = Canvas(result)
        var iconWidth = (size * scale).toInt()
        var iconHeight = (size * scale).toInt()
        if (bitmap.width > bitmap.height) {
            iconHeight = iconWidth * bitmap.height / bitmap.width
        } else {
            iconWidth = iconHeight * bitmap.width / bitmap.height
        }
        val rect = Rect(size / 2 - iconWidth / 2, size / 2 - iconHeight / 2, size / 2 + iconWidth / 2, size / 2 + iconHeight / 2)
        canvas.save()
        canvas.scale(outScale, outScale, (size / 2).toFloat(), (size / 2).toFloat())
        canvas.drawBitmap(bitmap, null, rect, bitmapPaint)
        canvas.restore()
        return result
    }

    fun scaleDrawable(drawable: Drawable, size: Int, scale: Float, outScale: Float, config: Config): Drawable {
        val bitmap = Bitmap.createBitmap(size, size, config)
        val canvas = Canvas(bitmap)
        var iconWidth = (size * scale).toInt()
        var iconHeight = (size * scale).toInt()
        if (drawable.intrinsicWidth > drawable.intrinsicHeight) {
            iconHeight = iconWidth * drawable.intrinsicHeight / drawable.intrinsicWidth
        } else {
            iconWidth = iconHeight * drawable.intrinsicWidth / drawable.intrinsicHeight
        }
        val rect = Rect(size / 2 - iconWidth / 2, size / 2 - iconHeight / 2, size / 2 + iconWidth / 2, size / 2 + iconHeight / 2)
        canvas.save()
        canvas.scale(outScale, outScale, (size / 2).toFloat(), (size / 2).toFloat())
        drawable.bounds = rect
        drawable.draw(canvas)
        canvas.restore()
        return BitmapDrawable(bitmap)
    }

    fun convertHardWareBitmap(src: Bitmap): Bitmap {
        if (src.config != Bitmap.Config.HARDWARE) {
            return src
        }
        val w = src.width
        val h = src.height
        val picture = Picture()
        val canvas = picture.beginRecording(w, h)
        canvas.drawBitmap(src, 0f, 0f, null)
        picture.endRecording()
        return Bitmap.createBitmap(picture, w, h, Bitmap.Config.ARGB_8888)
    }

    fun isTransparent(bitmap: Bitmap): Boolean {
        var invisiblePixelCount = 0
        var totalPixelCount = bitmap.height * bitmap.width
        for (h in 0 until bitmap.height) {
            for (w in 0 until bitmap.width) {
                if (Color.alpha(bitmap.getPixel(w, h)) <= MIN_VISIBLE_ALPHA) {
                    invisiblePixelCount++
                }

                val invisibleRatio = invisiblePixelCount.toFloat() / totalPixelCount.toFloat()
                if (invisibleRatio > (1.0f - PIXEL_DIFF_PERCENTAGE_THRESHOLD)) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * 保存bitmap对象到手机相册中，调试时可把中间的bitmap变量保存起来进行对比
     */
    fun saveToLocal(context: Context, bitmap: Bitmap, title: String, description: String) {
        if (DEBUG) {
            try {
                MediaStore.Images.Media.insertImage(
                    context.contentResolver,
                    bitmap,
                    title,
                    description
                )
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
        }
    }
}