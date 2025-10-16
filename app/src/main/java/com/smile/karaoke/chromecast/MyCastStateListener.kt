package com.smile.karaoke.chromecast

import android.annotation.SuppressLint
import android.util.Log
import androidx.media3.common.util.UnstableApi
import com.google.android.gms.cast.framework.CastState
import com.google.android.gms.cast.framework.CastStateListener
import com.smile.karaoke.utilities.LogUtil

@UnstableApi
class MyCastStateListener: CastStateListener {

    init {
        LogUtil.d(TAG, "MyCastStateListener is created")
    }

    @SuppressLint("LongLogTag")
    override fun onCastStateChanged(i: Int) {
        LogUtil.d(TAG, "onCastStateChanged.i = $i")
        when (i) {
            CastState.NO_DEVICES_AVAILABLE -> {
                LogUtil.d(TAG, "CastState is NO_DEVICES_AVAILABLE.")
            }
            CastState.NOT_CONNECTED -> {
                LogUtil.d(TAG, "CastState is NOT_CONNECTED.")
            }
            CastState.CONNECTING -> {
                Log.d(TAG, "CastState is CONNECTING.")
            }
            CastState.CONNECTED -> {
                LogUtil.d(TAG, "CastState is CONNECTED.")
            }
            else -> {
                LogUtil.d(TAG, "CastState is unknown.")
            }
        }
    }

    companion object {
        private const val TAG = "MyCastStateListener"
    }
}
