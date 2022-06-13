package com.lovely.bear.laboratory.dan.mu

import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.lovely.bear.laboratory.dan.mu.head.ChatHeadCacheStuffer
import com.lovely.bear.laboratory.dan.mu.head.ChatHeadStufferProxy
import com.lovely.bear.laboratory.dan.mu.head.RemoteChatHeadLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import master.flame.danmaku.controller.DrawHandler
import master.flame.danmaku.controller.IDanmakuView
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.DanmakuTimer
import master.flame.danmaku.danmaku.model.IDisplayer
import master.flame.danmaku.danmaku.model.android.DanmakuContext
import master.flame.danmaku.danmaku.model.android.Danmakus
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.coroutines.CoroutineContext

/**
 * 弹幕辅助类
 * [onSetupDanmuView] 弹幕配置
 * [addDanmu] 添加一条弹幕
 * [pause] [resume] 控制弹幕播放和暂停
 * [destroy] 销毁，注意销毁后不可再次使用
 *
 * @param context 弹幕所在的视图上下文，比如Activity，如果Context是[LifecycleOwner]，Helper将自动绑定生命周期
 * @param danmuView 三个可用子类：DanmakuSurfaceView、DanmakuTextureView、DanmakuView
 * @author guoyixiong
 */
open class BaseDanmuHelper(
    protected val context: Context,
    protected val danmuView: IDanmakuView
) {

    protected val tag = "DanmuHelper"

    protected val helperScope: CoroutineScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext = SupervisorJob()
    }

    protected lateinit var danmuContext: DanmakuContext
    private lateinit var remoteImageLoader: RemoteChatHeadLoader
    private lateinit var parser: BaseDanmakuParser

    private var setup: Boolean = false
    private var destroyed: Boolean = false

    init {
        //helper感知lifecycle
        if (context is LifecycleOwner) {
            context.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    super.onStart(owner)
                    resume()
                }

                override fun onPause(owner: LifecycleOwner) {
                    super.onPause(owner)
                    pause()
                }

                override fun onDestroy(owner: LifecycleOwner) {
                    super.onDestroy(owner)
                    destroy()
                }
            })
        }
    }

    /**
     * 开始配置弹幕
     */
    fun setupDanmuView() {
        if (setup) {
            Log.e(tag, "重复调用 setupDanmuView")
            return
        } else {
            setup = true
        }

        // DanmakuView

        // 设置最大显示行数
        val maxLinesPair = HashMap<Int, Int>()
        maxLinesPair[BaseDanmaku.TYPE_SCROLL_RL] = 3 // 滚动弹幕最大显示5行

        // 设置是否禁止重叠
        val overlappingEnablePair = HashMap<Int, Boolean>()
        overlappingEnablePair[BaseDanmaku.TYPE_SCROLL_RL] = true
        overlappingEnablePair[BaseDanmaku.TYPE_FIX_TOP] = true

        danmuContext = DanmakuContext.create()

        remoteImageLoader = RemoteChatHeadLoader(context = context) { dan, result ->
            if (result) {
                danmuView.invalidateDanmaku(dan.danmu, false)
            }
        }

        danmuContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_NONE)
            .setDuplicateMergingEnabled(false)
            .setScrollSpeedFactor(1.0f)
            .setScaleTextSize(1.0f)
            .setCacheStuffer(
                ChatHeadCacheStuffer(),
                ChatHeadStufferProxy(remoteImageLoader)
            )
            .setMaximumLines(maxLinesPair)
            .preventOverlapping(overlappingEnablePair).setDanmakuMargin(40)

        parser = createParser()

        danmuView.setCallback(object : DrawHandler.Callback {
            override fun updateTimer(timer: DanmakuTimer) {
                danmuViewCallbacks.forEach {
                    it.updateTimer(timer)
                }
            }

            override fun drawingFinished() {
                danmuViewCallbacks.forEach {
                    it.drawingFinished()
                }
            }

            override fun danmakuShown(danmaku: BaseDanmaku) {
                danmuViewCallbacks.forEach {
                    it.danmakuShown(danmaku)
                }
            }

            override fun prepared() {
                danmuView.start()
                danmuViewCallbacks.forEach {
                    it.prepared()
                }
            }
        })
//        mDanmakuView.onDanmakuClickListener = object : OnDanmakuClickListener {
//            override fun onDanmakuClick(danmakus: IDanmakus): Boolean {
//                return false
//            }
//
//            override fun onDanmakuLongClick(danmakus: IDanmakus): Boolean {
//                return false
//            }
//
//            override fun onViewClick(view: IDanmakuView): Boolean {
//                return false
//            }
//        }
        danmuView.prepare(parser, danmuContext)
        danmuView.showFPS(true)
        danmuView.enableDanmakuDrawingCache(true)

        onSetupDanmuView()
    }

    private val danmuViewCallbacks: CopyOnWriteArrayList<DrawHandler.Callback> =
        CopyOnWriteArrayList<DrawHandler.Callback>()

    protected fun addDanmuViewCallback(c: DrawHandler.Callback) {
        danmuViewCallbacks.add(c)
    }

    protected fun removeDanmuViewCallback(c: DrawHandler.Callback) {
        danmuViewCallbacks.remove(c)
    }

    /**
     * 子类配置弹幕View
     */
    protected open fun onSetupDanmuView() {

    }

    fun addDanmu(danmu: BaseDanmaku) {
        helperValid()
        if (!setup) {
            setupDanmuView()
        }
        danmuView.addDanmaku(danmu)
    }

    /**
     * 创建解析器
     */
    protected open fun createParser(): BaseDanmakuParser {
        return object : BaseDanmakuParser() {
            override fun parse(): Danmakus {
                return Danmakus()
            }
        }
    }

    /**
     * 暂停
     */
    fun pause() {
        helperValid()
        if (setup && danmuView.isPrepared) {
            danmuView.pause()
        }
        onPause()
    }

    /**
     * 恢复
     */
    fun resume() {
        helperValid()
        if (setup && danmuView.isPrepared) {
            danmuView.resume()
        }
        onResume()
    }

    /**
     * 结束和销毁
     */
    fun destroy() {
        destroyed = true
        helperScope.cancel()
        danmuView.release()
        remoteImageLoader.cancelAll()
        onDestroy()
    }

    protected open fun onPause() {

    }


    protected open fun onResume() {

    }

    protected open fun onDestroy() {

    }

    protected fun helperValid() {
        if (destroyed) {
            throw IllegalStateException("使用一个已被销毁的Helper")
        }
    }
}