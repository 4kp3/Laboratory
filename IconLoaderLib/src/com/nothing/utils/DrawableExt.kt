/*
Copyright (C), 2023, Nothing Technology
FileName: DrawableExt
Author: stephen.bi
Date: 2023/2/27 15:40
Description: Created for themed icons. Used to analyze an icon and create a themed icon with it.
History:
<author> <time> <version> <desc>
 */

package com.nothing.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.VectorDrawable
import android.util.Pair
import com.android.launcher3.icons.BaseIconFactory
import com.android.launcher3.icons.BitmapInfo
import com.android.launcher3.icons.IconGrayConverter
import com.android.launcher3.icons.ThemedIconDrawable
import com.nothing.launcher.icons.IconPackManager
import com.nothing.launcher.util.BitmapUtils
import com.nothing.launcher.util.BitmapUtils.toSquareBitmap
import com.nothing.launcher.util.MonoUtil

val shrinkInsetPercentage =
    AdaptiveIconDrawable.getExtraInsetFraction() / (1 + 2 * AdaptiveIconDrawable.getExtraInsetFraction())

/**
 * 根据初始图片类型，处理并返回对应的图片
 */
fun Drawable.getMonochrome(
    res: Resources,
    originIcon: Drawable?,
    iconConverter: IconGrayConverter,
): Pair<Int, Drawable?>? {
    return if (this is AdaptiveIconDrawable && monochrome != null) {
        // 谷歌已适配的图标
        if ((monochrome is VectorDrawable) || (monochrome is InsetDrawable)) {
            // VectorDrawable和InsetDrawable是大多数谷歌已适配图标的类型，均可直接使用
            monochrome?.let { Pair(BitmapInfo.FLAG_AOSP_MONO, remakeAospMono(it, iconConverter)) }
        } else {
            // 极少数图标是BitmapDrawable、LayerDrawable等类型，边距可能有问题，我们重新绘制后使用
            monochrome?.let {
                Pair(
                    BitmapInfo.FLAG_AOSP_MONO,
                    remakeAospMono(res, it, iconConverter)
                )
            }
        }
    } else {
        // 谷歌未适配图标，采用通用适配方案处理
        originIcon?.let {
            // 仅NothingThemed生效
            if (IconPackManager.instance.isForcedMonoEnabled && IconPackManager.instance.isNothingThemedIconSelected()) {
                Pair(BitmapInfo.FLAG_NT_MONO, createGeneralMono(res, it, iconConverter))
            } else {
                null
            }
        }
    }
}

/**
 * 根据原生monochrome图标的边界，判断是否需要调整其边距
 */
fun remakeAospMono(monochrome: Drawable, converter: IconGrayConverter): Drawable {
    MonoUtil.getContentRatio(monochrome).let {
        val needResize = it > 0
                && (it > IconGrayConverter.MAX_BLANK_RATIO
                || it < IconGrayConverter.MIN_BLANK_RATIO)
        return if (!needResize) {
            monochrome
        } else {
            BitmapUtils.scaleDrawable(
                monochrome,
                converter.iconSize,
                IconGrayConverter.THEMED_ICON_SCALE_RATIO / it,
                converter.outScale,
                Bitmap.Config.ALPHA_8
            )
        }
    }
}

/**
 * 少数monochrome图标太小或者太大了
 * 原因是三方app对图标边距的设置有问题，导致边距太大或太小
 * 所以我们取出图标，裁剪掉原有边距，重新加上正确的边距
 */
