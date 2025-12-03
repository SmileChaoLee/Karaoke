package youtube.services

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media3.common.util.UnstableApi
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.*
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.smile.karaoke.callbacks.MediaControllerCallback
import com.smile.karaoke.constants.MyPlayerConstants
import com.smile.karaoke.services.BasePlayService
import com.smile.karaoke.utilities.LogUtil
import youtube.callbacks.YouTubeSessionCallback
import youtube.listeners.FScreenListener
import youtube.listeners.PlayerListener
import youtube.presenters.YouTubePresenter

@UnstableApi
class YouTubeService : BasePlayService() {

    companion object {
        private const val TAG = "YouTubeService"
    }

    // private lateinit var audioManager: AudioManager
    // private var curAudioVolume by Delegates.notNull<Int>()
    private var mediaSessionCallback: YouTubeSessionCallback? = null
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

    private fun initPlayerListener(): YouTubePlayerListener {
        return PlayerListener(this@YouTubeService)
    }

    private fun initFullscreenListener(): FullscreenListener {
       return FScreenListener(this@YouTubeService)
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

    fun prepare(videoId: String) {
        LogUtil.i(TAG, "prepare")
        // mYouTubePlayer?.loadVideo("hPNJ7Ge6-uk", 0f)
        mYouTubePlayer?.loadVideo(videoId, 0f)   // play immediately
        // mYouTubePlayer?.cueVideo(videoId, 0f)
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
        // YouTubePlayer does not have stop() method
        presenter?.playingParam?.let {
            val playbackState = it.currentPlaybackState
            if (playbackState == PlaybackStateCompat.STATE_PAUSED) {
                LogUtil.d(TAG, "onStop.seekTo()")
                mYouTubePlayer?.seekTo(0f)
                // use the following method because seekTo() does not
                // trigger any event, so update the playback state manually
                if (it.finishState == MyPlayerConstants.STOPPED_BY_USER) {
                    // stopPlay(MyPlayerConstants.STOPPED_BY_USER)
                    LogUtil.d(TAG, "onStop.send PlaybackStateCompat.STATE_NONE")
                    setMediaPlaybackState(PlaybackStateCompat.STATE_NONE)
                } else {
                    // stopPlay(MyPlayerConstants.FINISHED_BY_PROGRAM)
                    LogUtil.d(TAG, "onStop.send PlaybackStateCompat.STATE_STOPPED")
                    setMediaPlaybackState(PlaybackStateCompat.STATE_STOPPED)
                }
            } else if (playbackState == PlaybackStateCompat.STATE_PLAYING) {
                mYouTubePlayer?.let { player ->
                    LogUtil.d(TAG, "onStop.player.pause()")
                    player.pause()
                    LogUtil.d(TAG, "onStop.player.seekTo()")
                    player.seekTo(0f)
                }
            }
        }
    }

    override fun initMediaCallback() {
        LogUtil.i(TAG, "initMediaCallback.presenter = $presenter")
        presenter?.let {
            mediaSessionCallback = YouTubeSessionCallback(this@YouTubeService)
            mediaSessionCompat?.setCallback(mediaSessionCallback)
            controllerCallback = MediaControllerCallback(it)
            mediaControllerCompat?.registerCallback(controllerCallback!!)
        }
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
        var state = MyPlayerConstants.PREPARE_MEDIA
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