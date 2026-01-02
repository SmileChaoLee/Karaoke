package com.smile.u2b

import androidx.media3.common.util.UnstableApi
import com.smile.karaoke.utilities.LogUtil

@UnstableApi
open class PhU2bPlayerActivity : U2bActivity() {

    private var mTAG : String = "PhU2bPlayerActivity"

    init {
        LogUtil.d(mTAG, "init")
        setTag(mTAG)
    }

    override fun needInterstitialAd(): Boolean {
        return false
    }
}