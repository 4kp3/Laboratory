package com.lovely.bear.laboratory.dan.mu.icon

import android.graphics.drawable.Drawable
import master.flame.danmaku.danmaku.model.BaseDanmaku

/**
 * 弹幕的图像数据
 * @author guoyixiong
 */

interface IImage {
    val drawable: Drawable
    val width: Int
    val height: Int
}

internal fun BaseDanmaku.isImageDan(): Boolean {
    return tag != null && tag is IImage
}

internal fun BaseDanmaku.image(): IImage? {
    return tag as? IImage
}

