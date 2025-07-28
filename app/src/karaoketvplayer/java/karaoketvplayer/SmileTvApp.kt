package karaoketvplayer

import android.app.Activity
import android.util.Log
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.google.android.ads.nativetemplates.TemplateView
import com.smile.karaoke.SmileAppBase
import com.smile.nativetemplates_models.GoogleAdMobNativeTemplate
import com.smile.smilelibraries.show_banner_ads.SetBannerAdView

class SmileTvApp : SmileAppBase() {

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
    }

    override fun initAds() {
        // No ads in this version
        Log.d(TAG, "initAds.do nothing")
    }

    override fun showBannerAd(activity: Activity?, bannerLayout: LinearLayout?): SetBannerAdView? {
        Log.d(TAG, "showBannerAd.do nothing")
        return null // No ads in this version
    }

    override fun geNativeTemplate(activity: Activity?, nativeLayout: FrameLayout?,
                                  nativeAdView: TemplateView?)
            : GoogleAdMobNativeTemplate? {
        Log.d(TAG, "geNativeTemplate.do nothing")
        return null // No ads in this version
    }

    companion object {
        private const val TAG = "SmileTvApp"
        private const val NATIVE_AD_ID = "ca-app-pub-8354869049759576/9847001557"
    }
}
