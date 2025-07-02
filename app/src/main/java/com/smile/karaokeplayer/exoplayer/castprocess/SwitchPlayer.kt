package com.smile.karaokeplayer.exoplayer.castprocess

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
        playService.castPlayer?.let { castP ->
            playService.exoPlayer?.let { exoP ->
                Log.d(TAG, "${msgString}.castPlayer and exoPlayer not null")
                exoP.currentMediaItem?.let {
                    val mediaUri = it.localConfiguration?.uri
                    Log.d(TAG, "${msgString}.mediaUri = $mediaUri")
                    val mediaFileName = mediaUri?.path
                    Log.d(TAG, "${msgString}.mediaFileName = $mediaFileName")
                    if (mediaUri == null || mediaFileName == null) {
                        playService.stopCasting()
                        return
                    }
                    val position = exoP.currentPosition
                    val playWhenReady = exoP.playWhenReady
                    exoP.stop() // do not use stopPlay()

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
                    Log.d(TAG, "${msgString}.localMediaUrl = $localMediaUrl")
                    playService.isCastSessionAvailable = true
                    val mediaItem =  it.buildUpon().setUri(localMediaUrl).build()
                    Log.d(TAG, "${msgString}.position = $position")
                    playService.setMediaItem(mediaItem, position)
                    playService.presenter!!.playingParam.currentAudioPosition = position
                    Log.d(TAG, "${msgString}.playWhenReady = $playWhenReady")
                    playService.setPlayWhenReady(playWhenReady)
                    playService.prepare()
                    playService.presenter!!.setCurrentPlayerToPlayerView()
                    Log.d(TAG, "${msgString}.castPlayer.currentMediaItem.uri " +
                            "= ${playService.castPlayer?.currentMediaItem?.localConfiguration?.uri}")
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
        playService.exoPlayer?.let { exoP ->
            playService.castPlayer?.let { castP ->
                Log.d(TAG, "${msgString}.castPlayer and exoPlayer not null")
                castP.stop()    // do not use stopPlay()
                playService.stopCasting()   // isCastSessionAvailable -> false
                castP.currentMediaItem?.let {
                    val mediaUri = it.localConfiguration?.uri
                    Log.d(TAG, "${msgString}.mediaUri = $mediaUri")
                    val mediaFileName = mediaUri?.path
                    Log.d(TAG, "${msgString}.mediaFileName = $mediaFileName")
                    if (mediaUri == null || mediaFileName.isNullOrEmpty()) {
                        return
                    }
                    val position = castP.currentPosition
                    val playWhenReady = castP.playWhenReady

                    // starting switching to exoPlayer.
                    // mediaUri need to be set to local uri?
                    val tempUri = File(mediaFileName).toUri()
                    Log.d(TAG, "${msgString}.tempUri = $tempUri")
                    val mediaItem =  it.buildUpon().setUri(tempUri).build()
                    playService.setMediaItem(mediaItem, position)
                    playService.presenter!!.playingParam.currentAudioPosition = position
                    Log.d(TAG, "${msgString}.playWhenReady = $playWhenReady")
                    playService.setPlayWhenReady(playWhenReady)
                    playService.prepare()
                    playService.presenter!!.setCurrentPlayerToPlayerView()
                }
            }
        }
    }

    companion object {
        private const val TAG = "SwitchPlayer"
    }
}