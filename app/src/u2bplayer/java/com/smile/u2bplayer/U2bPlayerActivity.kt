package com.smile.u2bplayer

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.smile.karaoke.utilities.LogUtil
import com.smile.u2bplayer.fragments.BkSearchFragment
import com.smile.u2bplayer.fragments.SearchYTFragment

@OptIn(UnstableApi::class)
open class U2bPlayerActivity : U2bBaseActivity() {

    private var mTAG : String = "U2bPlayerActivity"

    fun setTag(tag: String) {
        LogUtil.d(mTAG, "setTag.tag = $tag")
        mTAG = tag
    }

    override fun getSearchFragment() = BkSearchFragment()
}