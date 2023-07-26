package com.lovely.bear.laboratory.bitmap.data

import android.content.pm.LauncherApps
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.UserHandle
import com.lovely.bear.laboratory.MyApplication
import com.lovely.bear.laboratory.bitmap.icon.IconConfig
import com.lovely.bear.laboratory.bitmap.mono.makeMono

object AppIconLoader {

    val labelList = listOf(
//        "音乐",
        "哔哩哔哩",
        "AliExpress",
        "Word",
        "Microsoft Powerpoint",

        "TeraBox",
        "slice",
        "Botim",
        "Snaptube",

        "翻译",
        "UTS",
        "Confirmtkt",
        "Xstream",
        "Flightradar24",
//        "设置"
    )

    fun load(): List<Image> {
        val launcherApps = MyApplication.APP.getSystemService(LauncherApps::class.java)
        val result = launcherApps.getActivityList(null, UserHandle.getUserHandleForUid(0))
            .filter {
                val label = it.label.toString()
                labelList.any { l -> label.contains(l) }
//                true
            }.take(8)
            .mapNotNull {
                buildImage(it.getIcon(IconConfig.densityDpi)).apply {
                    appInfo = AppInfo(it.label.toString(), this is AdaptiveIconImage)

                    // 构建edge
                    makeEdgeBitmap(this)
                    if (this is AdaptiveIconImage) {
                        makeEdgeBitmap(this.fgBitmap)
                        makeEdgeBitmap(this.bgBitmap)
                    }

                    // 构建mono
                    mono = makeMono(this)
                }
            }.takeLast(8)
        return result
    }

    private val canvas = Canvas()

    fun buildImage(icon: Drawable): Image {
        val bitmap = getBitmap(icon)
        return if (icon is AdaptiveIconDrawable) {
            val fg = icon.foreground?.run { getBitmap(this) }
            val bg = icon.background?.run { getBitmap(this) }

            if (fg != null && bg != null) {
                AdaptiveIconImage(
                    label = "自适应图标",
                    icon = icon,
                    bitmap = bitmap,
                    fgBitmap = IconImage("前景", icon.foreground, fg),
                    bgBitmap = IconImage("背景", icon.background, bg)
                )
            } else IconImage(
                label = icon::class.simpleName ?: "",
                icon = icon,
                bitmap = bitmap
            )
        } else {
            IconImage(label = "单图", icon = icon, bitmap = bitmap)
        }
    }

    fun getBitmap(drawable: Drawable): Bitmap {
        return when (drawable) {
            is BitmapDrawable -> {
                drawable.bitmap
            }

            else -> {
                drawable.setBounds(0, 0, IconConfig.fullySizePx, IconConfig.fullySizePx)
                Bitmap.createBitmap(
                    IconConfig.fullySizePx,
                    IconConfig.fullySizePx,
                    Bitmap.Config.ARGB_8888,
                    true
                ).also {
                    canvas.setBitmap(it)
                    drawable.draw(canvas)
                }
            }
        }
    }
}