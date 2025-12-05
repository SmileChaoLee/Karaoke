package com.smile.youtube

import android.content.Intent
import com.smile.karaoke.BasePlayerActivity
import com.smile.karaoke.R
import com.smile.karaoke.utilities.LogUtil

abstract class HasYouTubeActivity: BasePlayerActivity() {
    companion object {
        private const val TAG = "HasYouTubeActivity"
    }

    override fun startYouTubePlayer() {
        LogUtil.i(TAG, "startYouTubePlayer")
        Intent(
            this@HasYouTubeActivity,
            YouTubeActivity::class.java
        ).also {
            loadingMessage.value = getString(R.string.loadingStr)
            youTubeLauncher.launch(it)
        }
    }
}