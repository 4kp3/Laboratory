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
import com.lovely.bear.laboratory.R
import com.lovely.bear.laboratory.dan.mu.head.ChatType
import com.lovely.bear.laboratory.dan.mu.head.RemoteChatHeadDan
import com.lovely.bear.laboratory.util.dpToPx
import com.lovely.bear.laboratory.util.getTextSizeByHeight
import com.lovely.bear.laboratory.dan.mu.head.ChatHeadCacheStuffer
import com.lovely.bear.laboratory.dan.mu.head.ChatHeadStufferProxy
import com.lovely.bear.laboratory.dan.mu.head.RemoteChatHeadLoader
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
import java.io.InputStream
import java.util.*

class DanmuActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var mDanmakuView: IDanmakuView
    private lateinit var mMediaController: View
    private lateinit var mBtnRotate: Button
    private lateinit var mBtnHideDanmaku: Button
    private lateinit var mBtnShowDanmaku: Button
    private lateinit var mParser: BaseDanmakuParser
    private lateinit var mBtnPauseDanmaku: Button
    private lateinit var mBtnResumeDanmaku: Button
    private lateinit var mBtnSendDanmaku: Button
    private lateinit var mBtnSendDanmakuTextAndImage: Button
    private lateinit var mBtnSendDanmakus: Button
    private lateinit var mContext: DanmakuContext
    private lateinit var mRemoteImageLoader: RemoteChatHeadLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_danmu)
        findViews()
    }

    private fun findViews() {
        mMediaController = findViewById(R.id.media_controller)
        mBtnRotate = findViewById(R.id.rotate)
        mBtnHideDanmaku = findViewById(R.id.btn_hide)
        mBtnShowDanmaku = findViewById(R.id.btn_show)
        mBtnPauseDanmaku = findViewById(R.id.btn_pause)
        mBtnResumeDanmaku = findViewById(R.id.btn_resume)
        mBtnSendDanmaku = findViewById(R.id.btn_send)
        mBtnSendDanmakuTextAndImage = findViewById(R.id.btn_send_image_text)
        mBtnSendDanmakus = findViewById(R.id.btn_send_danmakus)

        mBtnRotate.setOnClickListener(this)
        mBtnHideDanmaku.setOnClickListener(this)
        mMediaController.setOnClickListener(this)
        mBtnShowDanmaku.setOnClickListener(this)
        mBtnPauseDanmaku.setOnClickListener(this)
        mBtnResumeDanmaku.setOnClickListener(this)
        mBtnSendDanmaku.setOnClickListener(this)
        mBtnSendDanmakuTextAndImage.setOnClickListener(this)
        mBtnSendDanmakus.setOnClickListener(this)

        // VideoView
        val mVideoView = findViewById<View>(R.id.videoview) as VideoView
        // DanmakuView

        // 设置最大显示行数
        val maxLinesPair = HashMap<Int, Int>()
        maxLinesPair[BaseDanmaku.TYPE_SCROLL_RL] = 3 // 滚动弹幕最大显示5行
        // 设置是否禁止重叠
        val overlappingEnablePair = HashMap<Int, Boolean>()
        overlappingEnablePair[BaseDanmaku.TYPE_SCROLL_RL] = true
        overlappingEnablePair[BaseDanmaku.TYPE_FIX_TOP] = true
        mDanmakuView = findViewById<View>(R.id.sv_danmaku) as IDanmakuView
        mContext = DanmakuContext.create()
        mRemoteImageLoader = RemoteChatHeadLoader(context = this) { dan, result ->
            if (result) {
                mDanmakuView.invalidateDanmaku(dan.danmu, false)
            }
        }
        mContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_NONE)
            .setDuplicateMergingEnabled(false).setScrollSpeedFactor(1.2f).setScaleTextSize(1.2f)
            .setCacheStuffer(
                ChatHeadCacheStuffer(),
                ChatHeadStufferProxy(mRemoteImageLoader)
            ) // 图文混排使用SpannedCacheStuffer
            //        .setCacheStuffer(new BackgroundCacheStuffer())  // 绘制背景使用BackgroundCacheStuffer
            .setMaximumLines(maxLinesPair)
            .preventOverlapping(overlappingEnablePair).setDanmakuMargin(40)

