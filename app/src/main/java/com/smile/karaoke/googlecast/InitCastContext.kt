package com.smile.karaoke.googlecast

import android.content.Context
import android.util.Log
import com.google.android.gms.cast.framework.CastContext

object InitCastContext {
    private const val TAG = "InitCastContext"
    fun getInstance(context: Context): CastContext? {
        try {
            return CastContext.getSharedInstance(context)
        } catch (e: RuntimeException) {
            Log.e(TAG, "getInstance.Failed initialize CastContext", e)
            return null
        }
    }
}