package com.smile.u2bkktool

import android.app.Activity
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.google.android.ads.nativetemplates.TemplateView
import com.smile.karaoke.SmileAppBase
import com.smile.karaoke.utilities.LogUtil
import com.smile.nativetemplates_models.GoogleAdMobNativeTemplate
import com.smile.smilelibraries.google_ads_util.AdMobInterstitial
import com.smile.smilelibraries.show_banner_ads.SetBannerAdView
import com.smile.u2bkaraoke.dagger.interfaces.DaggerU2bKaOkComponent

class U2bKkToolApp : SmileAppBase() {
    companion object {
        private const val TAG = "U2bKkToolApp"
        val appCompBuilder = DaggerU2bKaOkComponent.builder()!!
        val appComponent = appCompBuilder.build()
    }

    override fun onCreate() {
        LogUtil.d(TAG, "onCreate")
        super.onCreate()
    }

    override fun initAds() {
        adMobBannerID = ""
        adMobNativeID = ""
    }

    override fun showBannerAd(activity: Activity?,
                              bannerLayout: LinearLayout?): SetBannerAdView? {
        LogUtil.d(TAG, "showBannerAd")
        return null
    }

    override fun getInterstitial(): AdMobInterstitial? {
        return null
    }

    override fun geNativeTemplate(activity: Activity?, nativeLayout: FrameLayout?,
                                  nativeAdView: TemplateView?)
            : GoogleAdMobNativeTemplate? {
        LogUtil.i(TAG, "geNativeTemplate")
        return null
    }
}