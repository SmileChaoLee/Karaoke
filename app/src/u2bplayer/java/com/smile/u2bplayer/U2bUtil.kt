package com.smile.u2bplayer

import android.app.Activity
import android.content.res.Configuration
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.u2bplayer.u2bplay_constants.U2bPlayConstants

object U2bUtil {

    fun getFavDatabaseName(): String {
        return U2bPlayConstants.U2B_FAV_DB_NAME
    }

    fun gridSpanCount(activity: Activity): Int {
        val spanCount = if (activity.resources.configuration.orientation
            == Configuration.ORIENTATION_PORTRAIT)
            U2bPlayConstants.PHONE_SPAN_COUNT
        else {
            val deviceType = ScreenUtil.getDeviceType(activity)
            if (deviceType == ScreenUtil.DEVICE_TYPE_ANDROID_TV) {
                U2bPlayConstants.TV_SPAN_COUNT
            } else {
                U2bPlayConstants.TABLET_SPAN_COUNT
            }
        }
        return spanCount
    }
}