package com.smile.u2b

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.smile.karaoke.utilities.LogUtil

// For Amazon Fire TV
@OptIn(UnstableApi::class)
class TvU2bPlayerActivity: PhU2bPlayerActivity() {
    private val mTAG : String = "TvU2bPlayerActivity"

    init {
        LogUtil.d(mTAG, "init")
        setTag(mTAG)
    }
}