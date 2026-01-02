package com.smile.videoplayer_app_activity

import android.content.Intent
import com.smile.karaoke.BasePlayerActivity
import com.smile.karaoke.R
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.AppLinkUtil
import com.smile.videoplayer.VlcPlayerActivity
import com.smile.u2b.U2bActivity

open class PhVlcPlayerActivity : BasePlayerActivity() {

    private var mTAG : String = "PhVlcPlayerActivity"
    fun setTag(tag: String) {
        LogUtil.d(mTAG, "setTag.tag = $tag")
        mTAG = tag
    }

    override fun getAppName(): String {
        return resources.getString(R.string.video_app_name)
    }

    override fun hasYouTubePlayer(): Boolean {
        return true
    }

    /*
    override fun startYouTubePlayer() {
        LogUtil.i(mTAG, "startYouTubePlayer()")
        AppLinkUtil.startAppLinkOnStore(this@PhVlcPlayerActivity,
            AppLinkUtil.YOUTUBE_LINK)
    }
    */

    override fun startYouTubePlayer() {
        LogUtil.i(mTAG, "startYouTubePlayer")
        Intent(
            this@PhVlcPlayerActivity,
            U2bActivity::class.java
        ).also {
            loadingMessage.value = getString(R.string.loadingStr)
            youTubeLauncher.launch(it)
        }
    }
    override fun getExoButtonName(): String {
        return resources.getString(R.string.installKaraokePlayer)
    }

    override fun getVlcButtonName(): String {
        return resources.getString(R.string.video_app_name)
    }

    override fun startExoPlayer() {
        LogUtil.i(mTAG, "startExoPlayer()")
        AppLinkUtil.startAppLinkOnStore(this@PhVlcPlayerActivity,
            AppLinkUtil.KARAOKE_LINK)
    }

    override fun startVlcPlayer() {
        LogUtil.i(mTAG, "startVlcPlayer()")
        Intent(
            this@PhVlcPlayerActivity,
            VlcPlayerActivity::class.java
        ).also {
            loadingMessage.value = getString(R.string.loadingStr)
            exoLauncher.launch(it)
        }
    }
}