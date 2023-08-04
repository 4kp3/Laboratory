package com.lovely.bear.laboratory.bitmap.data

import android.content.pm.LauncherApps
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.UserHandle
import android.util.Size
import com.lovely.bear.laboratory.MyApplication
import com.lovely.bear.laboratory.bitmap.IconDrawableAnalyse
import com.lovely.bear.laboratory.bitmap.analyse
import com.lovely.bear.laboratory.bitmap.icon.IconConfig
import com.lovely.bear.laboratory.bitmap.mono.makeMono
import com.lovely.bear.laboratory.bitmap.mono.toBitmap

object AppIconLoader {

    val labelList = listOf(
//        "Play 商店",
        //根据包名单独适配
//        "NykaaFashion",
//        "翻译",
//        "位智",// Waze
        "多邻国",
//        "Bewakoof",
//        "Flightradar24", // 已适配，无需再处理这个应用
//        "设置"
//        "音乐",
//        "哔哩",
//        "AliExpress",
//        "Word",
//        "Microsoft Powerpoint",

//        "TeraBox",
//        "slice",
//        "Botim",
//        "Snaptube",

//        "UTS",
//        "Confirmtkt",// 有纯色背景，否则launcher的图很难看
//        "Xstream",

//        "Chrome",
    )

    private val launcherApps by lazy { MyApplication.APP.getSystemService(LauncherApps::class.java) }


    fun loadSystemIcon(): List<IconDrawableAnalyse> {
        val launcherActivityInfos = loadApps().filter {
//            val label = it.label.toString()
//            labelList.any { l -> label.contains(l) }
                true
        }
        val iconImages = launcherActivityInfos.map {
            val d = it.getIcon(IconConfig.densityDpi)
            val label = it.label.toString()
            IconImage(label, d, d.toBitmap(Size(IconConfig.iconSizePx, IconConfig.iconSizePx)))
        }

        return analyse(iconImages)
    }

    private fun loadApps() = launcherApps.getActivityList(null, UserHandle.getUserHandleForUid(0))

    fun load(): List<Image> {
        val launcherActivityInfos = loadApps()
        val labels = launcherActivityInfos.map { it.label }
        val apps = launcherActivityInfos
            .filter {
                val label = it.label.toString()
                labelList.any { l -> label.contains(l) }
//                true
            }.take(20)
        val result = apps
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
            }
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