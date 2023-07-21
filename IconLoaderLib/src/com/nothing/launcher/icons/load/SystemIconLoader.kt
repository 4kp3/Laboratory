/*
Copyright (C), 2022, Nothing Technology
FileName: SystemIconLoader
Author: benny.fang
Date: 2022/11/21 20:29
Description: The loader responsible for obtaining native style icons
History:
<author> <time> <version> <desc>
 */

package com.nothing.launcher.icons.load

import android.content.Context
import com.android.launcher3.icons.BitmapInfo
import com.nothing.launcher.icons.model.data.IconPackInfo
import com.nothing.launcher.icons.model.data.IconRequest


internal class SystemIconLoader(val context: Context) : AbstractIconLoader() {
    override fun execute(
        iconPackInfo: IconPackInfo,
        iconRequest: IconRequest
    ): BitmapInfo? {
        val iconProvider = iconRequest.iconProvider
        val factory = iconRequest.factory
        val activityInfo = iconRequest.getEntryActivityInfo(iconPackInfo.useAppIcon())
        return if (activityInfo == null || factory == null || iconProvider == null) {
            null
        } else {
            createBadgedIconBitmap(
                factory, iconProvider.getIcon(iconRequest.isBigIcon, activityInfo, factory.fillResIconDpi), iconRequest
            )
        }
    }

}