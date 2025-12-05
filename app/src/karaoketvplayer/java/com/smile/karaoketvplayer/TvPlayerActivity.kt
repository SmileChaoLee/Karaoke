package com.smile.karaoketvplayer

import com.smile.karaoke.utilities.LogUtil

class TvPlayerActivity: PhPlayerActivity() {
    private val mTAG : String = "TvPlayerActivity"
    init {
        LogUtil.d(mTAG, "")
        setTag(mTAG)
    }
}