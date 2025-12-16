package com.smile.youtube

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.smile.karaoke.utilities.LogUtil

// For Amazon Fire TV
@OptIn(UnstableApi::class)
class TvYTPlayerActivity: PhYTPlayerActivity() {
    private val mTAG : String = "TvYTPlayerActivity"

    init {
        LogUtil.d(mTAG, "init")
        setTag(mTAG)
    }
}