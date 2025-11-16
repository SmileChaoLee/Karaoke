package com.smile.karaoke.interfaces

import android.graphics.Color
import android.view.View
import com.smile.karaoke.SmileAppBase

interface RecyclerItemListener {

    fun onItemClick(v: View?, position: Int)

    fun onItemViewFocusChanged(v: View?, position: Int, hasFocus: Boolean) {
        if (hasFocus) {
            v?.setBackgroundColor(SmileAppBase.accentColor)
        } else {
            v?.setBackgroundColor(myBackgroundColor(position))
        }
    }

    fun myBackgroundColor(position: Int): Int {
        return if (position % 2 == 0) Color.BLACK
        else SmileAppBase.transparentLightGray
    }
}