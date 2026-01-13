package com.smile.u2bkaraoke

import androidx.media3.common.util.UnstableApi
import com.smile.karaoke.utilities.LogUtil

@UnstableApi
open class PhU2bKaOkActivity : U2bKaOkActivity() {

    private var mTAG : String = "PhU2bKaOkActivity"

    init {
        LogUtil.d(mTAG, "init")
        setTag(mTAG)
    }

    override fun needInterstitialAd(): Boolean {
        return false
    }
}