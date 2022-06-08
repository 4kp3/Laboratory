package com.lovely.bear.laboratory.dan.mu.icon.image

import android.util.Log
import com.lovely.bear.laboratory.dan.mu.icon.padding.R2LPaddingDanmu
import master.flame.danmaku.danmaku.model.Duration
import master.flame.danmaku.danmaku.model.IDisplayer

/**
 * 从右向左类型的弹幕，支持左侧图片显示
 * 左侧图片将被垂直居中绘制在padding区间内，并靠左显示
 *
 * @author guoyixiong
 */
class R2LImageDanmu(
    val image: IImage,
    paddingStart: Int = 0,
    paddingTop: Int = 0,
    paddingEnd: Int = 0,
    paddingBottom: Int = 0,
    duration: Duration,
) : R2LPaddingDanmu(
    paddingStart, paddingTop, paddingEnd, paddingBottom, duration
) {

    private val logTag = "R2LImageDanmu"

    override var paddingStart: Int = image.width
        set(value) {
            //确保图像能有空间被正常绘制
            if (value >= image.width) {
                field = value
            }
        }

    /**
     * paddingTop+paintHeight+paddingBottom>=image.height
     */
    override var paddingTop: Int = minPaddingTop.toInt()
        set(value) {
            if (value >= minPaddingTop) {
                field = value
            }
        }

    override var paddingBottom: Int = minPaddingTop.toInt()
        set(value) {
            if (value >= minPaddingTop) {
                field = value
            }
        }

    private val minPaddingTop: Float
        get() {
            return if (paintHeight > image.drawableHeight) {
                0F
            } else {
                (image.drawableHeight - paintHeight) / 2F
            }
        }

    /**
     * 图片相对弹幕矩形的顶部高度
     * 确保图像居中绘制
     */
    val drawableTop: Float
        get() {
            return (height - image.drawableHeight) / 2F
        }

    override fun measure(displayer: IDisplayer, fromWorkerThread: Boolean) {
        super.measure(displayer, fromWorkerThread)
        //在字体测量结束后，校验paddingVertical是否合法
        if (paddingTop <= minPaddingTop) {
            paddingTop = minPaddingTop.toInt()
        }
        if (paddingBottom <= minPaddingTop) {
            paddingBottom = minPaddingTop.toInt()
        }

        Log.d(
            logTag,
            """测量measure
            padding：paddingStart=$paddingStart,paddingTop=$paddingTop,paddingEnd=$paddingEnd,paddingBottom=$paddingBottom
            minPaddingTop=$minPaddingTop,drawableTop=$drawableTop,textTop=$textTop
            image:width=${image.width},drawableWidth=${image.drawableWidth},drawableHeight=${image.drawableWidth}${image.drawableHeight},drawablePadding=${image.drawablePadding}
            text：paintWidth=$paintWidth,paintHeight=${paintHeight}
            danmu:width=$width,height=$height
        """.trimIndent()
        )
    }
}