package com.lovely.bear.laboratory.dan.mu.icon.chat.head

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.lovely.bear.laboratory.dan.mu.icon.image.ImageCacheStuffer
import com.lovely.bear.laboratory.dan.mu.icon.image.R2LImageDanmu
import com.lovely.bear.laboratory.dan.mu.icon.padding.R2LPaddingDanmu
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.android.AndroidDisplayer.DisplayerConfig
import master.flame.danmaku.danmaku.model.android.SpannedCacheStuffer

/**
 * 头像弹幕填充器
 * @see IChatHead
 * @see ChatHeadStufferProxy
 * @author guoyixiong
 */
open class ChatHeadCacheStuffer : ImageCacheStuffer() {

    private val backgroundPaint = Paint().apply { color= Color.RED }

    override fun drawDanmuBackground(
        danmaku: BaseDanmaku,
        canvas: Canvas,
        left: Float,
        top: Float
    ) {
        if (danmaku is R2LImageDanmu) {
            canvas.drawRoundRect(
                left,
                top,
                left + danmaku.width,
                top + danmaku.height,
                12F,
                12F,
                backgroundPaint
            )
        }
    }
}