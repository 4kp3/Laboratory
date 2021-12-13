package com.lovely.bear.laboratory

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup

/**
 *
 * @author guoyixiong
 */
class ProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    View(context, attrs, defStyleAttr) {

    init {
        minimumWidth = dpToPx(280F).toInt()
    }

    private val backgroundColor = Color.parseColor("#FFFFFF")

    //进度条背景色
    private val progressBackgroundColor = Color.parseColor("#F1F9FF")

    //进度背景节点（圆圈）色
    private val progressBackgroundNodeColor = Color.parseColor("#BBC9D4")

    //进度色
    private val progressAccentColor = Color.parseColor("#0091FF")

    //文本字体颜色
    private val textColor = Color.parseColor("#222222")

    //文本字体灰色
    private val textGreyColor = Color.parseColor("#999999")

    //标签（顶部和底部）文本字体大小
    private val labelTextSize = spToPx(12F)

    //进度（字母）文本字体大小
    private val progressTextSize = spToPx(10F)

    //指针圆圈半径
    private val pointCircleRadius = dpToPx(4.5F)

    //指针圆圈线条
    private val pointCircleStroke = dpToPx(2F)

    //指针圆圈到节点圆圈长度
    private val nodeToPointCircle = dpToPx(4F)

    private val nodeCircleRadius = dpToPx(8F)
    private val nodeCircleStroke = dpToPx(1.5F)

    private val basicCircleRadius = dpToPx(2.5F)

    private val textPaint = Paint()
    private val progressPaint = Paint()

    init {
        //progressAccentPaint.color = progressAccentColor
        progressPaint.color = progressBackgroundColor
        progressPaint.isAntiAlias = true

        textPaint.style = Paint.Style.FILL
        textPaint.isAntiAlias = true
        textPaint.textSize = labelTextSize
        textPaint.color = textGreyColor
    }

    //锚点
    private val centerE = PointF()
    private val centerD = PointF()
    private val centerC = PointF()
    private val centerB = PointF()
    private val centerA = PointF()
    private val nodes = listOf(centerE, centerD, centerC, centerB, centerA)

    private val basicCircleCenter = Array(BASIC_CIRCLE_COUNT) { PointF() }

    private val progressLineStart = PointF()
    private val progressLineBackgroundEnd = PointF()

    private var rightEdge = 0F

    //垂直锚点
    private var topTextTop = 0F
    private val topTextHeight = labelTextSize

    //指针marginTop（距离顶部文字距离）
    private val pointMarginTop = dpToPx(6F)
    private var pointTop = 0F
    private val pointCircleHeight = pointCircleRadius * 2
    private var pointLineTop = 0F
    private val pointLineShortHeight = nodeToPointCircle
    private var pointLineBottom = 0F
    private var nodeTop = 0F
    private var progressLineTop = 0F
    private val progressLineHeight = dpToPx(5F)
    private val pointLineWidth = dpToPx(5F)
    private val nodeHeight = nodeCircleRadius * 2

    //节点标签文本marginTop（距离顶部节点圆圈距离）
    private val nodeTextMarginTop = dpToPx(10F)
    private var nodeTextTop = 0F
    private val nodeTextHeight = labelTextSize

    private val nodeBackgroundPath = Path()
    private val nodeCirclesPath = Path()
    private val nodePointPath = Path()

    private var anchorReadiness: Boolean = false

    //进度
    private val pointCirclePoint = PointF()
    private val pointLineRect = RectF()
    private var progressLineEnd: Float? = null
    private var accentBasicCircle: List<PointF> = emptyList()
    private var backgroundBasicCircle: List<PointF> = emptyList()
    private var accentNodes: List<PointF> = emptyList()
    private var backgroundNodes: List<PointF> = emptyList()

    //进度文
    private var basicReq = ""
    private val black = "我"
    private var grey = ""
    private var a = ""
    private var b = ""
    private var c = ""

    private var value: Progress? = null

