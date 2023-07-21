/*
Copyright (C), 2022, Nothing Technology
FileName: IconPackManager
Author: benny.fang
Date: 2022/6/2 12:04
Description: Class for retrieving various kinds of information related to the icon packages and updating local icon pack selection.
History:
<author> <time> <version> <desc>
 */
package com.nothing.launcher.icons

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.pm.Signature
import android.graphics.Bitmap
import android.os.UserHandle
import androidx.annotation.WorkerThread
import com.android.launcher3.icons.BaseIconFactory
import com.android.launcher3.icons.BitmapInfo
import com.android.launcher3.icons.IconProvider
import com.android.launcher3.icons.cache.BaseIconCache
import com.nothing.ext.getIconPackInfoList
import com.nothing.launcher.icons.constant.IconPackStateConstant
import com.nothing.launcher.icons.constant.IconPackStateConstant.NOTHING_ICON_PACK_PACKAGE_ID
import com.nothing.launcher.icons.iconpack.cache.IconPackagesCache
import com.nothing.launcher.icons.iconpack.cache.OnIconPackPickedListener
import com.nothing.launcher.icons.iconpack.cache.OnNothingIconForceRenderChangedListener
import com.nothing.launcher.icons.load.IconLoaderProxy
import com.nothing.launcher.icons.model.data.IconPackInfo
import com.nothing.launcher.icons.model.data.ItemInfoForIconRequest
import com.nothing.launcher.icons.status.StateController
import com.nothing.utils.IconLogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.function.Function
import java.util.function.Supplier

