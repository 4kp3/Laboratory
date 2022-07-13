package com.lovely.bear.laboratory

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.TypedValue

/**
 * 问题标签Drawable
 */
class Tag(val text: String, resources: Resources) : Drawable() {

    init {
        initIfNot(resources)
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = textS
    }

    private val textBound = Rect()

    private val b = RectF()

    override fun draw(canvas: Canvas) {
        b.set(bounds)
        paint.color = solidColor
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(b, corner, corner, paint)
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.color = textColor
        drawText(text, b.centerX(), b.centerY(), paint, canvas)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
        invalidateSelf()
    }

    override fun getIntrinsicHeight(): Int {
        return (paint.textSize + paddingVertical + 0.5F).toInt()
    }

    override fun getIntrinsicWidth(): Int {
        return (paint.measureText(text) + paddingHorizontal + 0.5F).toInt()
    }


    override fun getOpacity(): Int {
        return PixelFormat.UNKNOWN
    }


    /**
     * 绘制单行文本
     * @param anchorX 水平参照点
     * @param anchorY 垂直起始点，文字的顶点
     */
    private fun drawText(
        text: String,
        //horizontalAlignment: Int,
        //verticalAlignment: Int,
        anchorX: Float,
        anchorY: Float,
        paint: Paint,
        canvas: Canvas
    ) {
        if (text.isBlank()) return
        paint.getTextBounds(text, 0, text.lastIndex, textBound)
        val textWidth = paint.measureText(text)
        val fontMetrics = paint.fontMetrics

        print(fontMetrics)

        val baseline = anchorY + (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
        val x = anchorX - textWidth / 2

        canvas.drawText(text, 0, text.length, x, baseline, paint)
    }

    companion object {
        private val textColor = Color.parseColor("#FF0091FF")
        private val solidColor = Color.parseColor("#FFEEF6FF")
        private var corner = -1F
        private var textS = -1F
        private var paddingHorizontal = -1F
        private var paddingVertical = -1F

        private fun initIfNot(resources: Resources) {
            if (textS == -1F) {
                textS = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP,
                    15F,
                    resources.displayMetrics
                )
                corner = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    2F,
                    resources.displayMetrics
                )

                paddingVertical = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    2F,
                    resources.displayMetrics
                )
                paddingHorizontal = paddingVertical * 2
            }
        }
    }
}