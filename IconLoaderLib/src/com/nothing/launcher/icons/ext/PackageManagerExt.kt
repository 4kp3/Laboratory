/*
Copyright (C), 2022, Nothing Technology
FileName: PackageManagerExt
Author: benny.fang
Date: 2022/6/9 15:43
Description: Extension class that make it easier for developers to use PackageManager's interface
History:
<author> <time> <version> <desc>
 */
package com.nothing.ext

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo

fun PackageManager.getIconPackInfoList(): List<ResolveInfo> {
    // find apps with intent-filter "com.gau.go.launcherex.theme" and return build the HashMap
    val adwlauncherthemes = this.queryIntentActivities(
        Intent("org.adw.launcher.THEMES"),
        PackageManager.GET_META_DATA
    )
    val golauncherthemes = this.queryIntentActivities(
        Intent("com.gau.go.launcherex.theme"),
        PackageManager.GET_META_DATA
    )

    // merge those lists
    // TODO: @Benny 这里的比较是对象维度，应该进行更细颗粒的比较如比较组件名。 后面再回头改
    val resolveInfoList: MutableList<ResolveInfo> = ArrayList(adwlauncherthemes)
    resolveInfoList.addAll(golauncherthemes)

    return resolveInfoList
}

fun PackageManager.getApplicationName(packageName: String?): String? {
    return packageName?.let {
        val applicationInfo = this.getApplicationInfo(it, PackageManager.GET_META_DATA)
        this.getApplicationLabel(applicationInfo).toString()
    }
}