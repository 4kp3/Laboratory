package com.lovely.bear.laboratory.bitmap

import android.graphics.Rect
import com.lovely.bear.laboratory.bitmap.data.CornerDirection
import com.lovely.bear.laboratory.bitmap.data.Direction
import com.lovely.bear.laboratory.bitmap.data.RoundRect
import com.lovely.bear.laboratory.bitmap.data.moveToCenter
import java.lang.Integer.min
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt


/*
* Copyright (C), 2023, Nothing Technology
* FileName: RectUtils
* Author: yixiong.guo
* Date: 2023/7/20 10:31
* Description:  
* History:
* <author> <time> <version> <desc>
*/
object RectUtils {

    /**
     * 把src完全放到dst中，需要的缩放比例
     *
     * gap src到dst的距离，距离越大，对src的缩放比例越大。
     * 方法会确保src到dst的最小距离是gap值
     * 距离考虑了dst是圆角的情况，此时如果src是正方形，最小距离可能位于四周的圆角部分，设置合理的距离避免src过于靠近外边界影响观感。
     *
     * 目前所有的圆角按照正圆处理
     *
     * @param cornerDistance dst的圆角到
     */
    fun getMinimalScale(dst: RoundRect, src: RoundRect, gap: Int): Pair< RoundRect,Float> {
//        if (src.width < dst.width  && src.height < dst.width ) {
//            return 1F
//        }

        // 最终缩放到外框中心
        val centerX = dst.content.centerX()
        val centerY = dst.content.centerY()

        // 先缩放到1:1
        val scaleEquals = getMinimalScale(dst.content, src)

        // 转换src
        var scaledSrc = if (scaleEquals < 1F) {
            src.scaleAndMove(scaleEquals,centerX=centerX,centerY=centerY)
        } else src

        // 找出四个方向的边界上哪里空隙最小
        val minEdgeDirection = getMinGapDirection(dst.content, scaledSrc.content)
        val minCorner = getMinCornerDistance(dst, scaledSrc)

        // 计算出gap差量，从而计算出缩放补偿量
        val minGap = min(minEdgeDirection.d, minCorner.d)

        val scale =
            if (minGap < gap) {
                val factor = gap * 1F / scaledSrc.width
                scaledSrc = scaledSrc.scaleAndMove(factor,centerX=centerX,centerY=centerY)
                scaleEquals - factor
            } else scaleEquals

        return Pair(scaledSrc,scale)
    }

    /**
     * @return 若src面积小于dst，返回1不缩放
     */
    fun getMinimalScale(dst: Rect, src: RoundRect): Float {
        if (src.width < dst.width() && src.height < dst.width()) {
            return 1F
        }

        val dstRatio = dst.width() / dst.height()
        val srcRatio = src.width / src.height

        return if (dstRatio > srcRatio) {
            dst.width() * 1F / src.width
        } else {
            dst.height() * 1F / src.height
        }
    }


    /**
     * 获取两个Rect之间距离最短的那条边的方位
     *
     * 根据左上角坐标体系计算
     */
     fun getMinGapDirection(dst: Rect, src: Rect): Direction {
        return listOf(
            Direction.LEFT(src.left - dst.left),
            Direction.TOP(src.top - dst.top),
            Direction.RIGHT(dst.right - src.right),
            Direction.BOTTOM(dst.bottom - src.bottom),
        ).min()
    }


    /**
     * 找出四个圆角到中心距离最小的值
     */
    private fun getMinCornerDistance(dst: RoundRect, src: RoundRect): CornerDirection {
        // 按照正圆角计算，x=y=roundRect.corners.xxx
        val leftTopDst = getCornerRadius(
            dst.corners.leftTop,
            dst.corners.leftTop,
            dst.width,
            dst.height
        )
        val rightTopDst = getCornerRadius(
            dst.corners.rightTop,
            dst.corners.rightTop,
            dst.width,
            dst.height
        )
        val rightBottomDst = getCornerRadius(
            dst.corners.rightBottom,
            dst.corners.rightBottom,
            dst.width,
            dst.height
        )
        val leftBottomDst = getCornerRadius(
            dst.corners.leftBottom,
            dst.corners.leftBottom,
            dst.width,
            dst.height
        )

        val leftTopSrc = getCornerRadius(
            src.corners.leftTop,
            src.corners.leftTop,
            src.width,
            src.height
        )
        val leftBottomSrc = getCornerRadius(
            src.corners.leftBottom,
            src.corners.leftBottom,
            src.width,
            src.height
        )
        val rightTopSrc = getCornerRadius(
            src.corners.rightTop,
            src.corners.rightTop,
            src.width,
            src.height
        )
        val rightBottomSrc = getCornerRadius(
            src.corners.rightBottom,
            src.corners.rightBottom,
            src.width,
            src.height
        )

        return listOf(
            CornerDirection.LeftTop((leftTopDst - leftTopSrc).toInt()),
            CornerDirection.RightTop((rightTopDst - rightTopSrc).toInt()),
            CornerDirection.RightBottom((rightBottomDst - rightBottomSrc).toInt()),
            CornerDirection.LeftBottom((leftBottomDst - leftTopSrc).toInt()),
        ).min()
    }

    /**
     * @param x x轴弧线半径
     * @param y y轴弧线半径
     */
    private fun getCornerRadius(x: Int, y: Int, w: Int, h: Int): Float {
        // 弧线中心点到矩形中心点的距离+弧线半径（取x y最大值）
        return sqrt((w / 2F - x).pow(2) + (h / 2F - y).pow(2)) + max(x, y)
    }


}

