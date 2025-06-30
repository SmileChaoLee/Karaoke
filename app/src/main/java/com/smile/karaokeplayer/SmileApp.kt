package com.smile.karaokeplayer

import android.content.res.Configuration
import android.util.Log
import androidx.multidex.MultiDexApplication
import com.facebook.ads.AudienceNetworkAds
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.cast.framework.CastContext
import com.smile.karaokeplayer.constants.CommonConstants

class SmileApp : MultiDexApplication() {
    var googleAdMobAppID = ""
    var testString = ""
    var leftChannelString = ""
    var rightChannelString = ""
    var stereoChannelString = ""
    // public FacebookInterstitial facebookInterstitial;
    // public AdMobInterstitial adMobInterstitial;
    var castContext: CastContext? = null

    override fun onCreate() {
        super.onCreate()
        val appContext = applicationContext
        leftChannelString = getString(R.string.leftChannelString)
        rightChannelString = getString(R.string.rightChannelString)
        stereoChannelString = getString(R.string.stereoChannelString)
        audioChannelMap.put(CommonConstants.LEFT_CHANNEL, leftChannelString)
        audioChannelMap.put(CommonConstants.RIGHT_CHANNEL, rightChannelString)
        audioChannelMap.put(CommonConstants.STEREO, stereoChannelString)
        audioChannelReverseMap.put(leftChannelString, CommonConstants.LEFT_CHANNEL)
        audioChannelReverseMap.put(rightChannelString, CommonConstants.RIGHT_CHANNEL)
        audioChannelReverseMap.put(stereoChannelString, CommonConstants.STEREO)

        // for debug mode and for facebook
        if (BuildConfig.DEBUG) {
            testString = "IMG_16_9_APP_INSTALL#"
        }
        setGoogleAdMobAndFacebookAudioNetwork()
        // google
        MobileAds.initialize(appContext
        ) { initializationStatus: InitializationStatus? ->
            Log.d(TAG, "Google AdMob was initialized successfully.")
        }
        // adMobInterstitial = new AdMobInterstitial(appContext, googleAdMobInterstitialID);
        // for the chrome cast
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "com.smile.karaokeplayer.BuildConfig.DEBUG")
            try {
                castContext = CastContext.getSharedInstance(this)
            } catch (e: RuntimeException) {
                castContext = null
                Log.e(TAG, "onCreate.Failed initialize CastContext", e)
            }
        }
        Log.d(TAG, "castContext = $castContext")
    }

    private fun setGoogleAdMobAndFacebookAudioNetwork() {
        AudienceNetworkAds.initialize(this)
        // facebookInterstitialID = "1712962715503258_1712963252169871";
        // facebookInterstitialID = testString + facebookInterstitialID;
        // facebookInterstitial = new FacebookInterstitial(appContext,
        //         facebookInterstitialID);
        facebookBannerID = testString + "1712962715503258_2019623008170559"
        googleAdMobAppID = "ca-app-pub-8354869049759576~5549171584"
        // googleAdMobInterstitialID = "ca-app-pub-8354869049759576/1418354889";
        googleAdMobBannerID = "ca-app-pub-8354869049759576/8267060571"
        googleAdMobNativeID = "ca-app-pub-8354869049759576/7985456524"
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d(TAG, "Configuration changed")
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.w(TAG, "System is running low on memory")
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Log.w(TAG, "onTrimMemory, level: = $level")
    }

    companion object {
        private const val TAG = "SmileApp"
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
