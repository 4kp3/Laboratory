/*
Copyright (C), 2022, Nothing Technology
FileName: BitmapUtils
Author: benny.fang
Date: 2022/11/21 16:09
Description: LauncherActivityInfo tools class
History:
<author> <time> <version> <desc>
 */
package com.nothing.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.os.UserHandle
import com.nothing.launcher.icons.model.data.IconRequest

object LauncherActivityInfoUtils {
    fun get(context: Context, iconRequest: IconRequest): LauncherActivityInfo? {
        return get(context, iconRequest.targetComponentName, iconRequest.userHandle)
    }

    fun get(context: Context, componentName: ComponentName?, userHandle: UserHandle?): LauncherActivityInfo? {
        return componentName?.let {
            val newIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                component = componentName
            }
            return@let context.getSystemService(LauncherApps::class.java)
                .resolveActivity(newIntent, userHandle)
        }
    }
}