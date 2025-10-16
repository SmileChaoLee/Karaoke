package karaokeplayer

import android.app.Activity
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.facebook.ads.AudienceNetworkAds
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.InitializationStatus
import com.smile.karaoke.BuildConfig
import com.smile.karaoke.SmileAppBase
import com.smile.karaoke.utilities.LogUtil
import com.smile.nativetemplates_models.GoogleAdMobNativeTemplate
import com.smile.smilelibraries.show_banner_ads.SetBannerAdView

class SmileKaraokeApp : SmileAppBase() {

    override fun onCreate() {
        super.onCreate()
        LogUtil.i(TAG, "onCreate")
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
            LogUtil.d(TAG, "Google AdMob was initialized successfully.")
        }
        // adMobInterstitial = new AdMobInterstitial(appContext, googleAdMobInterstitialID);
        // for the chrome cast
    }

    override fun showBannerAd(activity: Activity?, bannerLayout: LinearLayout?): SetBannerAdView? {
        LogUtil.d(TAG, "showBannerAd")
        return SetBannerAdView(activity, null,
            bannerLayout,
            googleAdMobBannerID, facebookBannerID, 0)
    }

    override fun geNativeTemplate(activity: Activity?, nativeLayout: FrameLayout?,
                                  nativeAdView: TemplateView?)
    : GoogleAdMobNativeTemplate? {
        LogUtil.d(TAG, "geNativeTemplate")
        return GoogleAdMobNativeTemplate(activity,
            nativeLayout,
            googleAdMobNativeID,
            nativeAdView)
    }

    companion object {
        private const val TAG = "SmileKaraokeApp"
    }
}
