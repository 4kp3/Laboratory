/*
Copyright (C), 2022, Nothing Technology
FileName: StateController
Author: benny.fang
Date: 2022/11/21 20:55
Description: The actual manager of icon pack state
History:
<author> <time> <version> <desc>
 */

package com.nothing.launcher.icons.status

import android.content.Context
import android.content.Intent
import androidx.annotation.MainThread
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.launcher3.icons.IconProvider
import com.nothing.launcher.icons.constant.IconPackStateConstant
import com.nothing.launcher.icons.constant.IconPackStateConstant.EXTRA_NOTHING_ICON_FORCE_RENDER_ENABLE
import com.nothing.launcher.icons.constant.IconPackStateConstant.SUPPORT_THEMED_ICON
import com.nothing.launcher.icons.constant.IconPackStateConstant.VALUE_THEMED_ICON
import com.nothing.launcher.icons.constant.IconPackStateConstant.VALUE_THEMED_ICON_NOTHING
import com.nothing.launcher.icons.iconpack.cache.IconPackSettingsCache
import com.nothing.launcher.icons.iconpack.cache.OnIconPackPickedListener
import com.nothing.launcher.icons.iconpack.cache.OnNothingIconForceRenderChangedListener
import com.nothing.utils.IconLogUtil
import kotlinx.coroutines.CoroutineScope


internal class StateController : OnIconPackPickedListener, OnNothingIconForceRenderChangedListener {

    companion object {
        private const val TAG = "StateController"
    }

    private val settingsCache: IconPackSettingsCache
    private val appContext: Context
    private val scope: CoroutineScope

    @Volatile
    private var refreshIconJob: RefreshIconJob? = null
        // add by yixiong for NOS-2146 @{
        // Care must be taken to avoid overriding tasks with higher priority
        set(new) {
            val old = field
            if (old != null && !old.isCompleted) {
                new ?: return
                /**
                 * When a task is replaced, urgent is selected between urgent and non-urgent,
                 * and global is selected between local and global.
                 *
                 * old[totalRefresh,!instant] + new[partialRefresh,instant] = [totalRefresh,instant]
                 * old[partialRefresh,instant] + new[totalRefresh,!instant] = [totalRefresh,instant]
                 */
                if ((old is RefreshAllIconsJob && !old.instant && new is RefreshForceRenderNothingIconJob)
                    || (old is RefreshForceRenderNothingIconJob && new is RefreshAllIconsJob && !new.instant)
                ) {
                    field = RefreshAllIconsJob(old.refreshContext, true)
                    return
                }
            }
            field = new
        }
    // @}

    constructor(appContext: Context, scope: CoroutineScope) {
        this.appContext = appContext
        this.scope = scope
        settingsCache = IconPackSettingsCache(appContext)
        // 无需反注册，声明周期与App一致
        startMonitorIconPackChange(this)
        startMonitorNothingIconForceRenderChange(this)
    }

    fun readPickedIconPack(): String {
        return settingsCache.getValue()
    }

    fun writePickedIconPack(
        callerContext: Context,
        selectedId: String,
        isIconPackUninstall: Boolean
    ): Boolean {
        // When uninstalling app, user probably stay in the desktop, need to refresh icon ASAP
        refreshIconJob = RefreshAllIconsJob(appContext, isIconPackUninstall)
        return settingsCache.writeValue(callerContext, selectedId)
    }

    fun isNothingIconPackForceRenderEnable() =
        settingsCache.isNothingIconForceRenderEnable

    fun writeNothingIconPackForceRenderEnable(
        callerContext: Context,
        value: Boolean,
        refreshIconEnable: Boolean = false
    ): Boolean {
        if (isNothingThemedIconsUsing() && refreshIconEnable) {
            // Launcher is using Nothing Theme,we need to refresh the desktop now.
            IconLogUtil.d(
                TAG,
                "writeNothingIconPackForceRenderEnable: RefreshForceRenderNothingIconJob was created."
            )
            refreshIconJob =
                RefreshForceRenderNothingIconJob(appContext, true, value)
        }
        return settingsCache.writeNothingIconForceRenderEnable(callerContext, value)
    }