fun remakeAospMono(res: Resources, monochrome: Drawable, converter: IconGrayConverter): Drawable {
    var bitmap = toSquareBitmap(
        monochrome,
        converter.iconSize,
        1f,
        Bitmap.Config.ALPHA_8,
        false
    )
    // 裁剪图片
    val size = bitmap.width
    val alphaArray = Array(size) { IntArray(size) }
    MonoUtil.readAlpha(bitmap, size, alphaArray)
    bitmap = MonoUtil.clipBitmap(bitmap, alphaArray)
    // 拉伸图片
    bitmap = BitmapUtils.scaleBitmap(
        bitmap,
        converter.iconSize,
        IconGrayConverter.THEMED_ICON_SCALE_RATIO,
        converter.outScale,
        Bitmap.Config.ALPHA_8
    )
    return BitmapDrawable(res, bitmap)
}

/**
 * 无monochrome图的图标，我们创建NT风格的mono图标
 */
fun createGeneralMono(res: Resources, icon: Drawable, iconConverter: IconGrayConverter): BitmapDrawable {
    val fetchedIcon: Drawable? = if (icon is AdaptiveIconDrawable && !iconConverter.isBadForeground) icon.foreground else icon
    return with(fetchedIcon ?: icon) {
        BitmapDrawable(res, iconConverter.grayAndDrawCircle(this))
    }
}

/**
 * 调整mono图标的边距，避免出现边距太大或太小的问题
 */
fun standardizeMono(context: Context, mono: Drawable, size: Int): Drawable {
    val converter = IconGrayConverter(size)
    return remakeAospMono(context.resources, mono, converter)
}

/**
 * 创建NT风格的mono图标
 */
private fun createNtAdaptiveIcon(context: Context, icon: Drawable?, size: Int): Drawable? {
    icon ?: return null
    val converter = IconGrayConverter(size)
    val bitmap = converter.grayAndDrawCircle(icon)
    return BitmapDrawable(context.resources, bitmap)
}

/**
 * 创建应用拖动时显示的动效图标
 */
fun createAdaptiveIcon(
    context: Context,
    width: Int,
    height: Int,
    icon: Drawable,
    bubbleTextViewIcon: Drawable?,
    outScale: Float,
    packageName: String?
): AdaptiveIconDrawable? {
    var result: Drawable? = null
    val colors = ThemedIconDrawable.getColors(context)
    val size: Int = kotlin.math.max(width, height)
    // 如果有现成的monochrome图标，则直接使用，而不再重新加载，使20ms以上的操作缩短为0-1ms
    if (bubbleTextViewIcon is ThemedIconDrawable && bubbleTextViewIcon.monoIcon != null) {
        // 由于有阴影，获取的图标已被缩小，此处进行放大，恢复之前的大小
        result = BaseIconFactory.ClippedMonoDrawable(
            BitmapDrawable(context.resources, bubbleTextViewIcon.monoIcon),
            (outScale - 1) / 2
        )
    } else {
        val aid = icon.mutate()
        if (aid is AdaptiveIconDrawable) {
            val mono = aid.monochrome
            if (mono != null) {
                result = if (mono is LayerDrawable || mono is BitmapDrawable) {
                    standardizeMono(context, mono, size)
                } else {
                    val converter = IconGrayConverter(size)
                    remakeAospMono(mono, converter)
                }
            } else {
                if (IconPackManager.instance.isForcedMonoEnabled) {
                    val drawable = if (isBadForeground(packageName) || aid.foreground == null) {
                        icon
                    } else {
                        aid.foreground
                    }
                    result = createNtAdaptiveIcon(context, drawable, size)
                }
            }
        } else {
            if (IconPackManager.instance.isForcedMonoEnabled) {
                result = createNtAdaptiveIcon(context, icon, size)
            }
        }
    }
    return result?.mutate()?.run {
        setTint(colors[1])
        AdaptiveIconDrawable(
            ColorDrawable(colors[0]),
            // 由于自适应图标会放大其内容，所以这里进行缩小，最后大小保持不变
            BaseIconFactory.ClippedMonoDrawable(this, shrinkInsetPercentage)
        )
    }
}

fun isBadForeground(packageName: String?): Boolean {
    return IconPackManager.instance.checkBadForegroundFunction?.apply(packageName) ?: false
}