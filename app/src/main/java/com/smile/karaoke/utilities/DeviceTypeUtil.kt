package com.smile.karaoke.utilities

import android.app.Activity
import android.content.res.Configuration
import com.smile.karaoke.constants.CommonConstants
import com.smile.smilelibraries.utilities.ScreenUtil

object DeviceTypeUtil {

    fun getDeviceType(activity: Activity): String {
        var deviceType: String
        val screenSize = ScreenUtil.getScreenSize(activity)
        val smallestWidth = if (screenSize.x < screenSize.y) screenSize.x else screenSize.y
        val smallestScreenWidthDp = ScreenUtil.pixelToDp(smallestWidth.toFloat())
        deviceType = if (smallestScreenWidthDp >= 600) {
            CommonConstants.DEVICE_TYPE_TABLET
        } else {
            CommonConstants.DEVICE_TYPE_PHONE
        }
        // More specific check for Android TV
        // This requires checking UI mode, not just screen width.
        val uiModeManager = activity.resources.configuration.uiMode
        val isTv = uiModeManager and Configuration.UI_MODE_TYPE_TELEVISION == Configuration.UI_MODE_TYPE_TELEVISION
        if (isTv) {
            deviceType = CommonConstants.DEVICE_TYPE_ANDROID_TV
        }

        return deviceType
    }
}