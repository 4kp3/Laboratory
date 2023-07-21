/*
Copyright (C), 2022, Nothing Technology
FileName: IconPackInfo
Author: benny.fang
Date: 2022/11/22 20:13
Description: Basic information about the current icon pack
History:
<author> <time> <version> <desc>
 */
package com.nothing.launcher.icons.model.data

import android.content.Context
import androidx.annotation.IntDef
import com.nothing.launcher.icons.IconPackManager
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

data class IconPackInfo(@Scenes val requestType: Int, var usePreloadedIconPackStyle: Boolean, val iconPackEntity: CachedIconEntity? = null) {

    companion object {
        @Retention(RetentionPolicy.SOURCE)
        @IntDef(
            Scenes.TASK,
            Scenes.PREVIEW_ICON,
            Scenes.DESKTOP_ICON,
            Scenes.ICON_PACK_LIST,
            Scenes.PACKAGE_ICON,
        )
        annotation class Scenes {
            companion object {
                const val TASK = 0
                const val PREVIEW_ICON = 1
                const val DESKTOP_ICON = 2
                const val ICON_PACK_LIST = 3
                const val PACKAGE_ICON = 4
            }
        }

        fun build(@Scenes type: Int, context: Context): IconPackInfo {
            return when (type) {
                // For requests from icon pack list scenes, always set the param of using preloaded style
                Scenes.ICON_PACK_LIST -> {
                    IconPackInfo(type, true, null)
                }
                else -> {
                    val isPreloadedIconSelected = IconPackManager.instance.isPreloadedIconSelected()
                    val matchedEntity =
                        if (isPreloadedIconSelected) null else IconPackManager.instance
                            .getMatchedEntity(context)
                    IconPackInfo(type, isPreloadedIconSelected, matchedEntity)
                }
            }
        }
    }

    fun useAppIcon() = isFromIconPackList() || isFromPackageIcon()

    fun isFromTask() = requestType == Scenes.TASK
    fun isFromPreviewIcon() = requestType == Scenes.PREVIEW_ICON
    private fun isFromIconPackList() = requestType == Scenes.ICON_PACK_LIST
    private fun isFromPackageIcon() = requestType == Scenes.PACKAGE_ICON
}
