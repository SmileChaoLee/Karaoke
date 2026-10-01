package com.smile.karaokeplayer2

import androidx.media3.common.util.UnstableApi
import com.smile.karaoke.utilities.LogUtil

@UnstableApi
class TvExo2PlayerActivity: PhExo2PlayerActivity() {
    private val mTAG : String = "TvExo2PlayerAct"
    init {
        LogUtil.d(mTAG, "")
        setTag(mTAG)
    }
}