package com.lovely.bear.laboratory.bitmap.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.VectorDrawable
import com.lovely.bear.laboratory.MyApplication
import com.lovely.bear.laboratory.bitmap.analyse.EdgeResult
import com.lovely.bear.laboratory.bitmap.mono.Mono
import com.lovely.bear.laboratory.bitmap.utils.typeDesc

open class Image(val bitmap: Bitmap) {
    var edgeBitmap: EdgeResult? = null
    var mono: Mono? = null
    var appInfo: AppInfo? = null
}

open class IconImage(val label: String, val icon: Drawable, bitmap: Bitmap) : Image(bitmap) {
    val iconType: String = icon.typeDesc()
}

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