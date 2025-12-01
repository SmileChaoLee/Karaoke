package youtube.services

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import androidx.media3.common.util.UnstableApi
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.*
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.smile.karaoke.constants.PlayerConstants
import com.smile.karaoke.services.BasePlayService
import com.smile.karaoke.utilities.LogUtil
import youtube.presenters.YouTubePresenter

@UnstableApi
class YouTubeService : BasePlayService() {

    companion object {
        private const val TAG = "YouTubeService"
    }

    // private lateinit var audioManager: AudioManager
    // private var curAudioVolume by Delegates.notNull<Int>()
    private var mediaSessionCallback = null
    private var controllerCallback = null
    var presenter : YouTubePresenter? = null
    var youTubeView: YouTubePlayerView? = null
    var mYouTubePlayer: YouTubePlayer? = null
    lateinit var playerListener: YouTubePlayerListener
    lateinit var fullscreenListener: FullscreenListener

    // Binder given to clients.
    private val binder = LocalBinder()
    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods.
        fun getService(): YouTubeService = this@YouTubeService
    }

    override fun onCreate() {
        super.onCreate()
        LogUtil.i(TAG, "onCreate")
        playerListener = initPlayerListener()
        fullscreenListener = initFullscreenListener()
        /*
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        curAudioVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        restoreAudioVolume()
        LogUtil.i(TAG, "onCreate.curAudioVolume = $curAudioVolume")
        */
    }

    override fun onBind(intent: Intent?): IBinder {
        LogUtil.i(TAG, "onBind.binder = $binder")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        LogUtil.i(TAG, "onUnbind.intent = $intent")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.i(TAG, "onDestroy")
        // restore the original audio volume before starting this app
        // restoreAudioVolume()
        //
        removeVideoPlayerView()
        releaseYouTubePlayer()
        mediaControllerCompat?.apply {
            controllerCallback?.let {
                unregisterCallback(it)
            }
        }
        mediaSessionCallback = null
    }

    /*
    fun restoreAudioVolume() {
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            curAudioVolume,
            AudioManager.FLAG_SHOW_UI)  // Shows the volume slider UI
    }
    */

    fun initPlayerListener(): YouTubePlayerListener {
        return object : YouTubePlayerListener {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                LogUtil.d(TAG, "YouTubePlayerListener.onReady")
                // mYouTubePlayer = youTubePlayer   // will be set from YouTubeFragment
                // val videoId = "hPNJ7Ge6-uk"
                // youTubePlayer.loadVideo(videoId, 0f)
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
        }
    }

    fun initFullscreenListener(): FullscreenListener {
        return object: FullscreenListener {
            override fun onEnterFullscreen(
                fullscreenView: View,
                exitFullscreen: () -> Unit
            ) {
                LogUtil.d(TAG, "FullscreenListener.onEnterFullscreen")
            }

            override fun onExitFullscreen() {
                LogUtil.d(TAG, "FullscreenListener.onExitFullscreen")
            }
        }
    }

    private fun releaseYouTubePlayer() {
        LogUtil.i(TAG, "releaseYouTubePlayer.youTubePlayer = $mYouTubePlayer")
    }

    fun setVideoPlayerView() {
        LogUtil.i(TAG,"setVideoPlayerView.youTubePlayer = $mYouTubePlayer")
    }

    fun removeVideoPlayerView() {
        LogUtil.i(TAG,"removeVideoPlayerView.youTubePlayer = $mYouTubePlayer")
    }

    fun setVideoWindowSize() {
        LogUtil.i(TAG,"setVideoWindowSize.youTubePlayer = $mYouTubePlayer")
    }

    fun prepare() {
        LogUtil.i(TAG, "prepare")
    }

    fun getAudioTrack(): Int {
        LogUtil.i(TAG, "getAudioTrack")
        return 0    // temporary
    }

    fun setAudioTrack(audioTrackId: Int) {
        LogUtil.i(TAG, "setAudioTrack")
    }

    fun getPlayingMediaInfo(audioTrackIndicesList: ArrayList<Int>):Int {
        LogUtil.i(TAG, "getPlayingMediaInfo")
        return 1    // temporary
    }

    override fun onPlay() {
        LogUtil.i(TAG, "onPlay.youTubePlayer = $mYouTubePlayer")
        mYouTubePlayer?.play()
    }

    override fun onPause() {
        LogUtil.i(TAG, "onPause.youTubePlayer = $mYouTubePlayer")
        mYouTubePlayer?.pause()
    }

    override fun onStop() {
        LogUtil.i(TAG, "onStop.youTubePlayer = $mYouTubePlayer")
        val playbackState = presenter?.playingParam?.currentPlaybackState
        if (playbackState == PlaybackStateCompat.STATE_PLAYING ||
            playbackState == PlaybackStateCompat.STATE_PAUSED) {
            mYouTubePlayer?.pause()
        }
    }

    override fun initMediaCallback() {
        LogUtil.i(TAG, "initMediaCallback")
    }

    override fun isPlaying(): Boolean {
        var isPlaying = false
        presenter?.playingParam?.let {
            isPlaying = it.currentPlaybackState == PlaybackStateCompat.STATE_PLAYING
        }
        LogUtil.d(TAG, "isPlaying.isPlaying = $isPlaying")
        return isPlaying
    }

    override fun setPlayerTime(progress: Long) {
        LogUtil.d(TAG, "setPlayerTime.progress = $progress")
    }

    override fun isSeekable(): Boolean {
        val isSeekable =  true
        LogUtil.d(TAG, "isSeekable.isSeekable = $isSeekable")
        return isSeekable
    }

    /*
    override fun setAudioVolume(volumeTmp: Float) {
        LogUtil.i(TAG, "setAudioVolume.volumeTmp = $volumeTmp")
        presenter?.playingParam?.let {
            // val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            // An integer from 0 to max volume, volumeTmp is between 0.0 and 1.0
            val volumeLevel = (volumeTmp * curAudioVolume).toInt()
            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                volumeLevel,
                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE)  // Shows the volume slider UI
            it.currentVolume = volumeTmp
            return
        }
        LogUtil.i(TAG, "setAudioVolume.presenter?.playingParam is null")
    }
    */

    override fun setAudioVolume(volumeTmp: Float) {
        LogUtil.i(TAG, "setAudioVolume.volumeTmp = $volumeTmp")
        // mYouTubePlayer?.setVolume(volumeTmp.toInt())
    }

    override fun getMediaDuration(): Long {
        val len = 0L
        LogUtil.d(TAG, "getMediaDuration.len")
        return len
    }

    override fun getCurrentPosition(): Long {
        val time = 0L
        LogUtil.d(TAG, "getCurrentPosition.time")
        return time
    }

    override fun getPlaybackState(): Int {
        var state = PlayerConstants.PREPARE_MEDIA
        presenter?.playingParam?.let {
            state = it.currentPlaybackState
        }
        LogUtil.d(TAG, "getPlaybackState.state")
        return state
    }

    override fun specificPlayerReplayMedia(currentAudioPosition: Long) {
        // song is playing, paused, or finished playing
        // switchAudioToVocal() // implement after VlcPlayer can be run
        mYouTubePlayer?.apply {
            presenter?.playingParam?.let {
                if (it.currentPlaybackState != PlaybackStateCompat.STATE_PLAYING) {
                    play()
                }
            }
        }
    }

    override fun switchDecoder() {
        // do nothing for now
    }
}