package com.smile.u2bplayer.services

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media3.common.util.UnstableApi
import com.pierfrancescosoffritti.androidyoutubeplayer.core.customui.DefaultPlayerUiController
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.*
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.smile.karaoke.callbacks.MediaControllerCallback
import com.smile.karaoke.constants.MyPlayerConstants
import com.smile.karaoke.services.BasePlayService
import com.smile.karaoke.utilities.LogUtil
import com.smile.u2bplayer.callbacks.U2bSessionCallback
import com.smile.u2bplayer.listeners.FScreenListener
import com.smile.u2bplayer.listeners.U2bPlayerListener
import com.smile.u2bplayer.presenters.U2bPresenter
import java.util.Locale

@UnstableApi
class U2bService : BasePlayService() {

    companion object {
        private const val TAG = "U2bService"
    }

    // private lateinit var audioManager: AudioManager
    // private var curAudioVolume by Delegates.notNull<Int>()
    private var mediaSessionCallback: U2bSessionCallback? = null
    var presenter : U2bPresenter? = null
    private var duration = 0L
    private var currentAudioPosition = 0L
    var u2bPlayer: YouTubePlayer? = null
    // do not use
    var u2bCastPlayer: YouTubePlayer? = null
    var isU2bCast = false

    // Binder given to clients.
    private val binder = LocalBinder()
    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods.
        fun getService(): U2bService = this@U2bService
    }

    override fun onCreate() {
        super.onCreate()
        LogUtil.i(TAG, "onCreate")
        // do not use this variable, isCastSessionAvailable because it is for local file
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

    fun initU2bPlayerListener(): U2bPlayerListener {
        return U2bPlayerListener(this@U2bService)
    }

    fun initFScreenListener(): FScreenListener {
        return FScreenListener()
    }

    fun initYouTubePlayerView(hideUI: Boolean = true): YouTubePlayerView {
        return YouTubePlayerView(applicationContext).apply {
            enableAutomaticInitialization = false    // a must for initialize()
            getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
                override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                    u2bPlayer = youTubePlayer
                    hideYoutubeFeatures(this@apply, youTubePlayer, hideUI)
                }
            })
        }
    }

    private fun hideYoutubeFeatures(yView: YouTubePlayerView, player: YouTubePlayer, hideUI: Boolean) {
        val default = DefaultPlayerUiController(yView,player)
        // default.setVideoTitle("")
        // default.showVideoTitle(false)
        // default.showYouTubeButton(false)
        // default.showSeekBar(false)
        // default.showDuration(false)
        // default.showCurrentTime(false)
        // default.showMenuButton(false)
        // default.showFullscreenButton(false)
        // default.showPlayPauseButton(false)
        // default.showBufferingProgress(false)
        default.showUi(!hideUI)
        default.showBufferingProgress(true)
        val defaultUI = default.rootView
        // Set the now-correctly-modified UI
        yView.setCustomPlayerUi(defaultUI)
    }

    fun prepare(videoId: String) {
        LogUtil.i(TAG, "prepare.isU2bCast = $isU2bCast")
        if (isU2bCast) {
            u2bCastPlayer?.loadVideo(videoId, 0f)   // play immediately
        } else {
            u2bPlayer?.loadVideo(videoId, 0f)   // play immediately
        }
    }

    fun getAudioTrack(): Int {
        return 0    // temporary
    }

    fun setAudioTrack(audioTrackId: Int) {
        // Try casting it to the internal implementation to see available methods:

        when (audioTrackId) {
            1 -> {
                // no caption
                // mYouTubePlayer?.setOption("captions", "track", "{}")
            }
            2 -> {
                // English, languageCode = "en"
            }
            else -> {
                // local language depending on device setting
                val languageCode: String = Locale.getDefault().language
            }
        }
    }

    fun getPlayingMediaInfo(audioTrackIndicesList: ArrayList<Int>):Int {
        return 1    // temporary
    }

    override fun onPlay() {
        LogUtil.i(TAG, "onPlay.isU2bCast = $isU2bCast")
        if (isU2bCast) {
            u2bCastPlayer?.play()
        } else {
            u2bPlayer?.play()
        }
    }

    override fun onPause() {
        LogUtil.i(TAG, "onPause.isU2bCast = $isU2bCast")
        if (isU2bCast) {
            u2bCastPlayer?.pause()
        } else {
            u2bPlayer?.pause()
        }
    }

    override fun onStop() {
        LogUtil.i(TAG, "onStop.isU2bCast = $isU2bCast")
        // YouTubePlayer does not have stop() method
        presenter?.playingParam?.let {
            val playbackState = it.currentPlaybackState
            if (playbackState == PlaybackStateCompat.STATE_PAUSED) {
                LogUtil.d(TAG, "onStop.seekTo()")
                if (isU2bCast) {
                    u2bCastPlayer?.seekTo(0f)
                } else {
                    u2bPlayer?.seekTo(0f)
                }
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
                if (isU2bCast) {
                    u2bCastPlayer?.let { player ->
                        player.pause()
                        player.seekTo(0f)
                    }
                } else {
                    u2bPlayer?.let { player ->
                        player.pause()
                        player.seekTo(0f)
                    }
                }
            }
        }
    }

    override fun initMediaCallback() {
        LogUtil.i(TAG, "initMediaCallback.presenter = $presenter")
        presenter?.let {
            mediaSessionCallback = U2bSessionCallback(this@U2bService)
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
        // progress is measured by millisecond
        LogUtil.d(TAG, "setPlayerTime.progress = $progress")
        val seconds = progress / 1000f
        LogUtil.d(TAG, "setPlayerTime.isU2bCast = $isU2bCast")
        if (isU2bCast) {
            u2bCastPlayer?.seekTo(seconds)
        } else {
            u2bPlayer?.seekTo(seconds)
        }
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
        val percentage = (volumeTmp * 100f).toInt()
        LogUtil.i(TAG, "setAudioVolume.isU2bCast = $isU2bCast")
        if (isU2bCast) {
            u2bCastPlayer?.setVolume(percentage)
        } else {
            u2bPlayer?.setVolume(percentage)
        }
    }

    fun setMediaDuration(duration: Long) {
        this.duration = duration
    }

    override fun getMediaDuration(): Long {
        return duration
    }

    fun setCurrentPosition(currentPosition: Long) {
        currentAudioPosition = currentPosition
    }

    override fun getCurrentPosition(): Long {
        LogUtil.d(TAG, "getCurrentPosition")
        return currentAudioPosition
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
        val logStr = "specificPlayerReplayMedia"
        LogUtil.i(TAG, "$logStr.currentAudioPosition = $currentAudioPosition")
        // song is playing, paused, or finished playing
        // switchAudioToVocal() // implement after VlcPlayer can be run
        val seconds = currentAudioPosition / 1000f
        LogUtil.i(TAG, "$logStr.isU2bCast = $isU2bCast")
        if (isU2bCast) {
            u2bCastPlayer?.apply {
                seekTo(seconds)
                play()
            }
        } else {
            u2bPlayer?.apply {
                seekTo(seconds)
                play()
            }
        }
    }

    override fun switchDecoder() {
        // do nothing for now
    }
}