package com.smile.karaoke.callbacks

import android.annotation.SuppressLint
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media3.common.util.UnstableApi
import com.smile.karaoke.presenters.PlayerBasePresenter
import com.smile.karaoke.utilities.LogUtil

@UnstableApi
class MediaControllerCallback(private val mPresenter: PlayerBasePresenter) :
    MediaControllerCompat.Callback() {

    companion object {
        private const val TAG = "MediaCtrlCallback"
    }

    @SuppressLint("LongLogTag")
    @Synchronized
    override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
        // super.onPlaybackStateChanged(state)
        LogUtil.d(TAG, "onPlaybackStateChanged().state = ${state?.playbackState}")
        state?.let {
            LogUtil.d(TAG, "onPlaybackStateChanged().mPresenter.updateStatusAndUi(state=${it.playbackState})")
            mPresenter.updateStatusAndUi(it)
        }
    }
}
