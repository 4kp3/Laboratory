package com.lovely.bear.laboratory.dan.mu

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import com.example.myapplication.R
import com.lovely.bear.laboratory.clip
import com.lovely.bear.laboratory.dan.mu.head.*
import com.lovely.bear.laboratory.dpToPx
import com.lovely.bear.laboratory.getTextSizeByHeight
import kotlinx.coroutines.*
import master.flame.danmaku.controller.DrawHandler
import master.flame.danmaku.controller.IDanmakuView
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.DanmakuTimer
import master.flame.danmaku.danmaku.model.image.R2LImageDanmu
import master.flame.danmaku.ui.widget.DanmakuView
import java.util.HashMap
import kotlin.math.min

/**
 * 头像弹幕辅助类
 * 设置完一组弹幕数据后，会自动循环播放
 *
 * 设置一组弹幕[setDanmu]
 *
 * @author guoyixiong
 */
open class ChatHeadDanmuHelper(
    context: Context,
    danmuView: IDanmakuView
) : BaseDanmuHelper(context, danmuView) {

    private val defaultIconBitmap: Bitmap by lazy {
        BitmapFactory.decodeResource(context.resources, R.drawable.img)
    }
    private val danmuShowGapTime = 300
    private var loopStartTime = 0L

    private val url = "https://photo.16pic.com/00/90/18/16pic_9018257_b.jpg"
    private val seed =
        "《极限竞速：地平线5》是由微软发行的一款赛车竞速游戏，也是地平线系列的最新作品。不同于正传Foza系列，" +
                "地平线系列的拟真元素要少了许多，重心更加偏向于街车与自由度方面。操作手感上也更轻量化，更加的爽" +
                "快和刺激。Xbox&Bethesda游戏展示会将于北京时间6月13日凌晨1点正式播出，" +
                "该节目将展示来自Xbox Game Studios、B社和微软Xbox在世界各地的合作伙伴的游戏作品。"

    private val danmu: MutableList<R2LImageDanmu> = mutableListOf()

    private var danmuLoopJob: Job? = null

    private val danmuViewCallback = object : DrawHandler.Callback {
        override fun prepared() {
        }

        override fun updateTimer(timer: DanmakuTimer?) {
        }

        override fun danmakuShown(danmaku: BaseDanmaku?) {
        }

        override fun drawingFinished() {
            Log.d(tag, "drawingFinished")
            danmuView.seekTo(loopStartTime)
        }
    }

    override fun onSetupDanmuView() {
        // 设置最大显示行数
        val maxLinesPair = HashMap<Int, Int>()
        maxLinesPair[BaseDanmaku.TYPE_SCROLL_RL] = 3 // 滚动弹幕最大显示5行

        danmuContext.setMaximumLines(maxLinesPair)
        danmuContext.setScrollSpeedFactor(0.75F)

        addDanmuViewCallback(danmuViewCallback)
    }

    /**
     * 设置要轮播的弹幕数据
     * 注意：这里是全量设置，新数据将会覆盖旧数据
     */
    fun setDanmu(data: List<ChatHeadInfo>) {
        val newDanmu =
            data.mapNotNull {
                createImageDanmu(it.avatarUrl, defaultIconBitmap, it.text, it.type)
            }
        newDanmu.forEachIndexed { index, danmu ->
            danmu.time += danmuShowGapTime * index + (Math.random() * danmuShowGapTime).toInt()
        }
        loopWith(newDanmu)
    }

    fun loopWith(data: List<R2LImageDanmu>) {

        //todo del
        data.forEachIndexed { index, danmu ->
            danmu.time += danmuShowGapTime * index + (Math.random() * danmuShowGapTime).toInt()
        }

        danmuLoopJob?.cancel()
        danmu.clear()
        danmu.addAll(data)
        if (danmu.isEmpty()) {
            return
        }
        loopStartTime = danmuView.currentTime
        danmu.forEach {
            addDanmu(it)
        }
    }


    fun addDanmaku() {
        danmuView.addDanmaku(getADanmu())
    }

    fun getADanmu(): R2LImageDanmu {
        //val danmaku = mContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL) ?: return
        val total = seed.length
        var start = 0
        var end = 0
        do {
            start = min((total * Math.random()).toInt(), 0)
            end = min((total * Math.random()).toInt(), total)
        } while (start >= end)

        val url = if (start % 2 == 0) url else null
        val type = if (start % 2 == 0) ChatType.NORMAL else ChatType.EMPLOYEE

        val danmu = createImageDanmu(url, defaultIconBitmap, clip(seed, 20, "..."), type)
        return danmu!!
    }

    /**
     * @param text 文字内容为空，本条弹幕将被忽略
     */
    private fun createImageDanmu(
        avatarUrl: String?,
        defaultAvatarBitmap: Bitmap,
        text: String,
        chatType: ChatType
    ): R2LImageDanmu? {

        if (text.isBlank()) return null

        val image = if (avatarUrl.isNullOrBlank()) {
            ChatHeadDan(
                defaultAvatarBitmap,
                chatType,
                context.resources
            )
        } else {
            RemoteChatHeadDan(
                avatarUrl,
                defaultAvatarBitmap,
                chatType,
                context.resources,
            )
        }
        val danmaku = R2LImageDanmu(
            image,
            danmuContext.mDanmakuFactory.MAX_Duration_Scroll_Danmaku,
        )
        if (image is RemoteChatHeadDan) {
            image.danmu = danmaku
        }

        val textHeight = dpToPx(12F, context) * 1F
        danmaku.size.apply {
            paddingStart = dpToPx(40F, context)
            paddingEnd = dpToPx(14.5F, context)
            setVerticalPadding(dpToPx(10F, context))
        }
        danmaku.textSize = getTextSizeByHeight(textHeight)

        danmaku.text = text
        danmaku.priority = 0 // 可能会被各种过滤器过滤并隐藏显示
        danmaku.isLive = false
        danmaku.time = danmuView.currentTime + 1200
        danmaku.textColor = Color.BLACK
        danmaku.borderColor = Color.GREEN
        return danmaku
    }

    override fun onDestroy() {
        super.onDestroy()
        removeDanmuViewCallback(danmuViewCallback)
        defaultIconBitmap.recycle()
    }
}

data class ChatHeadInfo(val avatarUrl: String?, val text: String, val type: ChatType)