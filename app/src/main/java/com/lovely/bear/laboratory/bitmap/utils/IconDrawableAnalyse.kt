package com.lovely.bear.laboratory.bitmap.utils

import com.lovely.bear.laboratory.bitmap.data.iconimage

/*
* copyright (c), 2023, nothing technology
* filename: adaptivedrawableanalyse
* author: yixiong.guo
* date: 2023/7/28 22:31
* description:
* history:
* <author> <time> <version> <desc>
*/

/**
 * 分析系统加载进来的图标尺寸
 * 和108的关系
 *
 * todo 记得尺寸在launcher中需要动态获取，避免分辨率改变导致硬编码出错
 *
 * thread:launcher-loader|launchericons 244594773:mfillresicondpi=640, miconbitmapsize=152 dp=57
 * thread:taskthumbnailiconcache-1|baseiconfactory 205304660:mfillresicondpi=420, miconbitmapsize=116 dp=44
 */
fun analyse(icons:list<iconimage>) {

}

data class icondrawableanalyseimage(val iconimage: iconimage)