    /**
     * 入口
     */
    fun setProgress(value: Progress) {
        this.value = value
        if (anchorReadiness) {
            calculate(value)
            invalidate()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        when (layoutParams.height) {
            ViewGroup.LayoutParams.WRAP_CONTENT->{
                setVerticalAnchor(paddingTop.toFloat())
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec((nodeTextTop+nodeTextHeight+dpToPx(4F)).toInt(),heightMode))
            }
        }

        anchorReadiness = false
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        setVerticalAnchor(0F)
        val totalHeight = nodeTextTop + nodeTextHeight
        val offset = (h - totalHeight - paddingTop - paddingBottom) / 2 + paddingTop
        setVerticalAnchor(offset)

        fun setHorizontalAnchor(xOffset: Float, line: Float) {
            val centerY = nodeTop + nodeCircleRadius
            centerE.set(xOffset + nodeCircleRadius, centerY)
            centerD.set(xOffset + line, centerY)
            centerC.set(xOffset + line * 2, centerY)
            centerB.set(xOffset + line * 3, centerY)
            centerA.set(xOffset + line * 4 - nodeCircleRadius, centerY)

            val circleSpace =
                (centerD.x - centerE.x - nodeCircleRadius * 2 - BASIC_CIRCLE_COUNT * basicCircleRadius * 2) / (BASIC_CIRCLE_COUNT + 1)
            val firstStart = centerE.x + nodeCircleRadius + basicCircleRadius
            basicCircleCenter.forEachIndexed { index, point ->
                val start = firstStart + (index + 1) * circleSpace + index * basicCircleRadius * 2
                point.set(start, progressLineTop + basicCircleRadius)
            }

            progressLineStart.set(centerD.x, progressLineTop)
            progressLineBackgroundEnd.set(centerA.x, progressLineTop + progressLineHeight)
        }

        fun setPoint(offsetX: Float) {
            pointCirclePoint.set(offsetX, pointTop + pointCircleRadius)
            pointLineRect.set(
                offsetX - pointLineWidth / 2,
                pointLineTop,
                offsetX + pointLineWidth / 2,
                pointLineBottom
            )
        }

        val innerPaddingHorizontal = dpToPx(10F)
        val line = (w - paddingStart - paddingEnd - innerPaddingHorizontal * 2) / 4
        val xOffset = paddingStart.toFloat() + innerPaddingHorizontal
        rightEdge = w - innerPaddingHorizontal
        setHorizontalAnchor(xOffset, line)

        setPoint(centerB.x)

        anchorReadiness = true

        value?.let {
            calculate(it)
        }
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        fun drawPoint(black: String, grey: String) {
            progressPaint.color = progressAccentColor
            progressPaint.style = Paint.Style.FILL
            val radius = pointLineWidth / 2
            nodePointPath.reset()
            nodePointPath.addRect(
                pointLineRect.left,
                pointLineRect.top,
                pointLineRect.right,
                pointLineRect.bottom - radius,
                Path.Direction.CCW
            )
            nodePointPath.addCircle(
                pointLineRect.centerX(),
                pointLineRect.bottom - radius,
                radius,
                Path.Direction.CCW
            )
            canvas.drawPath(nodePointPath, progressPaint)
            progressPaint.color = backgroundColor
            canvas.drawCircle(
                pointCirclePoint.x,
                pointCirclePoint.y,
                pointCircleRadius,
                progressPaint
            )
            progressPaint.color = progressAccentColor
            progressPaint.style = Paint.Style.STROKE
            progressPaint.strokeWidth = pointCircleStroke
            canvas.drawCircle(
                pointCirclePoint.x,
                pointCirclePoint.y,
                pointCircleRadius,
                progressPaint
            )

            textPaint.textSize = labelTextSize
            textPaint.color = textColor

            val x = pointCirclePoint.x - nodeCircleRadius
            val blackWidth = textPaint.measureText(black)
            val greyWidth = textPaint.measureText(grey)

            val textHoAli: Int
            val xBlack: Float
            val xGrey: Float

            if (x + blackWidth + greyWidth <= rightEdge) {
                textHoAli = TEXT_ALIGNMENT_RIGHT
                xBlack = x
                xGrey = x + blackWidth
            } else {
                textHoAli = TEXT_ALIGNMENT_LEFT
                xBlack = rightEdge - greyWidth
                xGrey = rightEdge
            }

            drawText(
                text = black,
                horizontalAlignment = textHoAli,
                verticalAlignment = TEXT_ALIGNMENT_BOTTOM,
                anchorX = xBlack,
                anchorY = topTextTop,
                paint = textPaint,
                canvas = canvas
            )
            textPaint.color = textGreyColor
            drawText(
                text = grey,
                horizontalAlignment = textHoAli,
                verticalAlignment = TEXT_ALIGNMENT_BOTTOM,
                anchorX = xGrey,
                anchorY = topTextTop,
                paint = textPaint,
                canvas = canvas
            )
        }

        fun drawCircles(nodes: List<PointF>, isAccent: Boolean) {
            nodeCirclesPath.reset()
            nodes.forEach {
                nodeCirclesPath.addCircle(it.x, it.y, basicCircleRadius, Path.Direction.CCW)
            }
            progressPaint.style = Paint.Style.FILL
            progressPaint.color =
                if (isAccent) progressAccentColor else progressBackgroundColor
            canvas.drawPath(nodeCirclesPath, progressPaint)
        }

        fun drawNodes(nodes: List<PointF>, isAccent: Boolean) {
            nodeBackgroundPath.reset()
            nodes.forEach {
                nodeBackgroundPath.addCircle(it.x, it.y, nodeCircleRadius, Path.Direction.CCW)
            }
            progressPaint.strokeWidth = nodeCircleStroke
            progressPaint.style = Paint.Style.FILL
            progressPaint.color = backgroundColor
            canvas.drawPath(nodeBackgroundPath, progressPaint)
            progressPaint.style = Paint.Style.STROKE
            progressPaint.color =
                if (isAccent) progressAccentColor else progressBackgroundNodeColor
            canvas.drawPath(nodeBackgroundPath, progressPaint)
        }

        fun drawAE(accentCount: Int) {
            val ch: Int = 'E'.code
            listOf(centerE, centerD, centerC, centerB, centerA).forEachIndexed { index, pointF ->
                drawText(
                    text = Char(ch - index).toString(),
                    horizontalAlignment = TEXT_ALIGNMENT_CENTER,
                    verticalAlignment = TEXT_ALIGNMENT_CENTER,
                    anchorX = pointF.x,
                    anchorY = pointF.y,
                    paint = textPaint.apply {
                        textPaint.textSize = progressTextSize
                        textPaint.color =
                            if (index < accentCount) progressAccentColor else progressBackgroundNodeColor
                    },
                    canvas = canvas
                )
            }
        }

        progressPaint.strokeWidth = progressLineHeight
        progressPaint.style = Paint.Style.FILL
        progressPaint.color = progressBackgroundColor
        canvas.drawRect(
            progressLineStart.x,
            progressLineStart.y,
            centerA.x,
            progressLineBackgroundEnd.y,
            progressPaint
        )
        progressPaint.color = progressAccentColor
        progressLineEnd?.let {
            canvas.drawRect(
                progressLineStart.x,
                progressLineStart.y,
                it,
                progressLineBackgroundEnd.y,
                progressPaint
            )
        }

        fun drawBasicDesc(text: String) {
            val x = centerE.x + (centerD.x - centerE.x) / 2
            textPaint.color = textColor
            textPaint.textSize = labelTextSize
            drawText(
                text = text,
                horizontalAlignment = TEXT_ALIGNMENT_CENTER,
                verticalAlignment = TEXT_ALIGNMENT_BOTTOM,
                anchorX = x,
                anchorY = nodeTextTop,
                paint = textPaint,
                canvas = canvas
            )
        }

        fun drawNodeEmptyText(s: String) {
            val x = centerD.x + (centerA.x - centerD.x) / 2
            textPaint.color = textGreyColor
            textPaint.textSize = labelTextSize
            drawText(
                text = s,
                horizontalAlignment = TEXT_ALIGNMENT_CENTER,
                verticalAlignment = TEXT_ALIGNMENT_BOTTOM,
                anchorX = x,
                anchorY = nodeTextTop,
                paint = textPaint,
                canvas = canvas
            )
        }

        fun drawNodeText(c: String, b: String, a: String) {
            textPaint.color = textColor
            textPaint.textSize = labelTextSize
            drawText(
                text = c,
                horizontalAlignment = TEXT_ALIGNMENT_CENTER,
                verticalAlignment = TEXT_ALIGNMENT_BOTTOM,
                anchorX = centerC.x,
                anchorY = nodeTextTop,
                paint = textPaint,
                canvas = canvas
            )
            drawText(
                text = b,
                horizontalAlignment = TEXT_ALIGNMENT_CENTER,
                verticalAlignment = TEXT_ALIGNMENT_BOTTOM,
                anchorX = centerB.x,
                anchorY = nodeTextTop,
                paint = textPaint,
                canvas = canvas
            )
            drawText(
                text = a,
                horizontalAlignment = TEXT_ALIGNMENT_CENTER,
                verticalAlignment = TEXT_ALIGNMENT_BOTTOM,
                anchorX = centerA.x,
                anchorY = nodeTextTop,
                paint = textPaint,
                canvas = canvas
            )
        }

        drawPoint(black, grey)

        drawCircles(accentBasicCircle, true)
        drawCircles(backgroundBasicCircle, false)

        drawNodes(accentNodes, true)
        drawNodes(backgroundNodes, false)

        drawAE(accentNodes.size)
        drawBasicDesc(basicReq)
        if (progressLineEnd == null) {
            drawNodeEmptyText(NOT_PUBLIC_DESC)
        } else {
            drawNodeText(c, b, a)
        }
    }

