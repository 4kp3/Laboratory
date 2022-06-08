package com.lovely.bear.laboratory.dan.mu.icon.padding

import android.graphics.Canvas
import com.lovely.bear.laboratory.dan.mu.icon.padding.R2LPaddingDanmu
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.android.AndroidDisplayer.DisplayerConfig
import master.flame.danmaku.danmaku.model.android.SpannedCacheStuffer

/**
 * 支持弹幕padding的填充器
 * @see R2LPaddingDanmu
 * @author guoyixiong
 */
open class PaddingCacheStuffer : SpannedCacheStuffer() {

    override fun drawDanmaku(
        danmaku: BaseDanmaku,
        canvas: Canvas,
        left: Float,
        top: Float,
        fromWorkerThread: Boolean,
        displayerConfig: DisplayerConfig
    ) {
        //当前实现为图像在左侧，后续可改为可配置图像相对位置
        if (danmaku is R2LPaddingDanmu) {
            drawDanmuBackground(danmaku, canvas, left, top)

            super.drawDanmaku(
                danmaku,
                canvas,
                left + danmaku.paddingStart,
                top + danmaku.paddingTop,
                fromWorkerThread,
                displayerConfig
            )
        } else {
            super.drawDanmaku(danmaku, canvas, left, top, fromWorkerThread, displayerConfig)
        }

        super.drawDanmaku(danmaku, canvas, left, top, fromWorkerThread, displayerConfig)
    }

    /**
     * 注意，这里的背景是指文本的背景，由参数[BaseDanmaku.padding]影响
     * 如果使用了[R2LPaddingDanmu]，应该使用方法 [drawDanmuBackground]
     * @see BaseDanmaku.padding
     */
    override fun drawBackground(danmaku: BaseDanmaku?, canvas: Canvas?, left: Float, top: Float) {
        super.drawBackground(danmaku, canvas, left, top)
    }

    /**
     * 绘制弹幕背景
     * 仅支持 [R2LPaddingDanmu]
     */
    protected open fun drawDanmuBackground(
        danmaku: BaseDanmaku,
        canvas: Canvas,
        left: Float,
        top: Float
    ) {

    }
}