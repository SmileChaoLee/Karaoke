package com.smile.u2bplayer.utilities

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.u2bplayer.u2bplay_constants.U2bPlayConstants

object U2bPlayerUtil {

    private const val TAG = "U2bPlayerUtil"

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

    fun saveKeyword(act: Activity, searchTerm: String) {
        val logStr = "saveKeyword"

        try {
            val fos = act.openFileOutput(
                U2bPlayConstants.KEYWORD_FILENAME,
                Context.MODE_PRIVATE
            )
            val savingLine = if (searchTerm[searchTerm.length - 1] != '\n') {
                searchTerm + "\n"
            } else {
                searchTerm
            }
            fos.write(savingLine.toByteArray())
            fos.close()
            LogUtil.d(TAG, "$logStr.succeeded to save searchTerm to file")
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
        }
    }
}