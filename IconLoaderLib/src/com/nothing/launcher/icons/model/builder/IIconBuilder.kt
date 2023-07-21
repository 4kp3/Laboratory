/*
Copyright (C), 2022, Nothing Technology
FileName: IIconRequestBuilder
Author: benny.fang
Date: 2022/11/21 19:51
Description: Abstract builder responsible for customize the request of various icon
History:
<author> <time> <version> <desc>
 */

package com.nothing.launcher.icons.model.builder

import android.content.ComponentName
import android.content.pm.LauncherActivityInfo
import android.os.UserHandle
import com.android.launcher3.icons.BaseIconFactory
import com.android.launcher3.icons.IconProvider


abstract class IIconBuilder<T> {
    abstract fun setIsDeepShortcut(isDeepShortcut: Boolean)
    abstract fun setIsBigIcon(isBig: Boolean)
    abstract fun setIsInstantApp(isInstantApp: Boolean)
    abstract fun setActivityInfo(info: LauncherActivityInfo?)
    abstract fun setTargetComponentName(targetComponentName: ComponentName?)
    abstract fun setTargetAppName(appName: String)
    abstract fun setUserHandle(userHandle: UserHandle?)
    abstract fun setIconSize(size: Int)
    abstract fun setIconProvider(iconProvider: IconProvider)
    abstract fun setFactory(iconFactory: BaseIconFactory)
    abstract fun build(): T
}