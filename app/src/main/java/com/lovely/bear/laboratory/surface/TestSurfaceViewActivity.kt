package com.lovely.bear.laboratory.surface

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import com.lovely.bear.laboratory.R


class TestSurfaceViewActivity : AppCompatActivity() {

    private val surfaceView: SurfaceView by lazy {
        findViewById(R.id.sv)
    }

    private val bound = Rect()
    private var mSurfaceHolder: SurfaceHolder? = null

    private var mRenderThread: RenderThread? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_surface_view)

        android.R.id.content

        surfaceView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                bound.set(0, 0, surfaceView.width, surfaceView.height)
                surfaceView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                mSurfaceHolder?.let {
                    mRenderThread = RenderThread(it, bound).also { it.start() }
                }
            }
        })

        surfaceView.holder.addCallback(object : SurfaceHolder.Callback2 {
            override fun surfaceCreated(holder: SurfaceHolder) {
                mRenderThread?.stopRender()
                mSurfaceHolder = holder
                if (bound.width() > 0) {
                    mRenderThread = RenderThread(holder, bound).also { it.start() }
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                //TODO("Not yet implemented")
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                mRenderThread?.stopRender()
                mSurfaceHolder=null
            }

            override fun surfaceRedrawNeeded(holder: SurfaceHolder) {
                //TODO("Not yet implemented")
            }
        })
    }
}

class RenderThread(val surfaceHolder: SurfaceHolder, val bound: Rect) : Thread() {

    val tag = this::class.simpleName

    @Volatile
    public var running: Boolean = true

    private val paint = Paint().apply {
        color = Color.GRAY
    }

    override fun run() {

        val radius = bound.width() / 8.0F
        val xMin: Float = bound.left + radius
        val xMax = bound.right - radius
        val y = bound.centerY().toFloat()
        var xCurr = xMin
        val xStep = 2F

        fun nextX(): Float {
            xCurr += xStep
            if (xCurr > xMax) {
                xCurr = xMin
            }
            return xCurr
        }

        while (running && !isInterrupted) {
            var canvas: Canvas? = null
            try {
                canvas = surfaceHolder.lockHardwareCanvas()
                canvas.drawCircle(nextX(), y, radius, paint)

            } catch (e: InterruptedException) {
                Log.e(tag, "InterruptedException")
            } finally {
                canvas?.let {
                    surfaceHolder.unlockCanvasAndPost(it)
                }
            }

            try {
                sleep(30)
            } catch (e: InterruptedException) {
                break
            }
        }

        Log.d(tag, "结束绘制")

    }

    fun stopRender() {
        running = false
        interrupt()
    }
}