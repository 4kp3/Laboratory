/*
Copyright (C), 2022, Nothing Technology
FileName: IconPackStateConstant
Author: benny.fang
Date: 2022/11/21 15:18
Description: Status constants for icon packs
History:
<author> <time> <version> <desc>
 */
package com.nothing.launcher.icons.constant

import com.android.launcher3.icons.BuildConfig.ICON_PACK_MANAGER_APP_NAME
import com.nothing.launcher.icons.IconPackManager
import com.nothing.launcher.icons.model.data.CachedIconEntity
import com.nothing.launcher.icons.model.data.CachedIconEntity.Companion.systemIconEntity
import com.nothing.launcher.icons.model.data.CachedIconEntity.Companion.themedIconEntity
import com.nothing.launcher.icons.model.data.CachedIconEntity.Companion.themedIconEntityNothing

object IconPackStateConstant {
    const val SUPPORT_THEMED_ICON = true
    const val USE_LOCAL_ICON_OVERRIDES = true
    const val KEY_SELECTED_ICON_PACK = "nothing_icon_pack"
    const val KEY_NOTHING_ICON_FORCE_RENDER_ENABLE = "nothing_icon_pack_force_render_enable"
    const val EXTRA_NOTHING_ICON_FORCE_RENDER_ENABLE = "nothing_icon_pack_force_render_enable"
    const val NOTHING_ICON_PACK_PACKAGE_ID = "com.nothing.icon"
    const val THEMED_ICON_PACK_IN_USE = "themed_icon_pack_in_use"
    const val VALUE_SYSTEM_ICON = "SYSTEM_ICONS" //系统默认图标
    const val VALUE_THEMED_ICON = "THEMED_ICONS" //带主题的图标
    // Added by stephen.bi for NOS-1721 @{
    const val VALUE_THEMED_ICON_NOTHING = "THEMED_ICONS_NOTHING" //Nothing风格带主题的图标
    // @}
    /* 默认图标包，这是以settings方式存储的值 */
    const val DEFAULT_VALUE = VALUE_SYSTEM_ICON
    val defaultIconEntity = systemIconEntity

    /*
    * 预置的图标包实体，存储了简单的信息
    * */
    val preloadIconEntityMap = LinkedHashMap<String, CachedIconEntity>().also {
        it[VALUE_SYSTEM_ICON] = systemIconEntity
        if (SUPPORT_THEMED_ICON) {
            // Added by stephen.bi for NOS-1721 @{
            if (IconPackManager.instance.supportHueChangeOnThemedIcons) {
                it[VALUE_THEMED_ICON_NOTHING] = themedIconEntityNothing
            }
            // @}
            it[VALUE_THEMED_ICON] = themedIconEntity
        }
    }
    val preloadIconPackList: List<String> = preloadIconEntityMap.keys.toList()

    // Define the manager name, only it can update the state of icon pack
    fun getManagerName(): String = ICON_PACK_MANAGER_APP_NAME
    fun getSystemIconOwner() = getManagerName()
    fun getThemedIconOwner() = getManagerName() + ".themed_icons"
    // Added by stephen.bi for NOS-1721 @{
    fun getThemedIconNothingOwner() = getManagerName() + ".themed_icons_nothing"
    // @}
}