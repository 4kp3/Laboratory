package com.lovely.bear.laboratory.dan.mu.icon

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import com.example.myapplication.R
import kotlin.math.min

/**
 * todo 使用缓冲池
 * 图像居中填充满
 * @author guoyixiong
 */
class ChatHeadDrawable(
    icon: Bitmap,
    type: ChatType,
    res: Resources,
    val width: Int,
    val height: Int
) : BitmapDrawable(res, icon) {

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
    private val decoratePaint: Paint = Paint().apply {
        strokeWidth = borderWidth
    }

    private var rectDirty = true

    private val employeeIconRect = Rect()

    init {
        loadEmployeeIcon(res)
        //设置头像居中
        gravity = Gravity.CENTER
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        //绘制装饰
        decoratePaint.color = borderColor

        canvas.drawCircle(
            bounds.exactCenterX(),
            bounds.exactCenterY(),
            min(bounds.height() / 2F, bounds.width() / 2F),
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

//    private fun updateImageRect() {
//        val bounds = bounds
//        val layoutDirection = layoutDirection
//        Gravity.apply(
//            Gravity.CENTER, width, height,
//            bounds, mDstRect, layoutDirection
//        )
//    }

    private fun updateEmployeeRectIfDirty() {
        if (rectDirty) {
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
        rectDirty = false
    }

    override fun onBoundsChange(bounds: Rect?) {
        super.onBoundsChange(bounds)
        rectDirty = true
    }

    override fun getIntrinsicWidth(): Int {
        return width
    }

    override fun getIntrinsicHeight(): Int {
        return height
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
                    缩放到当前密度（$targetDensityDpi）后的尺寸：width=${employeeIconWidth},height=${employeeIconHeight}
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

