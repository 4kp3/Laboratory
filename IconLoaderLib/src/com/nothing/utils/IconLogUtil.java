package com.nothing.utils;


import android.util.Log;

import com.android.launcher3.icons.BuildConfig;


/**
 * copy from launcher common module
 * @author yixiong.guo
 */
public class IconLogUtil {
    public final static String TAG = "NT-";

    // 自研日志工具开启开关时，会写这个settings值。App可以监听其来刷新Log的打印级别
    // public final static Uri LOG_SWITCH_URI = Settings.System.getUriFor("nt_log_capture");

    private static boolean DEBUG = BuildConfig.DEBUG;
    private final static boolean INFO = true;

    public static void initDebugFlag(boolean isLogSwitchOn) {
        DEBUG = isLogSwitchOn;
    }

    public static void changeDebugFlag(boolean isLogSwitchOn) {
        DEBUG = isLogSwitchOn;
    }

    public static void v(String message) {
        if (DEBUG || Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, message);
        }
    }

    public static void v(String tag, String message) {
        if (DEBUG || Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG + tag, message);
        }
    }

    public static void d(String message) {
        if (DEBUG || Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, message);
        }
    }

    public static void d(String tag, String message, Throwable e) {
        if (DEBUG || Log.isLoggable(TAG, Log.ERROR)) {
            Log.e(TAG + tag, message, e);
        }
    }

    public static void d(String tag, String message) {
        if (DEBUG || Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG + tag, message);
        }
    }

    public static void i(String message) {
        if (INFO || Log.isLoggable(TAG, Log.INFO)) {
            Log.i(TAG, message);
        }
    }

    public static void i(String tag, String message) {
        if (INFO || Log.isLoggable(TAG, Log.INFO)) {
            Log.i(TAG + tag, message);
        }
    }

    public static void w(String message) {
        if (DEBUG || Log.isLoggable(TAG, Log.WARN)) {
            Log.w(TAG, message);
        }
    }

    public static void w(String tag, String message) {
        if (DEBUG || Log.isLoggable(TAG, Log.WARN)) {
            Log.w(TAG + tag, message);
        }
    }

    public static void e(String message) {
        if (INFO || Log.isLoggable(TAG, Log.ERROR)) {
            Log.e(TAG, message);
        }
    }

    public static void e(String tag, String message) {
        if (INFO || Log.isLoggable(TAG, Log.ERROR)) {
            Log.e(TAG + tag, message);
        }
    }

    public static void e(String message, Throwable e) {
        if (DEBUG || Log.isLoggable(TAG, Log.ERROR)) {
            Log.e(TAG, message, e);
        }
    }

    public static void e(String tag, String message, Throwable e) {
        if (DEBUG || Log.isLoggable(TAG, Log.ERROR)) {
            Log.e(TAG + tag, message, e);
        }
    }

    public static void wtf(String message) {
        if (DEBUG || Log.isLoggable(TAG, Log.ASSERT)) {
            Log.wtf(TAG, message);
        }
    }

    public static void wtf(String tag, String message) {
        if (DEBUG || Log.isLoggable(TAG, Log.ASSERT)) {
            Log.wtf(TAG + tag, message);
        }
    }

    public static String getCallStack(int depth) {
        return getCallStack(depth, false);
    }

    public static String getCallstackWhenLogSwitchOn(int depth) {
        return getCallStack(depth, DEBUG);
    }

    /*
     * Convenient way to print call stack
     * */
    public static String getCallStack(int depth, boolean forcedPrint) {
        if (BuildConfig.DEBUG || forcedPrint) {
            // Avoid print useless element
            int ignoreLayer = 3;
            depth = depth + ignoreLayer;
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            StringBuilder stringBuilder = new StringBuilder();
            int i = 0;
            stringBuilder.append("----getCallStack total depth = " + depth + ", print depth = " + stackTrace.length + "----\n");
            for (StackTraceElement stackTraceElement : stackTrace) {
                if (i >= depth) {
                    break;
                }
                // Skip useless element
                if (i < ignoreLayer) {
                    i++;
                    continue;
                } else {
                    stringBuilder.append("    " + (i - ignoreLayer) + ": " + stackTraceElement.toString() + "\n");
                    i++;
                }
            }
            return stringBuilder.toString();
        }
        return "Don't print call stack in release build";
    }
}