    private fun setVerticalAnchor(offsetY: Float) {
        topTextTop = offsetY
        pointTop = topTextTop + topTextHeight + pointMarginTop
        pointLineTop = pointTop + pointCircleHeight
        nodeTop = pointLineTop + pointLineShortHeight
        progressLineTop = nodeTop + dpToPx(6F)
        pointLineBottom = nodeTop + nodeCircleRadius * 2
        nodeTextTop = nodeTop + nodeHeight + nodeTextMarginTop
    }

    private fun setPoint(offsetX: Float) {
        pointCirclePoint.set(offsetX, pointTop + pointCircleRadius)
        pointLineRect.set(
            offsetX - pointLineWidth / 2,
            pointLineTop,
            offsetX + pointLineWidth / 2,
            pointLineBottom
        )
    }

    private fun calculate(value: Progress) {

        basicReq = getBasicRequirements(value.total)
        grey = when {
            value.score != null && value.count >= value.total -> getScoreMy(value.score)
            value.count == value.total -> getScoreMy(0F)
            else -> getCountMy(value.count)
        }

        val node = value.node
        if (value.count >= value.total && value.score != null && node != null) {
            progressLineEnd = when (value.score) {
                0F -> centerD.x
                node.c -> centerC.x
                node.b -> centerB.x
                in node.a..100F -> centerA.x
                in 0F..node.c -> (centerC.x - centerD.x - 2 * nodeCircleRadius - pointLineWidth) * value.score / node.c + centerD.x + nodeCircleRadius + pointLineWidth / 2
                in node.c..node.b -> (centerB.x - centerC.x - 2 * nodeCircleRadius - pointLineWidth) * (value.score - node.c) / (node.b - node.c) + centerC.x + nodeCircleRadius + pointLineWidth / 2
                in node.b..node.a -> (centerA.x - centerB.x - 2 * nodeCircleRadius - pointLineWidth) * (value.score - node.b) / (node.a - node.b) + centerB.x + nodeCircleRadius + pointLineWidth / 2
                else -> centerA.x
            }
            setPoint(progressLineEnd!!)
            c = getScore(node.c)
            b = getScore(node.b)
            a = getScore(node.a)
        } else {
            when (value.count) {
                0 -> {
                    setPoint(centerE.x)
                }
                value.total -> {
                    setPoint(centerD.x)
                }
                else -> {
                    val c: Float = value.count * 1F / value.total
                    for (i in 0 until BASIC_CIRCLE_COUNT) {
                        val j = (i + 1) * 1F / BASIC_CIRCLE_COUNT
                        if (j >= c) {
                            basicCircleCenter.getOrNull(i)?.let {
                                setPoint(it.x)
                            }
                            break
                        }
                    }
                }
            }
            progressLineEnd = null
        }

        val pointX = pointCirclePoint.x
        accentBasicCircle = basicCircleCenter.filterIndexed { _, p ->
            p.x <= pointX
        }
        backgroundBasicCircle = basicCircleCenter.filterIndexed { _, p ->
            p.x > pointX
        }

        accentNodes = nodes.filter { it.x <= pointX }
        backgroundNodes = nodes.filter { it.x > pointX }
    }



