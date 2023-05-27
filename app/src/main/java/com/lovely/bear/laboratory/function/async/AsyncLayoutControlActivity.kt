package com.lovely.bear.laboratory.function.async

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.lovely.bear.laboratory.R

class AsyncLayoutControlActivity : AppCompatActivity() {

    //private lateinit var binding: ActivityMain2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        //binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(R.layout.activity_async_inflater)
    }

}