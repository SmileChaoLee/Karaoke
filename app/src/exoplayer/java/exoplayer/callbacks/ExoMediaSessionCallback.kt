package exoplayer.callbacks

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.trackselection.TrackSelectionParameters
import com.smile.karaokeplayer.constants.PlayerConstants
import com.smile.karaokeplayer.models.PlayingParameters
import exoplayer.presenters.ExoPlayerPresenter

class ExoMediaSessionCallback(private val mContext : Context, private val mPresenter : ExoPlayerPresenter)
    : MediaSessionCompat.Callback() {

    companion object {
        const val TAG: String = "ExoMediaSessionCallback"
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
        val playingParam: PlayingParameters = mPresenter.playingParam
        playingParam.isMediaPrepared = false
        val mediaItem = MediaItem.fromUri(uri)
        Log.d(TAG,"onPrepareFromUri().mPresenter.exoPlayer.getMediaItemCount() = " +
                mPresenter.exoPlayer.mediaItemCount)
        val trackParameters = TrackSelectionParameters.Builder(mContext).build()
        mPresenter.exoPlayer.trackSelectionParameters = trackParameters
        mPresenter.exoPlayer.setMediaItem(mediaItem)
        Log.d(TAG, "onPrepareFromUri().mPresenter.exoPlayer.prepare()")
        mPresenter.exoPlayer.prepare()
        val currentVolume = playingParam.currentVolume
        var currentAudioPosition = playingParam.currentAudioPosition
        var currentPlaybackState = playingParam.currentPlaybackState
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
        mPresenter.exoPlayer.seekTo(currentAudioPosition)

        when (currentPlaybackState) {
            PlaybackStateCompat.STATE_PAUSED -> {
                Log.d(TAG, "onPrepareFromUri().PlaybackStateCompat.STATE_PAUSED")
                mPresenter.exoPlayer.playWhenReady = false
            }
            PlaybackStateCompat.STATE_STOPPED -> {
                Log.d(TAG, "onPrepareFromUri().PlaybackStateCompat.STATE_STOPPED")
                mPresenter.exoPlayer.playWhenReady = false
            }
            PlaybackStateCompat.STATE_PLAYING -> {
                Log.d(TAG, "onPrepareFromUri().PlaybackStateCompat.STATE_PLAYING")
                mPresenter.exoPlayer.playWhenReady = true // start playing when ready
            }
            else -> {
                // PlaybackStateCompat.STATE_NONE:
                Log.d(TAG,"onPrepareFromUr().iPlaybackStateCompat.STATE_NONE or default")
                mPresenter.exoPlayer.playWhenReady = true // start playing when ready
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
        val controller: MediaControllerCompat = mPresenter.mediaControllerCompat
        controller.playbackState?.let {
            Log.d(TAG, "onPlay().controller.playbackState.state = ${it.state}")
            if (it.state != PlaybackStateCompat.STATE_PLAYING) {
                Log.d(TAG, "onPlay().mPresenter.exoPlayer.play()")
                mPresenter.exoPlayer.play()
            }
        }
    }

    @Synchronized
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause()")
        val controller: MediaControllerCompat = mPresenter.mediaControllerCompat
        controller.playbackState?.let {
            Log.d(TAG, "onPause().controller.playbackState.state = ${it.state}")
            if (it.state != PlaybackStateCompat.STATE_PAUSED) {
                Log.d(TAG, "onPause().mPresenter.exoPlayer.pause()")
                mPresenter.exoPlayer.pause()
            }
        }
    }

    @Synchronized
    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop()")
        val controller: MediaControllerCompat = mPresenter.mediaControllerCompat
        controller.playbackState?.let {
            Log.d(TAG, "onStop().controller.playbackState.state = ${it.state}")
            if (it.state != PlaybackStateCompat.STATE_STOPPED) {
                Log.d(TAG, "onStop().mPresenter.exoPlayer.stop()")
                mPresenter.exoPlayer.stop()
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