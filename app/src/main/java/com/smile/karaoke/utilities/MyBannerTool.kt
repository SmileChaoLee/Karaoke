package com.smile.karaoke.utilities

import android.view.View
import android.widget.LinearLayout

object MyBannerTool {
    fun setVisible(bannerLinearLayout : LinearLayout?, nativeVisibility : Int) {
        if (nativeVisibility != View.VISIBLE) {
            bannerLinearLayout?.visibility = View.VISIBLE
        }
    }
}