package com.lovely.bear.laboratory.widget

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import com.lovely.bear.laboratory.widget.action.ActionView
import com.lovely.bear.laboratory.widget.action.LaunchActivityItem

open class LaunchView @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    ActionView(context, attr, defStyleAttr) {

    private val flagsChooseView: FlagsChooseView

    private val launchFlag: Int?
        get() {
            return flagsChooseView.launchFlag
        }

    init {
        flagsChooseView = FlagsChooseView(context)
        addView(flagsChooseView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    fun <C : Activity> addItem(desc: String, clazz: Class<C>) {
        super.addItem(
            LaunchActivityItem(desc = desc, clazz = clazz, context = context) {
                launchFlag ?: 0
            })
    }
}