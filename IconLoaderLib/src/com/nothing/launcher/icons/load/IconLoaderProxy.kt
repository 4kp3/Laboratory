/*
Copyright (C), 2022, Nothing Technology
FileName: IconLoaderProxy
Author: benny.fang
Date: 2022/11/21 20:32
Description: Proxy class that preprocesses the request and execute the icon request
History:
<author> <time> <version> <desc>
 */

package com.nothing.launcher.icons.load

import android.content.Context
import android.graphics.Bitmap
import com.android.launcher3.icons.BaseIconFactory
import com.android.launcher3.icons.BitmapInfo
import com.android.launcher3.icons.IconProvider
import com.android.launcher3.icons.cache.BaseIconCache
import com.nothing.launcher.icons.IconPackManager
import com.nothing.launcher.icons.model.data.IconPackInfo
import com.nothing.launcher.icons.model.data.IconRequest
import com.nothing.launcher.icons.model.data.ItemInfoForIconRequest
import com.nothing.launcher.icons.model.director.IconRequestDirector

class IconLoaderProxy(private val appContext: Context) : AbstractIconLoader() {

    private val systemIconLoader = SystemIconLoader(appContext)
    private val thirdPartyIconLoader = ThirdPartyIconLoader()

    fun getBitmapInfo(
        itemInfoForIconRequest: ItemInfoForIconRequest,
        iconCache: BaseIconCache,
        iconPackInfo: IconPackInfo,
        iconFactory: BaseIconFactory,
        iconProvider: IconProvider
    ): BitmapInfo? {
        // TODO: DeepShortcut先临时这么调用，看看怎么融入到这个类里面
        return if (iconPackInfo.isFromPreviewIcon() && itemInfoForIconRequest.isDeepShortCut) {
            return iconCache.updateIconsForPkg(itemInfoForIconRequest)
        } else {
            getBitmapInfo(itemInfoForIconRequest, iconPackInfo, iconFactory, iconProvider)
        }
    }

    fun getBitmapInfo(
        originalRequest: ItemInfoForIconRequest,
        iconPackInfo: IconPackInfo,
        iconFactory: BaseIconFactory,
        iconProvider: IconProvider
    ): BitmapInfo? {
        val iconRequest =
            IconRequestDirector(appContext).create(originalRequest, iconProvider, iconFactory)
        return execute(iconPackInfo, iconRequest)
    }

    fun getSystemIconBitmap(
        appName: String,
        iconPackInfo: IconPackInfo,
        iconFactory: BaseIconFactory,
        iconProvider: IconProvider
    ): Bitmap? {
        val iconRequest = IconRequestDirector(appContext).create(appName, iconProvider, iconFactory)
        val bitmapInfo = execute(iconPackInfo, iconRequest).also {
            // Make sure icon factory be closed no matter it is used or not used
            iconFactory.close()
        }
        return bitmapInfo?.icon
    }

    override fun execute(options: IconPackInfo, iconRequest: IconRequest): BitmapInfo? {
        return when {
            options.isFromTask() -> {
                /*  For requests from multitasking scenarios,
                    it will handle the default icon package by himself */
                if (options.usePreloadedIconPackStyle) {
                    if (IconPackManager.instance.isThemedIconSelected()) {
                        systemIconLoader.execute(options, iconRequest)
                    } else {
                        null
                    }
                } else {
                    thirdPartyIconLoader.execute(options, iconRequest)
                }
            }
            else -> {
                if (options.usePreloadedIconPackStyle) {
                    systemIconLoader.execute(options, iconRequest)
                } else {
                    /* When 3rd party icon pack is using,
                       generate icons based on the resources they provide first,
                       and use native interfaces if there is no result
                    */
                    thirdPartyIconLoader.execute(options, iconRequest) ?: run {
                        options.usePreloadedIconPackStyle = true
                        iconRequest.setShrinkNonAdaptiveIcons(true)
                        execute(options, iconRequest)
                    }
                }
            }
        }
    }
}