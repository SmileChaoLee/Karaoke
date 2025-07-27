package com.smile.karaoke

import android.content.res.Configuration
import android.util.Log
import androidx.multidex.MultiDexApplication
import com.google.android.gms.cast.framework.CastContext
import com.smile.karaoke.constants.CommonConstants

abstract class SmileAppBase : MultiDexApplication() {
    var leftChannelString = ""
    var rightChannelString = ""
    var stereoChannelString = ""
    var castContext: CastContext? = null
    var googleAdMobAppID = ""

    abstract fun initAds()
    abstract fun initCastContext()

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        leftChannelString = getString(R.string.leftChannelString)
        rightChannelString = getString(R.string.rightChannelString)
        stereoChannelString = getString(R.string.stereoChannelString)
        audioChannelMap.put(CommonConstants.LEFT_CHANNEL, leftChannelString)
        audioChannelMap.put(CommonConstants.RIGHT_CHANNEL, rightChannelString)
        audioChannelMap.put(CommonConstants.STEREO, stereoChannelString)
        audioChannelReverseMap.put(leftChannelString, CommonConstants.LEFT_CHANNEL)
        audioChannelReverseMap.put(rightChannelString, CommonConstants.RIGHT_CHANNEL)
        audioChannelReverseMap.put(stereoChannelString, CommonConstants.STEREO)

        initAds()
        initCastContext()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d(TAG, "Configuration changed")
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.w(TAG, "System is running low on memory")
    }

    override fun onTerminate() {
        super.onTerminate()
        castContext == null
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Log.w(TAG, "onTrimMemory, level: = $level")
    }

    companion object {
        private const val TAG = "SmileAppBase"
        @JvmField
        var textFontSize: Float = 0f
        @JvmField
        var toastTextSize: Float = 0f
        @JvmField
        var fontSize: Float = 0f
        @JvmField
        val audioChannelMap = LinkedHashMap<Int, String>()
        @JvmField
        val audioChannelReverseMap = LinkedHashMap<String, Int>()
        var facebookBannerID = ""
        var googleAdMobBannerID = ""
        var googleAdMobNativeID = ""
    }
}
