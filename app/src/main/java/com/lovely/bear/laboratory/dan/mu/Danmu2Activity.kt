package com.lovely.bear.laboratory.dan.mu

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.text.style.ImageSpan
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenResumed
import com.example.myapplication.R
import com.lovely.bear.laboratory.clip
import com.lovely.bear.laboratory.dan.mu.head.*
import com.lovely.bear.laboratory.dpToPx
import com.lovely.bear.laboratory.getTextSizeByHeight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import master.flame.danmaku.controller.DrawHandler
import master.flame.danmaku.controller.IDanmakuView
import master.flame.danmaku.controller.IDanmakuView.OnDanmakuClickListener
import master.flame.danmaku.danmaku.loader.IllegalDataException
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.DanmakuTimer
import master.flame.danmaku.danmaku.model.IDanmakus
import master.flame.danmaku.danmaku.model.IDisplayer
import master.flame.danmaku.danmaku.model.android.DanmakuContext
import master.flame.danmaku.danmaku.model.android.Danmakus
import master.flame.danmaku.danmaku.model.image.R2LImageDanmu
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import master.flame.danmaku.danmaku.util.SystemClock
import master.flame.danmaku.ui.widget.DanmakuView
import java.io.InputStream
import java.util.*
import kotlin.math.min

class Danmu2Activity : AppCompatActivity(), View.OnClickListener {
    private lateinit var mDanmakuView: IDanmakuView
    private lateinit var mMediaController: View
    private lateinit var mParser: BaseDanmakuParser
    private lateinit var mBtnPauseDanmaku: Button
    private lateinit var mBtnResumeDanmaku: Button
    private lateinit var mBtnSendDanmaku: Button
    private lateinit var mBtnSendDanmakuTextAndImage: Button
    private lateinit var mBtnAutoSendDanmaku: Button
    private lateinit var mContext: DanmakuContext
    private lateinit var mRemoteImageLoader: RemoteChatHeadLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_danmu2)
        findViews()
        setDanmuView()
    }

    private fun findViews() {
        mMediaController = findViewById(R.id.media_controller)
        mBtnPauseDanmaku = findViewById(R.id.btn_pause)
        mBtnResumeDanmaku = findViewById(R.id.btn_resume)
        mBtnSendDanmaku = findViewById(R.id.btn_send)
        mBtnSendDanmakuTextAndImage = findViewById(R.id.btn_send_image_text)
        mBtnSendDanmakuTextAndImage = findViewById(R.id.btn_send_image_text)
        mBtnAutoSendDanmaku = findViewById(R.id.btn_auto_send)

        mDanmakuView = findViewById<DanmakuView>(R.id.v_danmaku)

        mMediaController.setOnClickListener(this)
        mBtnPauseDanmaku.setOnClickListener(this)
        mBtnResumeDanmaku.setOnClickListener(this)
        mBtnSendDanmaku.setOnClickListener(this)
        mBtnSendDanmakuTextAndImage.setOnClickListener(this)
        mBtnAutoSendDanmaku.setOnClickListener(this)
    }

    private fun setDanmuView() {
        // DanmakuView

        // 设置最大显示行数
        val maxLinesPair = HashMap<Int, Int>()
        maxLinesPair[BaseDanmaku.TYPE_SCROLL_RL] = 3 // 滚动弹幕最大显示5行

        // 设置是否禁止重叠
        val overlappingEnablePair = HashMap<Int, Boolean>()
        overlappingEnablePair[BaseDanmaku.TYPE_SCROLL_RL] = true
        overlappingEnablePair[BaseDanmaku.TYPE_FIX_TOP] = true

        mContext = DanmakuContext.create()

        mRemoteImageLoader = RemoteChatHeadLoader(context = this) { dan, result ->
            if (result) {
                mDanmakuView.invalidateDanmaku(dan.danmu, false)
            }
        }

        mContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_NONE)
            .setDuplicateMergingEnabled(false)
            .setScrollSpeedFactor(1.0f)
            .setScaleTextSize(1.0f)
            .setCacheStuffer(
                ChatHeadCacheStuffer(),
                ChatHeadStufferProxy(mRemoteImageLoader)
            )
            .setMaximumLines(maxLinesPair)
            .preventOverlapping(overlappingEnablePair).setDanmakuMargin(40)

