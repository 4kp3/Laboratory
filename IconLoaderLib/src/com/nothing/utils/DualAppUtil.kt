/*
Copyright (C), 2022, Nothing Technology
FileName: DualAppUtil
Author: batterman
Date: 2023/02/17 16:02
Description: DualApp tools class(双开相关辅助类：获取双开图标和双开信息等)
History:
<author> <time> <version> <desc>
 */
package src.com.nothing.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.UserHandle
import com.android.launcher3.icons.R

object DualAppUtil {
    const val DUAL_APP_USER_HANDLE_ID = 999

    // 获取双开角标或者work的user的角标，依据传入的user判断，如果是当前用户user是不允许调用这个方法的
    fun getUserBadge(user: UserHandle, context: Context): Drawable? {
        if (isDualAppUser(user)) {
            val res = Resources.getSystem()
                .getIdentifier("zzz_dual_app_icon_badge_plain", "drawable", "android")
            return context.getDrawable(res)
        }
        return context.getDrawable(R.drawable.ic_work_app_badge)
    }

    // 判断是否是双开的user
    fun isDualAppUser(user: UserHandle): Boolean {
        return user.getIdentifier() === DUAL_APP_USER_HANDLE_ID
    }
}