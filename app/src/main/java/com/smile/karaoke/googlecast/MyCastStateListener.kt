package com.smile.karaoke.googlecast

import android.annotation.SuppressLint
import android.util.Log
import androidx.media3.common.util.UnstableApi
import com.google.android.gms.cast.framework.CastState
import com.google.android.gms.cast.framework.CastStateListener

@UnstableApi
class MyCastStateListener: CastStateListener {

    init {
        Log.d(TAG, "MyCastStateListener is created")
    }

    @SuppressLint("LongLogTag")
    override fun onCastStateChanged(i: Int) {
        Log.d(TAG, "onCastStateChanged.i = $i")
        when (i) {
            CastState.NO_DEVICES_AVAILABLE -> {
                Log.d(TAG, "CastState is NO_DEVICES_AVAILABLE.")
            }
            CastState.NOT_CONNECTED -> {
                Log.d(TAG, "CastState is NOT_CONNECTED.")
            }
            CastState.CONNECTING -> {
                Log.d(TAG, "CastState is CONNECTING.")
            }
            CastState.CONNECTED -> {
                Log.d(TAG, "CastState is CONNECTED.")
            }
            else -> {
                Log.d(TAG, "CastState is unknown.")
            }
        }
    }

    companion object {
        private const val TAG = "MyCastStateListener"
    }
}
