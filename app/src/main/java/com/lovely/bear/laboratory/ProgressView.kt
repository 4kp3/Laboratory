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
        minimumHeight = dpToPx(70F).toInt()
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

    //标签（顶部）文本字体大小
    private val labelTextSize = spToPx(12F)

    //节点（底部）文本字体大小
    private val nodeTextSize = spToPx(11F)

    //进度（字母）文本字体大小
    private val progressTextSize = spToPx(10F)

    //指针圆圈半径
    private val pointCircleRadius = dpToPx(9F)

    //指针圆圈线条
    private val pointCircleStroke = dpToPx(2F)

    //指针圆圈到节点圆圈长度
    private val nodeToPointCircle = dpToPx(4F)

    private val nodeCircleRadius = dpToPx(8F)
    private val nodeCircleStroke = dpToPx(1.5F)

    private val basicCircleRadius = dpToPx(2.5F)

    //private val progressAccentPaint = Paint()
    private val progressPaint = Paint()

    init {
        //progressAccentPaint.color = progressAccentColor
        progressPaint.color = progressBackgroundColor
        progressPaint.isAntiAlias = true
    }

    //锚点
    private val centerE = PointF()
    private val centerD = PointF()
    private val centerC = PointF()
    private val centerB = PointF()
    private val centerA = PointF()

    private val basicCircleCenter = Array(BASIC_CIRCLE_COUNT) { PointF() }

    private val progressLineStart = PointF()
    private val progressLineEnd = PointF()

    private val pointCirclePoint = PointF()
    private val pointLine = PointF()

    //水平锚点

    //垂直锚点
    private var topTextTop = 0F
    private val topTextHeight = labelTextSize

    //指针marginTop（距离顶部文字距离）
    private val pointMarginTop = dpToPx(3F)
    private var pointTop = 0F
    private val pointCircleHeight = pointCircleRadius * 2
    private var pointLineTop = 0F
    private val pointLineShortHeight = nodeToPointCircle
    private var nodeTop = 0F
    private var progressLineTop = 0F
    private val progressLineHeight = dpToPx(5F)
    private val nodeHeight = nodeCircleRadius * 2

    //节点标签文本marginTop（距离顶部节点圆圈距离）
    private val nodeTextMarginTop = dpToPx(10F)
    private var nodeTextTop = 0F
    private val nodeTextHeight = nodeTextSize

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
//        when (heightMode) {
//            MeasureSpec.UNSPECIFIED -> {
//            }
//            MeasureSpec.AT_MOST, MeasureSpec.EXACTLY -> {
//                when (layoutParams.height) {
//                    ViewGroup.LayoutParams.WRAP_CONTENT->{
//                    }
//                }
//            }
//        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        fun setVerticalAnchor(offsetY: Float) {
            topTextTop = offsetY
            pointTop = topTextTop + topTextHeight + pointMarginTop
            pointLineTop = pointTop + pointCircleHeight
            nodeTop = pointLineTop + pointLineShortHeight
            progressLineTop = nodeTop + dpToPx(6F)
            nodeTextTop = nodeTop + nodeHeight + nodeTextMarginTop
        }

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
            progressLineEnd.set(centerA.x, progressLineTop + progressLineHeight)
        }

        val innerPaddingHorizontal = dpToPx(10F)
        val line = (w - paddingStart - paddingEnd - innerPaddingHorizontal * 2) / 4
        val xOffset = paddingStart.toFloat() + innerPaddingHorizontal
        setHorizontalAnchor(xOffset, line)
        //nodeD.set()
    }

    private val nodeBackgroundPath = Path()
    private val nodeCirclesPath = Path()
    private val nodeProgressPath = Path()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        /**
         * 阶段一，计算进度
         *
         * 阶段二，计算锚点
         * 1。水平方向，五个节点的开始位置
         * 2。垂直分层
         * 2。1 顶部标签位置
         * 2。2 指针起始
         * 2。3 节点起始
         * 2。4 底部标签起始
         *
         * 阶段三，绘制
         * 1。顶部标签
         * 2。指针
         * 3。进度条进度
         * 4。进度条背景
         * 5。底部标签
         */

        progressPaint.strokeWidth = progressLineHeight
        progressPaint.style = Paint.Style.FILL
        canvas.drawRect(
            progressLineStart.x,
            progressLineStart.y,
            centerA.x,
            progressLineEnd.y,
            progressPaint
        )

        fun drawCircles(nodes: List<PointF>, isAccent: Boolean) {
            nodeCirclesPath.reset()
            nodes.forEach {
                nodeCirclesPath.addCircle(it.x, it.y, basicCircleRadius, Path.Direction.CCW)
            }
            progressPaint.color =
                if (isAccent) progressAccentColor else progressBackgroundColor
            canvas.drawPath(nodeCirclesPath, progressPaint)
        }

        drawCircles(basicCircleCenter.toList(), false)

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

        drawNodes(listOf(centerE, centerD, centerC, centerB, centerA), false)

        progressPaint.color = Color.RED
        canvas.drawLine(centerE.x, centerE.y - 20, centerE.x, centerE.y + 20, progressPaint)
        canvas.drawLine(centerD.x, centerD.y - 20, centerD.x, centerE.y + 20, progressPaint)
    }

    private fun addCircle() {

    }

    fun setProgress() {
        //计算Path
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

    //private fun progress

    private class Struct() {


    }

    /**
     * 进度模型
     * @param section 阶段，0为基本要求阶段，1为评级阶段
     */
    sealed class Progress(val section: Int) {
        class Basic(value: Int, val total: Int = 6) : Progress(0)
        class Grade(value: Float, val node: Node?, val total: Float = 100F) : Progress(1) {
            class Node(val a: Float, val b: Float, val c: Float)
        }
    }

    companion object {
        //进度基本要求
        private const val BASIC_REQUIREMENTS = "%d次推荐"

        //分数文本
        private const val SCORE = "%f.1分"
        private const val BASIC_CIRCLE_COUNT = 6
        private const val GRADE_NODE_CIRCLE_STROKE_DP = 1.5

        private fun getBasicRequirements(count: Int): String =
            String.format(BASIC_REQUIREMENTS, count)

        private fun getScore(value: Float): String = String.format(SCORE, value)


    }
}