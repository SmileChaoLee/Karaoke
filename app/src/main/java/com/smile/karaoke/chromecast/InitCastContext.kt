package com.smile.karaoke.chromecast

import android.content.Context
import com.google.android.gms.cast.framework.CastContext
import com.smile.karaoke.utilities.LogUtil

object InitCastContext {
    private const val TAG = "InitCastContext"
    fun getInstance(context: Context): CastContext? {
        try {
            val instance = CastContext.getSharedInstance(context)
            LogUtil.i(TAG, "getInstance.CastContext initialized: $instance")
            return instance
        } catch (e: Exception) {
            LogUtil.e(TAG, "getInstance.Failed initialize CastContext: ${e.message}", e)
            return null
        }
    }
}