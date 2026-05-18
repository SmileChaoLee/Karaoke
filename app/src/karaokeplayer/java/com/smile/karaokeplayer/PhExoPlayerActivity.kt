package com.smile.karaokeplayer

import androidx.media3.common.util.UnstableApi
import com.smile.karaoke.utilities.LogUtil

@UnstableApi
open class PhExoPlayerActivity : ExoPlayerActivity() {

    private var mTAG : String = "PhExoPlayerActivity"

    init {
        LogUtil.d(mTAG, "init")
        setTag(mTAG)
    }

    override fun needInterstitialAd(): Boolean {
        return false
    }
}