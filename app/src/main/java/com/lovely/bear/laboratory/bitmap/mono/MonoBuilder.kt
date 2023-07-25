package com.lovely.bear.laboratory.bitmap.mono

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Size
import androidx.core.graphics.scale
import com.lovely.bear.laboratory.bitmap.PathUtils
import com.lovely.bear.laboratory.bitmap.RectUtils
import com.lovely.bear.laboratory.bitmap.analyse.EdgeResult
import com.lovely.bear.laboratory.bitmap.data.Corners
import com.lovely.bear.laboratory.bitmap.data.Image
import com.lovely.bear.laboratory.bitmap.data.RoundRect
import com.lovely.bear.laboratory.bitmap.data.makeEdgeBitmap
import com.lovely.bear.laboratory.bitmap.data.moveToCenter
import com.lovely.bear.laboratory.bitmap.dpSize
import com.lovely.bear.laboratory.bitmap.icon.IconConfig
import com.lovely.bear.laboratory.bitmap.toSize
import com.lovely.bear.laboratory.bitmap.trackIcon

/*
* Copyright (C), 2023, Nothing Technology
* FileName: MonoBuilder
* Author: yixiong.guo
* Date: 2023/7/16 19:09
* Description:
*
 * Bli漫画
 * 原因：构建mono时未包含背景层
 * 解决：检测前景、背景，如果包含内容，应该使用叠加后的图像构建mono
 *
 * Word
 * 原因：
 *      未进行圆形裁切或应用遮罩时，缩放比例过小
 *
 * 解决：缩放至合适大小，高度等于mono圆形区域直径
 *
 * 图层选择
 *      减少图层处理，可以提升速度
 *      减少背景图层，直接绘制内容，不会导致有色背景黑白化后干扰内容
 *
 *      AdaptiveIconDrawable中可以省略处理的图层：
 *      - 前景 完全不透明，使用前景，舍弃背景
 *              完全透明，使用背景
 *      - 背景
 *          透明，使用前景。
 *          1.纯白，使用前景，画笔注意。使用此背景的可能为传统图标、凸显非白色图标内容，此种情况下必须使用黑色画笔绘制内容。比如 Word
 *          2.纯黑，使用前景，画笔注意。使用白色。
 *          2.
 *          3.纯色，使用合体 todo 细化是否有情况可以丢弃的
 *              纯色背景目的是凸显前景，比如支付宝，可以丢弃不影响黑白前景效果
 *              影响：背景移除后阴影看起来很突兀怪异，比如原生机器人头图标
 *
 *      必须保留的图层
 *      1.有内容，比如Bli漫画背景
 *
 *      传统单图层，无法省略
 *
 * 内容最小半径确定：
 *      降低缩放比例，减少缩放对图标内容辨识度降低的影响。
 *      最小半径的计算是为了缩放到mono尺寸时，内容不至于缩放过小而降低辨识度，半径越小，缩放比例越小。
 *
 *      当前进行了透明度检测，根据透明度裁切，但遇到纯色比如白色背景（Google默认）将不会裁切，导致缩放比例过大，图标内容被显著缩小。
 *      比如 Word 背景就是纯白，当前被缩小为方形
 *
 * 缩放：
 *      - 根据内容最小半径和mono要求尺寸，计算出缩放比例，缩放后使用遮罩处理为圆形
 *      - 不选择裁切，是为了避免相对位置受到影响
 *      - 处理方形问题
 *
 *      结合安全区域计算，有范围限制
 *
 * 绘制mono：
 *      暗色模式，使用白笔绘制内容
 *      亮色模式，使用黑笔绘制内容
 *
 *      mono优化
 *          像素等级低，增加对比度
 *          等级多，不变，比如游戏图标
 *
 *      结合丢弃的图层对图标进行优化
 *
 * 图片缩放规则（内容大于mono方形窗）
 * - 全屏图标，裁剪为圆形，缩放到mono方形窗内，可以接受边角丢失，如漫画
 *      无论单层、双层
 * - 内容小于背景，根据内容形状缩放
 *      无论单层、双层
 *      小多少？
 *
 *      - 内容为长方形（长宽比需要测试），圆角裁切时可不进行缩放，如KUKU FM
 *              todo 如果本身是圆角，将不会进行缩放
 *      - 方形，圆形裁切之前进行安全距离缩放
 *          小图标灰度转换后，前景和背景混合，需要保留一定比例的背景才协调
 *
 * mono的圆角裁剪
 *
 *
* History:
* <author> <time> <version> <desc>
*/
object MonoBuilder {

//    fun buildUserVersion(request: MonoRequest):Mono {
////        val mono =
//            when (request) {
//            is BitmapMonoRequest->{
//               val d= IconConfig.converter.grayAndDrawCircle(BitmapDrawable(request.bitmap))
//                Mono.User(d, size = )
//            }
//            is DrawableMonoRequest->{
//                request.source.getMonochrome(APP.resources, request.source, IconConfig.converter)?.second
//            }
//        }
//
//    }

