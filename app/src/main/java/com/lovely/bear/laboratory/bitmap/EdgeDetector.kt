package com.lovely.bear.laboratory.bitmap

import android.graphics.Bitmap

interface EdgeDetector {
    fun detect(source:Bitmap): Result
}