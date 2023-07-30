package com.lovely.bear.laboratory.bitmap

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.Size
import com.android.launcher3.icons.BitmapInfo
import com.lovely.bear.laboratory.MyApplication
import com.lovely.bear.laboratory.bitmap.data.IconImage
import com.lovely.bear.laboratory.bitmap.icon.IconConfig
import com.lovely.bear.laboratory.bitmap.icon.IconSize
import com.lovely.bear.laboratory.bitmap.mono.MonoBuilder
import com.lovely.bear.laboratory.bitmap.mono.toBitmap
import com.lovely.bear.laboratory.bitmap.utils.toSize
import com.lovely.bear.laboratory.bitmap.utils.typeDesc

/*
* copyright (c), 2023, nothing technology
* filename: adaptivedrawableanalyse
* author: yixiong.guo
* date: 2023/7/28 22:31
* description:
* history:
* <author> <time> <version> <desc>
*/

/**
 * 分析系统加载进来的图标尺寸
 * 和108的关系
 *
 *
 * thread:launcher-loader|launchericons 244594773:mfillresicondpi=640, miconbitmapsize=152 dp=57
 * thread:taskthumbnailiconcache-1|baseiconfactory   205304660:mfillresicondpi=420, miconbitmapsize=116 dp=44
 */
fun analyse(icons: List<IconImage>): List<IconDrawableAnalyse> {
    return icons.map { analyse(it) }
}

fun analyse(iconImage: IconImage):IconDrawableAnalyse {

    val iconSize = Size(IconConfig.iconSizePx, IconConfig.iconSizePx)
    val fullIconSize = Size(IconConfig.fullySizePx, IconConfig.fullySizePx)

    val icon = iconImage.icon
    val system = IconDrawable(
        label = iconImage.label,
        drawable = icon,
        size = IconSize(sourceSize = icon.toSize(), requestSize = iconSize)
    )
    var fgIconDrawable: IconDrawable? = null
    var bgIconDrawable: IconDrawable? = null
    if (icon is AdaptiveIconDrawable) {
        fgIconDrawable = icon.foreground?.let {
            IconDrawable(
                label = "前景",
                it,
                size = IconSize(sourceSize = it.toSize(), requestSize = fullIconSize)
            )
        }

        bgIconDrawable = icon.background?.let {
            IconDrawable(
                label = "背景",
                it,
                size = IconSize(sourceSize = it.toSize(), requestSize = fullIconSize)
            )
        }
    }

    val userMono = MonoBuilder.buildUserVersion(system.drawable)

    system.originSizeBitmap =
        system.drawable.toBitmap(
            size = null,
            defaultSize = fullIconSize
        )
    system.sizedBitmap =
        system.drawable.toBitmap(size = iconSize)

    fgIconDrawable?.run {
        originSizeBitmap =
            drawable.toBitmap(
                size = null,
                defaultSize = fullIconSize
            )
        sizedBitmap =
            drawable.toBitmap(size = iconSize)
    }

    bgIconDrawable?.run {
        originSizeBitmap =
            drawable.toBitmap(
                size = null,
                defaultSize = fullIconSize
            )
        sizedBitmap =
            drawable.toBitmap(size = iconSize)
    }


    var devMono: Drawable? = null
    var circleGreyMaterialDrawable:Drawable? =null
    var sizedBitmap_: Bitmap? = null
    // 没有自带mono才创建
    if (userMono != null && userMono.first != BitmapInfo.FLAG_AOSP_MONO) {

        val isInWhiteList = false
        // 原则是保持类型不变，确保和launcher原始图标一致
        // AdaptiveIconDrawable 转换后依然是 AdaptiveIconDrawable，其余的drawable 仍然是其本身
        // todo AdaptiveIconDrawable 使用上一次的scale重新创建bitmap即可
        // 非 AdaptiveIconDrawable 直接使用缓存
        circleGreyMaterialDrawable =
            if (icon is AdaptiveIconDrawable) {
                // 取前景进行灰度转换，若是百名单则包括背景
                // 背景为空或者在白名单中，返回原图
                if (icon.background == null
                    || (icon.background is ColorDrawable && (icon.background!! as ColorDrawable).color == Color.TRANSPARENT)
                    || isInWhiteList
                )
                    icon
                /**
                 * 否则只取前景
                 *
                 * todo 是否丢弃背景需要决策
                 * 纯色不丢弃
                 * 有图案不丢弃
                 */
                else AdaptiveIconDrawable(null,icon.foreground)
                /**
                 * todo 是否裁剪小图标，需要思考
                 * 比如视频、阴影
                 */
            } else icon

        // 创建devMono
        // val oldBound = circleGreyMaterialDrawable.bounds
        val outScale = FloatArray(1) { 1F }
        // 获取系统缩放量
        // 图标缩放到适合mask的尺寸，返回缩放值
        // 注意系统可能会对原始BitmapDrawable包装，成为一个AdaptiveIconDrawable
        circleGreyMaterialDrawable = IconConfig.baseIconFactory.normalizeAndWrapToAdaptiveIcon(
            circleGreyMaterialDrawable,// this
            true,
            null,
            outScale
        )
        // 获取系统创建的相同的图标Bitmap
        // 这里使用的Drawable和系统区别在于，对于自适应图标只会取出前景
        // 这里得到的Bitmap应用了mask和上面的缩放量，这是最终用于桌面显示的图片
        val launcherBitmap: Bitmap =
            IconConfig.baseIconFactory.createIconBitmap(circleGreyMaterialDrawable, outScale[0])
        // 在此基础上，创建mono
        devMono = BitmapDrawable(
            MyApplication.APP.resources,
            IconConfig.converterCircle.grayAndDrawCircle(launcherBitmap)
        )
        // 已调整好尺寸
        sizedBitmap_ = launcherBitmap
    }
    val circleGreyMaterial:IconDrawable? = circleGreyMaterialDrawable?.let{
        IconDrawable(
            label = "mono圆形材料",
            drawable = it,
            size = IconSize(
                sourceSize = it.toSize() ?: iconSize,
                requestSize = iconSize
            )
        ).apply {
            sizedBitmap = sizedBitmap_
        }
    }


    return IconDrawableAnalyse(
        system = system,
        fg = fgIconDrawable,
        bg = bgIconDrawable,
        circleGreyMaterial = circleGreyMaterial,
        userMono = userMono?.second,
        devMono = devMono
    )
}

data class IconDrawableAnalyse(
    val system: IconDrawable,
    val fg: IconDrawable?,
    val bg: IconDrawable?,
    val userMono: Drawable?,
    val circleGreyMaterial: IconDrawable?,
    val devMono: Drawable?
)

data class IconDrawable(val label: String, val drawable: Drawable, val size: IconSize) {

    var originSizeBitmap: Bitmap? = null
    var sizedBitmap: Bitmap? = null

    override fun toString(): String {
        return "IconDrawable(label='$label', type=${drawable.typeDesc()},size=$size)"
    }

    fun sizeString(): String {
        return "type=${drawable.typeDesc()},size=$size"
    }
}
// data class IconBitmap(val drawable: Drawable,val suggestSize: Size)