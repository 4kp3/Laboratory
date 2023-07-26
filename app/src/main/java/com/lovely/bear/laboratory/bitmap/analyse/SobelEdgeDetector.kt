package com.lovely.bear.laboratory.bitmap.analyse

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.ui.unit.dp
import com.lovely.bear.laboratory.bitmap.data.Direction
import com.lovely.bear.laboratory.bitmap.trackIcon
import com.lovely.bear.laboratory.bitmap.utils.PixelUtils
import com.lovely.bear.laboratory.bitmap.utils.RectUtils
import kotlin.math.pow
import kotlin.math.sqrt


typealias ForegroundJudgment = (pixel: Int) -> Boolean


class SobelEdgeDetector : EdgeDetector {

    /**
     * 优化列表：
     * 灰度值算法权重如何平衡？
     *
     * 使用红色通道时的问题图片列表：
     * - 淘宝 图片的上半部分完全没有检测到
     *      原因：可能因为只取红色通道值，而上半部分恰好是橙色调，差异太小，被算法忽略
     *      优化：使用三通道加权计算灰度值✅
     *
     * - Play 橙色尖端未识别出来
     *      原因：未知
     *      优化：使用三通道加权计算灰度值✅
     *
     * 使用三通道加权计算灰度值后的问题图片列表：
     * - Kindle、美团 前景未检测到
     *      原因：黑色的人形/文字和周围透明像素的边缘未检测到
     *      优化：
     * - 文件极客 前景出现奇怪的大幅度弧线
     *      原因：是图片自带的，还是算法的缺陷？
     *      优化：?
     *
     *
     * 其余问题：
     * 边缘透明时，如何忽略，比如【导入的图像2】，四周系统增加了一圈
     *      优化，当图片超过72dp时，使用72dp遮罩裁切，这是规范中的最大展示范围
     *      需要增加一个尺寸预处理步骤
     *
     * 算法优化，忽略66dp安全区域
     */
    override fun detect(source: Bitmap): EdgeResult {

        val width: Int = source.width
        val height: Int = source.height
        val totalPixelsCount = width * height
        val bitmapRect = Rect(0, 0, width, height)
        val edgesBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        var isSolidColor: Boolean = true
        var isTransparent: Boolean = true
        var isCompletelyOpaque = source.config == Bitmap.Config.RGB_565
        // todo 确认这种场景
        var isBlackAndTransparent: Boolean = true
        var isOriginalBitmap: Boolean = false
        var maybeContentIsBlack: Boolean = false

        val noSet = -1111
        var blackPixelCount: Long = 0
        var firstPixel: Int = noSet
        var lastPixel: Int = noSet
        var checkedPixelCount = 0
        var completelyOpaquePixelCount = 0


        //        var firstPixel:Int=-1
        fun pixelForEach(value: Int) {
            if (firstPixel == noSet) {
                firstPixel = value
            } else if (firstPixel != value) {
                isSolidColor = false
            }

            if (value != Color.TRANSPARENT) {
                isTransparent = false

                if (PixelUtils.isBlackPixel(value)) {
                    blackPixelCount++
                } else {
                    isBlackAndTransparent = false
                }
            }
            checkedPixelCount++

            if (!isCompletelyOpaque) {
                // 半透明
                if (Color.alpha(value) < 255) {
                    isCompletelyOpaque = false
                }
            }

            if (checkedPixelCount == totalPixelsCount && isBlackAndTransparent) {
                maybeContentIsBlack = blackPixelCount < totalPixelsCount / 2
            }
        }


        // 定义Sobel算子
        val sobelX = arrayOf(intArrayOf(-1, 0, 1), intArrayOf(-2, 0, 2), intArrayOf(-1, 0, 1))
        val sobelY = arrayOf(intArrayOf(-1, -2, -1), intArrayOf(0, 0, 0), intArrayOf(1, 2, 1))

        // 迭代图像像素
        for (x in 1 until width - 1) {
            for (y in 1 until height - 1) {
                // 计算Sobel算子在当前像素周围3x3区域的梯度
                var gradX = 0
                var gradY = 0
                for (i in -1..1) {
                    for (j in -1..1) {
                        val pixel: Int = source.getPixel(x + i, y + j)

                        if (i == -1 && j == -1) {
                            pixelForEach(pixel)
                        }

//                        val gray = Color.red(pixel)
                        val gray =
                            (0.2989 * Color.red(pixel) + 0.5870 * Color.green(pixel) + 0.1140 * Color.blue(
                                pixel
                            )).toInt()
                        gradX += gray * sobelX[i + 1][j + 1]
                        gradY += gray * sobelY[i + 1][j + 1]
                    }
                }

                // 计算梯度幅值，并将其作为边缘像素的强度（在这里我们使用灰度值来表示强度）
                val edgeStrength = sqrt((gradX * gradX + gradY * gradY).toDouble()).toInt()
                edgesBitmap.setPixel(x, y, Color.rgb(edgeStrength, edgeStrength, edgeStrength))
            }
        }



        return if (isSolidColor) {
            EdgeResult.Blank(color = firstPixel, edgesBitmap, isCompletelyOpaque)
        } else {

            val edgePixelPredicate = EdgePixelPredicate(isBlackAndTransparent)
            // todo 渐变图既不是纯色也不是透明，找不到边缘，需要处理
            val analysBitmap = if (isBlackAndTransparent) {
                source
            } else edgesBitmap

            val bound = findCenterBoundingRect(
                analysBitmap,
                EdgePixelPredicate(isBlackAndTransparent),
                isBlackAndTransparent, maybeContentIsBlack
            )

            val p = Paint().apply {
                color = Color.RED
                style = Paint.Style.STROKE
                strokeWidth = 1.dp.value
            }
            val canvas = Canvas(edgesBitmap)
            canvas.drawRect(bound, p)

            // 内容矩形和图像外边界，分别比较四个边距，取最小的那条边作为最后方形的边
            val radius = when (RectUtils.getMinGapDirection(bitmapRect, bound)) {
                is Direction.LEFT -> {
                    bitmapRect.centerX() - bound.left
                }

                is Direction.RIGHT -> {
                    bound.right - bitmapRect.centerX()
                }

                is Direction.TOP -> {
                    bitmapRect.centerY() - bound.top
                }

                is Direction.BOTTOM -> {
                    bound.bottom - bitmapRect.centerY()
                }
            }

            val centerMinimum = Rect(
                bitmapRect.centerX() - radius, bitmapRect.centerY() - radius,
                bitmapRect.centerX() + radius, bitmapRect.centerY() + radius,
            )

            // 根据Bound计算
            val safeRadius = findCenterBoundingCorner(
                bitmap = analysBitmap,
                edgePixelPredicate = edgePixelPredicate,
                bound = bound,
                centerMinimum = centerMinimum,
            )

            val edgeCenterX = edgesBitmap.width / 2F
            val edgeCenterY = edgesBitmap.height / 2F

            p.color = Color.BLUE
            canvas.drawCircle(
                edgeCenterX,
                edgeCenterY,
                safeRadius.toFloat(),
                p
            )

            p.color = Color.RED
            canvas.drawLine(edgeCenterX, 0F, edgeCenterX, edgesBitmap.height.toFloat(), p)
            canvas.drawLine(0F, edgeCenterY, edgesBitmap.width.toFloat(), edgeCenterY, p)

            val backgroundColor = findBackgroundColor(source, bound = bound)

            EdgeResult.Image(
                isBlackAndTransparent,
                centerMinimum = centerMinimum,
                minimum = bound,
                safeRadius = safeRadius,
                backgroundColor = backgroundColor,
                bitmap = edgesBitmap,
                isCompletelyOpaque
            )
        }

    }