//        mParser = createParser(this.resources.openRawResource(R.raw.comments))
        mParser = createParser(null)

        mDanmakuView.setCallback(object : DrawHandler.Callback {
            override fun updateTimer(timer: DanmakuTimer) {}
            override fun drawingFinished() {}
            override fun danmakuShown(danmaku: BaseDanmaku) {}
            override fun prepared() {
                mDanmakuView.start()
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
        mDanmakuView.prepare(mParser, mContext)
        mDanmakuView.showFPS(true)
        mDanmakuView.enableDanmakuDrawingCache(true)
    }

    private fun createParser(stream: InputStream?): BaseDanmakuParser {
        if (stream == null) {
            return object : BaseDanmakuParser() {
                override fun parse(): Danmakus {
                    return Danmakus()
                }
            }
        }
        val loader = DanmakuLoaderFactory.create(DanmakuLoaderFactory.TAG_BILI)
        try {
            loader.load(stream)
        } catch (e: IllegalDataException) {
            e.printStackTrace()
        }
        val parser: BaseDanmakuParser = BiliDanmukuParser()
        val dataSource = loader.dataSource
        parser.load(dataSource)
        return parser
    }

    override fun onPause() {
        super.onPause()
        if (this::mDanmakuView.isInitialized && mDanmakuView.isPrepared) {
            mDanmakuView.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (this::mDanmakuView.isInitialized && mDanmakuView.isPrepared && mDanmakuView.isPaused) {
            mDanmakuView.resume()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::mDanmakuView.isInitialized) {
            mDanmakuView.release()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (this::mDanmakuView.isInitialized) {
            mDanmakuView.release()
        }
    }

    override fun onClick(v: View) {
//        if (v === mMediaController) {
//            mMediaController.visibility = View.GONE
//        }
        if (!this::mDanmakuView.isInitialized || !mDanmakuView.isPrepared) return

        else if (v === mBtnPauseDanmaku) {
            mDanmakuView.pause()
        } else if (v === mBtnResumeDanmaku) {
            mDanmakuView.resume()
        } else if (v === mBtnSendDanmaku) {
            addDanmaku()
        } else if (v === mBtnSendDanmakuTextAndImage) {
            addDanmaKuShowTextAndImage(false)
        } else if (v === mBtnAutoSendDanmaku) {
            autoSend()
        }
    }

    private fun autoSend() {
        lifecycleScope.launch {
            repeat(30) {
                addDanmaku()
                delay(30)
            }
        }
    }

    private lateinit var defaultIconBitmap: Bitmap
    private val url = "https://photo.16pic.com/00/90/18/16pic_9018257_b.jpg"

    private val seed =
        "《极限竞速：地平线5》是由微软发行的一款赛车竞速游戏，也是地平线系列的最新作品。不同于正传Foza系列，" +
                "地平线系列的拟真元素要少了许多，重心更加偏向于街车与自由度方面。操作手感上也更轻量化，更加的爽" +
                "快和刺激。Xbox&Bethesda游戏展示会将于北京时间6月13日凌晨1点正式播出，" +
                "该节目将展示来自Xbox Game Studios、B社和微软Xbox在世界各地的合作伙伴的游戏作品。"

    private fun addDanmaku() {
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

        initDanmuRes()

        val danmu = createImageDanmu(url, defaultIconBitmap, clip(seed, 20, "..."), type)
        if (danmu != null)
            mDanmakuView.addDanmaku(danmu)
    }

    private fun createImageDanmu(
        avatarUrl: String?,
        defaultAvatarBitmap: Bitmap,
        text: String,
        chatType: ChatType
    ): BaseDanmaku? {

        if (text.isBlank()) return null

        val image = if (avatarUrl.isNullOrBlank()) {
            ChatHeadDan(
                defaultAvatarBitmap,
                chatType,
                resources
            )
        } else {
            RemoteChatHeadDan(
                avatarUrl,
                defaultAvatarBitmap,
                chatType,
                resources,
            )
        }
        val danmaku = R2LImageDanmu(
            image,
            mContext.mDanmakuFactory.MAX_Duration_Scroll_Danmaku,
        )
        if (image is RemoteChatHeadDan) {
            image.danmu = danmaku
        }

        val textHeight = dpToPx(12F, applicationContext) * 1F
        danmaku.size.apply {
            paddingStart = dpToPx(40F, applicationContext)
            paddingEnd = dpToPx(14.5F, applicationContext)
            setVerticalPadding(dpToPx(10F, applicationContext))
        }
        danmaku.textSize = getTextSizeByHeight(textHeight)

        danmaku.text = text
        danmaku.priority = 0 // 可能会被各种过滤器过滤并隐藏显示
        danmaku.isLive = false
        danmaku.time = mDanmakuView.currentTime + 1200
        danmaku.textColor = Color.BLACK
        danmaku.borderColor = Color.GREEN
        return danmaku
    }

    private fun initDanmuRes() {
        if (!this::defaultIconBitmap.isInitialized) {
            defaultIconBitmap = BitmapFactory.decodeResource(resources, R.drawable.img)
        }
    }

    private fun addDanmaKuShowTextAndImage(islive: Boolean) {
        val danmaku = mContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL)
        val drawable = resources.getDrawable(R.drawable.ic_launcher_foreground)
        drawable.setBounds(0, 0, 100, 100)
        val spannable = createSpannable(drawable)
        danmaku.text = spannable
        //danmaku.padding = 5
        danmaku.priority = 1 // 一定会显示, 一般用于本机发送的弹幕
        danmaku.isLive = islive
        danmaku.time = mDanmakuView.currentTime + 1200
        danmaku.textSize = 25f * (mParser.displayer.density - 0.6f)
        danmaku.textColor = Color.RED
        danmaku.textShadowColor = 0 // 重要：如果有图文混排，最好不要设置描边(设textShadowColor=0)，否则会进行两次复杂的绘制导致运行效率降低
        danmaku.underlineColor = Color.GREEN
        mDanmakuView.addDanmaku(danmaku)
    }

    private fun createSpannable(drawable: Drawable): SpannableStringBuilder {
        val text = "bitmap"
        val spannableStringBuilder = SpannableStringBuilder(text)
        val span = ImageSpan(drawable) //ImageSpan.ALIGN_BOTTOM);
        spannableStringBuilder.setSpan(span, 0, text.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        spannableStringBuilder.append("图文混排")
        spannableStringBuilder.setSpan(
            BackgroundColorSpan(Color.parseColor("#8A2233B1")),
            0,
            spannableStringBuilder.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        return spannableStringBuilder
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mDanmakuView.config.setDanmakuMargin(20)
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mDanmakuView.config.setDanmakuMargin(40)
        }
    }
}