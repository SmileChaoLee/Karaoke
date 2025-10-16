package com.smile.karaoke.chromecast

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.google.android.gms.cast.CastMediaControlIntent
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import com.google.android.gms.cast.framework.media.CastMediaOptions
import com.google.android.gms.cast.framework.media.NotificationOptions
import com.smile.karaoke.BaseActivity
import com.smile.karaoke.utilities.LogUtil

class CastOptionsProvider : OptionsProvider {
    @OptIn(UnstableApi::class)
    override fun getCastOptions(appContext: Context): CastOptions {
        LogUtil.d(TAG, "getCastOptions")
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
        LogUtil.d(TAG, "getCastOptions.CastOptions.castOptions = $castOptions")

        return castOptions
    }

    override fun getAdditionalSessionProviders(context: Context): List<SessionProvider>? {
        LogUtil.d(TAG, "getAdditionalSessionProviders")
        return null
    }

    companion object {
        private const val TAG = "CastOptionsProvider"
    }
}