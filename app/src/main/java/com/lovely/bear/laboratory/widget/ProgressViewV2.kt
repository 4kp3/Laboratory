package com.lovely.bear.laboratory.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import com.lovely.bear.laboratory.R
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.KClass

class IntegrationProgress @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    ProgressViewV2(context, attrs, defStyleAttr) {

    override val defaultData: NodeV = buildData(0)

    fun setData(progress: Int) {
        setProgress(buildData(progress))
    }

    private fun buildData(progress: Int): NodeV {
        val n1 = NodeV(
            pre = null,
            next = null,
            node = IntegrateNode(isActive = progress >= 0, x = 0F, value = 0F, label = "0", 'E')
        )
        val n2 = NodeV(
            pre = null,
            next = null,
            node = IntegrateNode(isActive = progress >= 5, x = 0F, value = 5F, label = "5", 'D')
        )
        val n3 = NodeV(
            pre = null,
            next = null,
            node = IntegrateNode(isActive = progress >= 20, x = 0F, value = 20F, label = "20", 'C')
        )
        val n4 = NodeV(
            pre = null,
            next = null,
            node = IntegrateNode(isActive = progress >= 40, x = 0F, value = 40F, label = "40", 'B')
        )
        val n5 = NodeV(
            pre = null,
            next = null,
            node = IntegrateNode(isActive = progress >= 70, x = 0F, value = 70F, label = "70", 'A')
        )


        val progress0 = ProgressF(if (progress >= 5) 1F else progress / 5F)
        val progress1 = ProgressF(if (progress >= 20) 1F else (progress - 5F) / 15F)
        val progress2 = ProgressF(if (progress >= 40) 1F else (progress - 20F) / 20F)
        val progress3 = ProgressF(if (progress >= 70) 1F else (progress - 40F) / 30F)
        val p0 = ProgressV(pre = null, next = null, progress0)
        val p1 = ProgressV(pre = null, next = null, progress1)
        val p2 = ProgressV(pre = null, next = null, progress2)
        val p3 = ProgressV(pre = null, next = null, progress3)
        n1.next = p0
        p0.pre = n1
        p0.next = n2
        n2.pre = p0
        n2.next = p1
        p1.pre = n2
        p1.next = n3
        n3.pre = p1
        n3.next = p2
        p2.pre = n3
        p2.next = n4
        n4.pre = p2
        n4.next = p3
        p3.pre = n4
        p3.next = n5
        n5.pre = p3
        return n1
    }

}

class CountProgress @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    ProgressViewV2(context, attrs, defStyleAttr) {

    override val defaultData: NodeV = buildData(0F)

    fun setData(progress: Float) {
        setProgress(buildData(progress))
    }

    private fun buildData(progress: Float): NodeV {
        val n1 = NodeV(
            pre = null,
            next = null,
            node = CheckMarkNode(isActive = progress >= 0, x = 0F, value = 0F, label = "0个")
        )
        val n2 = NodeV(
            pre = null,
            next = null,
            node = CheckMarkNode(isActive = progress >= 1, x = 0F, value = 1F, label = "1个")
        )
        val n3 = NodeV(
            pre = null,
            next = null,
            node = CheckMarkNode(isActive = progress >= 2, x = 0F, value = 2F, label = "2个")
        )
        val n4 = NodeV(
            pre = null,
            next = null,
            node = CheckMarkNode(isActive = progress >= 3, x = 0F, value = 3F, label = "3个")
        )


        val progress0 = ProgressF(if (progress >= 1) 1F else progress)
        val progress1 = ProgressF(if (progress >= 2) 1F else (progress -1)/1F)
        val progress2 = ProgressF(if (progress >= 3) 1F else (progress -2)/1F)
        val p0 = ProgressV(pre = null, next = null, progress0)
        val p1 = ProgressV(pre = null, next = null, progress1)
        val p2 = ProgressV(pre = null, next = null, progress2)
        n1.next = p0
        p0.pre = n1
        p0.next = n2
        n2.pre = p0
        n2.next = p1
        p1.pre = n2
        p1.next = n3
        n3.pre = p1
        n3.next = p2
        p2.pre = n3
        p2.next = n4
        n4.pre = p2
        return n1
    }

}

