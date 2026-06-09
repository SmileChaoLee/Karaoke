package com.smile.u2bplayer.listeners

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.smile.karaoke.utilities.LogUtil
import com.smile.u2bplayer.services.U2bService

@OptIn(UnstableApi::class)
class U2bCastPlayerListener
    (private val playService: U2bService): U2bPlayerListener(playService) {

    private val mTAG = "U2bCastPlayerListener"

    init {
        LogUtil.d(mTAG, "U2bCastPlayerListener.init")
        setTag(mTAG)
    }

    override fun onReady(youTubePlayer: YouTubePlayer) {
        LogUtil.d(mTAG, "onReady - cast player ready")
        playService.u2bCastPlayer = youTubePlayer
        // Load the current video from local player onto cast player
        val currentVideoId = playService.getCurrentVideoId()
        if (currentVideoId.isNotEmpty()) {
            LogUtil.d(mTAG, "onReady - loading currentVideoId=$currentVideoId on cast player")
            val currentPosition = playService.getCurrentPosition() / 1000f // convert to seconds
            youTubePlayer.loadVideo(currentVideoId, currentPosition)
        } else {
            LogUtil.d(mTAG, "onReady - no current video to load")
        }
    }
}