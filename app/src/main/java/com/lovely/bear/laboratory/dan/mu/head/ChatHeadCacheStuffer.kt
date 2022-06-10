package com.lovely.bear.laboratory.dan.mu.head

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import master.flame.danmaku.danmaku.model.android.ImageCacheStuffer
import master.flame.danmaku.danmaku.model.image.R2LImageDanmu
import master.flame.danmaku.danmaku.model.BaseDanmaku

/**
 * 头像弹幕填充器
 * @see IChatHead
 * @see ChatHeadStufferProxy
 * @author guoyixiong
 */
open class ChatHeadCacheStuffer : ImageCacheStuffer() {

    private val backgroundPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FFF4F5F9") }

    override fun drawBackground(
        danmaku: BaseDanmaku,
        canvas: Canvas,
        left: Float,
        top: Float
    ) {
        if (danmaku is R2LImageDanmu) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val size = danmaku.size
                val radius = size.height / 2F - size.borderWidth
                canvas.drawRoundRect(
                    size.getContentStart(left),
                    size.getContentTop(top),
                    size.getContentEnd(left),
                    size.getContentBottom(top),
                    radius,
                    radius,
                    backgroundPaint
                )
            }
        }
    }
}