    // 寻找背景颜色值
    // 先采用首个像素，后续采用抽样值？
    /**
     * @return 若bound面积小于src，则找出边界之外的颜色，否则返回null
     */
    private fun findBackgroundColor(src: Bitmap, bound: Rect): Int? {
        if (src.width == bound.width() && src.height == bound.height()) return null

        val x = (bound.left - 1).takeIf { it >= 0 } ?: (src.width - 1)
        val y = (bound.top - 1).takeIf { it >= 0 } ?: (src.height - 1)

        return src.getPixel(x, y)
    }

    // 寻找包围centerMinimum的最小圆，但这个圆不会把内容像素排除在外
    private fun findCenterBoundingCorner(
        bitmap: Bitmap,
        edgePixelPredicate: EdgePixelPredicate,
        bound: Rect,
        centerMinimum: Rect,
    ): Int {

        // 把 centerMinimum 从中心分区
        // 四分之一轮廓线
        // x 是距离 centerX 的大小
        // index 是高度从 centerMinimum 顶部第一个像素开是
        val arrSize = centerMinimum.height() / 2 + 1
        val outline = Array<Int>(arrSize) { 0 } // 0表示第一个像素
        val centerX = centerMinimum.centerX()
        val centerY = centerMinimum.centerY()
        val width = bitmap.width

        // 遍历 bound 区域，填充轮廓点
        // 行扫描，找到第一个点后，这一行就跳过
        // 左半区域
        val lastX = ArrayList<Int>(8)
        val continueConfirmPixelCount = 2
        for (y in bound.top until bound.bottom) {

            // 第一和第三镜像y坐标
            val index = if (y < centerY) y - centerMinimum.top else centerMinimum.bottom - y
            val old = outline[index]

            lastX.clear()

            // 前置退出条件，行扫描只在现有点位之前，之后的永远小于当前的，忽略
            for (x in bound.left until (centerX - old)) {

                // 中心线跳过
                if (y == centerY) continue

                val pixel = bitmap.getPixel(x, y)
                // todo 根据不同图像，使用不同前景标记颜色
                if (edgePixelPredicate.test(pixel)) {

                    lastX.add(x)

                    // 连续像素确认才算边缘
                    if (lastX.size > continueConfirmPixelCount) {
                        break
                    }

                }
            }
            val dx = centerX - (lastX.firstOrNull() ?: centerX)

            if (dx > old) {
                outline[index] = dx
            }
        }

        // 右半区域
        for (y in bound.top until bound.bottom) {
            // 第二和第四镜像y坐标
            val index = if (y < centerY) y - centerMinimum.top else centerMinimum.bottom - y
            val old = outline[index]

            lastX.clear()

            // 前置退出条件，行扫描只在现有点位之前，之后的永远小于当前的，忽略
            for (x in bound.right downTo centerX + old) {

                // 中心线跳过
                if (y == centerY) continue

                // todo 像素访问改为数组
                if (x >= bitmap.width) {
                    trackIcon(this,"")
                }
                val pixel = bitmap.getPixel(x, y)
                // todo 根据不同图像，使用不同前景标记颜色
                if (edgePixelPredicate.test(pixel)) {

                    lastX.add(x)

                    // 连续两个像素确认
                    if (lastX.size > continueConfirmPixelCount) {
                        break
                    }

                }
            }

            val dx = (lastX.firstOrNull() ?: centerX) - centerX
            if (dx > old) {
                outline[index] = dx
            }
        }

        val halfY = centerMinimum.height() / 2

        // 计算最大半径
        var radius = 0F
        for (i in outline.indices) {
            val dx = outline[i]
            if (dx == 0) continue

            val curr = sqrt(
                dx.toFloat().pow(2) +
                        (halfY - i).toFloat().pow(2)
            )
            if (curr > radius) {
                radius = curr
            }
        }

        return radius.toInt()
    }

