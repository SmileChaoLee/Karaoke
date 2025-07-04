package com.smile.karaokeplayer.exoplayer.cast

import android.util.Log
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import com.smile.karaokeplayer.exoplayer.services.ExoPlayService
import java.io.File

@OptIn(UnstableApi::class)
class SwitchPlayer(private val playService: ExoPlayService) {

    fun transferPlaybackToCast() {
        val msgString = "transferPlaybackToCast"
        Log.d(TAG, msgString)
        if (playService.presenter == null) {
            Log.d(TAG, "${msgString}.presenter is null")
            return
        }
        val presenter = playService.presenter!!
        playService.castPlayer?.let { castP ->
            playService.exoPlayer?.let { exoP ->
                Log.d(TAG, "${msgString}.castPlayer and exoPlayer not null")
                presenter.mediaUri?.let {
                    val mediaUri = it
                    Log.d(TAG, "${msgString}.mediaUri = $mediaUri")
                    val mediaFileName = mediaUri.path
                    Log.d(TAG, "${msgString}.mediaFileName = $mediaFileName")
                    if (mediaFileName.isNullOrEmpty()) {
                        playService.stopCasting()
                        return
                    }
                    val playWhenReady = exoP.playWhenReady
                    val position = exoP.currentPosition
                    val playbackState = presenter.playingParam.currentPlaybackState
                    exoP.stop() // do not use playService.stopPlay()

                    // starting switching to castPlayer
                    playService.webServerAndCast.startWebServer(mediaFileName)
                    // must after startWebServer
                    val localMediaUrl = playService.webServerAndCast.getMediaUrl()
                    if (localMediaUrl.isEmpty()) {
                        Log.d(TAG, "${msgString}.localMediaUrl is empty")
                        playService.stopCasting()
                        return
                    }
                    //
                    playService.isCastSessionAvailable = true
                    Log.d(TAG, "${msgString}.localMediaUrl = $localMediaUrl")
                    Log.d(TAG, "${msgString}.position = $position")
                    Log.d(TAG, "${msgString}.playWhenReady = $playWhenReady")
                    presenter.mediaUri = localMediaUrl.toUri()
                    presenter.playingParam.preparedStatus = 0
                    presenter.playingParam.currentPlaybackState = playbackState
                    presenter.playingParam.currentAudioPosition = position
                    playService.setPlayWhenReady(playWhenReady)
                    presenter.setCurrentPlayerToPlayerView()
                    playService.startPlayWithParam(presenter, presenter.playingParam)
                }
            }
        }
    }

    fun transferPlaybackToLocal() {
        val msgString = "transferPlaybackToLocal"
        Log.d(TAG, msgString)
        if (playService.presenter == null) {
            Log.d(TAG, "${msgString}.presenter is null")
            return
        }
        val presenter = playService.presenter!!
        playService.exoPlayer?.let { exoP ->
            playService.castPlayer?.let { castP ->
                Log.d(TAG, "${msgString}.castPlayer and exoPlayer not null")
                val position = castP.currentPosition
                val playWhenReady = castP.playWhenReady
                val playbackState = presenter.playingParam.currentPlaybackState
                castP.stop()    // do not use stopPlay()
                playService.stopCasting()   // isCastSessionAvailable -> false
                presenter.setCurrentPlayerToPlayerView()
                presenter.mediaUri?.let {
                    val mediaUri = it
                    Log.d(TAG, "${msgString}.mediaUri = $mediaUri")
                    val mediaFileName = mediaUri.path
                    Log.d(TAG, "${msgString}.mediaFileName = $mediaFileName")
                    if (mediaFileName.isNullOrEmpty()) {
                        return
                    }
                    // starting switching to exoPlayer.
                    // mediaUri need to be set to local uri?
                    val tempUri = File(mediaFileName).toUri()
                    Log.d(TAG, "${msgString}.tempUri = $tempUri")
                    Log.d(TAG, "${msgString}.position = $position")
                    Log.d(TAG, "${msgString}.playWhenReady = $playWhenReady")
                    presenter.mediaUri = tempUri
                    presenter.playingParam.preparedStatus = 0
                    presenter.playingParam.currentPlaybackState = playbackState
                    presenter.playingParam.currentAudioPosition = position
                    playService.setPlayWhenReady(playWhenReady)
                    playService.startPlayWithParam(presenter, presenter.playingParam)
                }
            }
        }
    }

    companion object {
        private const val TAG = "SwitchPlayer"
    }
}