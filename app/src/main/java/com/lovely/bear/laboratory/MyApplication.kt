package com.lovely.bear.laboratory

import android.app.Application
import android.os.Debug
import com.lovely.bear.laboratory.performance.DebugTracing
import java.io.File

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

        DebugTracing.startApp2MainActivity()

        resources.javaClass.classLoader
        assets

        applicationInfo.sourceDir
    }

}