package com.smile.u2b.listeners

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.smile.karaoke.utilities.LogUtil
import com.smile.u2b.services.U2bService

@OptIn(UnstableApi::class)
class U2bCastPlayerListener
    (private val playService: U2bService): U2bPlayerListener(playService) {
    companion object {
        private const val TAG = "U2bCastPlayerListener"
    }

    override fun onReady(youTubePlayer: YouTubePlayer) {
        LogUtil.d(TAG, "onReady")
        playService.u2bCastPlayer = youTubePlayer
        // playService.isU2bCast
        youTubePlayer.play()
    }
}