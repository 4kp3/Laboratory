package com.lovely.bear.laboratory.dan.mu.head

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.Log
import android.util.TypedValue
import com.example.myapplication.R
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * todo 使用缓冲池
 * 图像居中填充满
 * @author guoyixiong
 */
class ChatHeadDrawable(
    bitmap: Bitmap,
    type: ChatType,
    private val res: Resources,
    val width: Int,
    val height: Int
) : Drawable() {

    private val drawMatrix = Matrix()

    //private var bitmapDestRect: Rect = Rect()
    private var bitmapBoundsDirty = true
    var bitmap: Bitmap = bitmap
        set(value) {
            field = value
            bitmapBoundsDirty = true
            if (isFirstSet) {
                isFirstSet = false
            } else invalidateSelf()
        }

    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    var type: ChatType = type
        set(value) {
            field = value
            invalidateSelf()
        }

    /**
     * 边框宽度，像素值
     * 当前为1dp
     */
    private val borderWidth =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1F, res.displayMetrics)

    /***
     * 边框颜色
     */
    private val borderColor: Int
        get() = when (type) {
            ChatType.NORMAL -> Color.WHITE
            ChatType.EMPLOYEE -> color0091ff
        }

    private val color0091ff = Color.parseColor("#FF0091FF")

    /**
     * 装饰画笔
     */
    private val decoratePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = borderWidth
        style = Paint.Style.STROKE
    }

    private var employeeRectDirty = true

    private val employeeIconRect = Rect()

    init {
        loadEmployeeIcon(res)
    }

    private var isFirstSet = true

    override fun setAlpha(alpha: Int) {
        if (bitmapPaint.alpha != alpha || decoratePaint.alpha != alpha) {
            bitmapPaint.alpha = alpha
            decoratePaint.alpha = alpha
            invalidateSelf()
        }
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        bitmapPaint.colorFilter = colorFilter
        decoratePaint.colorFilter = colorFilter
        invalidateSelf()
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int {
        return if (bitmap.hasAlpha() || bitmapPaint.alpha < 255) PixelFormat.TRANSLUCENT else PixelFormat.OPAQUE
    }

    private val circlePath = Path()

    override fun draw(canvas: Canvas) {
        updateBitmapBoundsIfDirty()

        val radius = getRadius()
        val cx = bounds.exactCenterX()
        val cy = bounds.exactCenterY()
        circlePath.reset()
        circlePath.addCircle(cx, cy, radius-borderWidth/2, Path.Direction.CW)
        canvas.save()
        circlePath.fillType = Path.FillType.EVEN_ODD
        canvas.clipPath(circlePath)
        canvas.drawBitmap(bitmap, drawMatrix, bitmapPaint)
        canvas.restore()

        //绘制装饰
        decoratePaint.color = borderColor

        canvas.drawCircle(
            cx,
            cy,
            radius - borderWidth/2,
            decoratePaint
        )

        if (type == ChatType.EMPLOYEE && employeeIconReady) {
            //绘制员工标识
            updateEmployeeRectIfDirty()
            canvas.drawBitmap(employeeIcon, null, employeeIconRect, null)
        }
    }

    private fun getRadius(): Float {
        return min(bounds.height() / 2F, bounds.width() / 2F)
    }

    private fun updateBitmapBoundsIfDirty() {
        if (bitmapBoundsDirty) {
            val bitmapWidth: Int = bitmap.width
            val bitmapHeight: Int = bitmap.height
            val bounds = bounds

            val dWidth: Int = bounds.width()
            val dHeight: Int = bounds.height()
            val scale: Float
            var dx = 0f
            var dy = 0f

            if (bitmapWidth * dHeight > dWidth * bitmapHeight) {
                scale = dHeight.toFloat() / bitmapHeight.toFloat()
                dx = (dWidth - bitmapWidth * scale) * 0.5f
            } else {
                scale = dWidth.toFloat() / bitmapWidth.toFloat()
                dy = (dHeight - bitmapHeight * scale) * 0.5f
            }

            drawMatrix.setScale(scale, scale)
            drawMatrix.postTranslate(
                bounds.left + dx.roundToInt().toFloat(),
                bounds.top + dy.roundToInt().toFloat()
            )
        }
        bitmapBoundsDirty = false
    }

    private fun updateEmployeeRectIfDirty() {
        if (employeeRectDirty) {
            //初始化员工标签
            if (!employeeIconReady) {
                Log.e(tag, "icon未初始化！")
            }
            val bounds = bounds
            val radius = getRadius().toInt()
            if (radius < employeeIconWidth || radius < employeeIconHeight) {
                Log.e(tag, "icon尺寸过大，将会产生未知效果，应该保持在半径内")
            }
            //放在右下角
            val l = bounds.centerX() + radius - employeeIconWidth
            val t = bounds.centerY() + radius - employeeIconHeight
            val r = l + employeeIconWidth
            val b = t + employeeIconHeight
            employeeIconRect.set(l, t, r, b)
        }
        employeeRectDirty = false
    }

    override fun onBoundsChange(bounds: Rect?) {
        super.onBoundsChange(bounds)
        employeeRectDirty = true
        bitmapBoundsDirty = true
    }

    override fun getIntrinsicWidth(): Int {
        return -1
    }

    override fun getIntrinsicHeight(): Int {
        return -1
    }


    companion object {
        private var employeeIconReady = false
        private lateinit var employeeIcon: Bitmap
        private var employeeIconWidth: Int = 0
        private var employeeIconHeight: Int = 0

        private const val tag = "ChatHeadDrawable"

        private fun loadEmployeeIcon(res: Resources) {
            if (!this::employeeIcon.isInitialized) {
                val dm = res.displayMetrics
                Log.d(
                    tag, """
                    DisplayMetrics: widthPixels=${dm.widthPixels},heightPixels=,${dm.heightPixels},
                    xdpi=${dm.xdpi},ydpi=${dm.ydpi}
                    density=${dm.density},densityDpi=${dm.densityDpi}
                """.trimIndent()
                )

                try {
                    val opt = BitmapFactory.Options()
                    opt.inJustDecodeBounds = true
                    BitmapFactory.decodeResource(res, R.drawable.ic_employee, opt)
                    Log.d(
                        tag, """
                    原始图像尺寸：outWidth=${opt.outWidth},outHeight=${opt.outHeight}
                    系统补上的值：inDensity=${opt.inDensity},inTargetDensity=${opt.inTargetDensity}
                    应该和上述DisplayMetrics一致
                """.trimIndent()
                    )
                    opt.inJustDecodeBounds = false

//                opt.inScaled = true
                    val targetDensityDpi = dm.densityDpi
//                opt.inDensity = DisplayMetrics.DENSITY_HIGH
//                opt.inTargetDensity = targetDensity
                    employeeIcon = BitmapFactory.decodeResource(res, R.drawable.ic_employee, opt)
                    employeeIconWidth = employeeIcon.getScaledWidth(targetDensityDpi)
                    employeeIconHeight = employeeIcon.getScaledHeight(targetDensityDpi)
                    Log.d(
                        tag, """
                    Bitmap图像尺寸：width=${employeeIcon.width},height=${employeeIcon.height}
                    缩放到当前密度（$targetDensityDpi）后的尺寸：width=$employeeIconWidth,height=$employeeIconHeight
                    缩放值应该要小于图像尺寸值，因为图像位于xxxhdpi，而当前手机屏幕是xxhdpi
                """.trimIndent()
                    )
                    employeeIconReady = true
                } catch (e: Exception) {
                    Log.e(tag, "员工Icon加载失败")
                    employeeIconReady = false
                }
            }
        }
    }
}

