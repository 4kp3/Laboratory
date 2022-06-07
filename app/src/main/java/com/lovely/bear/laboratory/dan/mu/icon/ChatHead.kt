package com.lovely.bear.laboratory.dan.mu.icon

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import master.flame.danmaku.danmaku.model.BaseDanmaku

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

class ChatHeadDan(bitmap: Bitmap, override val type: ChatType, res: Resources) : IChatHead {
    override val width: Int
        get() = 50
    override val height: Int
        get() = 50
    override val drawable: Drawable = ChatHeadDrawable(bitmap, type, res, width, height)
}

/**
 * @param danmu 头像所属弹幕，用于更新
 */
class RemoteChatHeadDan(
    val url: String,
    defaultBitmap: Bitmap,
    override val type: ChatType,
    private val res: Resources,
    val danmu: BaseDanmaku,
) : IChatHead {
    override val width: Int
        get() = 50
    override val height: Int
        get() = 50
    private val default: ChatHeadDrawable =
        ChatHeadDrawable(defaultBitmap, type, res, width, height)
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
            } else if (remoteDrawable != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                remoteDrawable?.bitmap = value
            } else {
                remoteDrawable = ChatHeadDrawable(value, type, res, width, height)
            }
        }
    override val drawable: Drawable
        get() = remoteDrawable ?: default
}
