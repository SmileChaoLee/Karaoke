package com.smile.karaoke.utilities

import android.util.Log
import com.smile.karaoke.BuildConfig

object LogUtil {

    @JvmStatic
    fun d(tag: String, msg: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg)
        }
    }

    @JvmStatic
    fun i(tag: String, msg: String) {
        Log.i(tag, msg)
    }

    @JvmStatic
    fun e(tag: String, msg: String, tr: Throwable? = null) {
        if (tr != null) {
            Log.e(tag, msg, tr)
        } else {
            Log.e(tag, msg)
        }
    }

    @JvmStatic
    fun w(tag: String, msg: String, tr: Throwable? = null) {
        if (tr != null) {
            Log.w(tag, msg, tr)
        } else {
            Log.w(tag, msg)
        }
    }
}