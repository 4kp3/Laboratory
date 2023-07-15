package com.lovely.bear.laboratory.bitmap

import com.lovely.bear.laboratory.MyApplication
import com.lovely.bear.laboratory.util.dpToPx

object IconConfig {

    val densityDpi = MyApplication.APP.resources.displayMetrics.densityDpi

    val iconSizeDp = 72F
    val iconSize = dpToPx(iconSizeDp,MyApplication.APP)

    fun setup() {

    }

}