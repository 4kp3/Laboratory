package com.lovely.bear.laboratory.dan.mu.head

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable

import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 远程头像加载器
 * @param  loadCallback 图像加载结果的通知
 * @param context 使用了Glide加载，需要传递上下文
 */
class RemoteChatHeadLoader(
    parentJob: Job? = null,
    context: Context,
    private val loadCallback: ((RemoteChatHeadDan, Boolean) -> Unit)? = null,
) {

    private val appContext = context.applicationContext

    private val scope: CoroutineScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext
            get() = SupervisorJob(parentJob) + Dispatchers.IO
    }

    fun load(dan: RemoteChatHeadDan) {
        scope.launch {
            val bitmap = try {
                kotlinx.coroutines.suspendCancellableCoroutine<Bitmap> {
                    val target = BitmapTarget(dan, it)
                    Glide.with(appContext).asBitmap().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .load(dan.url).into(target)
                    it.invokeOnCancellation {
                        Glide.with(appContext).clear(target)
                    }
                }
            } catch (e: Exception) {
                //Log.d(tag, "远程头像加载失败${dan.url}，e=$e")
                loadCallback?.invoke(dan, false)
                return@launch
            }
            Log.e(tag, "远程头像加载成功${dan.url}")
            dan.remoteBitmap = bitmap
            loadCallback?.invoke(dan, true)
        }
    }

    fun cancelAll() {
        scope.cancel()
    }

    companion object {
        private const val tag = "RemoteChatHeadLoader"
    }

    inner class BitmapTarget(
        private val dan: RemoteChatHeadDan,
        private val continuation: Continuation<Bitmap>
    ) : Target<Bitmap> {
        var r: Request? = null

        override fun onStart() {
        }

        override fun onStop() {
        }

        override fun onDestroy() {
        }

        override fun onLoadStarted(placeholder: Drawable?) {
        }

        override fun onLoadFailed(errorDrawable: Drawable?) {
            continuation.resumeWithException(LoadFailedException())
        }

        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            continuation.resume(resource)
        }

        override fun onLoadCleared(placeholder: Drawable?) {
        }

        override fun getSize(cb: SizeReadyCallback) {
            cb.onSizeReady(dan.drawableWidth, dan.drawableHeight)
        }

        override fun removeCallback(cb: SizeReadyCallback) {
        }

        override fun setRequest(request: Request?) {
            r = request
        }

        override fun getRequest(): Request? {
            return r
        }
    }

    class LoadFailedException : Exception("glide 图片加载失败")
}
