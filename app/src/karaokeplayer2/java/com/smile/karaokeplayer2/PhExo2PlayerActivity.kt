package com.smile.karaokeplayer2

import androidx.media3.common.util.UnstableApi
import com.smile.karaoke.utilities.LogUtil
import com.smile.karaokeplayer.ExoPlayerActivity

@UnstableApi
open class PhExo2PlayerActivity : ExoPlayerActivity() {

    private var mTAG : String = "PhExo2PlayerAct"

    fun setTag(tag: String) {
        LogUtil.d(mTAG, "setTag.tag = $tag")
        mTAG = tag
    }
}