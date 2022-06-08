package com.lovely.bear.laboratory.dan.mu

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.style.BackgroundColorSpan
import android.text.style.ImageSpan
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.lovely.bear.laboratory.dan.mu.icon.chat.head.*
import com.lovely.bear.laboratory.dan.mu.icon.image.R2LImageDanmu
import com.lovely.bear.laboratory.dpToPx
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
import master.flame.danmaku.danmaku.model.android.SpannedCacheStuffer
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

//    private val mCacheStufferAdapter: BaseCacheStuffer.Proxy = object : BaseCacheStuffer.Proxy() {
//        private var mDrawable: Drawable? = null
//        override fun prepareDrawing(danmaku: BaseDanmaku, fromWorkerThread: Boolean) {
//            if (danmaku.text is Spanned) { // 根据你的条件检查是否需要需要更新弹幕
//                // FIXME 这里只是简单启个线程来加载远程url图片，请使用你自己的异步线程池，最好加上你的缓存池
//                object : Thread() {
//                    override fun run() {
//                        val url = "http://www.bilibili.com/favicon.ico"
//                        var inputStream: InputStream? = null
//                        var drawable = mDrawable
//                        if (drawable == null) {
//                            try {
//                                val urlConnection = URL(url).openConnection()
//                                inputStream = urlConnection.getInputStream()
//                                drawable = BitmapDrawable.createFromStream(inputStream, "bitmap")
//                                mDrawable = drawable
//                            } catch (e: MalformedURLException) {
//                                e.printStackTrace()
//                            } catch (e: IOException) {
//                                e.printStackTrace()
//                            } finally {
//                                IOUtils.closeQuietly(inputStream)
//                            }
//                        }
//                        if (drawable != null) {
//                            drawable.setBounds(0, 0, 100, 100)
//                            val spannable = createSpannable(drawable)
//                            danmaku.text = spannable
//                            if (mDanmakuView != null) {
//                                mDanmakuView!!.invalidateDanmaku(danmaku, false)
//                            }
//                            return
//                        }
//                    }
//                }.start()
//            }
//        }
//
//        override fun releaseResource(danmaku: BaseDanmaku) {
//            // TODO 重要:清理含有ImageSpan的text中的一些占用内存的资源 例如drawable
//        }
//    }

    /**
     * 绘制背景(自定义弹幕样式)
     */
    private class BackgroundCacheStuffer : SpannedCacheStuffer() {
        // 通过扩展SimpleTextCacheStuffer或SpannedCacheStuffer个性化你的弹幕样式
        val paint = Paint()
        override fun measure(danmaku: BaseDanmaku, paint: TextPaint, fromWorkerThread: Boolean) {
            danmaku.padding = 10 // 在背景绘制模式下增加padding
            super.measure(danmaku, paint, fromWorkerThread)
        }

        public override fun drawBackground(
            danmaku: BaseDanmaku,
            canvas: Canvas,
            left: Float,
            top: Float
        ) {
            paint.color = -0x7edacf65
            canvas.drawRect(
                left + 2,
                top + 2,
                left + danmaku.paintWidth - 2,
                top + danmaku.paintHeight - 2,
                paint
            )
        }

        override fun drawStroke(
            danmaku: BaseDanmaku,
            lineText: String,
            canvas: Canvas,
            left: Float,
            top: Float,
            paint: Paint
        ) {
            // 禁用描边绘制
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_danmu)
        findViews()
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
        //mParser = createParser(this.resources.openRawResource(R.raw.comments))
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
        mDanmakuView.enableDanmakuDrawingCache(false)
        mVideoView.setOnPreparedListener { mediaPlayer -> mediaPlayer.start() }
        mVideoView.setVideoPath(Environment.getExternalStorageDirectory().toString() + "/1.flv")
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

        val danmaku = R2LImageDanmu(
            ChatHeadDan(defaultIconBitmap, ChatType.EMPLOYEE, resources),
            dpToPx(40F, this),
            dpToPx(10F, this),
            dpToPx(14.5F, this),
            dpToPx(10F, this),
            mContext.mDanmakuFactory.MAX_Duration_Scroll_Danmaku,
        )

        danmaku.text = "这是一条弹幕" + System.nanoTime()
        //danmaku.paddingEnd =
        //danmaku.padding = 5
        danmaku.priority = 0 // 可能会被各种过滤器过滤并隐藏显示
        danmaku.isLive = islive
        danmaku.time = mDanmakuView.currentTime + 1200
        danmaku.textSize = 60F
        danmaku.textColor = Color.WHITE
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
        danmaku.padding = 5
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