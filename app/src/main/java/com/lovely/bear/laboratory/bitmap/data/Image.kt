package com.lovely.bear.laboratory.bitmap.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import com.lovely.bear.laboratory.MyApplication
import com.lovely.bear.laboratory.bitmap.Result

open class Image(val bitmap: Bitmap) {
    var edgeBitmap: Result? = null
}

open class IconImage(val label: String, val icon: Drawable, bitmap: Bitmap) : Image(bitmap)

class AdaptiveIconImage(
    val fgBitmap: IconImage,
    val bgBitmap: IconImage,
    label: String,
    icon: Drawable,
    bitmap: Bitmap
) :
    IconImage(label, icon, bitmap) {

}

data class ResImage(val resId: Int) :
    Image(BitmapFactory.decodeResource(MyApplication.APP.resources, resId)) {
}