/*
Copyright (C), 2022, Nothing Technology
FileName: ItemInfoForIconRequest
Author: benny.fang
Date: 2022/11/22 17:44
Description: Necessary information to create a icon request
History:
<author> <time> <version> <desc>
 */
package com.nothing.launcher.icons.model.data

import android.content.ComponentName
import android.content.pm.LauncherActivityInfo
import android.os.UserHandle

data class ItemInfoForIconRequest(
    val isDeepShortCut: Boolean = false,
    val isBigIcon: Boolean = false,
    val isInstantApp: Boolean = false,
    val appName: String = "",
    val launcherActivityInfo: LauncherActivityInfo? = null,
    val targetComponentName: ComponentName? = null,
    val userHandle: UserHandle? = null,
    val iconSize: Int,
    val originItemInfo: Any? = null
)
