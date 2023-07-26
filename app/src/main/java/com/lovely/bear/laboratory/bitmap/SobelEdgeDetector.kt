package com.lovely.bear.laboratory.bitmap

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.ui.unit.dp
import com.lovely.bear.laboratory.bitmap.utils.PixelUtils
import kotlin.math.sqrt


typealias ForegroundJudgment  = (pixel:Int)->Boolean

//val WhiteJudgment =

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
    override fun detect(source: Bitmap): Result {

        val width: Int = source.width
        val height: Int = source.height
        val totalPixelsCount = width * height

        var isSolidColor: Boolean = true
        var isTransparent: Boolean = true
        var isBlackAndTransparent: Boolean = true
        var isOriginalBitmap: Boolean = false
        var maybeContentIsBlack: Boolean = false

        val noSet = -1111
        var blackPixelCount: Long = 0
        var firstPixel: Int = noSet
        var lastPixel: Int = noSet
        var pixelIndex = 0

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
            pixelIndex++

            if (pixelIndex == totalPixelsCount && isBlackAndTransparent) {
                maybeContentIsBlack = blackPixelCount < totalPixelsCount / 2
            }
        }

        val edgesBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

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
            Result.Blank(color = firstPixel, edgesBitmap)
        } else {

            val bound = findCenterBoundingRect(
                if (isBlackAndTransparent) {
                    source
                } else edgesBitmap,isBlackAndTransparent,maybeContentIsBlack
            )

            val canvas = Canvas(edgesBitmap)
            canvas.drawRect(bound, Paint().apply {
                color = Color.RED
                style = Paint.Style.STROKE
                strokeWidth = 1.dp.value
            })

            Result.Image(isBlackAndTransparent, bound, edgesBitmap)
        }

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
        var minX = width
        var minY = height
        var maxX = 0
        var maxY = 0

        // 迭代图像像素，找到前景内容的最小外接矩形
        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = bitmap.getPixel(x, y)
                // todo 根据不同图像，使用不同前景标记颜色
                if (
                    (isBlackAndTransparent && PixelUtils.isAlmostBlackPixel(pixel)) || PixelUtils.isAlmostWhitePixel(pixel)
                ) { // 假设前景像素为非黑色（不透明）
                    // 更新边界坐标
                    minX = Math.min(minX, x)
                    minY = Math.min(minY, y)
                    maxX = Math.max(maxX, x)
                    maxY = Math.max(maxY, y)
                }
            }
        }

        // 计算矩形的宽度和高度
        val rectWidth = maxX - minX + 1
        val rectHeight = maxY - minY + 1

        val actualRect = Rect(minX, minY, maxX, maxY)

        return actualRect
    }


    // todo 使用位字段

}

sealed class Result(
    val bitmap: Bitmap
) {

    class Blank(
        val color: Int,
        bitmap: Bitmap,
    ) : Result(bitmap) {
        val isTransparent = color == Color.TRANSPARENT

        override fun toString(): String {
            return "Blank(color=$color, isTransparent=$isTransparent)"
        }

    }

    class Image(
        val isBlackAndTransparent: Boolean,
        val centerMinimum: Rect,
        bitmap: Bitmap,
    ) : Result(bitmap) {

        override fun toString(): String {
            return "Image(isBlackAndTransparent=$isBlackAndTransparent, centerMinimum=$centerMinimum)"
        }
    }

}
