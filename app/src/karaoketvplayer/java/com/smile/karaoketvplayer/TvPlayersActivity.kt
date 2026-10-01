package com.smile.karaoketvplayer

import com.smile.karaoke.utilities.LogUtil

class TvPlayersActivity: PhPlayersActivity() {
    private val mTAG : String = "TvPlayersActivity"
    init {
        LogUtil.d(mTAG, "")
        setTag(mTAG)
    }
}