    /**
     *
     *
     * 降低干扰
     * 1.使用72*72的最大圆形遮罩预处理，消除四个角落的干扰
     * 2.
     * @param minimum 最小区域, 假如内容小于此区域，将停止查询。它始终位于图像中心
     */
    private fun findCenterBoundingRect(
        bitmap: Bitmap,
        edgePixelPredicate: EdgePixelPredicate,
        isBlackAndTransparent: Boolean,
        isBlackContent: Boolean,
        minimum: Rect? = null
    ): Rect {
        // 使用像素操作寻找前景内容的最小外接矩形
        // 迭代位图像素，确定包围前景内容的最小外接矩形的左上角坐标（x1，y1）和宽度和高度（w，h）
        // 返回包含边界矩形坐标的Rect [x1，y1，w，h]

        val width = bitmap.width
        val height = bitmap.height

        // 初始化边界坐标为图像尺寸的最大值和最小值，以确保能找到实际的边界
        // todo 确保边界值是可访问的！
        var minX = width-1
        var minY = height-1
        var maxX = 0
        var maxY = 0

        // 迭代图像像素，找到前景内容的最小外接矩形
        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = bitmap.getPixel(x, y)
                // todo 根据不同图像，使用不同前景标记颜色
                if (edgePixelPredicate.test(pixel)) {
                    // 更新边界坐标
                    minX = Math.min(minX, x)
                    minY = Math.min(minY, y)
                    maxX = Math.max(maxX, x)
                    maxY = Math.max(maxY, y)
                }
            }
        }

        // 计算矩形的宽度和高度
        val actualRect = Rect(minX, minY, maxX, maxY).apply {
            if (left > right || top > bottom) {
                trackIcon(this, "ERROR, 未找到最小外接矩形，返回原Rect")
                set(0, 0, width-1, height-1)
            }
        }

        return actualRect
    }


    // todo 使用位字段

}

class EdgePixelPredicate(val isBlackOrTransparent: Boolean) {
    // 假设前景像素为非黑色（不透明）
    fun test(pixel: Int): Boolean {
        return (isBlackOrTransparent && PixelUtils.isAlmostBlackPixel(pixel))
                || PixelUtils.isAlmostWhitePixel(pixel)
    }

}

sealed class EdgeResult(
    val bitmap: Bitmap,
    val isCompletelyOpaque: Boolean
) {

    class Blank(
        val color: Int,
        bitmap: Bitmap,
        isCompletelyOpaque: Boolean
    ) : EdgeResult(bitmap, isCompletelyOpaque) {
        val isTransparent = color == Color.TRANSPARENT

        override fun toString(): String {
            bitmap.hasAlpha()
            return "Blank(color=$color, isTransparent=$isTransparent)"
        }

    }

    class Image(
        val isBlackAndTransparent: Boolean,
        val centerMinimum: Rect,
        val minimum: Rect,
        val safeRadius: Int,
        val backgroundColor: Int?,
        bitmap: Bitmap,
        isCompletelyOpaque: Boolean
    ) : EdgeResult(bitmap, isCompletelyOpaque) {

        override fun toString(): String {
            return "Image(isBlackAndTransparent=$isBlackAndTransparent, centerMinimum=$centerMinimum)"
        }
    }

}
