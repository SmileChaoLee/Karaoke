package com.smile.karaokeplayer_app_activity

import android.content.Intent
import com.smile.karaoke.R
import com.smile.karaoke.utilities.LogUtil
import com.smile.karaokeplayer.ExoPlayerActivity
import com.smile.smilelibraries.utilities.AppLinkUtil
import com.smile.youtube.HasYouTubeActivity

open class PhExoPlayerActivity : HasYouTubeActivity() {

    private var mTAG : String = "PhExoPlayerActivity"
    fun setTag(tag: String) {
        LogUtil.d(mTAG, "setTag.tag = $tag")
        mTAG = tag
    }

    override fun getAppName(): String {
        return resources.getString(R.string.karaoke_app_name)
    }

    override fun getExoButtonName(): String {
        return resources.getString(R.string.karaoke_app_name)
    }

    override fun getVlcButtonName(): String {
        return resources.getString(R.string.installVideoPlayer)
    }

    override fun startExoPlayer() {
        LogUtil.i(mTAG, "startExoPlayer()")
        Intent(
            this@PhExoPlayerActivity,
            ExoPlayerActivity::class.java
        ).also {
            loadingMessage.value = getString(R.string.loadingStr)
            exoLauncher.launch(it)
        }
    }

    override fun startVlcPlayer() {
        LogUtil.i(mTAG, "startVlcPlayer()")
        AppLinkUtil.startAppLinkOnStore(this@PhExoPlayerActivity,
            AppLinkUtil.VIDEO_LINK)
    }
}