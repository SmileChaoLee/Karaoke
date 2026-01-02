package com.smile.u2b

import android.app.Activity
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.InitializationStatus
import com.smile.karaoke.SmileAppBase
import com.smile.karaoke.utilities.LogUtil
import com.smile.nativetemplates_models.GoogleAdMobNativeTemplate
import com.smile.smilelibraries.google_ads_util.AdMobInterstitial
import com.smile.smilelibraries.show_banner_ads.SetBannerAdView

class SmileU2bApp : SmileAppBase() {

    companion object {
        private const val TAG = "SmileU2bApp"
    }

    override fun onCreate() {
        super.onCreate()
        LogUtil.i(TAG, "onCreate")
    }

    override fun initAds() {
        adMobBannerID = "ca-app-pub-8354869049759576/1400953752"
        adMobNativeID = "ca-app-pub-8354869049759576/6170064706"
        // google
        MobileAds.initialize(applicationContext) {
            initializationStatus: InitializationStatus? ->
            LogUtil.i(TAG, "Google AdMob was initialized successfully.")
        }
        // for the chrome cast
    }

    override fun showBannerAd(activity: Activity?, bannerLayout: LinearLayout?): SetBannerAdView? {
        LogUtil.d(TAG, "showBannerAd")
        return SetBannerAdView(activity, null,
            bannerLayout,
            adMobBannerID, facebookBannerID, 0)
    }

    override fun getInterstitial(): AdMobInterstitial? {
        val adMobInterstitialID = "ca-app-pub-8354869049759576/7483146379"
        return AdMobInterstitial(applicationContext, adMobInterstitialID)
    }

    override fun geNativeTemplate(activity: Activity?, nativeLayout: FrameLayout?,
                                  nativeAdView: TemplateView?)
            : GoogleAdMobNativeTemplate? {
        LogUtil.i(TAG, "geNativeTemplate")
        return GoogleAdMobNativeTemplate(activity,
            nativeLayout,
            adMobNativeID,
            nativeAdView)
    }
}
