/*
Copyright (C), 2022, Nothing Technology
FileName: IconPackagesCache
Author: benny.fang
Date: 2022/11/21 21:21
Description: Cache all icon pack information on the device
History:
<author> <time> <version> <desc>
 */

package com.nothing.launcher.icons.iconpack.cache

import android.content.Context
import android.content.pm.ResolveInfo
import android.util.Log
import com.nothing.launcher.icons.constant.IconPackStateConstant
import com.nothing.launcher.icons.constant.IconPackStateConstant.preloadIconEntityMap
import com.nothing.launcher.icons.model.data.CachedIconEntity
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.concurrent.ConcurrentHashMap

class IconPackagesCache {
    companion object {
        private const val TAG = "IconPackagesCache"
    }

    private val iconPackCacheMap = ConcurrentHashMap<String, CachedIconEntity>()
    private val iconPackCacheFlow = MutableSharedFlow<ConcurrentHashMap<String, CachedIconEntity>>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    fun getIconPackCacheFlow() = iconPackCacheFlow.asSharedFlow()


    fun addCache(context: Context, info: ResolveInfo) {
        Log.i(TAG, "addCache: info is $info")
        IconPackagesReader.parseInfo(context, info, iconPackCacheMap)
        iconPackCacheFlow.tryEmit(iconPackCacheMap)
    }

    fun removeCache(packageName: String?) {
        iconPackCacheMap.remove(packageName)?.let {
            Log.i(TAG, "removeCache: removed $packageName")
            iconPackCacheFlow.tryEmit(iconPackCacheMap)
        }
    }

    fun inflateIconPacks(
        context: Context,
    ) {
        // Before we load all the content, make sure here is clean
        iconPackCacheMap.clear()
        // Add preloaded Icon manually
        iconPackCacheMap.putAll(preloadIconEntityMap)
        // Add existing and standard icon pack installed on device
        IconPackagesReader.inflateAllIconPack(context, iconPackCacheMap)
        // Load completed, notify the collector
        iconPackCacheFlow.tryEmit(iconPackCacheMap)
    }

    fun getMatchedEntity(context: Context, targetPackageId: String): CachedIconEntity {
        if (iconPackCacheMap.isEmpty()) {
            inflateIconPacks(context)
        }
        return iconPackCacheMap[targetPackageId] ?: IconPackStateConstant.defaultIconEntity
    }
}