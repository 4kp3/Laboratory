/*
Copyright (C), 2022, Nothing Technology
FileName: CachedIconEntity
Author: benny.fang
Date: 2022/6/9 15:39
Description: Picked icon package instance which is resident in memory
History:
<author> <time> <version> <desc>
 */
package com.nothing.launcher.icons.model.data

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import com.android.launcher3.icons.R
import com.nothing.launcher.icons.SharedApplication
import com.nothing.launcher.icons.constant.IconPackStateConstant
import com.nothing.launcher.util.BitmapUtils
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.io.InputStream
import java.util.Locale
import java.util.Random

data class CachedIconEntity(val packageName: String, val appName: String) {

    companion object {
        private const val TAG = "CachedIconEntity"
        val systemIconEntity =
            CachedIconEntity(IconPackStateConstant.getSystemIconOwner(), "Nothing Icon")
        val themedIconEntity =
            CachedIconEntity(IconPackStateConstant.getThemedIconOwner(), "Themed Icon")
        // Added by stephen.bi for NOS-1721 @{
        val themedIconEntityNothing =
            CachedIconEntity(IconPackStateConstant.getThemedIconNothingOwner(), "Themed Icon Nothing")
        // @}
    }

    private var isLoaded = false
    // In some unknown case, the iconPackResources is null before it is initialized
    private val iconPackResources: Resources by lazy {
        SharedApplication.getContext().packageManager.getResourcesForApplication(packageName)
    }

    private val iconPackDrawables = HashMap<String, String>() //缓存应用包名与图标资源名称的对应关系
    private val involvedPackages = HashSet<String>() //图标包中映射到的应用名称集合

    // 下列几个值不一定有配置
    private val iconPackBackImages: ArrayList<Bitmap> = ArrayList() // 背景图片
    private val iconPackMaskImages: ArrayList<Bitmap> = ArrayList() // 蒙层图片
    private val iconPackFrontImages: ArrayList<Bitmap> = ArrayList() // 前景图片
    private var iconPackFactor: Float = 1.0f // 缩放值

    private val loadLock = Any() // 资源加载的同步锁

    fun getIcon(
        context: Context,
        targetComponent: ComponentName?,
        iconSize: Int,
        defaultIconFromApk: () -> Drawable?,
    ): Drawable? {
        // Maybe load the icon resource
        loadIfNeed(iconSize)

        return targetComponent?.let {
            val iconFromIconPack: Drawable?
            val componentName = targetComponent.toString()
            val matchedResName: String? = iconPackDrawables[componentName]
            if (matchedResName != null) {
                iconFromIconPack = loadDrawableFromIconPackApp(matchedResName)
            } else {
                iconFromIconPack = with(componentName) {
                    val start = this.indexOf("{") + 1
                    val end = this.indexOf("}", start)
                    if (end > start) {
                        val assembledResName =
                            componentName.substring(start, end).lowercase(Locale.getDefault())
                                .replace(".", "_")
                                .replace("/", "_")
                        loadDrawableFromIconPackApp(assembledResName)
                    } else {
                        null
                    }
                }
            }
            iconFromIconPack ?: generateBitmap(context, defaultIconFromApk(), iconSize)
        }
    }

    private fun loadIfNeed(iconSize: Int? = null) {
        // 第一次检查，检查到已加载则不往下执行，避免同步锁造成的性能损耗
        if (isLoaded) {
            return
        }
        // 检查到未加载，先请求同步锁来保证只有一条线程在执行加载资源
        synchronized(loadLock) {
            // 第二次检查
            if (isLoaded) {
                /*这里对应场景为初次检查为false，接着等待别的线程释放锁，释放后很可能是加载完了。
                拿到锁进来后要再检查加载状态，未加载的话才继续往下加载资源*/
                return
            }
            loadResource(
                iconSize
                    ?: SharedApplication.getContext().resources.getDimensionPixelSize(R.dimen.default_icon_bitmap_size)
            )
        }
    }

