package com.lovely.bear.laboratory.main

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.ui.AppBarConfiguration
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivitySecondBinding
import com.google.android.material.snackbar.Snackbar
import com.lovely.bear.laboratory.util.toast
import leakcanary.AppWatcher

class SecondActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivitySecondBinding

    private val mH = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == MSG_WHAT_SHOW) {
                toast("Good morning!加油前进！")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)


//        val navController = findNavController(R.id.nav_host_fragment_content_second)
//        appBarConfiguration = AppBarConfiguration(navController.graph)
//        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAnchorView(R.id.fab)
                .setAction("Action", null).show()
        }

        binding.tvMakeMemoryLeak.setOnClickListener {
            mH.sendMessageDelayed(Message.obtain(mH, MSG_WHAT_SHOW), 5000)
        }

        binding.tvMakeMemoryLeakByStaticContext.setOnClickListener {
            if (leakContext == null) {
                leakContext = this
            } else {
                leakContext = null
                toast("静态 Context 已清空")
            }
        }

        binding.tvMakeMemoryLeakByStaticViews.setOnClickListener {
            if (leakView == null) {
                leakView = it
            } else {
                leakView = null
                toast("静态 View 已清空")
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
//        val navController = findNavController(R.id.nav_host_fragment_content_second)
//        return navController.navigateUp(appBarConfiguration)
//                || super.onSupportNavigateUp()
        return false
    }

    companion object {
        const val MSG_WHAT_SHOW = 1

        private var leakContext: Context? = null
        private var leakView: View? = null
    }
}