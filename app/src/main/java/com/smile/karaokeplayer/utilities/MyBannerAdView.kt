package com.smile.karaokeplayer.utilities

import android.view.View
import android.widget.LinearLayout

object MyBannerAdView {
    fun setVisible(bannerLinearLayout : LinearLayout?, nativeVisibility : Int) {
        if (nativeVisibility != View.VISIBLE) {
            bannerLinearLayout?.visibility = View.VISIBLE
        }
    }
}