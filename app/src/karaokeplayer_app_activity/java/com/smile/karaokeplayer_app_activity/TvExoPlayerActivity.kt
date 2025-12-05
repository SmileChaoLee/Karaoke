package com.smile.karaokeplayer_app_activity

import com.smile.karaoke.utilities.LogUtil

class TvExoPlayerActivity: PhExoPlayerActivity() {
    private val mTAG : String = "TvExoPlayActivity"
    init {
        LogUtil.d(mTAG, "")
        setTag(mTAG)
    }
}