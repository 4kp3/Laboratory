package com.lovely.bear.laboratory.start.startup

import android.content.Context
import android.util.Log
import androidx.startup.Initializer

private const val TAG = "ExampleLoggerInitializer"
class ExampleLoggerInitializer:Initializer<Int> {

    override fun create(context: Context): Int {
//        Log.d(TAG,"模拟耗时初始化任务，休眠3s")
//        Thread.sleep(1000*3)
        return 1
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf()
    }
}