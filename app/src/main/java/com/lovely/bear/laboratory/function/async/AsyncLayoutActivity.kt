package com.lovely.bear.laboratory.function.async

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import androidx.core.view.WindowCompat
import com.lovely.bear.laboratory.R
import java.lang.ref.WeakReference


class AsyncLayoutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        setContentViewWithAsyncContentView()
    }

    private fun setContentViewWithAsyncContentView() {
        // todo 如果未填充好，为了避免空白页面，在等待间隙可以显示 loading view
        val contentView = getContentViewOrWait(this) ?: return
        setContentView(contentView)
    }

    companion object {

        const val TAG = "AsyncLayoutActivity"

        private val lock = Any()

        // must access in UI Thread
        private var viewRef: WeakReference<View>? = null
        private var activityRef: WeakReference<AsyncLayoutActivity>? = null

        private fun getContentViewOrWait(activity: AsyncLayoutActivity): View? {
            Log.d(TAG, "getContentViewOrWait")
            val view = viewRef?.get()
            return if (view == null) {
                Log.d(TAG, "getContentViewOrWait 尚未准备好")
                activityRef = WeakReference(activity)
                null
            } else {
                Log.d(TAG, "getContentViewOrWait 获得View")
                viewRef = null
                activityRef = null
                view
            }
        }

        internal fun preloadContentView(context: Context) {
            Log.d(TAG, "preloadContentView 开始填充View")
            val parentViewGroup = FrameLayout(context)
            val layoutId = R.layout.activity_async_inflater
            AsyncLayoutInflater(context).inflate(
                layoutId, parentViewGroup
            ) { view, _, _ ->
                Log.d(TAG, "preloadContentView 填充View完成")
                viewRef = WeakReference(view)
                activityRef?.get()?.setContentViewWithAsyncContentView()
            }
        }
    }
}