/*
Copyright (C), 2022, Nothing Technology
FileName: IconRequestBuilder
Author: benny.fang
Date: 2022/11/21 19:56
Description: Builder responsible for customize the request of app icon
History:
<author> <time> <version> <desc>
 */

package com.nothing.launcher.icons.model.builder

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.os.UserHandle
import com.android.launcher3.icons.BaseIconFactory
import com.android.launcher3.icons.IconProvider
import com.nothing.launcher.icons.model.data.IconRequest


class ApplicationIconBuilder(context: Context) : IIconBuilder<IconRequest>() {
    private val iconRequest = IconRequest(context)

    override fun setIsDeepShortcut(isDeepShortcut: Boolean) {
        iconRequest.isDeepShortcut = isDeepShortcut
    }

    override fun setIsBigIcon(isBig: Boolean) {
        iconRequest.isBigIcon = isBig
    }


    override fun setIsInstantApp(isInstantApp: Boolean) {
        iconRequest.isInstantApp = isInstantApp
    }

    override fun setActivityInfo(info: LauncherActivityInfo?) {
        iconRequest.setLauncherActivityInfo(info)
    }

    override fun setTargetComponentName(targetComponentName: ComponentName?) {
        iconRequest.targetComponentName = targetComponentName
    }

    override fun setUserHandle(userHandle: UserHandle?) {
        iconRequest.setUser(userHandle)
    }

    override fun setIconSize(size: Int) {
        iconRequest.iconSize = size
    }

    override fun setIconProvider(iconProvider: IconProvider) {
        iconRequest.iconProvider = iconProvider
    }

    override fun setFactory(iconFactory: BaseIconFactory) {
        iconRequest.factory = iconFactory
    }

    override fun setTargetAppName(appName: String) {
        iconRequest.targetAppName = appName
    }

    override fun build(): IconRequest {
        return iconRequest
    }
}