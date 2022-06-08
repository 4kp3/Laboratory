package com.lovely.bear.laboratory.dan.mu.icon.image

import android.graphics.drawable.Drawable
import master.flame.danmaku.danmaku.model.BaseDanmaku

/**
 * 弹幕的图像数据
 * @author guoyixiong
 */
interface IImage {
    val drawable: Drawable

    /**
     * 图片的宽度，像素值
     */
    val drawableWidth: Int

    /**
     * 图片的高度，像素值
     */
    val drawableHeight: Int

    /**
     * 图片和文字的间距
     */
    val drawablePadding: Int

    /**
     * 占用总宽度
     */
    val width: Int
        get() = drawableWidth + drawablePadding
}
//
//internal fun BaseDanmaku.isImageDan(): Boolean {
//    return tag != null && tag is IImage
//}
//
//internal fun BaseDanmaku.image(): IImage? {
//    return tag as? IImage
//}

