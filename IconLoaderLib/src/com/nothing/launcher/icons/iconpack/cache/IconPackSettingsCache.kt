/*
Copyright (C), 2022, Nothing Technology
FileName: IconPackSettingsCache
Author: benny.fang
Date: 2022/11/22 14:19
Description: ContentObserver over icon pack keys that also has a caching layer.
History:
<author> <time> <version> <desc>
 */

package com.nothing.launcher.icons.iconpack.cache

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import com.nothing.launcher.icons.constant.IconPackStateConstant
import com.nothing.utils.IconLogUtil
import java.util.concurrent.CopyOnWriteArrayList

internal class IconPackSettingsCache(val appContext: Context) :
    ContentObserver(Handler(Looper.getMainLooper())) {

    companion object {
        private const val TAG = "IconPackSettingsCache"
        private const val NOTHING_ICON_FORCE_RENDER_ENABLE = 1
        private const val NOTHING_ICON_FORCE_RENDER_DISABLE = 0
        private const val DEFAULT_NOTHING_ICON_FORCE_RENDER_VALUE = NOTHING_ICON_FORCE_RENDER_ENABLE

        private val PICKED_ICON_PACK_URI =
            Settings.System.getUriFor(IconPackStateConstant.KEY_SELECTED_ICON_PACK)

        private val NOTHING_ICON_FORCE_RENDER_ENABLE_URI =
            Settings.System.getUriFor(IconPackStateConstant.KEY_NOTHING_ICON_FORCE_RENDER_ENABLE)
    }


    var isNothingIconForceRenderEnable: Boolean = readNothingIconForceRenderEnable()
        private set
    private val forceRenderListenerList: CopyOnWriteArrayList<OnNothingIconForceRenderChangedListener> =
        CopyOnWriteArrayList()

    private var pickedIconPack: String = readPickedIconPackValue()
    private val iconPackPickedListenerList: CopyOnWriteArrayList<OnIconPackPickedListener> =
        CopyOnWriteArrayList()

    private val resolver: ContentResolver = appContext.contentResolver

    init {
        /* IconPackSettingsCache lifecycle is same with app in our usage,
           hence we only call register but don't need unregister */
        resolver.registerContentObserver(PICKED_ICON_PACK_URI, false, this)
        resolver.registerContentObserver(NOTHING_ICON_FORCE_RENDER_ENABLE_URI, false, this)
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        if (uri == PICKED_ICON_PACK_URI) {
            pickedIconPack = readPickedIconPackValue()
            IconLogUtil.i(TAG, "onChange: pickedIconPack = $pickedIconPack")
            iconPackPickedListenerList.forEach {
                it.onNewPackSelected(pickedIconPack)
            }
        } else if (uri == NOTHING_ICON_FORCE_RENDER_ENABLE_URI) {
            isNothingIconForceRenderEnable = readNothingIconForceRenderEnable()
            IconLogUtil.i(
                TAG,
                "onChange: isNothingIconPackForceRenderEnable = $isNothingIconForceRenderEnable"
            )
            forceRenderListenerList.forEach {
                it.onNothingIconForceRenderChanged(isNothingIconForceRenderEnable)
            }
        }
    }

    fun getValue() = pickedIconPack

    fun writeValue(
        callerContext: Context,
        newSelectedPack: String
    ): Boolean {
        if (!callerCheck(callerContext)) return false

        val convertedSelectedId = when (newSelectedPack) {
            IconPackStateConstant.getSystemIconOwner() -> IconPackStateConstant.VALUE_SYSTEM_ICON
            IconPackStateConstant.getThemedIconOwner() -> IconPackStateConstant.VALUE_THEMED_ICON
            // Added by stephen.bi for NOS-1721 @{
            IconPackStateConstant.getThemedIconNothingOwner() -> IconPackStateConstant.VALUE_THEMED_ICON_NOTHING
            // @}
            else -> newSelectedPack
        }

        if (pickedIconPack == convertedSelectedId) {
            IconLogUtil.i(TAG, "updateSelectedIdIfNeed: no change, so skip this update request")
            return false
        }

        Settings.System.putString(
            resolver,
            IconPackStateConstant.KEY_SELECTED_ICON_PACK,
            convertedSelectedId
        )
        return true
    }

    private fun readPickedIconPackValue(): String {
        val settingsValue =
            Settings.System.getString(
                appContext.contentResolver,
                IconPackStateConstant.KEY_SELECTED_ICON_PACK
            )
        return if (settingsValue.isNullOrEmpty()) IconPackStateConstant.DEFAULT_VALUE else settingsValue
    }

    private fun readNothingIconForceRenderEnable(): Boolean = Settings.System.getInt(
        appContext.contentResolver,
        IconPackStateConstant.KEY_NOTHING_ICON_FORCE_RENDER_ENABLE,
        DEFAULT_NOTHING_ICON_FORCE_RENDER_VALUE
    ) == NOTHING_ICON_FORCE_RENDER_ENABLE

    fun writeNothingIconForceRenderEnable(
        callerContext: Context, value: Boolean
    ): Boolean {
        if (!callerCheck(callerContext)) return false

        if (isNothingIconForceRenderEnable == value) return false

        Settings.System.putInt(
            resolver,
            IconPackStateConstant.KEY_NOTHING_ICON_FORCE_RENDER_ENABLE,
            if (value) NOTHING_ICON_FORCE_RENDER_ENABLE else NOTHING_ICON_FORCE_RENDER_DISABLE
        )
        return true
    }

    fun registerIconPackPickedListener(listener: OnIconPackPickedListener) {
        iconPackPickedListenerList.add(listener)
    }

    fun unregisterIconPackPickedListener(listener: OnIconPackPickedListener) {
        iconPackPickedListenerList.remove(listener)
    }

    fun registerNothingForceRenderChangedListener(listener: OnNothingIconForceRenderChangedListener) {
        forceRenderListenerList.add(listener)
    }

    fun unregisterNothingForceRenderChangedListener(listener: OnNothingIconForceRenderChangedListener) {
        forceRenderListenerList.remove(listener)
    }

    private fun callerCheck(callerContext: Context): Boolean {
        if (callerContext.packageName != IconPackStateConstant.getManagerName()) {
            IconLogUtil.e(
                TAG,
                "Caller ${callerContext.packageName} isn't manager, ignore update request"
            )
            return false
        }
        return true
    }
}