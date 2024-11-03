package com.smile.karaokeplayer.utilities

import android.content.res.Configuration
import android.view.View
import android.widget.LinearLayout

object MyBannerAdView {
    fun setVisible(orientation : Int, bannerLinearLayout : LinearLayout?, nativeVisibility : Int) {
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (nativeVisibility != View.VISIBLE) {
                bannerLinearLayout?.visibility = View.VISIBLE
            }
        } else {
            bannerLinearLayout?.visibility = View.GONE
        }
    }
}