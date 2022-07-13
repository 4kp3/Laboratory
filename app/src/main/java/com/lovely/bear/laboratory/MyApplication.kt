package com.lovely.bear.laboratory

import android.app.Application

/**
 *
 * @author guoyixiong
 */
class MyApplication : Application() {

    companion object {
        lateinit var APP: Application
    }

    override fun onCreate() {
        super.onCreate()
        APP = this
    }

}