/**
 * 猎头进度条
 * @author guoyixiong
 */
open class ProgressViewV2 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    View(context, attrs, defStyleAttr) {

    private val minWidth = dpToPx(260F)
    private val minHeight = dpToPx(40F)

    init {
        minimumWidth = minWidth.toInt()
    }

    //锚点是否准备好
    private var anchorReadiness: Boolean = false

    private val nodeDrawMap = mutableMapOf<String, INodeDraw>()
    private val progressDrawMap = mutableMapOf<String, IProgressDraw>()

    init {
        addNodeDraw(CheckMarkNode::class, CheckMarkNodeDraw())
        addNodeDraw(IntegrateNode::class, IntegrateNodeDraw())
        addProgressDraw(ProgressF::class, LineProgressDraw())
    }

    //获取默认值，在用户设置之前一直使用
    //用于首次测量和首次绘制
    protected open val defaultData: NodeV by lazy {
        val n1 = NodeV(
            pre = null,
            next = null,
            node = IntegrateNode(isActive = true, x = 0F, value = 0F, label = "0", 'E')
        )
        val n2 = NodeV(
            pre = null,
            next = null,
            node = IntegrateNode(isActive = false, x = 0F, value = 1F, label = "5", 'D')
        )
        val n3 = NodeV(
            pre = null,
            next = null,
            node = CheckMarkNode(isActive = false, x = 0F, value = 2F, label = "2个")
        )
        val n4 = NodeV(
            pre = null,
            next = null,
            node = CheckMarkNode(isActive = false, x = 0F, value = 3F, label = "3个")
        )
        val progress0 = ProgressF(1F)
        val progress1 = ProgressF(0.01F)
        val progress2 = ProgressF(0F)
        val p0 = ProgressV(pre = null, next = null, progress0)
        val p1 = ProgressV(pre = null, next = null, progress1)
        val p2 = ProgressV(pre = null, next = null, progress2)
        n1.next = p0
        p0.pre = n1
        p0.next = n2
        n2.pre = p0
        n2.next = p1
        p1.pre = n2
        p1.next = n3
        n3.pre = p1
        n3.next = p2
        p2.pre = n3
        p2.next = n4
        n4.pre = p2
        n1
    }

    private var _data: NodeV? = null
    private val data: NodeV
        get() {
            val d = _data
            return d ?: defaultData
        }

    //view rect
    private val vRect: RectF = RectF()

    private val miniSpace = RectF(0F, 0F, minWidth, minHeight)

    private val spaceGetRectF = RectF()

    /**
     * 入口
     */
    protected fun setProgress(value: NodeV) {
        this._data = value
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        when (layoutParams.height) {
            ViewGroup.LayoutParams.WRAP_CONTENT -> {

                fun getNodeHeight(pre: Float, nodeV: NodeV): Float {
                    getNodeDraw(node = nodeV.node).getBounds(nodeV.node, miniSpace, spaceGetRectF)
                    val maxH = max(pre, spaceGetRectF.height())
                    val nextNode = nodeV.next?.next ?: return maxH
                    return getNodeHeight(maxH, nextNode)
                }

                val maxNodeHeight = getNodeHeight(
                    0F,
                    nodeV = data
                )

                //Log.d("measure", "高度测量结果:$maxNodeHeight")
                setMeasuredDimension(
                    widthMeasureSpec,
                    MeasureSpec.makeMeasureSpec(
                        (paddingTop + paddingBottom + maxNodeHeight + 0.5F).toInt(),
                        heightMode
                    )
                )
            }
            else -> {
                setMeasuredDimension(
                    widthMeasureSpec,
                    heightMeasureSpec
                )
            }
        }

        anchorReadiness = false
    }

