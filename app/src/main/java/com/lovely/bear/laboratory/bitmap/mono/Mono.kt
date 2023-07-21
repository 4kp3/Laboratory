package com.lovely.bear.laboratory.bitmap.mono

import android.graphics.Bitmap
import android.util.Size

/*
* Copyright (C), 2023, Nothing Technology
* FileName: Mono
* Author: yixiong.guo
* Date: 2023/7/16 19:11
* Description:  
* History:
* <author> <time> <version> <desc>
*/

sealed class Mono(val bitmap:Bitmap,val size:Size ){

    class Original ( bitmap:Bitmap,  size:Size ):Mono(bitmap,size){
        override fun toString(): String {
            return "Original:$size"
        }
    }

    class User ( bitmap:Bitmap,  size:Size, request: MonoRequest):Mono(bitmap,size){
        override fun toString(): String {
            return "User:$size"
        }
    }

    class Auto ( bitmap:Bitmap,  size:Size, request: MonoRequest):Mono(bitmap,size){
        override fun toString(): String {
            return "Auto:$size"
        }
    }
}
