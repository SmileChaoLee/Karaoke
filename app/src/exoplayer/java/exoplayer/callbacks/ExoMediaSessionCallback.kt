package exoplayer.callbacks

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.os.BundleCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.MimeTypes
import com.smile.karaokeplayer.constants.PlayerConstants
import com.smile.karaokeplayer.models.PlayingParameters
import exoplayer.presenters.ExoPlayerPresenter
import exoplayer.services.ExoPlayService

@UnstableApi
class ExoMediaSessionCallback(private val presenter : ExoPlayerPresenter,
                              private val playService: ExoPlayService)
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

    @OptIn(UnstableApi::class)
    @Synchronized
    override fun onPrepareFromUri(uri: Uri, extras: Bundle?) {
        Log.d(TAG, "onPrepareFromUri().Uri = $uri")
        val playingParam: PlayingParameters? = presenter.playingParam
        playingParam?.preparedStatus = 1
        val trackParameters = TrackSelectionParameters.Builder(playService.applicationContext).build()
        playService.setTrackSelectionParameters(trackParameters)
        // val mediaItem = MediaItem.fromUri(uri)
        val mediaItem = MediaItem.Builder()
            .setUri(uri)
            .setMediaMetadata(MediaMetadata.Builder().setTitle("Opened Media").build())
            .setMimeType(MimeTypes.BASE_TYPE_VIDEO) // .setDrmConfiguration(null)
            .build()
        playService.setMediaItem(mediaItem)
        Log.d(TAG,"onPrepareFromUri().service.exoPlayer.getMediaItemCount() = " +
                playService.getMediaItemCount())
        Log.d(TAG, "onPrepareFromUri().service.exoPlayer.prepare()")
        playService.prepare()
        val currentVolume = playingParam?.currentVolume
        var currentAudioPosition = playingParam?.currentAudioPosition
        var currentPlaybackState = playingParam?.currentPlaybackState
        Log.d(TAG, "onPrepareFromUri().currentVolume = " + currentVolume +
                ", currentAudioPosition= " + currentAudioPosition + ", currentPlaybackState = " +
                currentPlaybackState)
        extras?.let {
            Log.d(TAG, "onPrepareFromUri().extras is not null.")
            val playingParamOrigin = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                BundleCompat.getParcelable(it,
                    PlayerConstants.PlayingParamOrigin,
                    PlayingParameters::class.java)
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
            playService.setPlayerTime(it)
        }

        when (currentPlaybackState) {
            PlaybackStateCompat.STATE_PAUSED -> {
                Log.d(TAG, "onPrepareFromUri().PlaybackStateCompat.STATE_PAUSED")
                playService.setPlayWhenReady(false)
            }
            PlaybackStateCompat.STATE_STOPPED -> {
                // playing was finished
                Log.d(TAG, "onPrepareFromUri().PlaybackStateCompat.STATE_STOPPED")
                playService.setPlayWhenReady(false)
            }
            PlaybackStateCompat.STATE_PLAYING -> {
                Log.d(TAG, "onPrepareFromUri().PlaybackStateCompat.STATE_PLAYING")
                playService.setPlayWhenReady(true)  // start playing when ready
            }
            PlayerConstants.PREPARE_MEDIA -> {
                Log.d(TAG, "onPrepareFromUri().PlayerConstants.PREPARE_MEDIA")
                playService.setPlayWhenReady(true)  // start playing when ready
            }
            else -> {
                // PlaybackStateCompat.STATE_NONE:
                // stopped by user
                Log.d(TAG,"onPrepareFromUr().PlaybackStateCompat.STATE_NONE or default")
                playService.setPlayWhenReady(false)
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
        playService.onPlay()
    }

    @Synchronized
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause()")
        playService.onPause()
    }

    @Synchronized
    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop()")
        playService.onStop()
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