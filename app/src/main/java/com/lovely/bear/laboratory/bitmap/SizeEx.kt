package com.lovely.bear.laboratory.bitmap

import android.graphics.Bitmap
import android.util.Size
import com.lovely.bear.laboratory.util.pxToDp

/*
* Copyright (C), 2023, Nothing Technology
* FileName: SizeEx
* Author: yixiong.guo
* Date: 2023/7/18 20:36
* Description:  
* History:
* <author> <time> <version> <desc>
*/
fun Bitmap.toSize(): Size {
    return Size(this.width,this.height)
}

fun android.util.Size.dpSize():String {
    return "dpSize[${pxToDp(width)},${pxToDp(height)}]"
}