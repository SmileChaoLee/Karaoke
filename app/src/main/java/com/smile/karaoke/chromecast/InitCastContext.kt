package com.smile.karaoke.chromecast

import android.content.Context
import com.google.android.gms.cast.framework.CastContext
import com.smile.karaoke.utilities.LogUtil

object InitCastContext {
    private const val TAG = "InitCastContext"
    fun getInstance(context: Context): CastContext? {
        try {
            return CastContext.getSharedInstance(context)
        } catch (e: RuntimeException) {
            LogUtil.e(TAG, "getInstance.Failed initialize CastContext", e)
            return null
        }
    }
}