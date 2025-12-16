package com.smile.youtube

import androidx.media3.common.util.UnstableApi
import com.smile.karaoke.utilities.LogUtil

@UnstableApi
open class PhYTPlayerActivity : YouTubeActivity() {

    private var mTAG : String = "PhYTPlayerActivity"

    init {
        LogUtil.d(mTAG, "init")
        setTag(mTAG)
    }

    override fun needInterstitialAd(): Boolean {
        return false
    }
}