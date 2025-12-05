package com.smile.videoplayer_app_activity

import com.smile.karaoke.utilities.LogUtil

class TvVlcPlayerActivity: PhVlcPlayerActivity() {
    private val mTAG : String = "TvVlcPlayActivity"
    init {
        LogUtil.d(mTAG, "")
        setTag(mTAG)
    }
}