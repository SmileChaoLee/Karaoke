package com.smile.u2bplayer.listeners

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.smile.karaoke.utilities.LogUtil
import com.smile.u2bplayer.services.U2bService

@OptIn(UnstableApi::class)
class U2bCastPlayerListenerOld
    (private val playService: U2bService): AbstractYouTubePlayerListener() {

    companion object {
        private const val TAG = "U2bCastPlayerListenerOld"
    }

    init {
        LogUtil.d(TAG, "U2bCastPlayerListenerOld initialized")
    }

    override fun onReady(youTubePlayer: YouTubePlayer) {
        LogUtil.d(TAG, "onReady - cast player ready")
        playService.u2bCastPlayer = youTubePlayer
        
        // Load the current video from local player onto cast player
        val currentVideoId = playService.getCurrentVideoId()
        if (currentVideoId.isNotEmpty()) {
            LogUtil.d(TAG, "onReady - loading currentVideoId=$currentVideoId on cast player")
            val currentPosition = playService.getCurrentPosition() / 1000f // convert to seconds
            youTubePlayer.loadVideo(currentVideoId, currentPosition)
        } else {
            LogUtil.d(TAG, "onReady - no current video to load")
        }
    }

    override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
        LogUtil.d(TAG, "onStateChange: $state")
    }

    override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
        LogUtil.e(TAG, "onError: $error")
    }

    override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {
        LogUtil.d(TAG, "onVideoId: $videoId")
    }
}