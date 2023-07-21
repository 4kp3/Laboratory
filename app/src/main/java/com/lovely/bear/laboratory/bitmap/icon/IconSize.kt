package com.lovely.bear.laboratory.bitmap.icon

import android.util.Size

/*
* Copyright (C), 2023, Nothing Technology
* FileName: IconSize
* Author: yixiong.guo
* Date: 2023/7/16 19:14
* Description:  
* History:
* <author> <time> <version> <desc>
*/
data class IconSize(val actualSize:Size,val requestSize:Size,val sourceSize:Size){
    override fun toString(): String {
        return "IconSize(actualSize=$actualSize, requestSize=$requestSize, sourceSize=$sourceSize)"
    }
}