    private fun loadResource(iconSize: Int) {
        // load appfilter.xml from the icon pack package
        var appfilterStream: InputStream? = null
        try {
            var parser: XmlPullParser? = null
            val appFilterId = iconPackResources.getIdentifier("appfilter", "xml", packageName)
            if (appFilterId > 0) {
                parser = iconPackResources.getXml(appFilterId)
            } else {
                // no resource found, try to open it from assests folder
                try {
                    appfilterStream =
                        iconPackResources.assets.open("appfilter.xml")
                    val factory = XmlPullParserFactory.newInstance().apply {
                        isNamespaceAware = true
                    }
                    parser = factory.newPullParser().apply {
                        setInput(appfilterStream, "utf-8")
                    }
                } catch (ioException: IOException) {
                    Log.e(TAG, "No appfilter.xml file. IOException is ${ioException.message}")
                }
            }

            parser?.let {
                reset()

                var eventType: Int = it.eventType
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        when (it.name) {
                            "iconback" -> if (it.attributeCount > 0) {
                                parseNonTransparentBitmap(it, iconPackBackImages, iconSize)
                            }
                            "iconmask" -> if (it.attributeCount > 0) {
                                parseBitmap(it, iconPackMaskImages, iconSize)
                            }
                            "iconupon" -> if (it.attributeCount > 0) {
                                parseBitmap(it, iconPackFrontImages, iconSize)
                            }
                            "scale" -> if (it.attributeCount > 0 && it.getAttributeName(0)
                                    .equals("factor")
                            ) {
                                // In some icon packs, the factor isn't digit according to google reports
                                val readValue = it.getAttributeValue(0).toFloatOrNull() ?: 1.0f
                                iconPackFactor = 1.0f.coerceAtMost(readValue)
                            }
                            "item" -> {
                                var componentName: String? = null
                                var drawableName: String? = null

                                for (i in 0 until it.attributeCount) {
                                    if (it.getAttributeName(i) == "component") {
                                        componentName = it.getAttributeValue(i)
                                    } else if (it.getAttributeName(i) == "drawable") {
                                        drawableName = it.getAttributeValue(i)
                                    }
                                }
                                if (componentName != null
                                    && drawableName != null
                                    && !iconPackDrawables.containsKey(componentName)
                                ) {
                                    iconPackDrawables[componentName] = drawableName
                                    getPackageNameFromComponentNameString(componentName)?.let { involvedPackage ->
                                        involvedPackages.add(involvedPackage)
                                    }
                                }
                            }
                        }
                    }
                    eventType = parser.next()
                }
            }
            isLoaded = true
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "Cannot load icon pack for ${this}, exception is ${e.message}")
        } catch (e: XmlPullParserException) {
            Log.e(TAG, "Cannot parse icon pack appfilter.xml for ${this}, exception is ${e.message}")
        } catch (e: IOException) {
            Log.e(TAG, "load error for ${this}, exception is ${e.message}")
        } finally {
            appfilterStream?.close()
        }
    }

    private fun getPackageNameFromComponentNameString(componentName: String): String? {
        val start = componentName.indexOf("{") + 1
        val end = componentName.indexOf("}", start)
        var packageName: String? = null
        if (end > start) {
            val pureComponentName =
                componentName.substring(start, end).lowercase(Locale.getDefault())
            val pureComponentArray = pureComponentName.split("/")
            // ABR13T-1484: 如果componentName为"ComponentInfo{/}"时, split方法返回的将是一个空数组，不能直接访问index=0
            takeIf { pureComponentArray.isNotEmpty() }?.let {
                packageName = pureComponentArray[0]
            }
        }
        return packageName
    }

    private fun reset() {
        involvedPackages.clear()
        iconPackDrawables.clear()
        iconPackBackImages.clear()
        iconPackMaskImages.clear()
        iconPackFrontImages.clear()
        iconPackFactor = 1.0f
        isLoaded = false
    }

    private fun parseNonTransparentBitmap(
        parser: XmlPullParser,
        list: MutableList<Bitmap>,
        iconSize: Int
    ) {
        for (i in 0 until parser.attributeCount) {
            if (parser.getAttributeName(i).startsWith("img")) {
                loadBitmapFromIconPackApp(parser.getAttributeValue(i), iconSize)?.let {
                    val standardIconSize =
                        SharedApplication.getContext().resources.getDimensionPixelSize(
                            R.dimen.default_icon_bitmap_size
                        ) / 2
                    val scaledBitmap = getLauncherIconSizeBitmap(standardIconSize, 1.0f, it)
                    if (!BitmapUtils.isTransparent(scaledBitmap)) {
                        list.add(it)
                    }
                }
            }
        }
    }

    private fun parseBitmap(parser: XmlPullParser, list: MutableList<Bitmap>, iconSize: Int) {
        for (i in 0 until parser.attributeCount) {
            if (parser.getAttributeName(i).startsWith("img")) {
                loadBitmapFromIconPackApp(parser.getAttributeValue(i), iconSize)?.let {
                    list.add(it)
                }
            }
        }
    }

    private fun loadDrawableFromIconPackApp(drawableName: String): Drawable? {
        val id: Int = iconPackResources.getIdentifier(drawableName, "drawable", packageName)
        if (id > 0) {
            try {
                return iconPackResources.getDrawable(id, null)
            } catch (e: Exception) {
                Log.e(TAG, "loadDrawableFromIconPackApp error: ${e.message}")
            }
        }
        return null
    }

    private fun loadBitmapFromIconPackApp(drawableName: String, iconSize: Int): Bitmap? {
        return loadDrawableFromIconPackApp(drawableName)?.let {
            BitmapUtils.drawableToBitmap(
                it,
                iconSize
            )
        }
    }

    // 如果没有对应图标映射，取出apk中的原始图片根据蒙层、背景、前景等进行处理。
    private fun generateBitmap(
        context: Context,
        componentIcon: Drawable?,
        iconSize: Int
    ): Drawable? {
        val defaultIcon = componentIcon?.let { BitmapUtils.drawableToBitmap(it, iconSize) }

        // if no support images in the icon pack return the bitmap itself
        if (iconPackBackImages.isEmpty() || defaultIcon == null || defaultIcon.isInValid()) {
            return null
        }
        val result = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // 绘制background
        getRandomBitmap(iconSize, iconPackBackImages)?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }

        // 绘制原始图标
        val scaledIconBitmap: Bitmap =
            getLauncherIconSizeBitmap(
                iconSize,
                iconPackFactor,
                BitmapUtils.convertHardWareBitmap(defaultIcon)
            )

        // 绘制mask
        if (iconPackMaskImages.isNotEmpty()) {
            val createBitmap3 = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888)
            Canvas(createBitmap3).apply {
                // draw the scaled bitmap
                drawBitmap(
                    scaledIconBitmap,
                    null,
                    Rect(
                        (iconSize - scaledIconBitmap.width) / 2,
                        (iconSize - scaledIconBitmap.height) / 2,
                        (scaledIconBitmap.width + iconSize) / 2,
                        (scaledIconBitmap.height + iconSize) / 2
                    ),
                    null
                )

                // paint the bitmap with mask into the result
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
                }
                // draw the mask
                getRandomBitmap(iconSize, iconPackMaskImages)?.let {
                    this.drawBitmap(it, 0.0f, 0.0f, paint)
                }
            }
            canvas.drawBitmap(createBitmap3, 0.0f, 0.0f, Paint(Paint.ANTI_ALIAS_FLAG))
        } else {
            canvas.drawBitmap(
                scaledIconBitmap,
                null,
                Rect(
                    (iconSize - scaledIconBitmap.width) / 2,
                    (iconSize - scaledIconBitmap.height) / 2,
                    (scaledIconBitmap.width + iconSize) / 2,
                    (scaledIconBitmap.height + iconSize) / 2
                ),
                null
            )
        }

        // paint the front
        getRandomBitmap(iconSize, iconPackFrontImages)?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }
        return BitmapDrawable(context.resources, result)
    }

    private fun Bitmap.isInValid(): Boolean {
        return this.height == 0 || this.width == 0
    }

    private fun getRandomBitmap(iconSize: Int, list: List<Bitmap>?): Bitmap? {
        return if (list.isNullOrEmpty()) {
            null
        } else getLauncherIconSizeBitmap(iconSize, 1.0f, list[Random().nextInt(list.size)])
    }

    // 生成与Launcher icon size大小一致的图标.
    private fun getLauncherIconSizeBitmap(iconSize: Int, scale: Float, bitmap: Bitmap): Bitmap {
        return if (bitmap.height == (iconSize * scale).toInt()) {
            bitmap
        } else {
            val iconScale = (iconSize.toFloat() * scale) / bitmap.height.toFloat()
            Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                Matrix().apply { postScale(iconScale, iconScale) },
                true
            )
        }
    }

    /*
    * 判断传入的应用名称是否涉及到系统侧的模糊匹配，背景可以参考https://nothingtech.atlassian.net/browse/ABR13T-1432
    *
    * 系统侧由于基本上只能拿到应用名称，无法拿到具体活动组件名称，因此他们取图标包的资源时，都是用模糊匹配的。
    * 也就是系统接口会从图标包中的映射关系中找到第一个与应用名匹配的资源，就会拿去用返回给调用方
    * */
    fun isFuzzyMatch(packageName: String?): Boolean {
        loadIfNeed()
        return involvedPackages.contains(packageName)
    }
}
