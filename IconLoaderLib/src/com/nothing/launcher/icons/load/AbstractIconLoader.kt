/*
Copyright (C), 2022, Nothing Technology
FileName: AbstractIconLoader
Author: benny.fang
Date: 2022/11/21 20:22
Description: Abstract class to handle icon request
History:
<author> <time> <version> <desc>
 */


package com.nothing.launcher.icons.load

import android.graphics.drawable.Drawable
import com.android.launcher3.icons.BaseIconFactory
import com.android.launcher3.icons.BitmapInfo
import com.nothing.launcher.icons.model.data.IconPackInfo
import com.nothing.launcher.icons.model.data.IconRequest

abstract class AbstractIconLoader {
    abstract fun execute(
        iconPackInfo: IconPackInfo,
        iconRequest: IconRequest
    ): BitmapInfo?

    fun createBadgedIconBitmap(
        iconFactory: BaseIconFactory,
        drawable: Drawable,
        options: IconRequest
    ): BitmapInfo? {
        return iconFactory.use { factory ->
            factory.createBadgedIconBitmap(
                drawable,
                options
            )
        }
    }
}