    /**
     * 计算坐标
     * @param w View总宽度
     * @param h View总高度
     */
    protected fun layout(w: Int, h: Int, data: NodeV) {
        val availableW = w - paddingStart - paddingEnd
        val availableH = h - paddingTop - paddingBottom
        val availableStart = paddingStart
        val availableEnd = w - paddingEnd
        val availableTop = paddingTop
        val availableBottom = h - paddingBottom
        val centerH = h / 2.0F

        vRect.set(
            availableStart * 1.0F,
            availableTop * 1.0F,
            availableEnd * 1.0F,
            availableBottom * 1.0F
        )

        val rectNodeWidthGet = RectF()

        //先减去所有node的宽度，剩下的计算单位权重
        fun getNodeWidth(sum: Float, nodeV: NodeV): Float {
            getNodeDraw(nodeV.node).getExclusiveRect(nodeV.node, vRect, rectNodeWidthGet)
            val curr = rectNodeWidthGet.width() + sum
            val nextProgress = nodeV.next?.next ?: return curr
            return getNodeWidth(sum = curr, nodeV = nextProgress)
        }

        fun getTotalWeight(sum: Float, progressV: ProgressV): Float {
            val pre = progressV.pre
            val next = progressV.next
            if (pre == null || next == null) {
                return sum + 1F//缺省为1
            } else {
                val curr = getProgressDraw(progressV.progress).getWeight(
                    pre.node,
                    progressV.progress,
                    next.node
                )
                val newSum = curr + sum
                val nextProgressV = progressV.next?.next ?: return newSum
                return getTotalWeight(newSum, nextProgressV)
            }
        }

        val firstNodeV = data
        val firstProgressV = data.next ?: return
        val totalWeight = getTotalWeight(0F, firstProgressV)
        if (totalWeight == 0F) return
        val perWeight: Float =
            (availableW - getNodeWidth(0F, firstNodeV)) / totalWeight

        var lastEnd: Float = availableStart * 1.0F
        val rectNodeSetXGet = RectF()
        fun setXForV(start: Float, nodeV: NodeV) {
            getNodeDraw(nodeV.node).getExclusiveRect(nodeV.node, vRect, rectNodeSetXGet)
            val startNodeWidth = rectNodeSetXGet.width()
            nodeV.node.x = start + startNodeWidth / 2F
            val progressV = nodeV.next ?: return
            val endNodeV = progressV.next ?: return
            val progressWidth = getProgressDraw(progressV.progress).getWeight(
                nodeV.node,
                progressV.progress,
                endNodeV.node
            ) * perWeight
            //endNodeV.node.x = start+startNodeWidth+progressWidth+endNodeWidth/2
            setXForV(start + startNodeWidth + progressWidth, endNodeV)
        }
        setXForV(availableStart * 1.0F, nodeV = firstNodeV)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        layout(right - left, bottom - top, data)
    }

    /**
     * 根据数据找到具体的绘制器
     */
    protected fun getNodeDraw(node: Node): INodeDraw {
        return nodeDrawMap[getKey(node)] ?: CheckMarkNodeDraw()
    }

    fun <N, T : KClass<N>> addNodeDraw(node: T, draw: INodeDraw) where N : Node {
        nodeDrawMap[getKey(node)] = draw
    }

    /**
     * 根据数据找到具体的绘制器
     */
    protected fun getProgressDraw(progress: ProgressF): IProgressDraw {
        return progressDrawMap[getKey(progress)] ?: LineProgressDraw()
    }

    fun <P, T : KClass<P>> addProgressDraw(progress: T, draw: IProgressDraw) where P : ProgressF {
        progressDrawMap[getKey(progress)] = draw
    }

