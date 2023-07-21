/*
Copyright (C), 2022, Nothing Technology
FileName: ThemedIconProvider
Author: benny.fang
Date: 2022/11/30 11:47
Description:  Extension of {@link IconProvider} with support for overriding theme icons
History:
<author> <time> <version> <desc>
 */

package com.nothing.launcher.icons

import android.content.Context
import android.util.ArrayMap
import android.util.Log
import com.android.launcher3.icons.IconProvider
import com.android.launcher3.icons.R
import com.nothing.launcher.icons.IconPackManager.Companion.instance
import com.nothing.launcher.icons.constant.IconPackStateConstant.USE_LOCAL_ICON_OVERRIDES
import org.xmlpull.v1.XmlPullParser


open class ThemedIconProvider : IconProvider {

    companion object {
        private const val TAG_ICON = "icon"
        private const val ATTR_PACKAGE = "package"
        private const val ATTR_DRAWABLE = "drawable"

        private const val TAG = "ThemedIconProvider"
    }

    private val disabledMap = emptyMap<String, ThemeData>()
    private var themedIconMap: Map<String, ThemeData>? = null
    private var supportsIconTheme = false


    constructor(context: Context?) : super(context) {
        setIconThemeSupported(instance.isThemedIconSelected())
    }

    fun setIconThemeSupported(isSupported: Boolean) {
        supportsIconTheme = isSupported
        themedIconMap = if (isSupported && canUseLocalIconOverrides()) null else disabledMap
    }

    open fun canUseLocalIconOverrides(): Boolean {
        return USE_LOCAL_ICON_OVERRIDES
    }

    override fun getThemeDataForPackage(packageName: String?): ThemeData? {
        return getThemedIconMap()?.get(packageName)
    }

    override fun getSystemIconState(): String {
        return super.getSystemIconState() + if (supportsIconTheme) ",with-theme" else ",no-theme"
    }

    private fun getThemedIconMap(): Map<String, ThemeData>? {
        if (themedIconMap != null) {
            return themedIconMap
        }
        val map = ArrayMap<String, ThemeData>()
        val res = mContext.resources
        kotlin.runCatching {
            res.getXml(R.xml.nt_grayscale_icon_map).use { parser ->
                val depth = parser.depth
                var type: Int
                while (parser.next().also { type = it } != XmlPullParser.START_TAG
                    && type != XmlPullParser.END_DOCUMENT
                );
                while ((parser.next().also { type = it } != XmlPullParser.END_TAG
                            || parser.depth > depth) && type != XmlPullParser.END_DOCUMENT
                ) {
                    if (type != XmlPullParser.START_TAG) {
                        continue
                    }
                    if (TAG_ICON == parser.name) {
                        val pkg =
                            parser.getAttributeValue(null, ATTR_PACKAGE)
                        val iconId = parser.getAttributeResourceValue(
                            null,
                            ATTR_DRAWABLE,
                            0
                        )
                        if (iconId != 0 && pkg?.isNotEmpty() == true) {
                            map[pkg] = ThemeData(res, iconId)
                        }
                    }
                }
            }
        }.onFailure {
            Log.e(TAG, "Unable to parse icon map", it)
        }
        themedIconMap = map
        return themedIconMap
    }
}