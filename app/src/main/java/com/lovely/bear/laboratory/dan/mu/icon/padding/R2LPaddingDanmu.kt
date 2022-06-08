package com.lovely.bear.laboratory.dan.mu.icon.padding

import master.flame.danmaku.danmaku.model.Duration
import master.flame.danmaku.danmaku.model.IDisplayer
import master.flame.danmaku.danmaku.model.R2LDanmaku
import kotlin.math.max

/**
 * 从右向左类型的弹幕，支持四向padding设置
 * 注，padding相对于文本而言
 * @author guoyixiong
 */
open class R2LPaddingDanmu(
    paddingStart: Int = 0,
    paddingTop: Int = 0,
    paddingEnd: Int = 0,
    paddingBottom: Int = 0,
    duration: Duration,
) : R2LDanmaku(duration) {

    open var paddingStart: Int = paddingStart
        set(value) {
            field = max(value, 0)
        }
    open var paddingTop: Int = paddingTop
        set(value) {
            field = max(value, 0)
        }
    open var paddingEnd: Int = paddingEnd
        set(value) {
            field = max(value, 0)
        }
    open var paddingBottom: Int = paddingBottom
        set(value) {
            field = max(value, 0)
        }

    val paddingHorizontal: Int
        get() {
            return paddingStart + paddingEnd
        }

    val paddingVertical: Int
        get() {
            return paddingTop + paddingBottom
        }

    /**
     * 弹幕总宽度
     */
    val width: Float
        get(){
            return paintWidth + paddingHorizontal
        }

    /**
     * 弹幕总高度
     */
    val height: Float
        get() {
            return paintHeight + paddingVertical
        }

    val textTop: Float
        get() {
            return paddingTop * 1F
        }

    override fun measure(displayer: IDisplayer, fromWorkerThread: Boolean) {
        super.measure(displayer, fromWorkerThread)
        mDistance = (displayer.width + width).toInt()
        mStepX = mDistance / duration.value.toFloat()
    }

    override fun getAccurateLeft(displayer: IDisplayer, currTime: Long): Float {
        val elapsedTime = currTime - actualTime
        return if (elapsedTime >= duration.value) {
            -width
        } else displayer.width - elapsedTime * mStepX
    }

    override fun getRectAtTime(displayer: IDisplayer, currTime: Long): FloatArray? {
        if (!isMeasured) return null
        val left = getAccurateLeft(displayer, time)
        if (RECT == null) {
            RECT = FloatArray(4)
        }
        RECT[0] = left
        RECT[1] = y
        RECT[2] = left + width
        RECT[3] = y + height
        return RECT
    }

    override fun getLeft() = x

    override fun getTop() = y

    override fun getRight() = x + width

    override fun getBottom() = y + height

}