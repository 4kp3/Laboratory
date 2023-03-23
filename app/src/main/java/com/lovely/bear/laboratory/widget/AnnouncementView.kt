package com.lovely.bear.laboratory.widget

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.Choreographer
import android.view.Choreographer.FrameCallback
import android.widget.TextView
import java.lang.ref.WeakReference

/**
 * 跑马灯效果，延迟3s开始跑马灯滚动
 * @author guoyixiong
 */
class AnnouncementView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatTextView(context, attrs, defStyleAttr) {

    init {
        //限制单行水平滚动
        maxLines = 1
        setSingleLine()
        setHorizontallyScrolling(true)
        //禁止原本的marquee
        ellipsize = null
        //无限滚动
        marqueeRepeatLimit = -1
    }

    private var mMarquee: Marquee? = null

    private fun canMarquee(): Boolean {
        val width: Int = right - left - compoundPaddingLeft - compoundPaddingRight
        return width > 0 && layout.getLineWidth(0) > width
    }

    private fun startMarquee() {
        if ((mMarquee == null || mMarquee!!.isStopped) && (isFocused || isSelected)
            && lineCount == 1 && canMarquee()
        ) {
            Log.d(TAG, "startMarquee")
            if (mMarquee == null) mMarquee = Marquee(this)
            //无限滚动
            mMarquee!!.start(marqueeRepeatLimit)
        }
    }

    private fun stopMarquee() {
        if (mMarquee != null && !mMarquee!!.isStopped) {
            Log.d(TAG, "stopMarquee")
            mMarquee!!.stop()
        }
    }

    private fun startStopMarquee(start: Boolean) {
        if (start) {
            startMarquee()
        } else {
            stopMarquee()
        }
    }

    override fun setSelected(selected: Boolean) {
        val wasSelected = isSelected

        super.setSelected(selected)

        if (selected != wasSelected) {
            startStopMarquee(selected)
        }
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        startStopMarquee(focused)
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        startStopMarquee(hasWindowFocus)
    }

    companion object {
        const val TAG = "AnnouncementView"
    }

    private class Marquee(v: TextView) {
        private val mView: WeakReference<TextView>
        private val mChoreographer: Choreographer
        private var mStatus = MARQUEE_STOPPED
        private val mPixelsPerMs: Float
        private var mMaxScroll = 0f
        private var mRepeatLimit = 0
        var scroll = 0f
            private set
        private var mLastAnimationMs: Long = 0
        private val mTickCallback = FrameCallback { tick() }
        private val mStartCallback = FrameCallback {
            mStatus = MARQUEE_RUNNING
            mLastAnimationMs = System.currentTimeMillis()
            tick()
        }
        private val mRestartCallback = FrameCallback {
            if (mStatus == MARQUEE_RUNNING) {
                if (mRepeatLimit >= 0) {
                    mRepeatLimit--
                }
                start(mRepeatLimit)
            }
        }

        fun tick() {
            if (mStatus != MARQUEE_RUNNING) {
                return
            }
            mChoreographer.removeFrameCallback(mTickCallback)
            val textView = mView.get()
            if (textView != null && (textView.isFocused || textView.isSelected)) {
                val currentMs: Long = System.currentTimeMillis()
                val deltaMs = currentMs - mLastAnimationMs
                mLastAnimationMs = currentMs
                val deltaPx = deltaMs * mPixelsPerMs
                scroll += deltaPx
                if (scroll > mMaxScroll) {
                    scroll = mMaxScroll
                    mChoreographer.postFrameCallbackDelayed(
                        mRestartCallback,
                        MARQUEE_DELAY.toLong()
                    )
                } else {
                    mChoreographer.postFrameCallback(mTickCallback)
                }
                scroll(scroll, textView)
            }
        }

        fun start(repeatLimit: Int) {
            if (repeatLimit == 0) {
                stop()
                return
            }
            mRepeatLimit = repeatLimit
            val textView = mView.get()
            if (textView != null && textView.layout != null) {
                mStatus = MARQUEE_STARTING
                scroll = 0.0f
//                val textWidth = (textView.width - textView.compoundPaddingLeft
//                        - textView.compoundPaddingRight)
                val lineWidth: Float = textView.layout.getLineWidth(0)
                //val gap = textWidth / 3.0f
                mMaxScroll = lineWidth
                scroll(scroll, textView)
                mChoreographer.postFrameCallbackDelayed(mStartCallback, MARQUEE_DELAY.toLong())
            }
        }

        fun stop() {
            mStatus = MARQUEE_STOPPED
            mChoreographer.removeFrameCallback(mStartCallback)
            mChoreographer.removeFrameCallback(mRestartCallback)
            mChoreographer.removeFrameCallback(mTickCallback)
            resetScroll()
        }

        private fun resetScroll() {
            scroll = 0.0f
            val textView = mView.get()
            textView?.let {
                scroll(0F, it)
            }
        }

        private fun scroll(x: Float, textView: TextView) {
            textView.scrollTo(x.toInt(), textView.scrollY)
        }

        val isRunning: Boolean
            get() {
                return mStatus == MARQUEE_RUNNING
            }
        val isStopped: Boolean
            get() {
                return mStatus == MARQUEE_STOPPED
            }

        init {
            val density = v.context.resources.displayMetrics.density
            mPixelsPerMs = MARQUEE_DP_PER_SECOND * density / 1000f
            mView = WeakReference(v)
            mChoreographer = Choreographer.getInstance()
        }

        companion object {
            private const val MARQUEE_DELTA_MAX = 0.07f
            private const val MARQUEE_DELAY = 3000//延迟3s再开始滚动
            private const val MARQUEE_DP_PER_SECOND = 30
            private const val MARQUEE_STOPPED: Byte = 0x0
            private const val MARQUEE_STARTING: Byte = 0x1
            private const val MARQUEE_RUNNING: Byte = 0x2
        }

    }
}