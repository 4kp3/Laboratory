package com.lovely.bear.laboratory.bitmap.icon

import com.android.launcher3.icons.IconGrayConverter
import com.android.launcher3.icons.IconGrayConverterAuto
import com.lovely.bear.laboratory.MyApplication
import com.lovely.bear.laboratory.bitmap.trackIcon
import com.lovely.bear.laboratory.util.dpToPx
import kotlin.math.sqrt


/*
* Copyright (C), 2023, Nothing Technology
* FileName: IconConfig
* Author: yixiong.guo
* Date: 2023/7/13 11:54
* Description:
* History:
* <author> <time> <version> <desc>
*/
object IconConfig {

    val minimalSafeZoneCalibreDp = 66F

    // 最小显示（圆形）区域
    var minimalSafeZoneCalibrePx = 0
        private set

    // 最大显示区域
    val safeZoneLengthDp = 72F
    var safeZoneLengthPx = 0
        private set
    var monoRatio = 42 / 108F
        private set

    var safeZoneRatio = 66 / 108F

    var densityDpi = 0
        private set

    // 图标全尺寸，未裁剪/缩放
    var fullySizeDp = 108F
        private set

    var fullySizePx = 0
        private set

    /**
     * todo 记得尺寸在launcher中需要动态获取，避免分辨率改变导致硬编码出错
     * 4列 66.28？
     * 大图标，小图标
     * 多任务图标
     */
    var iconSizeDp = 66.28571F
        private set

    var iconSizePx = 0
        private set

    var safeZoneDp = 66.28571F
        private set

    var safeZonePx = 0
        private set

    // mono 窗口大小，方形
    var monoSizeDp = iconSizeDp * monoRatio
        private set

    var monoSizePx = 0
        private set

    // 下面的比率，是一个圆角占边长的比例
    // 默认方形图标的圆角大小，占图片边长的比例，比如0.2，最终会有0.4比例为圆弧
    val defaultSquareCornerRadiusRatio = 0.4F / 2

    // 全图内容类型图标的圆角比例，暂定位圆形比如Bili漫画
    val defaultFullContentIconCornerRadiusRatio = 1F / 2

    // 默认空隙占比mono尺寸比率
    val defaultGapRatio = 0.25F / 2

    var monoOuterCircleRadiusDp = sqrt(monoSizeDp * monoSizeDp / 2F)
    var monoOuterCircleRadiusPx = dpToPx(monoOuterCircleRadiusDp)

    /**
     * 包围mono方形的最小圆
     */
    val monoRadiusPx: Float
        get() = sqrt(monoSizePx * monoSizePx / 2F)

    val converter: IconGrayConverter by lazy {
        IconGrayConverter(iconSizePx)
    }

    val converterAuto: IconGrayConverterAuto by lazy {
        IconGrayConverterAuto()
    }

    init {
        setup()
    }

    fun setup() {
//        densityDpi = MyApplication.APP.resources.configuration.densityDpi
        densityDpi = 640
        iconSizePx = dpToPx(iconSizeDp)
        fullySizePx = dpToPx(fullySizeDp)
        monoSizePx = dpToPx(monoSizeDp)

        safeZoneLengthPx = dpToPx(safeZoneLengthDp)
        minimalSafeZoneCalibrePx = dpToPx(minimalSafeZoneCalibreDp)

        trackIcon(this,toString())
    }

    override fun toString(): String {
        return "IconConfig(minimalSafeZoneCalibreDp=$minimalSafeZoneCalibreDp, minimalSafeZoneCalibrePx=$minimalSafeZoneCalibrePx, safeZoneLengthDp=$safeZoneLengthDp, safeZoneLengthPx=$safeZoneLengthPx, monoRatio=$monoRatio, safeZoneRatio=$safeZoneRatio, densityDpi=$densityDpi, fullySizeDp=$fullySizeDp, fullySizePx=$fullySizePx, iconSizeDp=$iconSizeDp, iconSizePx=$iconSizePx, safeZoneDp=$safeZoneDp, safeZonePx=$safeZonePx, monoSizeDp=$monoSizeDp, monoSizePx=$monoSizePx, defaultSquareCornerRadiusRatio=$defaultSquareCornerRadiusRatio, defaultFullContentIconCornerRadiusRatio=$defaultFullContentIconCornerRadiusRatio, defaultGapRatio=$defaultGapRatio, monoOuterCircleRadiusDp=$monoOuterCircleRadiusDp, monoOuterCircleRadiusPx=$monoOuterCircleRadiusPx, monoRadiusPx=$monoRadiusPx)"
    }


}

