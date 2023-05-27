package com.lovely.bear.laboratory.performance

import android.os.Debug
import android.util.Log
import com.lovely.bear.laboratory.MyApplication
import java.lang.reflect.Method

object DebugTracing {

    private val TAG = "DebugTracing"

    private var enable = false

    /**
     * 启用 App 自定义 Tag，也可以使用命令行参数 -a
     * 反射修改的意义在于，手机内录跟踪数据时可以录到自定义tag
     */
    fun enableCustomTraceTag() {
        try {
            val trace = Class.forName("android.os.Trace")
            val setAppTracingAllowed: Method =
                trace.getDeclaredMethod("setAppTracingAllowed", Boolean::class.javaPrimitiveType)
            setAppTracingAllowed.invoke(null, true)
        } catch (e: Exception) {
            Log.e("DebugTracing","开启自定义Tag失败")
        }
    }

    fun startApp2MainActivity() {

        if (!enable) return

        val dir = MyApplication.APP.externalCacheDir
        if (dir == null) {
            Log.d(TAG, "无目录")
            return
        }
        val path = dir.absolutePath + "/app_2_mainActivity"
        Log.d(TAG, "路径：$path")
        Debug.startMethodTracing(path)
    }

    fun stopMethodTracing() {
        if (!enable) return
        Debug.stopMethodTracing()
    }

    fun dumpNow(tag:String?) {
        if (!enable) return

        val dir = MyApplication.APP.externalCacheDir
        if (dir == null) {
            Log.d(TAG, "无目录")
            return
        }
        val fileName = "dump_hprof_${System.currentTimeMillis()}${tag?.let { "_$tag" } ?: ""}.hprof"
        val file = dir.absolutePath + "/$fileName"
        Log.d(TAG, "堆转储文件：$file")
        Debug.dumpHprofData(file)
    }
}