package karaoketvplayer

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.*
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.smile.karaoke.databinding.ActivityYoutubeViewbindingBinding
import com.smile.karaoke.utilities.LogUtil

@OptIn(UnstableApi::class)
open class YouTubeActivityAlone : AppCompatActivity() {

    companion object {
        private const val TAG : String = "YouTubeActivityAlone"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        val binding = ActivityYoutubeViewbindingBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val player = object: YouTubePlayer {
            override fun loadVideo(videoId: String, startSeconds: Float) {
                LogUtil.d(TAG, "YouTubePlayer.loadVideo")
            }

            override fun cueVideo(videoId: String, startSeconds: Float) {
                LogUtil.d(TAG, "YouTubePlayer.cueVideo")
            }

            override fun play() {
                LogUtil.d(TAG, "YouTubePlayer.play")
            }

            override fun pause() {
                LogUtil.d(TAG, "YouTubePlayer.pause")
            }

            override fun nextVideo() {
                LogUtil.d(TAG, "YouTubePlayer.nextVideo")
            }

            override fun previousVideo() {
                LogUtil.d(TAG, "YouTubePlayer.previousVideo")
            }

            override fun playVideoAt(index: Int) {
                LogUtil.d(TAG, "YouTubePlayer.playVideoAt")
            }

            override fun setLoop(loop: Boolean) {
                LogUtil.d(TAG, "YouTubePlayer.setLoop")
            }

            override fun setShuffle(shuffle: Boolean) {
                LogUtil.d(TAG, "YouTubePlayer.setShuffle")
            }

            override fun mute() {
                LogUtil.d(TAG, "YouTubePlayer.mute")
            }

            override fun unMute() {
                LogUtil.d(TAG, "YouTubePlayer.unMute")
            }

            override fun isMutedAsync(callback: BooleanProvider) {
                LogUtil.d(TAG, "YouTubePlayer.isMutedAsync")
            }

            override fun setVolume(volumePercent: Int) {
                LogUtil.d(TAG, "YouTubePlayer.setVolume")
            }

            override fun seekTo(time: Float) {
                LogUtil.d(TAG, "YouTubePlayer.seekTo")
            }

            override fun setPlaybackRate(playbackRate: PlayerConstants.PlaybackRate) {
                LogUtil.d(TAG, "YouTubePlayer.setPlaybackRate")
            }

            override fun addListener(listener: YouTubePlayerListener): Boolean {
                LogUtil.d(TAG, "YouTubePlayer.addListener")
                return true
            }

            override fun removeListener(listener: YouTubePlayerListener): Boolean {
                LogUtil.d(TAG, "YouTubePlayer.removeListener")
                return true
            }
        }

        val youTubeView = YouTubePlayerView(this)
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT)
        youTubeView.layoutParams = layoutParams

        // val decorView: View = window.decorView as ViewGroup // root view
        // (decorView as ViewGroup).addView(youTubeView)    // works too
        (binding.root as ViewGroup).addView(youTubeView)    // works

        // val youTubeView2 = findViewById<YouTubePlayerView>(R.id.youtubePlayerView)
        // val youTubeView2 = binding.youtubePlayerView
        lifecycle.addObserver(youTubeView)
        youTubeView.addFullscreenListener(object: FullscreenListener {
            override fun onEnterFullscreen(
                fullscreenView: View,
                exitFullscreen: () -> Unit
            ) {
                LogUtil.d(TAG, "FullscreenListener.onEnterFullscreen")
            }

            override fun onExitFullscreen() {
                LogUtil.d(TAG, "FullscreenListener.onExitFullscreen")
            }
        })

        youTubeView.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
            override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                LogUtil.d(TAG, "getYouTubePlayerWhenReady.onYouTubePlayer")
            }
        })

        youTubeView.addYouTubePlayerListener(object : YouTubePlayerListener {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                LogUtil.d(TAG, "YouTubePlayerListener.onReady")
                val videoId = "hPNJ7Ge6-uk"
                youTubePlayer.loadVideo(videoId, 0f)
            }

            override fun onStateChange(
                youTubePlayer: YouTubePlayer,
                state: PlayerConstants.PlayerState
            ) {
                LogUtil.d(TAG, "YouTubePlayerListener.onStateChange")
            }

            override fun onPlaybackQualityChange(
                youTubePlayer: YouTubePlayer,
                playbackQuality: PlayerConstants.PlaybackQuality
            ) {
                LogUtil.d(TAG, "YouTubePlayerListener.onPlaybackQualityChange")
            }

            override fun onPlaybackRateChange(
                youTubePlayer: YouTubePlayer,
                playbackRate: PlayerConstants.PlaybackRate
            ) {
                LogUtil.d(TAG, "YouTubePlayerListener.onPlaybackRateChange")
            }

            override fun onError(
                youTubePlayer: YouTubePlayer,
                error: PlayerConstants.PlayerError
            ) {
                LogUtil.d(TAG, "YouTubePlayerListener.onError")
            }

            override fun onCurrentSecond(
                youTubePlayer: YouTubePlayer,
                second: Float
            ) {
                LogUtil.d(TAG, "YouTubePlayerListener.onCurrentSecond")
            }

            override fun onVideoDuration(
                youTubePlayer: YouTubePlayer,
                duration: Float
            ) {
                LogUtil.d(TAG, "YouTubePlayerListener.onVideoDuration")
            }

            override fun onVideoLoadedFraction(
                youTubePlayer: YouTubePlayer,
                loadedFraction: Float
            ) {
                LogUtil.d(TAG, "YouTubePlayerListener.onVideoLoadedFraction")
            }

            override fun onVideoId(
                youTubePlayer: YouTubePlayer,
                videoId: String
            ) {
                LogUtil.d(TAG, "YouTubePlayerListener.onVideoId")
            }

            override fun onApiChange(youTubePlayer: YouTubePlayer) {
                LogUtil.d(TAG, "YouTubePlayerListener.onApiChange")
            }
        })
    }
}