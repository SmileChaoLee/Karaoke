package com.smile.youtube.listeners

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.smile.karaoke.utilities.LogUtil
import com.smile.youtube.services.YouTubeService

@OptIn(UnstableApi::class)
class YTCastPlayerListener
    (private val playService: YouTubeService): YTPlayerListener(playService) {
    companion object {
        private const val TAG = "YTCastPlayerListener"
    }

    override fun onReady(youTubePlayer: YouTubePlayer) {
        LogUtil.d(TAG, "onReady")
        playService.mYTCastPlayer = youTubePlayer
        playService.isYTCast
        youTubePlayer.play()
    }
}