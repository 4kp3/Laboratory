package com.lovely.bear.laboratory.bitmap.data

import com.lovely.bear.laboratory.R

/*
* Copyright (C), 2023, Nothing Technology
* FileName: LocalIconLoader
* Author: yixiong.guo
* Date: 2023/7/16 17:48
* Description:  
* History:
* <author> <time> <version> <desc>
*/
object LocalIconLoader {
    private val resImages = listOf(R.mipmap.icon_ios_cloud_192)
    fun load(): List<Image> {
        return resImages.map {
            ResImage(it).also { resImage ->
                makeEdgeBitmap(resImage)
            }
        }
    }

}