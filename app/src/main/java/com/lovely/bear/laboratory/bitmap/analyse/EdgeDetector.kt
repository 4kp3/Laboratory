package com.lovely.bear.laboratory.bitmap.analyse

import android.graphics.Bitmap

interface EdgeDetector {
    fun detect(source:Bitmap): EdgeResult
}