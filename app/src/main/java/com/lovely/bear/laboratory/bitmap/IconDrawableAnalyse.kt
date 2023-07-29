package com.lovely.bear.laboratory.bitmap

import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.util.Size
import com.lovely.bear.laboratory.bitmap.data.IconImage
import com.lovely.bear.laboratory.bitmap.icon.IconConfig
import com.lovely.bear.laboratory.bitmap.icon.IconSize
import com.lovely.bear.laboratory.bitmap.mono.MonoBuilder
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
//    val source = if (icon is AdaptiveIconDrawable) icon.foreground else icon

    val userMono = MonoBuilder.buildUserVersion((fgIconDrawable ?: system).drawable)

    return IconDrawableAnalyse(
        system = system,
        fg = fgIconDrawable,
        bg = bgIconDrawable,
        userMono = userMono,
        devMono = null
    )
}

data class IconDrawableAnalyse(
    val system: IconDrawable,
    val fg: IconDrawable?,
    val bg: IconDrawable?,
    val userMono: Drawable?,
    val devMono: Drawable?
)

data class IconDrawable(val label: String, val drawable: Drawable, val size: IconSize) {
    override fun toString(): String {
        return "IconDrawable(label='$label', type=${drawable.typeDesc()},size=$size)"
    }

    fun sizeString() : String {
        return "type=${drawable.typeDesc()},size=$size"
    }
}
// data class IconBitmap(val drawable: Drawable,val suggestSize: Size)