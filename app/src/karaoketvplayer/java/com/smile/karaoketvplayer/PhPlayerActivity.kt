package com.smile.karaoketvplayer

import android.content.Intent
import com.smile.karaoke.BasePlayerActivity
import com.smile.karaoke.R
import com.smile.karaoke.utilities.LogUtil
import com.smile.karaokeplayer.ExoPlayerActivity
import com.smile.videoplayer.VlcPlayerActivity
import com.smile.u2bplayer.U2bPlayerActivity

open class PhPlayerActivity : BasePlayerActivity() {

    private var mTAG : String = "PhPlayerActivity"

    fun setTag(tag: String) {
        LogUtil.d(mTAG, "setTag.tag = $tag")
        mTAG = tag
    }

    override fun getAppName(): String {
        return resources.getString(R.string.karaoke_tv_app_name)
    }

    override fun hasU2bPlayer(): Boolean {
        return true
    }

    override fun startU2bPlayer() {
        LogUtil.i(mTAG, "startU2bPlayer")
        Intent(
            this@PhPlayerActivity,
            U2bPlayerActivity::class.java
        ).also {
            loadingMessage.value = getString(R.string.loadingStr)
            u2bPlayerLauncher.launch(it)
        }
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
            this@PhPlayerActivity,
            ExoPlayerActivity::class.java
        ).also {
            loadingMessage.value = getString(R.string.loadingStr)
            exoLauncher.launch(it)
        }
    }

    override fun startVlcPlayer() {
        LogUtil.i(mTAG, "startVlcPlayer()")
        Intent(
            this@PhPlayerActivity,
            VlcPlayerActivity::class.java
        ).also {
            loadingMessage.value = getString(R.string.loadingStr)
            vlcLauncher.launch(it)
        }
    }
}