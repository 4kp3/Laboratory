package com.lovely.bear.laboratory.widget.action

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import com.lovely.bear.laboratory.R
import com.lovely.bear.laboratory.launch.*

open class ActionView @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    LinearLayout(context, attr, defStyleAttr) {

    init {
        orientation = VERTICAL
        dividerDrawable = context.getDrawable(R.drawable.black_line)

        gravity = Gravity.CENTER_HORIZONTAL
    }

     fun addItem(actionItem: ActionItem) {
        val tv = Button(context)

        tv.setOnClickListener {
            actionItem.doAction()
        }

        tv.text = actionItem.desc
        val lp = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        addView(tv, lp)
    }

}