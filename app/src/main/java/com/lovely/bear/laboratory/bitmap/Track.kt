package com.lovely.bear.laboratory.bitmap

import android.util.Log

/*
* Copyright (C), 2023, Nothing Technology
* FileName: Track
* Author: yixiong.guo
* Date: 2023/7/16 19:30
* Description:  
* History:
* <author> <time> <version> <desc>
*/

const val TRACK_ICON = "TRACK_ICON"

fun trackIcon(message:String) {
    Log.d(TRACK_ICON,message)
}

fun trackIcon(caller:Any,message:String) {
    Log.d(TRACK_ICON,"${caller::class.java.simpleName}:$message")
}