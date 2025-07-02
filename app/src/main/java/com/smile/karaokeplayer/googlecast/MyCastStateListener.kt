package com.smile.karaokeplayer.googlecast

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.media3.common.util.UnstableApi
import com.google.android.gms.cast.framework.CastState
import com.google.android.gms.cast.framework.CastStateListener
import com.smile.karaokeplayer.R
import com.smile.karaokeplayer.SmileApp
import com.smile.karaokeplayer.fragments.PlayerBaseFragment
import com.smile.smilelibraries.utilities.ScreenUtil

@UnstableApi
class MyCastStateListener(
    fragment: PlayerBaseFragment
): CastStateListener {

    private val mActivity: Activity? = fragment.activity
    private val toastTextSize = SmileApp.toastTextSize

    init {
        Log.d(TAG, "MyCastStateListener is created")
    }

    @SuppressLint("LongLogTag")
    override fun onCastStateChanged(i: Int) {
        Log.d(TAG, "onCastStateChanged.i = $i")
        var msgString = ""
        when (i) {
            CastState.NO_DEVICES_AVAILABLE -> {
                Log.d(TAG, "CastState is NO_DEVICES_AVAILABLE.")
                mActivity?.let {
                    msgString = it.getString(R.string.no_chromecast_devices_avaiable)
                }
            }
            CastState.NOT_CONNECTED -> {
                Log.d(TAG, "CastState is NOT_CONNECTED.")
                mActivity?.let {
                    msgString = it.getString(R.string.chromecast_not_connected)
                }
            }
            CastState.CONNECTING -> {
                Log.d(TAG, "CastState is CONNECTING.")
                mActivity?.let {
                    msgString = it.getString(R.string.chromecast_is_connecting)
                }
            }
            CastState.CONNECTED -> {
                Log.d(TAG, "CastState is CONNECTED.")
                mActivity?.let {
                    msgString = it.getString(R.string.chromecast_is_connected)
                }
            }
            else -> {
                Log.d(TAG, "CastState is unknown.")
            }
        }
        ScreenUtil.showToast(
            mActivity, msgString,
            toastTextSize,
            ScreenUtil.FontSize_Pixel_Type,
            Toast.LENGTH_SHORT
        )
    }

    companion object {
        private const val TAG = "MyCastStateListener"
    }
}
