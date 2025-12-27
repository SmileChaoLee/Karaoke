package com.smile.karaoke.adapters

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager

class MyLayoutManager(ctx: Context?, count: Int): GridLayoutManager(ctx,
    count) {

    /*
    override fun onInterceptFocusSearch(focused: View, direction: Int): View? {
        val pos = getPosition(focused)
        // Check if the direction is UP (or FOCUS_UP, depending on your context)
        // and if the previous position is valid
        if (direction == View.FOCUS_UP && pos > 0) {
            // Ensure the previous item is laid out and ready
            val view = findViewByPosition(pos - 1)
            if (view == null) {
                scrollToPosition(pos - 1)
            }
        }
        return super.onInterceptFocusSearch(focused, direction)
    }
    */

    override fun isAutoMeasureEnabled(): Boolean {
        return false
    }
}