    private fun getKey(clazz: Any): String {
        return if (clazz is KClass<*>) {
            clazz.qualifiedName ?: clazz.simpleName ?: "1"
        } else clazz::class.run {
            qualifiedName ?: simpleName ?: "1"
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        layout(w, h, data)
    }

    //draw cache object
    private val centerPGet = PointF()
    private val c1 = RectF()
    private val c2 = RectF()
    private val c3 = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        fun startDraw(node: NodeV, drawNode: Boolean, drawProgress: Boolean) {
            val firstNode = node
            val nodeStartDraw = getNodeDraw(firstNode.node)
            if (drawNode) {
                nodeStartDraw.draw(
                    t = firstNode.node,
                    space = vRect,
                    canvas
                )
            }
            firstNode.next?.let {
                val nextNode = it.next ?: return@let
                if (drawProgress) {
                    val nextNodeDraw = getNodeDraw(nextNode.node)

                    val preNodeExcl = c1.apply {
                        nodeStartDraw.getExclusiveRect(firstNode.node, space = vRect, this)
                    }

                    val nextNodeExcl = c2.apply {
                        nextNodeDraw.getExclusiveRect(nextNode.node, space = vRect, this)
                    }

                    val progressRect = c3.apply {
                        set(
                            preNodeExcl.right,
                            vRect.top,
                            nextNodeExcl.left,
                            vRect.bottom
                        )
                    }
                    //draw progress
                    nodeStartDraw.getGeometryCenter(
                        firstNode.node,
                        space = vRect,
                        p = centerPGet,
                    )
                    getProgressDraw(it.progress).draw(
                        t = it.progress,
                        rect = progressRect,
                        geometryCenterY = centerPGet.y,
                        canvas
                    )
                }

                startDraw(nextNode, drawNode, drawProgress)
            }
        }
        startDraw(data, drawNode = false, drawProgress = true)
        startDraw(data, drawNode = true, drawProgress = false)
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
        if (text.isBlank()) return
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


    class NodeV(
        var pre: ProgressV?,
        var next: ProgressV?,
        val node: Node
    )

    class ProgressV(
        var pre: NodeV?,
        var next: NodeV?,
        val progress: ProgressF,
    )

    /**
     * @param value 占此段的比例
     */
    open class ProgressF(val value: Float)

    /**
     * @param x 该Node代表的节点的中心坐标
     */
    open class Node(val isActive: Boolean, var x: Float = -1F, val value: Float)

    interface INodeDraw {
        /**
         * 如何可以的话返回绘制区域
         * @param space:
         * @param r 需要把数据写入的对象
         */
        fun getBounds(t: Node, space: RectF, r: RectF)

        /**
         * 声明排他性区域，避免其它的绘制进入此区域
         * @param r 该区域的中心点横坐标即为Node.x
         */
        fun getExclusiveRect(t: Node, space: RectF, r: RectF)

        /**
         * 获取此节点的几何中心
         */
        fun getGeometryCenter(t: Node, space: RectF, p: PointF)

        /**
         * @param space 建议绘制区域
         */
        fun draw(t: Node, space: RectF, canvas: Canvas)
    }

    interface IProgressDraw {

        fun getWeight(start: Node, t: ProgressF, end: Node): Float

        /**
         * @param rect 可绘制区域
         * @param geometryCenterY node的几何中心Y坐标，方便进度条垂直对齐
         */
        fun draw(t: ProgressF, rect: RectF, geometryCenterY: Float, canvas: Canvas)
    }

    internal inner class LineProgressDraw(
        private val accentColor: Int = Color.parseColor("#0091FF"),
        private val backgroundColor: Int = Color.parseColor("#F1F4F7"),
        private val round: Boolean = true,
        private val endAccentCompensation: Float = 2F,//左侧的线条补偿，即和节点重合一部分
        private val strokeDP: Float = 5F
    ) : IProgressDraw {

        private val height = dpToPx(strokeDP)

        private val paint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            //strokeCap = if (round) Paint.Cap.ROUND else Paint.Cap.SQUARE
            //strokeWidth = dpToPx(strokeDP)
        }

        //等比例
        override fun getWeight(start: Node, t: ProgressF, end: Node) = 1F

        override fun draw(t: ProgressF, rect: RectF, geometryCenterY: Float, canvas: Canvas) {
            val top = geometryCenterY - height / 2
            val bottom = geometryCenterY + height / 2
            val leftWidthCom = rect.left - endAccentCompensation

            //background
            if (t.value < 1) {
                paint.color = backgroundColor
                canvas.drawRect(
                    leftWidthCom,
                    top,
                    rect.right + endAccentCompensation,
                    bottom,
                    paint
                )
                //canvas.drawRect(rect.left, geometryCenterY-4F, rect.right, geometryCenterY+4F, paint)
            }
            //accent
            if (t.value > 0) {
                paint.color = accentColor

                if (t.value >= 1.0F) {
                    canvas.drawRect(
                        leftWidthCom,
                        top,
                        rect.right + endAccentCompensation,
                        bottom,
                        paint
                    )
                } else {
                    val total = rect.width() * min(t.value, 1.0F)
                    val radius = height / 2
                    val rectWidth = max(total - radius, 0F)
                    if (rectWidth > 0) {
                        canvas.drawRect(
                            leftWidthCom,
                            top,
                            rect.left + rectWidth + 1,
                            bottom,
                            paint
                        )
                    } else {
                        //没有矩形宽度时单独绘制补偿
                        canvas.drawRect(
                            leftWidthCom,
                            top,
                            rect.left + 1,
                            bottom,
                            paint
                        )
                    }
                    canvas.drawArc(
                        rect.left + rectWidth - radius,
                        top,
                        rect.left + rectWidth + radius,
                        bottom,
                        -90F,
                        180F,
                        true,
                        paint
                    )
                }

//                canvas.drawLine(rect.left, geometryCenterY, rect.right, geometryCenterY, paint)
            }
        }
    }

