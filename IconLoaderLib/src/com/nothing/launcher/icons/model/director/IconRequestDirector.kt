/*
Copyright (C), 2022, Nothing Technology
FileName: IconRequestDirector
Author: benny.fang
Date: 2022/11/21 20:14
Description: Director responsible for communicating with client and return suitable icon request
History:
<author> <time> <version> <desc>
 */

package com.nothing.launcher.icons.model.director

import android.content.Context
import com.android.launcher3.icons.BaseIconFactory
import com.android.launcher3.icons.IconProvider
import com.nothing.launcher.icons.model.builder.ApplicationIconBuilder
import com.nothing.launcher.icons.model.data.IconRequest
import com.nothing.launcher.icons.model.data.ItemInfoForIconRequest


class IconRequestDirector(private val context: Context) {
    private val appIconBuilder: ApplicationIconBuilder by lazy {
        ApplicationIconBuilder(context)
    }

    fun create(
        itemInfoForIconRequest: ItemInfoForIconRequest,
        iconProvider: IconProvider,
        iconFactory: BaseIconFactory
    ): IconRequest {
        return appIconBuilder.apply {
            setIsBigIcon(itemInfoForIconRequest.isBigIcon)
            setIsInstantApp(itemInfoForIconRequest.isInstantApp)
            setActivityInfo(itemInfoForIconRequest.launcherActivityInfo)
            setTargetComponentName(itemInfoForIconRequest.targetComponentName)
            setUserHandle(itemInfoForIconRequest.userHandle)
            setIconSize(itemInfoForIconRequest.iconSize)
            setIconProvider(iconProvider)
            setFactory(iconFactory)
            if (itemInfoForIconRequest.appName.isNotEmpty()) {
                setTargetAppName(itemInfoForIconRequest.appName)
            }
        }.build()
    }

    fun create(
        appName: String,
        iconProvider: IconProvider,
        iconFactory: BaseIconFactory
    ): IconRequest {
        return appIconBuilder.apply {
            setTargetAppName(appName)
            setIconProvider(iconProvider)
            setFactory(iconFactory)
        }.build()
    }
}