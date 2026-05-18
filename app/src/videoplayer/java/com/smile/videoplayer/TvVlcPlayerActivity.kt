package com.smile.videoplayer

import androidx.media3.common.util.UnstableApi
import com.smile.karaoke.utilities.LogUtil

@UnstableApi
class TvVlcPlayerActivity: PhVlcPlayerActivity() {
    private val mTAG : String = "TvVlcPlayActivity"
    init {
        LogUtil.d(mTAG, "")
        setTag(mTAG)
    }
}