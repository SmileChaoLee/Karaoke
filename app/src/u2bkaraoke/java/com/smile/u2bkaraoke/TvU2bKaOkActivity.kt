package com.smile.u2bkaraoke

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.smile.karaoke.utilities.LogUtil

// For Amazon Fire TV
@OptIn(UnstableApi::class)
class TvU2bKaOkActivity: PhU2bKaOkActivity() {
    private val mTAG : String = "TvU2bKaOkActivity"

    init {
        LogUtil.d(mTAG, "init")
        setTag(mTAG)
    }
}