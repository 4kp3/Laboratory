package com.lovely.bear.laboratory.widget

import android.content.Context
import android.util.AttributeSet
import com.lovely.bear.laboratory.launch.*

class ActivityTaskLaunchModeLaunchView @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    LaunchView(context, attr, defStyleAttr) {

    init {
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
}