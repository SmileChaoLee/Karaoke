package com.smile.karaoke.utilities

import android.app.Activity
import android.content.res.Configuration
import android.view.View
import android.widget.LinearLayout
import com.smile.karaoke.constants.CommonConstants
import com.smile.smilelibraries.utilities.ScreenUtil

object CommonUtil {
    fun setVisible(bannerLinearLayout : LinearLayout?, nativeVisibility : Int) {
        if (nativeVisibility != View.VISIBLE) {
            bannerLinearLayout?.visibility = View.VISIBLE
        }
    }

    fun gridSpanCount(activity: Activity): Int {
        val spanCount = if (activity.resources.configuration.orientation
            == Configuration.ORIENTATION_PORTRAIT)
            CommonConstants.PHONE_SPAN_COUNT
        else {
            val deviceType = ScreenUtil.getDeviceType(activity)
            if (deviceType == ScreenUtil.DEVICE_TYPE_ANDROID_TV) {
                CommonConstants.TV_SPAN_COUNT
            } else {
                CommonConstants.TABLET_SPAN_COUNT
            }
        }
        return spanCount
    }
}