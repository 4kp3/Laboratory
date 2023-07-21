package com.lovely.bear.laboratory.bitmap.data

import com.lovely.bear.laboratory.bitmap.analyse.ContentBounding

/*
* Copyright (C), 2023, Nothing Technology
* FileName: MakeEdgeBitmap
* Author: yixiong.guo
* Date: 2023/7/16 17:51
* Description:  
* History:
* <author> <time> <version> <desc>
*/

fun makeEdgeBitmap(image: Image) {
    image.edgeBitmap = ContentBounding.get(image.bitmap)
}