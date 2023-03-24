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
import com.lovely.bear.laboratory.R
import com.lovely.bear.laboratory.util.clip
import com.lovely.bear.laboratory.dan.mu.head.*
import com.lovely.bear.laboratory.util.dpToPx
import com.lovely.bear.laboratory.util.getTextSizeByHeight
import kotlinx.coroutines.cancel
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
    private lateinit var mBtnPauseDanmaku: Button
    private lateinit var mBtnResumeDanmaku: Button
    private lateinit var mBtnSendDanmaku: Button
    private lateinit var mBtnSendDanmakuTextAndImage: Button
    private lateinit var mBtnAutoSendDanmaku: Button

    private lateinit var helper: ChatHeadDanmuHelper


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
        helper = ChatHeadDanmuHelper(this, mDanmakuView)
        helper.setupDanmuView()
    }

    override fun onClick(v: View) {
//        if (v === mMediaController) {
//            mMediaController.visibility = View.GONE
//        }
        if (!this::mDanmakuView.isInitialized || !mDanmakuView.isPrepared) return
        else if (v === mBtnPauseDanmaku) {
            helper.pause()
        } else if (v === mBtnResumeDanmaku) {
            helper.resume()
        } else if (v === mBtnSendDanmaku) {
            addDanmaku()
        } else if (v === mBtnSendDanmakuTextAndImage) {
            //addDanmaKuShowTextAndImage(false)
        } else if (v === mBtnAutoSendDanmaku) {
            autoSend()
        }
    }

    private fun autoSend() {
        val data= List(30,){
            helper.getADanmu()
        }
        helper.loopWith(data)
    }

    private fun addDanmaku() {
        helper.addDanmaku()
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

//    private fun addDanmaKuShowTextAndImage(islive: Boolean) {
//        val danmaku = mContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL)
//        val drawable = resources.getDrawable(R.drawable.ic_launcher_foreground)
//        drawable.setBounds(0, 0, 100, 100)
//        val spannable = createSpannable(drawable)
//        danmaku.text = spannable
//        //danmaku.padding = 5
//        danmaku.priority = 1 // 一定会显示, 一般用于本机发送的弹幕
//        danmaku.isLive = islive
//        danmaku.time = mDanmakuView.currentTime + 1200
//        danmaku.textSize = 25f * (mParser.displayer.density - 0.6f)
//        danmaku.textColor = Color.RED
//        danmaku.textShadowColor = 0 // 重要：如果有图文混排，最好不要设置描边(设textShadowColor=0)，否则会进行两次复杂的绘制导致运行效率降低
//        danmaku.underlineColor = Color.GREEN
//        mDanmakuView.addDanmaku(danmaku)
//    }

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