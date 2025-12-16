package com.smile.youtube.listeners

import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.smile.karaoke.constants.MyPlayerConstants
import com.smile.karaoke.utilities.LogUtil
import com.smile.youtube.services.YouTubeService

@OptIn(UnstableApi::class)
open class YTPlayerListener (private val playService: YouTubeService) :
    YouTubePlayerListener {

    companion object {
        private const val TAG = "YTPlayerListener"
    }

    override fun onReady(youTubePlayer: YouTubePlayer) {
        LogUtil.d(TAG, "onReady")
        // mYouTubePlayer = youTubePlayer   // will be set from YouTubeFragment
        // val videoId = "hPNJ7Ge6-uk"
        // youTubePlayer.loadVideo(videoId, 0f)
    }

    override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
        LogUtil.d(TAG, "onStateChange.state = $state")
        when (state) {
            PlayerConstants.PlayerState.VIDEO_CUED -> {
                // to do
            }
            PlayerConstants.PlayerState.BUFFERING -> {
                LogUtil.d(TAG, "onStateChange.send PlaybackStateCompat.STATE_BUFFERING")
                playService.setMediaPlaybackState(PlaybackStateCompat.STATE_BUFFERING)
            }
            PlayerConstants.PlayerState.PLAYING -> {
                LogUtil.d(TAG, "onStateChange.send PlaybackStateCompat.STATE_PLAYING")
                playService.setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING)
            }
            PlayerConstants.PlayerState.PAUSED -> {
                playService.presenter?.playingParam?.let {
                    if (it.finishState == MyPlayerConstants.STOPPED_BY_USER) {
                        // User stop the playing
                        LogUtil.d(TAG, "onStateChange.send PlaybackStateCompat.STATE_NONE")
                        playService.setMediaPlaybackState(PlaybackStateCompat.STATE_NONE)
                        return
                    } else if (it.finishState == MyPlayerConstants.FINISHED_BY_PROGRAM) {
                        LogUtil.d(TAG, "onStateChange.send PlaybackStateCompat.STATE_STOPPED")
                        playService.setMediaPlaybackState(PlaybackStateCompat.STATE_STOPPED)
                        return
                    }
                }
                LogUtil.d(TAG, "onStateChange.send PlaybackStateCompat.STATE_PAUSED")
                playService.setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED)
            }
            PlayerConstants.PlayerState.ENDED -> {
                LogUtil.d(TAG, "onStateChange.send PlaybackStateCompat.STATE_STOPPED")
                playService.setMediaPlaybackState(PlaybackStateCompat.STATE_STOPPED)
                // playService.hideYoutubeFeatures()    // screen will be black after this
            }
            PlayerConstants.PlayerState.UNSTARTED -> {
                LogUtil.d(TAG, "onStateChange.UNSTARTED.send No event")
            }
            PlayerConstants.PlayerState.UNKNOWN -> {
                LogUtil.d(TAG, "onStateChange.UNKNOWN.send No event")
            }
        }
    }

    override fun onPlaybackQualityChange(youTubePlayer: YouTubePlayer,
        playbackQuality: PlayerConstants.PlaybackQuality) {
        LogUtil.d(TAG, "onPlaybackQualityChange")
    }

    override fun onPlaybackRateChange(youTubePlayer: YouTubePlayer,
                                      playbackRate: PlayerConstants.PlaybackRate) {
        LogUtil.d(TAG, "onPlaybackRateChange")
    }

    override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
        LogUtil.d(TAG, "onError.error = $error")
    }

    override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
        // LogUtil.d(TAG, "onCurrentSecond.second = $second")
        playService.setCurrentPosition((second * 1000f).toLong())
    }

    override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
        // duration is measured in seconds
        LogUtil.d(TAG, "onVideoDuration.duration = $duration")
        playService.setMediaDuration((duration * 1000f).toLong())
    }

    override fun onVideoLoadedFraction(youTubePlayer: YouTubePlayer, loadedFraction: Float) {
        // LogUtil.d(TAG, "onVideoLoadedFraction.loadedFraction = $loadedFraction")
    }

    override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {
        LogUtil.d(TAG, "onVideoId.videoId = $videoId")
    }

    override fun onApiChange(youTubePlayer: YouTubePlayer) {
        LogUtil.d(TAG, "onApiChange")
    }
}