package com.lovely.bear.laboratory.bitmap.icon

import com.android.launcher3.icons.IconGrayConverter
import com.android.launcher3.icons.IconGrayConverterAuto
import com.lovely.bear.laboratory.MyApplication
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

    var monoRatio = 42 / 108F
        private set

    var safeZoneRatio = 66/108F

    var densityDpi = 0
        private set

    // 图标全尺寸，未裁剪/缩放
    var fullySizeDp = 108F
        private set

    var fullySizePx = 0
        private set

    // 图标显示尺寸，view 窗口大小
    var iconSizeDp = 66.28571F
        private set

    var iconSizePx = 0
        private set

    var safeZoneDp = 66.28571F
        private set

    var safeZonePx = 0
        private set

    // mono 窗口大小，方形
    var monoSizeDp = iconSizeDp* monoRatio
        private set

    var monoSizePx = 0
        private set

    var monoOuterCircleRadiusDp = sqrt(monoSizeDp*monoSizeDp/2F)
    var monoOuterCircleRadiusPx = dpToPx(monoOuterCircleRadiusDp)

    /**
     * 包围mono方形的最小圆
     */
    val monoRadiusPx: Float
        get() = sqrt(monoSizePx * monoSizePx / 2F)

    val converter: IconGrayConverter by lazy {
        IconGrayConverter(monoSizePx)
    }

    val converterAuto: IconGrayConverterAuto by lazy {
        IconGrayConverterAuto()
    }

    init {
        setup()
    }

    fun setup() {
        densityDpi = MyApplication.APP.resources.configuration.densityDpi
        iconSizePx = dpToPx(iconSizeDp)
        fullySizePx = dpToPx(fullySizeDp)
        monoSizePx = dpToPx(monoSizeDp)
    }

}

