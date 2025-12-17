package com.smile.karaokeplayer2

import android.content.Intent
import com.smile.karaoke.BasePlayerActivity
import com.smile.karaoke.R
import com.smile.karaoke.utilities.LogUtil
import com.smile.karaokeplayer.ExoPlayerActivity
import com.smile.videoplayer.VlcPlayerActivity

open class Ph2PlayerActivity : BasePlayerActivity() {

    private var mTAG : String = "Ph2PlayerActivity"

    fun setTag(tag: String) {
        LogUtil.d(mTAG, "setTag.tag = $tag")
        mTAG = tag
    }

    override fun getAppName(): String {
        return resources.getString(R.string.karaoke_app_name)
    }

    // Amazon appstore does not allow playing YouTube
    override fun hasYouTubePlayer(): Boolean {
        return false
    }

    override fun startYouTubePlayer() {
        LogUtil.i(mTAG, "startYouTubePlayer()")
        // do nothing
    }

    override fun getExoButtonName(): String {
        return resources.getString(R.string.exoPlayerName)
    }

    override fun getVlcButtonName(): String {
        return resources.getString(R.string.vlcPlayerName)
    }

    override fun startExoPlayer() {
        LogUtil.i(mTAG, "startExoPlayer()")
        Intent(
            this@Ph2PlayerActivity,
            ExoPlayerActivity::class.java
        ).also {
            loadingMessage.value = getString(R.string.loadingStr)
            exoLauncher.launch(it)
        }
    }

    override fun startVlcPlayer() {
        LogUtil.i(mTAG, "startVlcPlayer()")
        Intent(
            this@Ph2PlayerActivity,
            VlcPlayerActivity::class.java
        ).also {
            loadingMessage.value = getString(R.string.loadingStr)
            vlcLauncher.launch(it)
        }
    }
}