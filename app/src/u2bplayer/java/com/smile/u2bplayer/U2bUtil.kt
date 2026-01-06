package com.smile.u2bplayer

import android.app.Activity
import android.content.res.Configuration
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.u2bplayer.u2b_constants.U2bConstants

object U2bUtil {

    fun getFavDatabaseName(): String {
        return U2bConstants.U2B_FAV_DB_NAME
    }

    fun gridSpanCount(activity: Activity): Int {
        val spanCount = if (activity.resources.configuration.orientation
            == Configuration.ORIENTATION_PORTRAIT)
            U2bConstants.PHONE_SPAN_COUNT
        else {
            val deviceType = ScreenUtil.getDeviceType(activity)
            if (deviceType == ScreenUtil.DEVICE_TYPE_ANDROID_TV) {
                U2bConstants.TV_SPAN_COUNT
            } else {
                U2bConstants.TABLET_SPAN_COUNT
            }
        }
        return spanCount
    }
}