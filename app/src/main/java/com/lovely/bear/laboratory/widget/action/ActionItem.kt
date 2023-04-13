package com.lovely.bear.laboratory.widget.action

import android.app.Activity
import android.content.Context
import android.content.Intent

interface ActionItem {
    val desc: String
    fun doAction()
}

class LaunchActivityItem<ACTIVITY : Activity>(
    override val desc: String,
    private val clazz: Class<ACTIVITY>,
    private val context: Context,
    private val launchFlags: (() -> Int)?,
) : ActionItem {
    override fun doAction() {
        val i = Intent(context.applicationContext, clazz)
        launchFlags?.invoke()?.let {
            i.setFlags(it)
        }
        context.startActivity(i)
    }
}