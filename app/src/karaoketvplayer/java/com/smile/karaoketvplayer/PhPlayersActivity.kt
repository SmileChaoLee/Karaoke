package com.smile.karaoketvplayer

import android.content.Intent
import com.smile.karaoke.BasePlayerActivity
import com.smile.karaoke.R
import com.smile.karaoke.utilities.LogUtil
import com.smile.karaokeplayer.ExoPlayerActivity
import com.smile.u2bkaraoke.U2bKaOkActivity
import com.smile.videoplayer.VlcPlayerActivity
import com.smile.u2bplayer.U2bPlayerActivity

open class PhPlayersActivity : BasePlayerActivity() {

    private var mTAG : String = "PhPlayersActivity"

    fun setTag(tag: String) {
        LogUtil.d(mTAG, "setTag.tag = $tag")
        mTAG = tag
    }

    override fun getAppName(): String {
        return resources.getString(R.string.karaoke_tv_app_name)
    }

    override fun startU2bPlayer() {
        LogUtil.d(mTAG, "startU2bPlayer")
        Intent(
            this@PhPlayersActivity,
            U2bPlayerActivity::class.java
        ).also {
            loadingMessage.value = getString(R.string.loadingStr)
            u2bPlayerLauncher.launch(it)
        }
    }

    override fun startU2bKaraoke() {
        LogUtil.d(mTAG, "startU2bKaraoke")
        Intent(
            this@PhPlayersActivity,
            U2bKaOkActivity::class.java
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
        LogUtil.d(mTAG, "startExoPlayer()")
        Intent(
            this@PhPlayersActivity,
            ExoPlayerActivity::class.java
        ).also {
            loadingMessage.value = getString(R.string.loadingStr)
            exoLauncher.launch(it)
        }
    }

    override fun startVlcPlayer() {
        LogUtil.d(mTAG, "startVlcPlayer()")
        Intent(
            this@PhPlayersActivity,
            VlcPlayerActivity::class.java
        ).also {
            loadingMessage.value = getString(R.string.loadingStr)
            vlcLauncher.launch(it)
        }
    }
}