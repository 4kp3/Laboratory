package com.lovely.bear.laboratory.widget

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.core.view.updatePadding
import com.lovely.bear.laboratory.util.dpToPx

class FlagsChooseView @JvmOverloads constructor (context: Context, attr: AttributeSet? = null, defStyleAttr: Int = 0) :
    LinearLayout(context, attr) {

    init {
        orientation = VERTICAL
        //dividerDrawable = context.getDrawable(R.drawable.black_line)

        //gravity = Gravity.CENTER_HORIZONTAL

        addItem("FLAG_ACTIVITY_SINGLE_TOP", Intent.FLAG_ACTIVITY_SINGLE_TOP)
        addItem("FLAG_ACTIVITY_NEW_TASK", Intent.FLAG_ACTIVITY_NEW_TASK)
        addItem("FLAG_ACTIVITY_CLEAR_TOP", Intent.FLAG_ACTIVITY_CLEAR_TOP)

    }

    var launchFlag: Int? = null

    private fun addItem(text: String, value: Int) {
        val rb = CheckBox(context)
        rb.text = text

        rb.setOnCheckedChangeListener { v, isChecked ->
            if (isChecked) {
                if (launchFlag == null) {
                    launchFlag = value
                } else {
                    launchFlag = launchFlag!! or value
                }
            } else {
                if (launchFlag != null) {
                    launchFlag = launchFlag!! and value.inv()
                }
            }
        }

        rb.updatePadding(left=dpToPx(4F,context),right=dpToPx(4F,context))
        val lp = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)

        addView(rb, lp)
    }

}