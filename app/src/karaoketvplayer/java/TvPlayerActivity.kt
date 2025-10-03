package com.smile.karaoke

import android.util.Log

class TvPlayerActivity: PhPlayerActivity() {
    private val mTAG : String = "TvPlayerActivity"
    init {
        Log.d(mTAG, "")
        setTag(mTAG)
    }
}