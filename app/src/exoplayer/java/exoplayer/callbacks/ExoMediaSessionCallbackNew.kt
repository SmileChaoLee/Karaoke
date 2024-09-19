package exoplayer.callbacks

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.trackselection.TrackSelectionParameters
import com.smile.karaokeplayer.constants.PlayerConstants
import com.smile.karaokeplayer.models.PlayingParameters
import exoplayer.presenters.ExoPlayerPresenter
import exoplayer.services.ExoPlayService

class ExoMediaSessionCallbackNew(private val presenter : ExoPlayerPresenter,
                                 private val service: ExoPlayService)
    : MediaSessionCompat.Callback() {

    companion object {
        const val TAG: String = "ExoMediaSessionCallbackNew"
    }

    @Synchronized
    override fun onCommand(command: String?, extras: Bundle?, cb: ResultReceiver?) {
        super.onCommand(command, extras, cb)
        Log.d(TAG, "onCommand()")
    }

    @Synchronized
    override fun onPrepare() {
        super.onPrepare()
        Log.d(TAG, "onPrepare()")
    }

    @Synchronized
    override fun onPrepareFromMediaId(mediaId: String?, extras: Bundle?) {
        super.onPrepareFromMediaId(mediaId, extras)
        Log.d(TAG, "onPrepareFromMediaId()")
    }

    @Synchronized
    override fun onPrepareFromUri(uri: Uri, extras: Bundle?) {
        Log.d(TAG, "onPrepareFromUri().Uri = $uri")
        val playingParam: PlayingParameters? = presenter.playingParam
        playingParam?.isMediaPrepared = false
        val mediaItem = MediaItem.fromUri(uri)
        Log.d(TAG,"onPrepareFromUri().service.exoPlayer.getMediaItemCount() = " +
                service.exoPlayer?.mediaItemCount)
        val trackParameters = TrackSelectionParameters.Builder(service.applicationContext).build()
        service.exoPlayer?.trackSelectionParameters = trackParameters
        service.exoPlayer?.setMediaItem(mediaItem)
        Log.d(TAG, "onPrepareFromUri().service.exoPlayer.prepare()")
        service.exoPlayer?.prepare()
        val currentVolume = playingParam?.currentVolume
        var currentAudioPosition = playingParam?.currentAudioPosition
        var currentPlaybackState = playingParam?.currentPlaybackState
        Log.d(TAG, "onPrepareFromUri().currentVolume = " + currentVolume +
                ", currentAudioPosition= " + currentAudioPosition + ", currentPlaybackState = " +
                currentPlaybackState)
        extras?.let {
            Log.d(TAG, "onPrepareFromUri().extras is not null.")
            val playingParamOrigin = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getParcelable(
                    PlayerConstants.PlayingParamOrigin,
                    PlayingParameters::class.java
                )
            } else it.getParcelable(PlayerConstants.PlayingParamOrigin)
            playingParamOrigin?.let { playIt ->
                currentPlaybackState = playIt.currentPlaybackState
                currentAudioPosition = playIt.currentAudioPosition
                Log.d(TAG,
                    "onPrepareFromUri().not null.currentVolume = " + currentVolume +
                            ", currentAudioPosition= " + currentAudioPosition + ", currentPlaybackState = " +
                            currentPlaybackState)
            }
        }
        currentAudioPosition?.let {
            service.exoPlayer?.seekTo(it)
        }

        when (currentPlaybackState) {
            PlaybackStateCompat.STATE_PAUSED -> {
                Log.d(TAG, "onPrepareFromUri().PlaybackStateCompat.STATE_PAUSED")
                service.exoPlayer?.playWhenReady = false
            }
            PlaybackStateCompat.STATE_STOPPED -> {
                Log.d(TAG, "onPrepareFromUri().PlaybackStateCompat.STATE_STOPPED")
                service.exoPlayer?.playWhenReady = false
            }
            PlaybackStateCompat.STATE_PLAYING -> {
                Log.d(TAG, "onPrepareFromUri().PlaybackStateCompat.STATE_PLAYING")
                service.exoPlayer?.playWhenReady = true // start playing when ready
            }
            else -> {
                // PlaybackStateCompat.STATE_NONE:
                Log.d(TAG,"onPrepareFromUr().iPlaybackStateCompat.STATE_NONE or default")
                service.exoPlayer?.playWhenReady = true // start playing when ready
            }
        }
    }

    @Synchronized
    override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
        super.onPlayFromMediaId(mediaId, extras)
        Log.d(TAG, "onPlayFromMediaId()")
    }

    @Synchronized
    override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
        super.onPlayFromUri(uri, extras)
        Log.d(TAG, "onPlayFromUri()")
    }

    @Synchronized
    override fun onPlay() {
        super.onPlay()
        Log.d(TAG, "onPlay()")
        service.exoPlayer?.apply {
            Log.d(TAG, "onPlay().service.exoPlayer not null")
            if (playbackState == Player.STATE_READY && !isPlaying) {
                Log.d(TAG, "onPlay().service.exoPlayer is not playing, so play()")
                play()
            }
        }
    }

    @Synchronized
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause()")
        service.exoPlayer?.apply {
            Log.d(TAG, "onPause().service.exoPlayer not null")
            if (playbackState == Player.STATE_READY && isPlaying) {
                Log.d(TAG, "onPause().service.exoPlayer is playing, so pause()")
                pause()
            }
        }
    }

    @Synchronized
    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop()")
        service.exoPlayer?.apply {
            Log.d(TAG, "onPause().service.exoPlayer not null")
            if (playbackState == Player.STATE_READY || playbackState == Player.STATE_BUFFERING) {
                Log.d(TAG, "onPlay().service.exoPlayer is Player.STATE_READY or " +
                        "Player.STATE_BUFFERING , so stop()")
                stop()
            }
        }
    }

    @Synchronized
    override fun onFastForward() {
        super.onFastForward()
        Log.d(TAG, "onFastForward()")
    }

    @Synchronized
    override fun onRewind() {
        super.onRewind()
        Log.d(TAG, "onRewind()")
    }
}