    private val textBound = Rect()

    /**
     * 绘制单行文本
     * @param anchorX 水平参照点
     * @param anchorY 垂直起始点，文字的顶点
     */
    private fun drawText(
        text: String,
        horizontalAlignment: Int,
        verticalAlignment: Int,
        anchorX: Float,
        anchorY: Float,
        paint: Paint,
        canvas: Canvas
    ) {
        paint.getTextBounds(text, 0, text.lastIndex, textBound)
        val textWidth = paint.measureText(text)
        val fontMetrics = paint.fontMetrics

        print(fontMetrics)

        val baseline = when (verticalAlignment) {
            TEXT_ALIGNMENT_TOP -> {
                anchorY - fontMetrics.bottom
            }
            TEXT_ALIGNMENT_CENTER -> {
                anchorY + (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
            }
            else -> {
                anchorY - fontMetrics.top
            }
        }
        val x = when (horizontalAlignment) {
            TEXT_ALIGNMENT_LEFT -> {
                anchorX - textWidth
            }
            TEXT_ALIGNMENT_CENTER -> {
                anchorX - textWidth / 2
            }
            else -> {
                anchorX
            }
        }

        canvas.drawText(text, 0, text.length, x, baseline, paint)

//        canvas.drawLine(anchorX, anchorY - 20, anchorX, anchorY + 20, paint)
//        canvas.drawLine(anchorX - 20, anchorY, anchorX + 20, anchorY, paint)
//        textBound.offset(x.toInt(), baseline.toInt())
//        paint.style = Paint.Style.STROKE
//        canvas.drawRect(textBound, paint)
    }

    private fun spToPx(value: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            value,
            context.resources.displayMetrics
        )
    }

