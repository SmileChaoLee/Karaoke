package com.smile.videoplayer

import androidx.media3.common.util.UnstableApi
import com.smile.karaoke.utilities.LogUtil

@UnstableApi
open class PhVlcPlayerActivity : VlcPlayerActivity() {

    private var mTAG : String = "PhVlcPlayerActivity"

    init {
        LogUtil.d(mTAG, "init")
        setTag(mTAG)
    }
}