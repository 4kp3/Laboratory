package com.lovely.bear.laboratory.dan.mu.icon

import android.content.Context
import android.graphics.Bitmap

import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resumeWithException

/**
 * 远程头像加载器
 * @param  loadCallback 图像加载结果的通知
 * @param context 使用了Glide加载，需要传递上下文
 */
class RemoteChatHeadLoader(
    parentJob: Job? = null,
    private val loadCallback: ((RemoteChatHeadDan, Boolean) -> Unit)? = null,
    context: Context
) {

    private val appContext = context.applicationContext

    private val scope: CoroutineScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext
            get() = SupervisorJob(parentJob) + Dispatchers.IO
    }

    fun load(dan: RemoteChatHeadDan) {
        scope.launch {
            val future =
                Glide.with(appContext).asBitmap().diskCacheStrategy(DiskCacheStrategy.NONE)
                    .load(dan.url).submit(dan.width, dan.height)
            val bitmap = kotlinx.coroutines.suspendCancellableCoroutine<Bitmap> {
                it.invokeOnCancellation { future.cancel(true) }
                try {
                    it.resumeWith(Result.success(future.get()))
                } catch (e: Exception) {
                    it.resumeWithException(e)
                    Log.e(tag, "远程头像加载失败${dan.url}，e=$e")
                    loadCallback?.invoke(dan, false)
                }
            }
            dan.remoteBitmap = bitmap
            yield()
            loadCallback?.invoke(dan, true)
        }
    }

    fun cancelAll() {
        scope.cancel()
    }

    companion object {
        private const val tag = "RemoteChatHeadLoader"
    }
}
