package com.smile.u2bkaraoke

import com.smile.karaoke.utilities.LogUtil
import com.smile.u2bkaraoke.fragments.SongListFragment

open class U2bKaOkActivity : U2bKkBaseActivity() {

    private var mTAG : String = "U2bKaOkActivity"

    fun setTag(tag: String) {
        LogUtil.d(mTAG, "setTag.tag = $tag")
        mTAG = tag
    }

    override fun getSongListFragment(): SongListFragment {
        return SongListFragment()
    }
}
