package com.smile.karaokeplayer

import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.smile.karaoke.UpBaseActivity
import com.smile.karaoke.utilities.LogUtil
import com.smile.karaokeplayer.fragments.ExoFavFragment
import com.smile.karaokeplayer.fragments.ExoOpenFFragment
import com.smile.karaokeplayer.fragments.ExoPlayerFragment

@OptIn(UnstableApi::class)
open class ExoPlayerActivity : UpBaseActivity() {

    private var mTAG : String = "ExoPlayerActivity"
    fun setTag(tag: String) {
        LogUtil.d(mTAG, "setTag.tag = $tag")
        mTAG = tag
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.d(mTAG, "onCreate")
        super.onCreate(savedInstanceState)
    }

    @OptIn(UnstableApi::class)
    override fun getFragment() = ExoPlayerFragment()
    override fun getOpenFileFragment() = ExoOpenFFragment()
    override fun getFavoriteFragment() = ExoFavFragment()
}