package com.lovely.bear.laboratory.dan.mu.icon.chat.head

import com.lovely.bear.laboratory.dan.mu.icon.image.R2LImageDanmu
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.android.BaseCacheStuffer

/**
 * 带有头像的弹幕填充器代理
 * @author guoyixiong
 */
class ChatHeadStufferProxy(val imageLoader: RemoteChatHeadLoader) : BaseCacheStuffer.Proxy() {
    /**
     * @param fromWorkerThread 未理解
     */
    override fun prepareDrawing(danmaku: BaseDanmaku, fromWorkerThread: Boolean) {
        if (danmaku is R2LImageDanmu) {
            if (danmaku.image is RemoteChatHeadDan) {
                imageLoader.load(danmaku.image)
            }
        }
    }

    override fun releaseResource(danmaku: BaseDanmaku) {
        //todo 处理Bitmap
    }
}