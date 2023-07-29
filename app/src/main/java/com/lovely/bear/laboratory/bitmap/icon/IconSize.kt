package com.lovely.bear.laboratory.bitmap.icon

import android.util.Size
import com.lovely.bear.laboratory.bitmap.utils.dpSize

/*
* Copyright (C), 2023, Nothing Technology
* FileName: IconSize
* Author: yixiong.guo
* Date: 2023/7/16 19:14
* Description:  
* History:
* <author> <time> <version> <desc>
*/
data class IconSize(val sourceSize:Size,val requestSize:Size,){
    override fun toString(): String {
        return "IconSize(requestSize=$requestSize,${requestSize.dpSize()}, sourceSize=$sourceSize),${sourceSize.dpSize()}"
    }
}