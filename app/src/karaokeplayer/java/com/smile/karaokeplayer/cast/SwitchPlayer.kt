package com.smile.karaokeplayer.cast

import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import com.smile.karaoke.utilities.LogUtil
import com.smile.karaokeplayer.services.ExoPlayService
import java.io.File

@OptIn(UnstableApi::class)
class SwitchPlayer(private val playService: ExoPlayService) {

    fun transferPlaybackToCast() {
        val msgString = "transferPlaybackToCast"
        LogUtil.i(TAG, msgString)
        if (playService.presenter == null) {
            LogUtil.d(TAG, "${msgString}.presenter is null")
            return
        }
        val presenter = playService.presenter!!
        playService.castPlayer?.let { castP ->
            playService.exoPlayer?.let { exoP ->
                LogUtil.d(TAG, "${msgString}.castPlayer and exoPlayer not null")
                val playWhenReady = exoP.playWhenReady
                val position = exoP.currentPosition
                val playbackState = presenter.playingParam.currentPlaybackState
                presenter.mediaUri?.let {
                    val mediaUri = it
                    LogUtil.d(TAG, "${msgString}.mediaUri = $mediaUri")
                    val mediaFileName = mediaUri.path
                    LogUtil.d(TAG, "${msgString}.mediaFileName = $mediaFileName")
                    if (mediaFileName.isNullOrEmpty()) {
                        playService.stopCasting()
                        return
                    }
                    // starting switching to castPlayer
                    playService.webServerAndCast.startWebServer(mediaFileName)
                    // must after startWebServer
                    val localMediaUrl = playService.webServerAndCast.getMediaUrl()
                    if (localMediaUrl.isEmpty()) {
                        LogUtil.d(TAG, "${msgString}.localMediaUrl is empty")
                        playService.stopCasting()
                        return
                    }
                    playService.isCastSessionAvailable = true
                    // remove all the players listeners
                    playService.removeExoPlayerListener()
                    playService.removeCastPlayerListener()
                    // must after isCastSessionAvailable = true
                    presenter.presentView.setCurrentPlayerToPlayerView()

                    exoP.stop() // do not use playService.stopPlay()
                    LogUtil.d(TAG, "${msgString}.localMediaUrl = $localMediaUrl")
                    LogUtil.d(TAG, "${msgString}.position = $position")
                    LogUtil.d(TAG, "${msgString}.playWhenReady = $playWhenReady")
                    presenter.mediaUri = localMediaUrl.toUri()
                    presenter.playingParam.preparedStatus = 0
                    presenter.playingParam.currentPlaybackState = playbackState
                    presenter.playingParam.currentAudioPosition = position
                    val playingParam = presenter.playingParam.copy()
                    playService.setPlayWhenReady(playWhenReady)
                    playService.startPlayWithParam(presenter, playingParam)

                    playService.addCastPlayerListener()
                }
            }
        }
    }

    fun transferPlaybackToLocal() {
        val msgString = "transferPlaybackToLocal"
        LogUtil.i(TAG, msgString)
        if (playService.presenter == null) {
            LogUtil.d(TAG, "${msgString}.presenter is null")
            return
        }
        val presenter = playService.presenter!!

        // remove all the players listeners
        playService.removeCastPlayerListener()
        playService.removeExoPlayerListener()
        playService.stopCasting()   // isCastSessionAvailable -> false
        // must after stopCasting()
        presenter.presentView.setCurrentPlayerToPlayerView()

        playService.exoPlayer?.let { exoP ->
            playService.castPlayer?.let { castP ->
                LogUtil.d(TAG, "${msgString}.castPlayer and exoPlayer not null")
                val position = castP.currentPosition
                val playWhenReady = castP.playWhenReady
                val playbackState = presenter.playingParam.currentPlaybackState

                castP.stop()    // do not use stopPlay()
                presenter.mediaUri?.let {
                    val mediaUri = it
                    LogUtil.d(TAG, "${msgString}.mediaUri = $mediaUri")
                    val mediaFileName = mediaUri.path
                    LogUtil.d(TAG, "${msgString}.mediaFileName = $mediaFileName")
                    if (mediaFileName.isNullOrEmpty()) {
                        return
                    }
                    // starting switching to exoPlayer.
                    // mediaUri need to be set to local uri?
                    val tempUri = File(mediaFileName).toUri()
                    LogUtil.d(TAG, "${msgString}.tempUri = $tempUri")
                    LogUtil.d(TAG, "${msgString}.position = $position")
                    LogUtil.d(TAG, "${msgString}.playWhenReady = $playWhenReady")
                    presenter.mediaUri = tempUri
                    presenter.playingParam.preparedStatus = 0
                    presenter.playingParam.currentPlaybackState = playbackState
                    presenter.playingParam.currentAudioPosition = position
                    val playingParam = presenter.playingParam.copy()
                    playService.setPlayWhenReady(playWhenReady)
                    playService.startPlayWithParam(presenter, playingParam)
                }
            }
        }
        playService.addExoPlayerListener()
    }

    companion object {
        private const val TAG = "SwitchPlayer"
    }
}