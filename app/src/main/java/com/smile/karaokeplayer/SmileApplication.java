package com.smile.karaokeplayer;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDexApplication;

import com.facebook.ads.AudienceNetworkAds;
import com.google.android.gms.ads.MobileAds;
import com.smile.karaokeplayer.constants.CommonConstants;
import com.smile.smilelibraries.utilities.ScreenUtil;

import java.util.LinkedHashMap;

public class SmileApplication extends MultiDexApplication {

    private static final String TAG = "SmileApplication";

    // protected String facebookInterstitialID = "";
    protected String googleAdMobAppID = "";
    // protected String googleAdMobInterstitialID = "";
    protected String testString = "";

    public static int FontSize_Scale_Type = ScreenUtil.FontSize_Pixel_Type;
    public static String leftChannelString;
    public static String rightChannelString;
    public static String stereoChannelString;
    public static LinkedHashMap<Integer, String> audioChannelMap;
    public static LinkedHashMap<String, Integer> audioChannelReverseMap;

    public static Resources AppResources;
    public static Context AppContext;
    public static String facebookBannerID = "";
    public static String googleAdMobBannerID = "";
    public static String googleAdMobNativeID = "";
    // public FacebookInterstitial facebookInterstitial;
    // public AdMobInterstitial adMobInterstitial;

    @Override
    public void onCreate() {
        super.onCreate();

        AppResources = getResources();
        AppContext = getApplicationContext();

        leftChannelString = getString(R.string.leftChannelString);
        rightChannelString = getString(R.string.rightChannelString);
        stereoChannelString = getString(R.string.stereoChannelString);

        audioChannelMap = new LinkedHashMap<>();
        audioChannelMap.put(CommonConstants.LeftChannel, leftChannelString);
        audioChannelMap.put(CommonConstants.RightChannel, rightChannelString);
        audioChannelMap.put(CommonConstants.StereoChannel, stereoChannelString);

        audioChannelReverseMap = new LinkedHashMap<>();
        audioChannelReverseMap.put(leftChannelString, CommonConstants.LeftChannel);
        audioChannelReverseMap.put(rightChannelString, CommonConstants.RightChannel);
        audioChannelReverseMap.put(stereoChannelString, CommonConstants.StereoChannel);

        // for debug mode and for facebook
        if (com.smile.karaokeplayer.BuildConfig.DEBUG) {
            testString = "IMG_16_9_APP_INSTALL#";
        }

        setGoogleAdMobAndFacebookAudioNetwork();

        // google
        MobileAds.initialize(AppContext, initializationStatus -> Log.d(TAG, "Google AdMob was initialized successfully."));
        // adMobInterstitial = new AdMobInterstitial(AppContext, googleAdMobInterstitialID);
    }

    private void setGoogleAdMobAndFacebookAudioNetwork() {
        AudienceNetworkAds.initialize(this);
        // facebookInterstitialID = "1712962715503258_1712963252169871";
        // facebookInterstitialID = testString + facebookInterstitialID;
        // facebookInterstitial = new FacebookInterstitial(AppContext,
        //         facebookInterstitialID);
        facebookBannerID = testString + "1712962715503258_2019623008170559";
        googleAdMobAppID = "ca-app-pub-8354869049759576~5549171584";
        // googleAdMobInterstitialID = "ca-app-pub-8354869049759576/1418354889";
        googleAdMobBannerID = "ca-app-pub-8354869049759576/8267060571";
        googleAdMobNativeID = "ca-app-pub-8354869049759576/7985456524";
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "Configuration changed");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.w(TAG, "System is running low on memory");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Log.w(TAG, "onTrimMemory, level: = " + level);
    }
}