    //对应 CheckMarkNodeDraw
    internal class CheckMarkNode(isActive: Boolean, x: Float, value: Float, val label: String) :
        Node(isActive = isActive, x = x, value = value)

    //对应 CheckMarkNode
    internal inner class CheckMarkNodeDraw : INodeDraw {

        private val circleRadius = dpToPx(8F)

        private val activeImage =
            BitmapFactory.decodeResource(context.resources, R.drawable.check_mark_active)
        private val inactiveImage =
            BitmapFactory.decodeResource(context.resources, R.drawable.check_mark_inactive)

        private val labelActiveColor = Color.parseColor("#0091FF")
        private val labelInactiveColor = Color.parseColor("#222222")

        //绘制中心X轴距离圆圈底部
        private val rectCenterY2Circle = dpToPx(4F)
        private val rectCenterY2Label = dpToPx(5F)
        private val labelTextSize = spToPx(12F)
        private val circleRect = RectF()
        private val centerP = PointF()
        private val paint = Paint().apply {
            isAntiAlias = true
        }
        private val labelPaint = Paint().apply {
            textSize = labelTextSize
            isAntiAlias = true
        }

        override fun getBounds(t: Node, space: RectF, r: RectF) {
            val dRectCenterY = space.centerY()
            r.set(0F, dRectCenterY - dpToPx(20F), 0F, dRectCenterY + dpToPx(20F))
        }

        override fun getExclusiveRect(t: Node, space: RectF, r: RectF) {
            //返回圆圈区域
            //绘制区域中心
            getGeometryCenter(t, space, centerP)
            r.set(
                centerP.x - circleRadius,
                centerP.y - circleRadius,
                centerP.x + circleRadius,
                centerP.y + circleRadius
            )
        }

        override fun getGeometryCenter(t: Node, space: RectF, p: PointF) {
            //绘制区域中心
            val dRectCenterY = space.centerY()
            //中心距离圆圈4dp
            val centerY = dRectCenterY - rectCenterY2Circle - circleRadius
            p.set(t.x, centerY)
        }


        override fun draw(t: Node, space: RectF, canvas: Canvas) {
            getExclusiveRect(t, space, circleRect)
            canvas.drawBitmap(
                if (t.isActive) activeImage else inactiveImage,
                null,
                circleRect,
                paint
            )

            if (t is CheckMarkNode) {
                val textTop = space.centerY() + rectCenterY2Label
                drawText(
                    t.label,
                    TEXT_ALIGNMENT_CENTER,
                    TEXT_ALIGNMENT_BOTTOM,
                    t.x,
                    textTop,
                    labelPaint.apply {
                        color = if (t.isActive) labelActiveColor else labelInactiveColor
                    },
                    canvas
                )
            }
        }
    }

