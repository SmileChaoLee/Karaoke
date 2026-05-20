package com.smile.karaoke.utilities

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.get
import androidx.core.view.size
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

    fun setScreenOrientation(act: Activity, orientation: Int): Int {
        val res = act.resources
        val orgOrientation = res.configuration.orientation
        act.requestedOrientation = when (orientation) {
            Configuration.ORIENTATION_PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            Configuration.ORIENTATION_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        return orgOrientation
    }

    fun closeMenu(menu: Menu?) {
        menu?.let {
            for (i in 0 until it.size) {
                it[i].subMenu?.let { it2 ->
                    closeMenu(it2)
                }
            }
            it.close()
        }
    }

    fun disableButtonForSometime(button: View) {
        val seconds = 0.2f  // 200 ms
        button.isEnabled = false
        val handler = Handler(Looper.getMainLooper())
        val runnable = Runnable {
            handler.removeCallbacksAndMessages(null)
            button.isEnabled = true
        }
        handler.postDelayed(runnable, (seconds * 1000.0).toLong())
    }
}