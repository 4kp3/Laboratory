package com.lovely.bear.laboratory.bitmap.mono

import android.graphics.drawable.AdaptiveIconDrawable
import android.util.Size
import com.lovely.bear.laboratory.bitmap.utils.PixelUtils
import com.lovely.bear.laboratory.bitmap.analyse.EdgeResult
import com.lovely.bear.laboratory.bitmap.data.AdaptiveIconImage
import com.lovely.bear.laboratory.bitmap.data.Image
import com.lovely.bear.laboratory.bitmap.data.makeEdgeBitmap
import com.lovely.bear.laboratory.bitmap.dpSize
import com.lovely.bear.laboratory.bitmap.trackIcon

/*
* Copyright (C), 2023, Nothing Technology
* FileName: MakeMono
* Author: yixiong.guo
* Date: 2023/7/18 19:26
* Description:  
* History:
* <author> <time> <version> <desc>
*/

private const val TAG = "makeMono"

/**
 * mono处理时，需要先缩放，转换灰度，再把灰度图放大到icon大小
 */
fun makeMono(image: Image): Mono {

    trackIcon("makeMono start")

    val candidate =
        if (image is AdaptiveIconImage) {
            val mono = if (image.icon is AdaptiveIconDrawable) image.icon.monochrome else null
            if (mono != null) {
                val monoSize = Size(mono.intrinsicWidth, mono.intrinsicHeight)
                mono.setBounds(0,0,monoSize.width,monoSize.height)
                trackIcon("使用原始 mono, $monoSize,${monoSize.dpSize()}")
                trackIcon("makeMono end")
                return Mono.Original(mono.toBitmap(monoSize), monoSize)
            }

            layerSelector(image)

        } else image

    // todo 纯色检测，纯色不应该传递给mono？

    val req = MonoRequest(candidate)
    return MonoBuilder.buildAuto(req).also {
        trackIcon("makeMono end")
    }
}

private fun layerSelector(image: AdaptiveIconImage): Image {

    makeEdgesIfNot(image.fgBitmap)
    val fgEdge = image.fgBitmap.edgeBitmap!!

    return when {
        fgEdge.isCompletelyOpaque -> {
            trackIcon("前景完全不透明，使用前景，忽略背景")
            image.fgBitmap
        }

        fgEdge is EdgeResult.Blank && fgEdge.isTransparent -> {
            trackIcon("前景透明，使用背景")
            image.bgBitmap
        }

        else -> {
            makeEdgesIfNot(image.bgBitmap)
            val bgEdge = image.bgBitmap.edgeBitmap!!
            when {
                // 纯色图
                bgEdge is EdgeResult.Blank -> {
                    when {
                        bgEdge.isTransparent -> {
                            trackIcon("背景透明，使用前景")
                            image.fgBitmap
                        }
                        // todo 传出color信息给灰度转换器
                        PixelUtils.isAlmostWhitePixelStrict(bgEdge.color) -> {
                            trackIcon("背景几乎白色，使用前景")
                            image.fgBitmap
                        }

                        PixelUtils.isAlmostBlackPixelStrict(bgEdge.color) -> {
                            trackIcon("背景几乎黑色，使用前景")
                            image.fgBitmap
                        }

                        else -> {
                            // 暂时使用系统返回的图片作为合成图
                            trackIcon("使用混合")
                            image
                        }
                    }
                }

                // 彩图，使用合成图
                else -> {
                    trackIcon("使用混合")
                    image
                }
            }
        }
    }
}

/**
 * not null
 */
private fun makeEdgesIfNot(image: Image) {
    if (image.edgeBitmap != null)
        makeEdgeBitmap(image)
}
