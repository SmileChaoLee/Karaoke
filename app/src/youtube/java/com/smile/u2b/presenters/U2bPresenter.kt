package com.smile.u2b.presenters

import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.smile.karaoke.presenters.PlayerBasePresenter
import com.smile.karaoke.utilities.LogUtil
import com.smile.u2b.services.U2bService

@OptIn(UnstableApi::class)
class U2bPresenter(private val presentView: U2bPresentView)
    : PlayerBasePresenter(presentView) {

    companion object {
        private const val TAG = "U2bPresenter"
    }

    interface U2bPresentView: BasePresentView {
        // nothing for now
    }

    private fun getU2bService(): U2bService? {
        return presentView.getPlayService() as? U2bService
    }

    // implementing methods of U2bPresenter.U2bPresentView
    override fun initializeVariables(
        savedInstanceState: Bundle?,
        callingIntent: Intent?,
        isAutoPlay: Boolean) {
        LogUtil.d(TAG, "initializeVariables")
        initializeVariablesBase(savedInstanceState, callingIntent, isAutoPlay)
    }

    override fun setAudioTrackAndChannel(audioTrackIndex: Int, audioChannel: Int) {
        val logStr = "setAudioTrackAndChannel"
        LogUtil.d(TAG, logStr)
        val playService = getU2bService() ?: return
        playService.setAudioTrack(audioTrackIndex)
        mPlayingParam.currentAudioTrackIndexPlayed = audioTrackIndex
        // select audio channel
        mPlayingParam.currentChannelPlayed = audioChannel
        playService.setAudioVolume(mPlayingParam.currentVolume)
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
        val numTracks = getNumberOfAudioTracks()
        presentView.buildAudioTrackMenuItem(numTracks)
    }

    override fun getNumberOfAudioTracks(): Int {
        LogUtil.d(TAG, "getNumberOfAudioTracks")
        return 3    // 3 languages, No caption, English, and Local Language
    }

    override fun getNumberOfVideoTracks(): Int {
        LogUtil.d(TAG, "getNumberOfVideoTracks")
        return 1    // temporary
    }
    // end of implementing methods of U2bPresenter.U2bPresentView
}