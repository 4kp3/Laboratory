package com.lovely.bear.laboratory.widget

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import com.lovely.bear.laboratory.R
import com.lovely.bear.laboratory.launch.*

class LaunchView @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    LinearLayout(context, attr, defStyleAttr) {

    private val flagsChooseView: FlagsChooseView

    private val launchFlag: Int?
        get() {
            return flagsChooseView.launchFlag
        }

    init {
        orientation = VERTICAL
        dividerDrawable = context.getDrawable(R.drawable.black_line)

        gravity = Gravity.CENTER_HORIZONTAL

        flagsChooseView = FlagsChooseView(context)
        addView(flagsChooseView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

        addItem("打开 LaunchTestStandardActivity 标准页面", LaunchTestStandardActivity::class.java)
        addItem("打开 LaunchTestSingleTopActivity 单顶页面", LaunchTestSingleTopActivity::class.java)
        addItem("打开 LaunchTestSingleTaskActivity 单任务页面", LaunchTestSingleTaskActivity::class.java)
        addItem(
            "打开 LaunchTestSingleInstanceActivity 独立任务页面",
            LaunchTestSingleInstanceActivity::class.java
        )
        addItem(
            "打开 LaunchTestSingleTaskIslandAffiActivity 独立任务页面\n指定任务栈 ",
            LaunchTestSingleTaskIslandAffiActivity::class.java
        )
    }

    private fun <C : Activity> addItem(text: String, clazz: Class<C>) {
        val tv = Button(context)


        tv.setOnClickListener {
            val i = Intent(context.applicationContext, clazz)
            launchFlag?.let { i.setFlags(launchFlag!!) }
            context.startActivity(i)
        }

        tv.text = text
        val lp = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        addView(tv, lp)
    }

}