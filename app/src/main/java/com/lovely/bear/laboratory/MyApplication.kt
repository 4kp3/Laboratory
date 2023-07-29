package com.lovely.bear.laboratory

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.lovely.bear.laboratory.performance.DebugTracing
import com.nothing.launcher.icons.SharedApplication

/**
 *
 * @author guoyixiong
 */
class MyApplication : Application() {

    companion object {
        const val TAG = "MyApplication"
        lateinit var APP: Application
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        //DebugTracing.enableCustomTraceTag()
    }

    override fun onCreate() {
        super.onCreate()
        APP = this

        DebugTracing.startApp2MainActivity()
        SharedApplication.init(this)
    }

//    private fun checkApkSignature() {
//        val packageInfo =
//            packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
//        val sb = java.lang.StringBuilder()
//        for (sig in packageInfo.signingInfo.apkContentsSigners) {
//            //sig.
//        }
//        Log.d(TAG, sb.toString())
//    }


}