/*
Copyright (C), 2022, Nothing Technology
FileName: OnIconPackPickedListener
Author: benny.fang
Date: 2022/11/22 15:57
Description: A listener for the currently selected icon pack
History:
<author> <time> <version> <desc>
 */
package com.nothing.launcher.icons.iconpack.cache

interface OnIconPackPickedListener {
    fun onNewPackSelected(packageName: String)
}