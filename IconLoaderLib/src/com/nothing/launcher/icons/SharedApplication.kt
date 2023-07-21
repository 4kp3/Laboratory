package com.nothing.launcher.icons

import android.app.Application
import android.content.Context

/*
Copyright (C), 2022, Nothing Technology
FileName: SharedApplication
Author: benny.fang
Date: 2022/11/23 15:14
Description: The singleton that holds the Application application, and it needs to be initialized when App#onCreate
History:
<author> <time> <version> <desc>
 */

object SharedApplication {
    private lateinit var application: Application
    fun init(app: Application) {
        application = app
    }

    fun getContext(): Context = application.applicationContext
}