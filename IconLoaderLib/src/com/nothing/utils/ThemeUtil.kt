/*
Copyright (C), 2023, Nothing Technology
FileName: ThemeUtil
Author: stephen.bi
Date: 2023/03/08 16:02
Description: A tool class about the phone theme.
History:
<author> <time> <version> <desc>
 */
package com.nothing.utils

import android.content.Context
import android.content.res.Configuration

object ThemeUtil {

    fun isDarkTheme(context: Context): Boolean {
        val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }
}