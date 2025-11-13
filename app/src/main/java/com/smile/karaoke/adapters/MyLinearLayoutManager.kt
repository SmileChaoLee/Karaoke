package com.smile.karaoke.adapters

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.smile.karaoke.utilities.LogUtil

class MyLinearLayoutManager(ctx: Context?): LinearLayoutManager(ctx) {

    companion object {
        private const val TAG = "MyLayoutManager"
    }

    override fun onInterceptFocusSearch(focused: View, direction: Int): View? {
        val pos = getPosition(focused)
        LogUtil.d(TAG, "onInterceptFocusSearch.pos = $pos")
        // Check if the direction is UP (or FOCUS_UP, depending on your context)
        // and if the previous position is valid
        if (direction == View.FOCUS_UP && pos > 0) {
            // Ensure the previous item is laid out and ready
            val view = findViewByPosition(pos - 1)
            if (view == null) {
                LogUtil.d(TAG, "onInterceptFocusSearch.view not present")
                scrollToPosition(pos - 1)
            }
        }
        return super.onInterceptFocusSearch(focused, direction)
    }

    override fun isAutoMeasureEnabled(): Boolean {
        return false
    }
}