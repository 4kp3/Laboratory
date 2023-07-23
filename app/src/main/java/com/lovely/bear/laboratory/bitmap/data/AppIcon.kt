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

object AppIcon {


    fun getAllImages():List<Image> {
        val launcherApps = MyApplication.APP.getSystemService(LauncherApps::class.java)
        return launcherApps.getActivityList(null, UserHandle.getUserHandleForUid(0))
            .filter{
//                it.label.toString() == "Play 商店"
                it.label.toString().contains("Kindle")
//                true
            }
            .map {
            val icon = it.getIcon(IconConfig.densityDpi)


            val bitmap = getBitmap(icon)
            if (icon is AdaptiveIconDrawable) {
                val fg = icon.foreground?.run { getBitmap(this) }
                val bg = icon.background?.run { getBitmap(this) }
                if (fg != null && bg != null) {
                    AdaptiveIconImage(
                        label = icon::class.simpleName?:"",
                        icon = icon,
                        bitmap = bitmap,
                        fgBitmap = IconImage("前景",icon.foreground,fg),
                        bgBitmap = IconImage("背景",icon.background,bg)
                    )
                }else null
            } else {
                IconImage(label = icon::class.simpleName?:"", icon = icon, bitmap = bitmap)
            }
            }.filterNotNull()
    }

    val canvas = Canvas()

    fun getBitmap(drawable: Drawable): Bitmap {
        return when (drawable) {
            is BitmapDrawable -> {
                drawable.bitmap
            }

            else -> {
                drawable.setBounds(0, 0, IconConfig.iconSizePx, IconConfig.iconSizePx)
                Bitmap.createBitmap(
                    IconConfig.iconSizePx,
                    IconConfig.iconSizePx,
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