    //对应 IntegrateNodeDraw
    internal class IntegrateNode(
        isActive: Boolean,
        x: Float,
        value: Float,
        val label: String,
        val node: Char
    ) :
        Node(isActive = isActive, x = x, value = value)

    //对应 IntegrateNode
    internal inner class IntegrateNodeDraw() : INodeDraw {

        private val circleRadius = dpToPx(8F)

        private val activeColor = Color.parseColor("#0091FF")
        private val inactiveColor = Color.parseColor("#D4DEE5")
        private val nodeColor = Color.WHITE

        private val labelActiveColor = Color.parseColor("#0091FF")
        private val labelInactiveColor = Color.parseColor("#222222")

        //绘制中心X轴距离圆圈底部
        private val rectCenterY2Circle = dpToPx(4F)
        private val rectCenterY2Label = dpToPx(5F)
        private val labelTextSize = spToPx(12F)
        private val nodeTextSize = spToPx(10F)
        private val paint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        private val nodeTextPaint = Paint().apply {
            textSize = nodeTextSize
            isAntiAlias = true
            color = nodeColor
        }
        private val labelPaint = Paint().apply {
            textSize = labelTextSize
            isAntiAlias = true
        }

        override fun getBounds(t: Node, space: RectF, r: RectF) {
            val dRectCenterY = space.centerY()
            r.set(0F, dRectCenterY - dpToPx(20F), 0F, dRectCenterY + dpToPx(20F))
        }

        override fun getExclusiveRect(t: Node, space: RectF, r: RectF) {
            //返回圆圈区域
            //绘制区域中心
            getGeometryCenter(t, space, centerP)
            r.set(
                centerP.x - circleRadius,
                centerP.y - circleRadius,
                centerP.x + circleRadius,
                centerP.y + circleRadius
            )
        }

        override fun getGeometryCenter(t: Node, space: RectF, p: PointF) {
            //绘制区域中心
            val dRectCenterY = space.centerY()
            //中心距离圆圈4dp
            val centerY = dRectCenterY - rectCenterY2Circle - circleRadius
            p.set(t.x, centerY)
        }

        private val centerP = PointF()

        override fun draw(t: Node, space: RectF, canvas: Canvas) {
            getGeometryCenter(t, space, centerP)
            canvas.drawCircle(
                centerP.x,
                centerP.y,
                circleRadius,
                paint.apply {
                    color = if (t.isActive) activeColor else inactiveColor
                }
            )

            if (t is IntegrateNode) {
                drawText(
                    t.node.toString(),
                    TEXT_ALIGNMENT_CENTER,
                    TEXT_ALIGNMENT_CENTER,
                    centerP.x,
                    centerP.y,
                    nodeTextPaint,
                    canvas
                )

                val textTop = space.centerY() + rectCenterY2Label
                drawText(
                    t.label,
                    TEXT_ALIGNMENT_CENTER,
                    TEXT_ALIGNMENT_BOTTOM,
                    t.x,
                    textTop,
                    labelPaint.apply {
                        color = if (t.isActive) labelActiveColor else labelInactiveColor
                    },
                    canvas
                )
            }
        }
    }

    companion object {

        private const val TEXT_ALIGNMENT_LEFT = 0
        private const val TEXT_ALIGNMENT_CENTER = 1
        private const val TEXT_ALIGNMENT_RIGHT = 2
        private const val TEXT_ALIGNMENT_TOP = 3
        private const val TEXT_ALIGNMENT_BOTTOM = 4

    }
}