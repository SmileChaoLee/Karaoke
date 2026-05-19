package com.smile.u2bplayer

import androidx.media3.common.util.UnstableApi
import com.smile.karaoke.utilities.LogUtil

@UnstableApi
open class PhU2bPlayerActivity : U2bPlayerActivity() {

    private var mTAG : String = "PhU2bPlayerActivity"

    init {
        LogUtil.d(mTAG, "init")
        setTag(mTAG)
    }
}