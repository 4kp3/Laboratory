/*
Copyright (C), 2022, Nothing Technology
FileName: ThirdPartyIconLoader
Author: benny.fang
Date: 2022/11/21 20:30
Description: The loader responsible for obtaining icon pack style icons
History:
<author> <time> <version> <desc>
 */

package com.nothing.launcher.icons.load

import com.android.launcher3.icons.BitmapInfo
import com.nothing.launcher.icons.model.data.IconPackInfo
import com.nothing.launcher.icons.model.data.IconRequest

class ThirdPartyIconLoader : AbstractIconLoader() {
    override fun execute(
        iconPackInfo: IconPackInfo,
        iconRequest: IconRequest
    ): BitmapInfo? {
        // 不需要转化成AdaptiveIcon
        iconRequest.setShrinkNonAdaptiveIcons(false)

        val iconProvider = iconRequest.iconProvider
        val factory = iconRequest.factory
        val activityInfo = iconRequest.getEntryActivityInfo(iconPackInfo.useAppIcon())
        return if (activityInfo == null || factory == null || iconProvider == null) {
            null
        } else {
            val iconDrawable = iconProvider.getIconForIconPack(
                iconPackInfo.iconPackEntity,
                activityInfo,
                iconRequest.iconSize,
                factory.fillResIconDpi
            )
            iconDrawable?.let {
                createBadgedIconBitmap(
                    factory, it, iconRequest
                )
            }
        }
    }
}