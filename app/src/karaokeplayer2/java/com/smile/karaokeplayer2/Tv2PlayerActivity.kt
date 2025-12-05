package com.smile.karaokeplayer2

import com.smile.karaoke.utilities.LogUtil

class Tv2PlayerActivity: Ph2PlayerActivity() {

    private val mTAG : String = "Tv2PlayerActivity"
    init {
        LogUtil.d(mTAG, "")
        setTag(mTAG)
    }
}