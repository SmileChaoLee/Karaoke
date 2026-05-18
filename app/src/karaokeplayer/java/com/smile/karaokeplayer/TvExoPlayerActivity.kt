package com.smile.karaokeplayer

import androidx.media3.common.util.UnstableApi
import com.smile.karaoke.utilities.LogUtil

@UnstableApi
class TvExoPlayerActivity: PhExoPlayerActivity() {
    private val mTAG : String = "TvExoPlayActivity"
    init {
        LogUtil.d(mTAG, "")
        setTag(mTAG)
    }
}