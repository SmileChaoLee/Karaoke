package com.smile.karaoke.exoplayer.cast

import android.util.Log
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import com.smile.karaoke.exoplayer.services.ExoPlayService
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
                val playWhenReady = exoP.playWhenReady
                val position = exoP.currentPosition
                val playbackState = presenter.playingParam.currentPlaybackState
                presenter.mediaUri?.let {
                    val mediaUri = it
                    Log.d(TAG, "${msgString}.mediaUri = $mediaUri")
                    val mediaFileName = mediaUri.path
                    Log.d(TAG, "${msgString}.mediaFileName = $mediaFileName")
                    if (mediaFileName.isNullOrEmpty()) {
                        playService.stopCasting()
                        return
                    }
                    // starting switching to castPlayer
                    playService.webServerAndCast.startWebServer(mediaFileName)
                    // must after startWebServer
                    val localMediaUrl = playService.webServerAndCast.getMediaUrl()
                    if (localMediaUrl.isEmpty()) {
                        Log.d(TAG, "${msgString}.localMediaUrl is empty")
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
                    Log.d(TAG, "${msgString}.localMediaUrl = $localMediaUrl")
                    Log.d(TAG, "${msgString}.position = $position")
                    Log.d(TAG, "${msgString}.playWhenReady = $playWhenReady")
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
        Log.d(TAG, msgString)
        if (playService.presenter == null) {
            Log.d(TAG, "${msgString}.presenter is null")
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
                Log.d(TAG, "${msgString}.castPlayer and exoPlayer not null")
                val position = castP.currentPosition
                val playWhenReady = castP.playWhenReady
                val playbackState = presenter.playingParam.currentPlaybackState

                castP.stop()    // do not use stopPlay()
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