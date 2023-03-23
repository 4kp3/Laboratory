package com.lovely.bear.laboratory.dan.mu.head

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.lovely.bear.laboratory.util.dpToPx
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.image.IImage

interface IChatHead : IImage {
    val type: ChatType
}

/**
 * 聊天类型
 */
enum class ChatType {
    NORMAL,//普通人
    EMPLOYEE //职员
}

class ChatHeadDan(bitmap: Bitmap, override val type: ChatType, private val res: Resources) :
    IChatHead {
    override val drawablePadding: Int
        get() = 0
    override val drawableWidth: Int
        get() = dpToPx(32F, res)
    override val drawableHeight: Int
        get() = dpToPx(32F, res)
    override val drawable: Drawable =
        ChatHeadDrawable(bitmap, type, res, drawableWidth, drawableHeight)
}

class RemoteChatHeadDan(
    val url: String,
    defaultBitmap: Bitmap,
    override val type: ChatType,
    private val res: Resources,
) : IChatHead {
    /**
     * 头像所属弹幕，用于更新
     */
    var danmu: BaseDanmaku? = null
    override val drawablePadding: Int
        get() = 0
    override val drawableWidth: Int
        get() = dpToPx(32F, res)
    override val drawableHeight: Int
        get() = dpToPx(32F, res)
    private val default: ChatHeadDrawable =
        ChatHeadDrawable(defaultBitmap, type, res, drawableWidth, drawableHeight)
    private var remoteDrawable: ChatHeadDrawable? = null
    internal var remoteBitmap: Bitmap? = null
        set(value) {
            if (value == field) return
            field = value
            if (value != null && remoteDrawable != null && value == remoteDrawable!!.bitmap) {
                return
            }
            if (value == null) {
                remoteDrawable = null
            } else if (remoteDrawable != null) {
                remoteDrawable?.bitmap = value
            } else {
                remoteDrawable = ChatHeadDrawable(value, type, res, drawableWidth, drawableHeight)
            }
        }
    override val drawable: Drawable
        get() = remoteDrawable ?: default
}
