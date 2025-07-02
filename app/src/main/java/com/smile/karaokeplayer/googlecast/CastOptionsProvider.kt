package com.smile.karaokeplayer.googlecast

import android.content.Context
import android.util.Log
import com.google.android.gms.cast.CastMediaControlIntent
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import com.google.android.gms.cast.framework.media.CastMediaOptions
import com.google.android.gms.cast.framework.media.NotificationOptions
import com.smile.karaokeplayer.BaseActivity

class CastOptionsProvider : OptionsProvider {
    override fun getCastOptions(appContext: Context): CastOptions {
        Log.d(TAG, "getCastOptions")
        val notificationOptions = NotificationOptions.Builder()
            .setTargetActivityClassName(BaseActivity::class.java.name)
            .build()
        val mediaOptions = CastMediaOptions.Builder()
            .setNotificationOptions(notificationOptions)
            .setExpandedControllerActivityClassName(BaseActivity::class.java.name)
            .build()

        val castOptions = CastOptions.Builder()
            .setReceiverApplicationId(
                CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID)
            .setCastMediaOptions(mediaOptions)
            .build()
        Log.d(TAG, "getCastOptions.CastOptions.castOptions = $castOptions")

        return castOptions
    }

    override fun getAdditionalSessionProviders(context: Context): List<SessionProvider>? {
        Log.d(TAG, "getAdditionalSessionProviders")
        return null
    }

    companion object {
        private const val TAG = "CastOptionsProvider"
    }
}