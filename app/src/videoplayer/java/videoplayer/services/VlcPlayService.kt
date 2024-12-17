package videoplayer.services

import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.smile.karaokeplayer.constants.CommonConstants
import com.smile.karaokeplayer.services.BasePlayService
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.interfaces.IMedia
import org.videolan.libvlc.util.DisplayManager
import org.videolan.libvlc.util.VLCVideoLayout
import videoplayer.Callbacks.VlcMediaControllerCallback
import videoplayer.Callbacks.VlcMediaSessionCallback
import videoplayer.Listeners.VlcPlayerEventListener
import videoplayer.Presenters.VlcPlayerPresenter

class VlcPlayService : BasePlayService() {

    companion object {
        private const val TAG = "VlcPlayService"
    }

    private var presenter : VlcPlayerPresenter? = null
    private var mediaSessionCallback: VlcMediaSessionCallback? = null
    private var controllerCallback: VlcMediaControllerCallback? = null
    var libVLC: LibVLC? = null
    var vlcPlayer: MediaPlayer? = null

    // Binder given to clients.
    private val binder = LocalBinder()
    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods.
        fun getService(): VlcPlayService = this@VlcPlayService
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "onBind.binder = $binder")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind.intent = $intent")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
        detachPlayerViews()
        releaseVlcPlayer()
        mediaControllerCompat?.apply {
            controllerCallback?.let {
                registerCallback(it)
            }
        }
    }

    fun setPresenter(presenter: VlcPlayerPresenter) {
        this.presenter = presenter
    }

    fun initVlcPlayer() {
        Log.d(TAG, "initVlcPlayer.presenter = $presenter")
        presenter?.let {
            libVLC = LibVLC(it.activity)
            vlcPlayer = MediaPlayer(libVLC)
            vlcPlayer?.apply {
                setEventListener(VlcPlayerEventListener(it, this@VlcPlayService))
            }
        }
    }

    private fun releaseVlcPlayer() {
        Log.d(TAG, "releaseVlcPlayer.vlcPlayer = $vlcPlayer")
        vlcPlayer?.apply {
            stop()
            detachViews()
            release()
            vlcPlayer = null
        }
        libVLC?.release()
        libVLC = null
    }

    fun attachPlayerViews(videoVLCPlayerView: VLCVideoLayout, dm: DisplayManager?,
        enableSUBTITLES: Boolean, use_TEXTURE_VIEW: Boolean) {
        Log.d(TAG,"attachPlayerViews.vlcPlayer = $vlcPlayer")
        vlcPlayer?.apply {
            Log.d(TAG,"attachPlayerViews.areViewsAttached = " + vlcVout.areViewsAttached())
            if (!vlcVout.areViewsAttached()) {
                attachViews(videoVLCPlayerView, dm, enableSUBTITLES, use_TEXTURE_VIEW)
            }
        }
    }

    fun detachPlayerViews() {
        Log.d(TAG,"detachPlayerViews.vlcPlayer = $vlcPlayer")
        vlcPlayer?.apply {
            Log.d(TAG,"detachPlayerViews.areViewsAttached = " + vlcVout.areViewsAttached())
            if (vlcVout.areViewsAttached()) {
                detachViews()
            }
        }
    }

    fun prepare(med: IMedia) {
        vlcPlayer?.apply {
            media = med
            play()
        }
    }
    fun createMedia(uri: Uri): IMedia {
        return Media(libVLC, uri)
    }
    fun setAudioTrack(audioTrackId: Int) {
        vlcPlayer?.setAudioTrack(audioTrackId)
    }
    fun onPlay() {
        Log.d(TAG, "onPlay.presenter = $presenter")
        presenter?.playingParam?.let {
            Log.d(TAG, "onPlay().vlcPlayer = $vlcPlayer")
            vlcPlayer?.apply {
                if (it.isMediaPrepared &&
                    it.currentPlaybackState != PlaybackStateCompat.STATE_PLAYING) {
                    Log.d(TAG, "onPlay().vlcPlayer is not playing, so play()")
                    play()
                }
            }
        }
    }
    fun onPause() {
        Log.d(TAG, "onPause.presenter = $presenter")
        presenter?.playingParam?.let {
            Log.d(TAG, "onPause().vlcPlayer = $vlcPlayer")
            vlcPlayer?.apply {
                if (it.isMediaPrepared &&
                    it.currentPlaybackState == PlaybackStateCompat.STATE_PLAYING) {
                    Log.d(TAG, "onPause().vlcPlayer is playing, so pause()")
                    pause()
                }
            }
        }
    }
    fun onStop() {
        Log.d(TAG, "onStop.presenter = $presenter")
        presenter?.playingParam?.let {
            Log.d(TAG, "onStop().vlcPlayer = $vlcPlayer")
            vlcPlayer?.apply {
                if (it.isMediaPrepared ||
                    it.currentPlaybackState == PlaybackStateCompat.STATE_BUFFERING) {
                    Log.d(TAG, "onStop().vlcPlayer is PlaybackStateCompat.STATE_READY or " +
                            "PlaybackStateCompat.STATE_BUFFERING , so stop()")
                    stop()
                }
            }
        }
    }
    // For ExoPlayerListener.java
    fun isPlaying(): Boolean? {
        Log.d(TAG, "isPlaying().vlcPlayer?.isPlaying = ${vlcPlayer?.isPlaying}")
        return vlcPlayer?.isPlaying
    }
    //

    override fun initMediaCallback() {
        Log.d(TAG, "initMediaCallback.presenter = $presenter")
        presenter?.let {
            mediaSessionCallback = VlcMediaSessionCallback(it, this@VlcPlayService)
            Log.d(TAG,"initMediaCallback.mediaSessionCallback = $mediaSessionCallback")
            mediaSessionCompat?.setCallback(mediaSessionCallback)
            controllerCallback = VlcMediaControllerCallback(it)
            Log.d(TAG,"initMediaCallback.controllerCallback = $controllerCallback")
            mediaControllerCompat?.registerCallback(controllerCallback!!)
        }
    }

    override fun setPlayerTime(progress: Long) {
        Log.d(TAG, "setPlayerTime")
        vlcPlayer?.setTime(progress)
    }

    override fun isSeekable(): Boolean {
        Log.d(TAG, "isSeekable")
        vlcPlayer?.apply {
            return isSeekable
        }
        return false;
    }

    override fun setAudioVolume(volumeTmp: Float) {
        Log.d(TAG, "setAudioVolume")
        presenter?.playingParam?.let {
            Log.d(TAG, "setAudioVolume.presenter?.playingParam is not null")
            // get current channel
            val audioChannel: Int = it.currentChannelPlayed
            var leftVolume: Float = volumeTmp
            var rightVolume: Float = volumeTmp
            when (audioChannel) {
                CommonConstants.LeftChannel -> rightVolume = 0f
                CommonConstants.RightChannel -> leftVolume = 0f
                CommonConstants.StereoChannel -> leftVolume = rightVolume
            }
            it.currentVolume = volumeTmp

            // removed on 2022-08-29 for testing
            val vlcMaxVolume = 100
            vlcPlayer?.setVolume((volumeTmp * vlcMaxVolume).toInt())
        }
        Log.d(TAG, "setAudioVolume.presenter?.playingParam is null")
    }

    override fun getMediaDuration(): Long {
        vlcPlayer?.apply {
            return length
        }
        return 0
    }

    override fun getCurrentPosition(): Long {
        vlcPlayer?.apply {
            return time
        }
        return 0
    }

    override fun getPlaybackState(): Int {
        vlcPlayer?.apply {
            return playerState
        }
        // return vlcPlayer.STATE_IDLE
        return PlaybackStateCompat.STATE_NONE
    }

    override fun specificPlayerReplayMedia(currentAudioPosition: Long) {
        // song is playing, paused, or finished playing
        // switchAudioToVocal() // implement after VlcPlayer can be run
        vlcPlayer?.apply {
            setTime(currentAudioPosition) // use time to set position
            if (!isPlaying) {
                play()
            }
        }
    }
}