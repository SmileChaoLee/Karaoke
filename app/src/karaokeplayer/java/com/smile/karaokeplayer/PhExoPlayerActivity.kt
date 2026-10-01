package com.smile.karaokeplayer

import android.content.Intent
import androidx.media3.common.util.UnstableApi
import com.smile.karaoke.BasePlayerActivity
import com.smile.karaoke.R
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.AppLinkUtil

@UnstableApi
open class PhExoPlayerActivity : BasePlayerActivity() {

    private var mTAG : String = "PhExoPlayerActivity"

    fun setTag(tag: String) {
        LogUtil.d(mTAG, "setTag.tag = $tag")
        mTAG = tag
    }

    override fun getAppName(): String {
        return resources.getString(R.string.karaoke_app_name)
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
            this@PhExoPlayerActivity,
            ExoPlayerActivity::class.java
        ).also {
            loadingMessage.value = getString(R.string.loadingStr)
            exoLauncher.launch(it)
        }
    }

    override fun startVlcPlayer() {
        LogUtil.d(mTAG, "startVlcPlayer()")
        AppLinkUtil.startAppLinkOnStore(
            this@PhExoPlayerActivity,
            AppLinkUtil.VIDEO_LINK
        )
    }

    override fun startU2bKaraoke() {
        LogUtil.d(mTAG, "startU2bKaraoke")
        AppLinkUtil.startAppLinkOnStore(
            this@PhExoPlayerActivity,
            AppLinkUtil.U2B_KARAOKE_LINK
        )
    }

    override fun startU2bPlayer() {
        LogUtil.d(mTAG, "startU2bPlayer")
        AppLinkUtil.startAppLinkOnStore(
            this@PhExoPlayerActivity,
            AppLinkUtil.U2B_PLAYER_LINK
        )
    }
}