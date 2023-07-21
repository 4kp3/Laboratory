package com.lovely.bear.laboratory.bitmap.analyse

import android.graphics.Bitmap

object ContentBounding {

    fun get(source:Bitmap): EdgeResult {

        val edgeDetector: EdgeDetector = SobelEdgeDetector()
        return edgeDetector.detect(source)

    }


}