package com.smile.youtube

import android.app.Activity
import android.content.res.Configuration
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.youtube.yt_constants.YTConstants

object YTUtil {

    fun getFavDatabaseName(): String {
        return YTConstants.YT_FAV_DB_NAME
    }

    fun gridSpanCount(activity: Activity): Int {
        val spanCount = if (activity.resources.configuration.orientation
            == Configuration.ORIENTATION_PORTRAIT)
            YTConstants.PHONE_SPAN_COUNT
        else {
            val deviceType = ScreenUtil.getDeviceType(activity)
            if (deviceType == ScreenUtil.DEVICE_TYPE_ANDROID_TV) {
                YTConstants.TV_SPAN_COUNT
            } else {
                YTConstants.TABLET_SPAN_COUNT
            }
        }
        return spanCount
    }
}