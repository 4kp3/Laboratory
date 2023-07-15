package com.lovely.bear.laboratory.bitmap

import android.graphics.Bitmap

object ContentBounding {

    fun get(source:Bitmap):Result{

        val edgeDetector:EdgeDetector = SobelEdgeDetector()
        return edgeDetector.detect(source)

    }


}