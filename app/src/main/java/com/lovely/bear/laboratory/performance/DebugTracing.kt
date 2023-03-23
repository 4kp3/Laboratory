package com.lovely.bear.laboratory.performance

import android.os.Debug
import android.util.Log
import com.lovely.bear.laboratory.MyApplication

object DebugTracing {

    private val TAG="DebugTracing"

    fun startApp2MainActivity() {
        val dir = MyApplication.APP.externalCacheDir
        if (dir == null) {
            Log.d(TAG,"无目录")
            return
        }
        val path=dir.absolutePath+"/app_2_mainActivity"
        Log.d(TAG,"路径：$path")
        Debug.startMethodTracing(path)
    }

    fun stopMethodTracing() {
        Debug.stopMethodTracing()
    }

    fun dumpNow(tag:String?) {
        val dir = MyApplication.APP.externalCacheDir
        if (dir == null) {
            Log.d(TAG,"无目录")
            return
        }
        val fileName="dump_hprof_${System.currentTimeMillis()}${tag?.let { "_$tag" }?:""}.hprof"
        val file=dir.absolutePath+"/$fileName"
        Log.d(TAG,"堆转储文件：$file")
        Debug.dumpHprofData(file)
    }
}