    private fun dpToPx(value: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value,
            context.resources.displayMetrics
        )
    }

    /**
     * 进度模型
     */
    class Progress(
        val count: Int,
        val total: Int,
        val score: Float?,
        val node: Node?
    ) {
        class Node(val a: Float, val b: Float, val c: Float)
    }

    companion object {
        //进度基本要求
        private const val BASIC_REQUIREMENTS = "%d次推荐"

        //分数文本
        private const val SCORE = "%.1f分"

        private const val SCORE_MY = "（%.1f分）"
        private const val COUNT_MY = "（%d次）"

        private const val NOT_PUBLIC_DESC = "等级分数要求（待公布）"

        private const val BASIC_CIRCLE_COUNT = 6
        private const val GRADE_NODE_CIRCLE_STROKE_DP = 1.5

        private const val TEXT_ALIGNMENT_LEFT = 0
        private const val TEXT_ALIGNMENT_CENTER = 1
        private const val TEXT_ALIGNMENT_RIGHT = 2
        private const val TEXT_ALIGNMENT_TOP = 3
        private const val TEXT_ALIGNMENT_BOTTOM = 4

        private fun getBasicRequirements(count: Int): String =
            String.format(BASIC_REQUIREMENTS, count)

        private fun getCountMy(count: Int) = String.format(COUNT_MY, count)
        private fun getScoreMy(score: Float) = String.format(SCORE_MY, score)

        private fun getScore(value: Float): String = String.format(SCORE, value)


    }
}