//        mParser = createParser(this.resources.openRawResource(R.raw.comments))
        mParser = createParser(null)
        mDanmakuView.setCallback(object : DrawHandler.Callback {
            override fun updateTimer(timer: DanmakuTimer) {}
            override fun drawingFinished() {}
            override fun danmakuShown(danmaku: BaseDanmaku) {
//                    Log.d("DFM", "danmakuShown(): text=" + danmaku.text);
            }

            override fun prepared() {
                mDanmakuView.start()
            }
        })
        mDanmakuView.onDanmakuClickListener = object : OnDanmakuClickListener {
            override fun onDanmakuClick(danmakus: IDanmakus): Boolean {
                Log.d("DFM", "onDanmakuClick: danmakus size:" + danmakus.size())
                val latest = danmakus.last()
                if (null != latest) {
                    Log.d("DFM", "onDanmakuClick: text of latest danmaku:" + latest.text)
                    return true
                }
                return false
            }

            override fun onDanmakuLongClick(danmakus: IDanmakus): Boolean {
                return false
            }

            override fun onViewClick(view: IDanmakuView): Boolean {
                mMediaController.visibility = View.VISIBLE
                return false
            }
        }
        mDanmakuView.prepare(mParser, mContext)
        mDanmakuView.showFPS(true)
        mDanmakuView.enableDanmakuDrawingCache(true)
        mVideoView.setOnPreparedListener { mediaPlayer -> mediaPlayer.start() }
        //mVideoView.setVideoPath(Environment.getExternalStorageDirectory().toString() + "/1.flv")

        //如果使用了padding，缩放开启后会影响预期效果
        mContext.setScaleTextSize(1F)
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
            // dont forget release!
            mDanmakuView.release()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (this::mDanmakuView.isInitialized) {
            // dont forget release!
            mDanmakuView.release()
            //mDanmakuView = null
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onClick(v: View) {
        if (v === mMediaController) {
            mMediaController.visibility = View.GONE
        }
        if (mDanmakuView == null || !mDanmakuView.isPrepared) return
        if (v === mBtnRotate) {
            requestedOrientation =
                if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) ActivityInfo.SCREEN_ORIENTATION_PORTRAIT else ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else if (v === mBtnHideDanmaku) {
            mDanmakuView.hide()
            // mPausedPosition = mDanmakuView.hideAndPauseDrawTask();
        } else if (v === mBtnShowDanmaku) {
            mDanmakuView.show()
            // mDanmakuView.showAndResumeDrawTask(mPausedPosition); // sync to the video time in your practice
        } else if (v === mBtnPauseDanmaku) {
            mDanmakuView.pause()
        } else if (v === mBtnResumeDanmaku) {
            mDanmakuView.resume()
        } else if (v === mBtnSendDanmaku) {
            addDanmaku(false)
        } else if (v === mBtnSendDanmakuTextAndImage) {
            addDanmaKuShowTextAndImage(false)
        } else if (v === mBtnSendDanmakus) {
            val b = mBtnSendDanmakus.tag as Boolean
            timer.cancel()
            if (b == null || !b) {
                mBtnSendDanmakus.setText(R.string.cancel_sending_danmakus)
                timer = Timer()
                timer.schedule(AsyncAddTask(), 0, 1000)
                mBtnSendDanmakus.tag = true
            } else {
                mBtnSendDanmakus.setText(R.string.send_danmakus)
                mBtnSendDanmakus.tag = false
            }
        }
    }

    var timer = Timer()

    internal inner class AsyncAddTask : TimerTask() {
        override fun run() {
            for (i in 0..19) {
                addDanmaku(true)
                SystemClock.sleep(20)
            }
        }
    }

    private lateinit var defaultIconBitmap: Bitmap

    private fun addDanmaku(islive: Boolean) {
        //val danmaku = mContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL) ?: return

        if (!this::defaultIconBitmap.isInitialized) {
            defaultIconBitmap = BitmapFactory.decodeResource(resources, R.drawable.img)
        }
        val image = RemoteChatHeadDan(
            "https://photo.16pic.com/00/90/18/16pic_9018257_b.jpg",
            defaultIconBitmap,
            ChatType.NORMAL,
            resources,
        )
        val danmaku = R2LImageDanmu(
            image,
            mContext.mDanmakuFactory.MAX_Duration_Scroll_Danmaku,
        )
        image.danmu = danmaku

        val textHeight = dpToPx(12F, applicationContext) * 1F
        danmaku.size.apply {
            paddingStart = dpToPx(40F, applicationContext)
            paddingEnd = dpToPx(14.5F, applicationContext)
            setVerticalPadding(dpToPx(10F, applicationContext))
        }

        danmaku.text = "这是一条弹幕" + System.nanoTime()
        danmaku.priority = 0 // 可能会被各种过滤器过滤并隐藏显示
        danmaku.isLive = islive
        danmaku.time = mDanmakuView.currentTime + 1200
        danmaku.textSize = getTextSizeByHeight(textHeight)
        danmaku.textColor = Color.BLACK
        //danmaku.textShadowColor = Color.WHITE
        // danmaku.underlineColor = Color.GREEN;
        danmaku.borderColor = Color.GREEN
        mDanmakuView.addDanmaku(danmaku)
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