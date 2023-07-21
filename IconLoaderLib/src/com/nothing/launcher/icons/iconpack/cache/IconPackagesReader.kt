/*
Copyright (C), 2022, Nothing Technology
FileName: ThirdPartyIconPackParser
Author: benny.fang
Date: 2022/11/21 21:40
Description: Utilities class that provides icon pack reading and parsing
History:
<author> <time> <version> <desc>
 */
package com.nothing.launcher.icons.iconpack.cache

import android.content.Context
import android.content.pm.ResolveInfo
import android.util.Log
import com.nothing.ext.getApplicationName
import com.nothing.ext.getIconPackInfoList
import com.nothing.launcher.icons.model.data.CachedIconEntity
import java.util.concurrent.ConcurrentHashMap

object IconPackagesReader {
    private const val TAG = "IconPackageReader"

    fun inflateAllIconPack(context: Context, entityMap: ConcurrentHashMap<String, CachedIconEntity>) {
        val iconPackInfoList = context.packageManager.getIconPackInfoList()
        iconPackInfoList.forEach { resolveInfo ->
            kotlin.runCatching {
                parseInfo(context, resolveInfo, entityMap)
                true
            }.onFailure {
                Log.e(TAG,
                    "loadIconIfNeed: resolveInfo $resolveInfo parsed failed, error message is ${it.message}"
                )
            }
        }
    }

    fun parseInfo(context: Context, resolveInfo: ResolveInfo, entityMap: ConcurrentHashMap<String, CachedIconEntity>) {
        val packageName = resolveInfo.activityInfo.packageName
        val appName = context.packageManager.getApplicationName(packageName)
        if (packageName.isNotEmpty() && appName?.isNotEmpty() == true) {
            val cacheEntity = CachedIconEntity(packageName, appName)
            val id = cacheEntity.packageName
            entityMap[id] = cacheEntity
        }
//        Log.d(TAG,
//            "parseIconPackInfo: parsed packageName is $packageName, appName is $appName"
//        )
    }
}