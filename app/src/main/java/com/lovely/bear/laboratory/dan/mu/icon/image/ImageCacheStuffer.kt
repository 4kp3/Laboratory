package com.lovely.bear.laboratory.dan.mu.icon.image

import android.graphics.Canvas
import android.text.TextPaint
import com.lovely.bear.laboratory.dan.mu.icon.padding.PaddingCacheStuffer
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.android.AndroidDisplayer.DisplayerConfig
import kotlin.math.ceil

/**
 * 带有图片的弹幕填充器
 * @see IImage
 * @author guoyixiong
 */
open class ImageCacheStuffer : PaddingCacheStuffer() {

    override fun drawDanmaku(
        danmaku: BaseDanmaku,
        canvas: Canvas,
        left: Float,
        top: Float,
        fromWorkerThread: Boolean,
        displayerConfig: DisplayerConfig
    ) {
        super.drawDanmaku(danmaku, canvas, left, top, fromWorkerThread, displayerConfig)
        //当前实现为图像在左侧，后续可改为可配置图像相对位置
        if (danmaku is R2LImageDanmu) {
            danmaku.image.drawable.run {
                val t = top + danmaku.drawableTop
                val r = left + danmaku.image.drawableWidth
                val b = t + danmaku.image.drawableHeight

                setBounds(ceil(left).toInt(), ceil(t).toInt(), ceil(r).toInt(), ceil(b).toInt())
                draw(canvas)
            }
        }

    }

}