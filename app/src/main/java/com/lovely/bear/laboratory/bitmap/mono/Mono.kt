package com.lovely.bear.laboratory.bitmap.mono

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
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

open  class Mono(val size:Size ,val label:String?=null){
    var extra:Mono?=null
}

open class BitmapMono(val bitmap:Bitmap,  size:Size ,  label:String?=null):Mono(size=size,label=label){
    class Original ( bitmap:Bitmap,  size:Size ,label:String?=null): BitmapMono(bitmap,size,label){
        override fun toString(): String {
            return "Original:$size"
        }
    }

    class User ( bitmap:Bitmap,  size:Size, request: MonoRequest,label:String?=null):BitmapMono(bitmap,size,label){
        override fun toString(): String {
            return "User:$size"
        }
    }

    class Auto ( bitmap:Bitmap,  size:Size, request: MonoRequest,label:String?=null):BitmapMono(bitmap,size,label){
        override fun toString(): String {
            return "Auto:$size"
        }
    }
    class System ( bitmap:Bitmap,  size:Size, request: MonoRequest,label:String?=null):BitmapMono(bitmap,size,label){
        override fun toString(): String {
            return "System:$size"
        }
    }
}

open class DrawableMono(val drawable: Drawable,size:Size ,  label:String?=null):Mono(size=size,label=label)


