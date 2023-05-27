package com.lovely.bear.laboratory.launch

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker.InputMode
import com.lovely.bear.laboratory.R
import com.lovely.bear.laboratory.util.toast
import retrofit2.http.Body
import retrofit2.http.GET

open class BaseLaunchTestActivity : AppCompatActivity() {

    val tvTip: TextView by lazy {
        findViewById<TextView>(R.id.tv_tip)
    }


    @GET("")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch_test_base)
        supportActionBar?.title = this::class.simpleName
        //子类设置到 fl 中
        showAll("onCreate")
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        showAll("onNewIntent")
    }

    private fun showAll(text: String) {
        toast(text)
        showText(text)
    }

    private fun showText(text: String) {
        tvTip.text = text
        tvTip.postDelayed(Runnable {
            tvTip.text = "测试"
        }, 1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        toast("onDestroy")
    }
}