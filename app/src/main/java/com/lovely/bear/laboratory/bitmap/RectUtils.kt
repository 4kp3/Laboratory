package com.lovely.bear.laboratory.bitmap

import android.graphics.Rect
import com.lovely.bear.laboratory.bitmap.data.RoundRect
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

    // 圆角为角落到中心点的距离
    fun getMinimalScale(dst: RoundRect, src: RoundRect, cornerDistance: Int): Float {
//        if (src.width < dst.width  && src.height < dst.width ) {
//            return 1F
//        }


        val dstCornerDistance = getMaxCornerDistance(dst)
        val srcCornerDistance = getMaxCornerDistance(src)
        val

        // 中心到角的距离
        // 角圆心到中心的距离
        val dstD =
            sqrt((dst.width / 2F - dstCorner).pow(2) + (dst.height / 2F - dstCorner).pow(2)) + dstCorner

        return 1F
    }


    private fun getMaxCornerDistance(roundRect: RoundRect): Float {
        // 按照正圆角计算，x=y=roundRect.corners.xxx
        val leftTopD = getCornerDistance(
            roundRect.corners.leftTop,
            roundRect.corners.leftTop,
            roundRect.width,
            roundRect.height
        )
        val leftBottomD = getCornerDistance(
            roundRect.corners.leftBottom,
            roundRect.corners.leftBottom,
            roundRect.width,
            roundRect.height
        )
        val rightTopD = getCornerDistance(
            roundRect.corners.rightTop,
            roundRect.corners.rightTop,
            roundRect.width,
            roundRect.height
        )
        val rightBottomD = getCornerDistance(
            roundRect.corners.rightBottom,
            roundRect.corners.rightBottom,
            roundRect.width,
            roundRect.height
        )

        return listOf(leftTopD, leftBottomD, rightTopD, rightBottomD).max()
    }

    /**
     * @param x x轴弧线半径
     * @param y y轴弧线半径
     */
    private fun getCornerDistance(x: Int, y: Int, w: Int, h: Int): Float {
        // 弧线中心点到矩形中心点的距离+弧线半径（取x y最大值）
        return sqrt((w / 2F - x).pow(2) + (h / 2F - y).pow(2)) + max(x, y)
    }
}