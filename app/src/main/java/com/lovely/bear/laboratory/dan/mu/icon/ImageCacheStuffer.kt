package com.lovely.bear.laboratory.dan.mu.icon

import android.graphics.Canvas
import android.text.TextPaint
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.android.SpannedCacheStuffer

/**
 * 带有图片的弹幕填充器
 * @see IImage
 * @author guoyixiong
 */
class ImageCacheStuffer: SpannedCacheStuffer() {
    override fun measure(danmaku: BaseDanmaku, paint: TextPaint?, fromWorkerThread: Boolean) {
        if (danmaku.isImageDan()) {

        }else super.measure(danmaku, paint, fromWorkerThread)
    }

    override fun drawBackground(danmaku: BaseDanmaku, canvas: Canvas, left: Float, top: Float) {
        paint.setColor(-0x7edacf65)
        canvas.drawRect(
            left + 2,
            top + 2,
            left + danmaku.paintWidth - 2,
            top + danmaku.paintHeight - 2,
            paint
        )
    }
}