package com.lovely.bear.laboratory

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Paint
import android.text.TextUtils
import android.util.TypedValue
import androidx.annotation.Nullable
import kotlin.math.ceil

/**
 * 工具方法
 * @author guoyixiong
 */

inline fun <reified T> startActivity(context: Context) where T : Activity {
    context.startActivity(Intent(context, T::class.java))
}

fun dpToPx(value: Float, resources: Resources): Int {
    return ceil(value * resources.displayMetrics.density).toInt()
}

fun dpToPx(value: Float, context: Context): Int {
    return dpToPx(value, context.resources)
}

fun spToPx(value: Float, context: Context): Int {
    return ceil(
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            value,
            context.resources.displayMetrics
        )
    ).toInt()
}

fun getTextSizeByHeight(expect: Float): Float {
    val p = Paint()
    p.textSize = expect
    val step = 0.01F
//    val t = "你好"
//    val b = Rect()

    fun getH(): Float {
        val fontMetrics: Paint.FontMetrics = p.fontMetrics
        return fontMetrics.descent - fontMetrics.ascent + fontMetrics.leading
    }

    val first = getH()
    if (first > expect) {
        do {
            p.textSize = p.textSize - step
        } while (getH() > expect)
    }
    if (first < expect) {
        do {
            p.textSize = p.textSize + step
        } while (getH() < expect)
    }

    return p.textSize
}

fun <T : CharSequence> trimToSize(text: T, size: Int): T {
    var s = size
    if (TextUtils.isEmpty(text) || text.length <= s) return text
    if (Character.isHighSurrogate(text[s - 1]) && Character.isLowSurrogate(text[s])) {
        s -= 1
    }
    return text.subSequence(0, s) as T
}

fun clip(origin: CharSequence?, length: Int, end: String?): String {
    return when {
        origin == null -> {
            ""
        }
        origin.length <= length -> {
            origin.toString()
        }
        else -> TextUtils.concat(trimToSize(origin, length), end) as String
    }
}