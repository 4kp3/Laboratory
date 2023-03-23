package com.lovely.bear.laboratory.start

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.lovely.bear.laboratory.main.MainActivity
import kotlinx.coroutines.delay

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    private var startMain: Boolean = false

    override fun onResume() {
        super.onResume()
        if (!startMain) {
            startMain = true
            lifecycleScope.launchWhenResumed {
                delay(100)
                finish()
                com.lovely.bear.laboratory.util.startActivity<MainActivity>(this@SplashActivity)
            }
        }
    }
}