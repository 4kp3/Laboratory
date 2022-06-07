package com.lovely.bear.laboratory.dan.mu.icon

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
        danmaku.image()?.let {
            if (it is RemoteChatHeadDan) {
                imageLoader.load(it)
            }
        }
    }

    override fun releaseResource(danmaku: BaseDanmaku) {
        //todo 处理Bitmap
    }
}