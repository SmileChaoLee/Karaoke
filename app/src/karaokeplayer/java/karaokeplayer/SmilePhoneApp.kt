package karaokeplayer

import android.util.Log
import com.facebook.ads.AudienceNetworkAds
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.InitializationStatus
import com.smile.karaoke.BuildConfig
import com.smile.karaoke.R
import com.smile.karaoke.SmileAppBase
import com.smile.karaoke.constants.CommonConstants
import com.smile.karaoke.googlecast.InitCastContext

class SmilePhoneApp : SmileAppBase() {

    override fun onCreate() {
        super.onCreate()
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

    override fun initAds() {
        AudienceNetworkAds.initialize(this)
        // facebookInterstitialID = "1712962715503258_1712963252169871";
        // facebookInterstitialID = testString + facebookInterstitialID;
        // facebookInterstitial = new FacebookInterstitial(appContext,
        //         facebookInterstitialID);
        // for debug mode and for facebook
        val testString = if (BuildConfig.DEBUG) "IMG_16_9_APP_INSTALL#" else ""
        facebookBannerID = testString + "1712962715503258_2019623008170559"
        googleAdMobAppID = "ca-app-pub-8354869049759576~5549171584"
        // googleAdMobInterstitialID = "ca-app-pub-8354869049759576/1418354889";
        googleAdMobBannerID = "ca-app-pub-8354869049759576/8267060571"
        googleAdMobNativeID = "ca-app-pub-8354869049759576/7985456524"
        // google
        MobileAds.initialize(applicationContext
        ) { initializationStatus: InitializationStatus? ->
            Log.d(TAG, "Google AdMob was initialized successfully.")
        }
        // adMobInterstitial = new AdMobInterstitial(appContext, googleAdMobInterstitialID);
        // for the chrome cast
    }

    override fun initCastContext() {
        castContext = InitCastContext.getInstance(this)
        Log.d(TAG, "castContext = $castContext")
    }

    companion object {
        private const val TAG = "SmilePhoneApp"
    }
}
