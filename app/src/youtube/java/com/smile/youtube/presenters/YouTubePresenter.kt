package com.smile.youtube.presenters

import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.smile.karaoke.presenters.PlayerBasePresenter
import com.smile.karaoke.utilities.LogUtil

@OptIn(UnstableApi::class)
class YouTubePresenter(private val youTubePresentView: YouTubePresentView)
    : PlayerBasePresenter(youTubePresentView) {

    companion object {
        private const val TAG = "YouTubePresenter"
    }

    interface YouTubePresentView: BasePresentView {
        // fun initYouTubePlayer()
    }

    // implementing methods of YouTubePresenter.YouTubePresentView
    override fun initializeVariables(
        savedInstanceState: Bundle?,
        callingIntent: Intent?,
        isAutoPlay: Boolean
    ) {
        LogUtil.d(TAG, "initializeVariables")
        initializeVariablesBase(savedInstanceState, callingIntent, isAutoPlay)
    }

    override fun setAudioTrackAndChannel(audioTrackIndex: Int, audioChannel: Int) {
        LogUtil.d(TAG, "setAudioTrackAndChannel")
    }

    override fun switchAudioToMusic() {
        LogUtil.d(TAG, "switchAudioToMusic")
    }

    override fun switchAudioToVocal() {
        LogUtil.d(TAG, "switchAudioToVocal")
    }

    override fun startDurationBarHandler() {
        durationSeekBarHandler.postDelayed(durationSeekBarRunnable, 1000)
    }

    override fun removeMsgFromDurationBarHandler() {
        durationSeekBarHandler.removeCallbacksAndMessages(null)
    }

    override fun setAudioActionSubMenu() {
        LogUtil.d(TAG, "setAudioActionSubMenu")
    }

    override fun getNumberOfAudioTracks(): Int {
        LogUtil.d(TAG, "getNumberOfAudioTracks")
        return 1    // temporary
    }
    // end of implementing methods of YouTubePresenter.YouTubePresentView
}