    fun isPreloadedIconsUsing(): Boolean {
        return isSystemIconsUsing() || isThemedIconsUsing()
    }

    fun isSystemIconsUsing(): Boolean {
        return IconPackStateConstant.VALUE_SYSTEM_ICON == readPickedIconPack()
    }

    // Modified by stephen.bi for NOS-1721 @{
    fun isThemedIconsUsing(): Boolean {
        return SUPPORT_THEMED_ICON
                && (VALUE_THEMED_ICON_NOTHING == readPickedIconPack()
                || VALUE_THEMED_ICON == readPickedIconPack())
    }

    fun isNothingThemedIconsUsing(): Boolean {
        return SUPPORT_THEMED_ICON && VALUE_THEMED_ICON_NOTHING == readPickedIconPack()
    }

    fun isColorThemedIconsUsing(): Boolean {
        return SUPPORT_THEMED_ICON && VALUE_THEMED_ICON == readPickedIconPack()
    }
    // @}

    fun startMonitorIconPackChange(onSelectListener: OnIconPackPickedListener) {
        settingsCache.registerIconPackPickedListener(onSelectListener)
    }

    fun stopMonitorIconPackChange(onSelectListener: OnIconPackPickedListener) {
        settingsCache.unregisterIconPackPickedListener(onSelectListener)
    }

    fun startMonitorNothingIconForceRenderChange(listener: OnNothingIconForceRenderChangedListener) {
        settingsCache.registerNothingForceRenderChangedListener(listener)
    }

    fun stopMonitorNothingIconForceRenderChange(listener: OnNothingIconForceRenderChangedListener) {
        settingsCache.unregisterNothingForceRenderChangedListener(listener)
    }

    /*
    * If there still have pending job, notify it to be changed to immediately job
    * */
    fun executePendingUpdateImmediately() {
        refreshIconJob?.ensureExecute()
    }

    override fun onNewPackSelected(packageName: String) {
        refreshIconJob?.tryExecute()
    }

    override fun onNothingIconForceRenderChanged(value: Boolean) {
        IconLogUtil.d(TAG, "onNothingIconForceRenderChanged value = $value")
        refreshIconJob?.tryExecute()
    }

    /*
       * Refresh job will be executed at now or at the certain timing
       * */
    private abstract class RefreshIconJob(
        val refreshContext: Context,
        val instant: Boolean
    ) {
        var isCompleted = false
            private set

        @MainThread
        fun ensureExecute() {
            execute()
        }

        @MainThread
        fun tryExecute() {
            if (instant) {
                execute()
            }
        }

        private fun execute() {
            if (isCompleted) {
                return
            }
            isCompleted = true
            onExecute()
        }

        protected abstract fun onExecute()
    }

    private class RefreshAllIconsJob(
        refreshContext: Context,
        instant: Boolean
    ) : RefreshIconJob(refreshContext, instant) {
        override fun onExecute() {
            IconLogUtil.i(TAG, "RefreshIconJob execute, all icons will be refreshed now.")
            LocalBroadcastManager.getInstance(refreshContext)
                .sendBroadcast(Intent(IconProvider.ACTION_APPLY_PICKED_ICON_PACK))
        }
    }

    private class RefreshForceRenderNothingIconJob(
        refreshContext: Context,
        instant: Boolean,
        val isNothingForceRenderEnable: Boolean
    ) : RefreshIconJob(refreshContext, instant) {
        override fun onExecute() {
            IconLogUtil.i(
                TAG,
                "NothingIconForceRenderRefreshJob execute, unsuitable icons will be refreshed now."
            )
            LocalBroadcastManager.getInstance(refreshContext)
                .sendBroadcast(
                    Intent(IconProvider.ACTION_NOTHING_ICON_FORCE_RENDER_ENABLE_CHANGED)
                        .putExtra(
                            EXTRA_NOTHING_ICON_FORCE_RENDER_ENABLE,
                            isNothingForceRenderEnable
                        )
                )
        }
    }
}