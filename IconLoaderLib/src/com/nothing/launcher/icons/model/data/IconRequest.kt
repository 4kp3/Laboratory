/*
Copyright (C), 2022, Nothing Technology
FileName: IconRequest
Author: benny.fang
Date: 2022/11/21 19:42
Description: Information about the current icon request
History:
<author> <time> <version> <desc>
 */

package com.nothing.launcher.icons.model.data

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.os.Process
import com.android.launcher3.icons.BaseIconFactory
import com.android.launcher3.icons.IconProvider
import com.android.launcher3.icons.R
import com.nothing.utils.LauncherActivityInfoUtils

class IconRequest(val context: Context) : BaseIconFactory.IconOptions() {
    var isDeepShortcut = false
    var isBigIcon = false
    private var launcherActivityInfo: LauncherActivityInfo? = null
    var targetComponentName: ComponentName? = null
    var targetAppName: String? = null
    var iconSize = context.resources.getDimensionPixelSize(R.dimen.default_icon_bitmap_size)
    var iconProvider: IconProvider? = null
    var factory: BaseIconFactory? = null
    var isInstantApp: Boolean = false

    fun getPackageName(): String? {
        return targetAppName
            ?: targetComponentName?.packageName
            ?: getEntryActivityInfo(false)?.componentName?.packageName
    }

    fun setLauncherActivityInfo(activityInfo: LauncherActivityInfo?) {
        launcherActivityInfo = activityInfo
    }

    fun getEntryActivityInfo(useAppEntry: Boolean): LauncherActivityInfo? {
        return if (useAppEntry) getAppEntryActivityInfo() else getSpecifiedEntryActivityInfo()
    }

    private fun getSpecifiedEntryActivityInfo(): LauncherActivityInfo? {
        return launcherActivityInfo ?: LauncherActivityInfoUtils.get(context, this)
    }

    /*
    * 获取应用图标
    * */
    private fun getAppEntryActivityInfo(): LauncherActivityInfo? {
        return targetAppName?.let {
            context.getSystemService(LauncherApps::class.java)
                .getActivityList(targetAppName, userHandle ?: Process.myUserHandle()).firstOrNull()
        }
    }
}