class IconPackManager(val appContext: Context) :
    CoroutineScope by CoroutineScope(SupervisorJob() + Dispatchers.IO) {
    companion object {
        private const val TAG = "IconPackManager"

        val instance: IconPackManager by lazy {
            IconPackManager(SharedApplication.getContext())
        }
//        @Volatile
//        private var instance: IconPackManager? = null
//        fun getInstance(context: Context): IconPackManager {
//            return instance ?: synchronized(this) {
//                instance ?: IconPackManager(context.applicationContext).also {
//                    instance = it
//                }
//            }
//        }
    }

    private val cache: IconPackagesCache = IconPackagesCache()
    private val stateController: StateController = StateController(appContext, this)
    private val iconLoadProxy: IconLoaderProxy = IconLoaderProxy(appContext)

    val iconPackCacheFlow = cache.getIconPackCacheFlow()
    val isForcedMonoEnabled: Boolean
        get() {
            return stateController.isNothingIconPackForceRenderEnable()
        }
    var supportHueChangeOnThemedIcons = false
    var supportCustomizeThemedIconSize = false

    private val pm by lazy { appContext.packageManager }
    private val launcherPackageSignature: Signature? by lazy { getPackageSignature(appContext.packageName) }

    /**
     * Check via PackageManager if NothingIconPack is installed. If so, turn on the forced rendering
     * switch; otherwise turn it off.
     *
     * @param nothingIconPackInstalled Whether the NothingIconPack app is installed. Can be null.
     * @param refreshIconEnable When the forced rendering is changed, we should refresh. In init, we usually don't need to.
     * @return Whether the forced rendering switch attribute has changed compared to the cached value
     *
     * @see startMonitorNothingForceRenderChange
     * @see stopMonitorNothingForceRenderChange
     */
    fun loadNothingIconPackForceRenderEnable(
        nothingIconPackInstalled: Boolean? = null,
        refreshIconEnable: Boolean = false
    ): Boolean {
        val iconPackageSignature: Signature? by lazy {
            getPackageSignature(NOTHING_ICON_PACK_PACKAGE_ID).also {
                if (it == null && nothingIconPackInstalled == true) {
                    IconLogUtil.e(
                        TAG, "NothingIcon was installed${
                            if (nothingIconPackInstalled == true)
                                "(parameter)" else "(query PM)"
                        },but didn't find the signature"
                    )
                }
            }
        }
        val isNothingIconInstalled = nothingIconPackInstalled
            ?: (iconPackageSignature != null)

        val oldValue = stateController.isNothingIconPackForceRenderEnable()
        var newValue = false

        // Verify the signature of the icon pack app. If it matches the Launcher's signature, then allow it.
        if (isNothingIconInstalled) {

            if (iconPackageSignature != null) {

                newValue = iconPackageSignature == launcherPackageSignature
                if (!newValue) {
                    IconLogUtil.e(TAG, "NothingIcon was installed, but had an incorrect signature.")
                }

            }
        }

        IconLogUtil.i(
            TAG, "reloadNothingIconPackForceRenderEnable parameter nothingIconPackInstalled" +
                    " = $nothingIconPackInstalled, newValue = $newValue, oldValue = $oldValue"
        )

        // Update the local value and make sure the update is successful.
        return oldValue != newValue && stateController.writeNothingIconPackForceRenderEnable(
            appContext,
            newValue,
            refreshIconEnable
        )
    }

    //State START
    fun tryRemovePackages(
        callerContext: Context,
        removedPackages: HashSet<String>?
    ) {
        // first update forceRenderEnable
        launch {
            var isNothingIconPackUninstall = false
            removedPackages?.forEach {
                // Reset icon pack selection
                if (readPickedIconPack() == it) {
                    updateSelectedIdIfNeed(
                        callerContext,
                        IconPackStateConstant.DEFAULT_VALUE,
                        true
                    )
                    IconLogUtil.i(TAG, "tryRemovePackages: reset icon pack to SYSTEM")
                }
                // Remove invalid cache
                tryRemoveFromCache(it)

                if (!isNothingIconPackUninstall) {
                    isNothingIconPackUninstall = it == NOTHING_ICON_PACK_PACKAGE_ID
                }
            }

            if (isNothingIconPackUninstall) {
                loadNothingIconPackForceRenderEnable(
                    nothingIconPackInstalled = false,
                    refreshIconEnable = true
                )
            }
        }
    }

    fun tryAddPackages(context: Context, packages: Array<String>?) {
        val iconPackList = context.packageManager.getIconPackInfoList()
        var isNothingIconPackInstalled = false
        packages?.forEach { pkgName ->
            iconPackList.firstOrNull { iconPackInfo -> iconPackInfo.activityInfo.packageName == pkgName }
                ?.let {
                    addCache(context, it)
                }
            if (!isNothingIconPackInstalled) {
                isNothingIconPackInstalled = pkgName == NOTHING_ICON_PACK_PACKAGE_ID
            }
        }

        if (isNothingIconPackInstalled) {
            // Can run in background thread
            launch {
                loadNothingIconPackForceRenderEnable(
                    nothingIconPackInstalled = true,
                    refreshIconEnable = true
                )
            }
        }
    }

    fun readPickedIconPack(): String {
        return stateController.readPickedIconPack()
    }

    @WorkerThread
    fun updateSelectedIdIfNeed(
        callerContext: Context,
        selectedId: String,
        isIconPackUninstall: Boolean
    ): Boolean {
        return stateController.writePickedIconPack(
            callerContext, selectedId, isIconPackUninstall
        )
    }

    fun isPreloadedIconSelected(): Boolean {
        return stateController.isPreloadedIconsUsing()
    }

    fun isSystemIconSelected(): Boolean {
        return stateController.isSystemIconsUsing()
    }

    fun isThemedIconSelected(): Boolean {
        return stateController.isThemedIconsUsing()
    }

    // Added by stephen.bi for NOS-1721 @{
    var checkBadForegroundFunction: Function<String?, Boolean>? = null

    fun isNothingThemedIconSelected(): Boolean {
        return stateController.isNothingThemedIconsUsing()
    }

    fun isColorThemedIconSelected(): Boolean {
        return stateController.isColorThemedIconsUsing()
    }
    // @}

    fun startMonitorIconPackChange(onSelectListener: OnIconPackPickedListener) {
        stateController.startMonitorIconPackChange(onSelectListener)
    }

    fun stopMonitorIconPackChange(onSelectListener: OnIconPackPickedListener) {
        stateController.stopMonitorIconPackChange(onSelectListener)
    }

    fun startMonitorNothingForceRenderChange(listener: OnNothingIconForceRenderChangedListener) {
        stateController.startMonitorNothingIconForceRenderChange(listener)
    }

    fun stopMonitorNothingForceRenderChange(listener: OnNothingIconForceRenderChangedListener) {
        stateController.stopMonitorNothingIconForceRenderChange(listener)
    }

    fun executePendingUpdateImmediately() {
        stateController.executePendingUpdateImmediately()
    }
    //State END

    //IconPack cache START
    private fun addCache(context: Context, info: ResolveInfo) {
        cache.addCache(context, info)
    }

    private fun tryRemoveFromCache(packageName: String?) {
        cache.removeCache(packageName)
    }

    fun loadIconIfNeed(context: Context) {
        cache.inflateIconPacks(context)
    }

    /*
    * 检查当前应用的图标是否会从系统的三方图标包模糊匹配逻辑中取得
    * */
    fun isFuzzyMatchInIconPack(packageName: String?): Boolean {
        return (!isPreloadedIconSelected()
                && getMatchedEntity(appContext).isFuzzyMatch(packageName))
    }

    fun getMatchedEntity(context: Context) =
        cache.getMatchedEntity(context, stateController.readPickedIconPack())
    //IconPack cache END

    /*Icon Load Start*/
    fun getTaskBitmapInfo(
        componentName: ComponentName?, userHandle: UserHandle?,
        iconFactory: BaseIconFactory,
        iconProvider: IconProvider
    ): BitmapInfo? {
        val previewInfo = ItemInfoForIconRequest(
            targetComponentName = componentName,
            userHandle = userHandle,
            iconSize = iconFactory.iconBitmapSize,
        )
        val iconPackInfo = IconPackInfo.build(IconPackInfo.Companion.Scenes.TASK, appContext)
        return iconLoadProxy.getBitmapInfo(previewInfo, iconPackInfo, iconFactory, iconProvider)
    }

    /*
    * For the case of requesting the application icon, you can use this interface
    * */
    fun getPackageBitmapInfo(
        appName: String,
        userHandle: UserHandle,
        isInstantApp: Boolean,
        iconFactory: BaseIconFactory,
        iconProvider: IconProvider?,
        fallbackBitmapInfo: Supplier<BitmapInfo?>
    ): BitmapInfo? {
        return iconProvider?.let {
            val previewInfo = ItemInfoForIconRequest(
                isInstantApp = isInstantApp,
                appName = appName,
                userHandle = userHandle,
                iconSize = iconFactory.iconBitmapSize
            )
            val iconPackInfo =
                IconPackInfo.build(IconPackInfo.Companion.Scenes.PACKAGE_ICON, appContext)
            iconLoadProxy.getBitmapInfo(previewInfo, iconPackInfo, iconFactory, it)
        } ?: fallbackBitmapInfo.get()
    }

    /*
    * For the case of requesting the activity component, you can use this interface
    * */
    fun getDesktopBitmapInfo(
        isBigIcon: Boolean,
        activityInfo: LauncherActivityInfo,
        iconFactory: BaseIconFactory,
        iconProvider: IconProvider
    ): BitmapInfo? {
        val previewInfo = ItemInfoForIconRequest(
            isBigIcon = isBigIcon,
            launcherActivityInfo = activityInfo,
            iconSize = iconFactory.iconBitmapSize,
            userHandle = activityInfo.user
        )
        val iconPackInfo =
            IconPackInfo.build(IconPackInfo.Companion.Scenes.DESKTOP_ICON, appContext)
        return iconLoadProxy.getBitmapInfo(previewInfo, iconPackInfo, iconFactory, iconProvider)
    }

    fun getIconInIconPackList(
        packageName: String,
        iconFactory: BaseIconFactory,
        iconProvider: IconProvider
    ): Bitmap? {
        val iconPackInfo =
            IconPackInfo.build(IconPackInfo.Companion.Scenes.ICON_PACK_LIST, appContext)
        return iconLoadProxy.getSystemIconBitmap(
            packageName,
            iconPackInfo,
            iconFactory,
            iconProvider
        )
    }

    fun getPreviewBitmapInfo(
        originalRequest: ItemInfoForIconRequest,
        iconCache: BaseIconCache,
        iconFactory: BaseIconFactory,
        iconProvider: IconProvider
    ): BitmapInfo? {
        val iconPackInfo =
            IconPackInfo.build(IconPackInfo.Companion.Scenes.PREVIEW_ICON, appContext)
        return iconLoadProxy.getBitmapInfo(
            originalRequest,
            iconCache,
            iconPackInfo,
            iconFactory,
            iconProvider
        )
    }
    /*Icon Load END*/

    private fun getPackageSignature(packageName: String): Signature? = try {
        pm.getPackageInfo(
            packageName, PackageManager.PackageInfoFlags.of(
                PackageManager.GET_SIGNING_CERTIFICATES.toLong()
            )
        ).signingInfo?.apkContentsSigners?.firstOrNull()
    } catch (notFound: PackageManager.NameNotFoundException) {
        // not installed
        null
    }
}