    fun buildFromComposed() {

    }

    /**
     * 采用了边界检测后，计算出最适合的缩放比例，从原图中创建mono
     * @param request 必须是图像，不能为空白或者纯色
     */
    fun buildAuto(request: MonoRequest): Mono.Auto {
        // mono 边长
        val sideLength = request.size.width
        val halfSideLength = sideLength / 2

        trackIcon("请求尺寸 ${request.size}，半边 $halfSideLength，请求尺寸 ${request.size.dpSize()}")

        // 采集边界信息
        val image = request.source
        val srcBitmap = image.bitmap
        val sourceSize = image.bitmap.toSize()
        trackIcon("原始图像尺寸 $sourceSize")

        val edgeResult = image.edgeBitmap ?: makeEdgeBitmap(image)

        if (edgeResult !is EdgeResult.Image) throw IllegalArgumentException("非图像，不可进行mono转换")

        trackIcon("图像分析结果：内容边界${edgeResult.minimum}，中心边界${edgeResult.centerMinimum}")

        /**
         * 计算图像内容的缩放值，以及进行原图裁剪，用于下一步处理
         * mono展示窗口为边长sideLength的方形，图像内容最终会填充到这里，所以最大尺寸也为sideLength的方形
         * - 当 contentArea>monoViewWindow，按照contentArea最长边缩放确保内容都被展示
         * - 当 contentArea<=monoViewWindow，不需要缩放，裁剪原图至monoViewWindow大小
         *
         * 先缩放再处理
         */
        val center = edgeResult.centerMinimum
        // todo 分析器分析圆角,这里先用0圆角
        // 必须有内容的圆角数据，用来确定轮廓圆角大小，否则最终绘制圆角时可能把内容裁切或者大小不合适
        val srcRoundRect = RoundRect(center, Corners())
        val dstInfo = getGoodOutline(request)
        // todo 后面所有的边界使用dst，而不是mono size
        val gap = dstInfo.second
        val dstRoundRect = dstInfo.first

        val scaledInfo = RectUtils.getMinimalScale(dstRoundRect, srcRoundRect, gap)
//        val scaledSrcRoundRect = scaledInfo.first
        val scaleFactor = scaledInfo.second

        trackIcon("图像裁剪规划：外框${dstRoundRect}\n   缩放比例${scaleFactor}, gap $gap")

        val greyRect = Rect(0, 0, sideLength, sideLength)
        val greyMaterial: Bitmap



        // 缩小后裁剪
        // todo 边距补偿，无需放大给到mono转换器，避免无谓转换，最后再转一次即可
        val scaledBitmap = if (scaleFactor<1F) srcBitmap.scale(
            (sourceSize.width * scaleFactor).toInt(),
            (sourceSize.height * scaleFactor).toInt()
        ) else srcBitmap

        // 越界处理，若原图小于mono尺寸，需要创建一个更大的bitmap，用空白填充周围空隙
        if (scaledBitmap.width < greyRect.width() || scaledBitmap.height < greyRect.height()) {

            trackIcon("缩放后的原图小于greyRect要求尺寸，进行扩充：scaledBitmap[${scaledBitmap.width},${scaledBitmap.height}],greyRect $greyRect")

            // 填充原图
            greyMaterial = Bitmap.createBitmap(
                greyRect.width(),
                greyRect.height(),
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas()
            canvas.setBitmap(greyMaterial)
            // 中心居中绘制
            val x = (greyRect.width() - scaledBitmap.width) / 2F
            val y = (greyRect.height() - scaledBitmap.height) / 2F

            val centerRectF=RectF(x,y,x+scaledBitmap.width.toFloat(),y+scaledBitmap.height.toFloat())
            // todo 矩阵转换是否更高效
            canvas.drawBitmap(scaledBitmap,null, centerRectF, null)
        } else {
            trackIcon("缩放后的原图大于greyRect尺寸，直接进行裁剪")

            greyRect.moveToCenter(scaledBitmap.width/2, scaledBitmap.height/2)
            // 对原图裁切
            greyMaterial = Bitmap.createBitmap(
                scaledBitmap,
                greyRect.left,
                greyRect.top,
                greyRect.width(),
                greyRect.height()
            )
        }

//        trackIcon("内容最小中心矩形 $center，内容原始矩形 ${edgeResult.minimum}，缩放比例$scale")

        // 灰度处理, 无需缩放
        val mono = IconConfig.converterAuto.grayAndDrawCircle(
            greyMaterial,
            sideLength,
        )

        // 裁切为圆形，要求返回的mono必须为mono size大小的方形
        val canvas = Canvas()
        val circleMono = Bitmap.createBitmap(sideLength, sideLength, Bitmap.Config.ALPHA_8)
        canvas.setBitmap(circleMono)
        val roundRectPath =
//        roundRectPath.addRect(RectF(0F,0F,sideLength.toFloat(),sideLength.toFloat()),Path.Direction.CW)
            PathUtils.createRoundedRectPath(
            0F,
            0F,
            sideLength.toFloat(),
            sideLength.toFloat(),
            dstRoundRect.corners.normalCorner.toFloat()
        )
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            // 圆形之内透明度保留，之外丢弃
            shader = BitmapShader(mono, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }
        canvas.drawPath(roundRectPath, paint)

        val monoSize = Size(mono.width, mono.height)
//        trackIcon("最终 mono 大小：$monoSize")

        return Mono.Auto(circleMono, size = monoSize, request)
    }

}


/**
 * 圆角裁剪
 * 1.内容为矩形
 *      计算圆角，
 */
fun getGoodScale(content: Rect, origin: Rect, monoSize: Size): Float {
    val fullContentRatio = 0.98F

    var scale: Float = 1F
    var cropRect = Rect()
    var keepCorner = false

    // 全屏裁切
    if (content.width() >= origin.width() * fullContentRatio && content.height() >= origin.height()) {
        trackIcon("getGoodScale, 全屏裁切")
    } else if (content.width() <= monoSize.width && content.height() <= monoSize.height) {

    } else {

    }
    return 1F;
}

/**
 * 根据图像获取最佳边界，最终的mono内容会缩放到返回的边界中
 */
fun getGoodOutline(request: MonoRequest): Pair<RoundRect, Int> {
    val monoSize = request.size
    // todo 根据内容，确定圆角大小
    val rect = RoundRect(content = Rect(0, 0, monoSize.width, monoSize.height), corners = Corners())
    // todo 根据内容，确定空隙大小
    return Pair(rect, 10)
}

fun Drawable.toBitmap(size: Size): Bitmap {
    if (this is BitmapDrawable) return bitmap
    val bitmap = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    draw(canvas)
    return bitmap
}


data class MonoRequest(
    val source: Image,
    val size: Size = Size(IconConfig.monoSizePx, IconConfig.monoSizePx)
)