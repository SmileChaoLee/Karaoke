package youtube.callbacks

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.smile.karaoke.constants.MyPlayerConstants
import com.smile.karaoke.models.PlayingParameters
import com.smile.karaoke.utilities.LogUtil
import youtube.services.YouTubeService

@UnstableApi
class YouTubeSessionCallback(private val playService: YouTubeService)
    : MediaSessionCompat.Callback() {

    companion object {
        const val TAG: String = "YouTubeSessionCallback"
    }

    @Synchronized
    override fun onCommand(command: String?, extras: Bundle?, cb: ResultReceiver?) {
        super.onCommand(command, extras, cb)
        LogUtil.d(TAG, "onCommand()")
    }

    @Synchronized
    override fun onPrepare() {
        super.onPrepare()
        LogUtil.d(TAG, "onPrepare()")
    }

    @Synchronized
    override fun onPrepareFromMediaId(mediaId: String?, extras: Bundle?) {
        super.onPrepareFromMediaId(mediaId, extras)
        LogUtil.d(TAG, "onPrepareFromMediaId()")
    }

    @OptIn(UnstableApi::class)
    @Synchronized
    override fun onPrepareFromUri(uri: Uri, extras: Bundle?) {
        val presenter = playService.presenter
        LogUtil.d(TAG, "onPrepareFromUri().Uri = $uri")
        LogUtil.d(TAG, "onPrepareFromUri().Uri.path = ${uri.path}")
        val playingParam: PlayingParameters? = presenter?.playingParam
        playingParam?.preparedStatus = 1
        LogUtil.d(TAG, "removeVideoPlayerView")
        // presenter.presentView.removeVideoPlayerView()
        LogUtil.d(TAG, "setVideoPlayerView")
        // presenter.presentView.setVideoPlayerView()
        LogUtil.d(TAG, "onPrepareFromUri().playService.prepare()")
        playService.prepare(uri.toString())
        val currentVolume = playingParam?.currentVolume
        var currentAudioPosition = playingParam?.currentAudioPosition
        var currentPlaybackState = playingParam?.currentPlaybackState
        LogUtil.d(
            TAG, "onPrepareFromUri().currentVolume = " + currentVolume +
                ", currentAudioPosition= " + currentAudioPosition + ", currentPlaybackState = " +
                currentPlaybackState)
        extras?.let {
            LogUtil.d(TAG, "onPrepareFromUri().extras is not null.")
            val playingParamOrigin = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getParcelable(MyPlayerConstants.PlayingParamOrigin,
                    PlayingParameters::class.java)
            } else it.getParcelable(MyPlayerConstants.PlayingParamOrigin)
            playingParamOrigin?.let { playIt ->
                currentPlaybackState = playIt.currentPlaybackState
                currentAudioPosition = playIt.currentAudioPosition
                LogUtil.d(
                    TAG,
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
                LogUtil.d(TAG, "onPrepareFromUri().PlaybackStateCompat.STATE_PAUSED")
                playService.onPause()
            }
            PlaybackStateCompat.STATE_STOPPED -> {
                // playing was finished
                LogUtil.d(TAG, "onPrepareFromUri().PlaybackStateCompat.STATE_STOPPED")
                playService.onStop()
            }
            PlaybackStateCompat.STATE_PLAYING -> {
                LogUtil.d(TAG, "onPrepareFromUri().PlaybackStateCompat.STATE_PLAYING")
                playService.onPlay()
            }
            MyPlayerConstants.PREPARE_MEDIA -> {
                LogUtil.d(TAG, "onPrepareFromUri().PlayerConstants.PREPARE_MEDIA")
                playService.onPlay()
            }
            else -> {
                // PlaybackStateCompat.STATE_NONE:
                // stopped by user
                LogUtil.d(TAG,"onPrepareFromUr().PlaybackStateCompat.STATE_NONE or default")
                playService.onStop()
            }
        }
    }

    @Synchronized
    override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
        super.onPlayFromMediaId(mediaId, extras)
        LogUtil.d(TAG, "onPlayFromMediaId()")
    }

    @Synchronized
    override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
        super.onPlayFromUri(uri, extras)
        LogUtil.d(TAG, "onPlayFromUri()")
    }

    @Synchronized
    override fun onPlay() {
        super.onPlay()
        LogUtil.d(TAG, "onPlay()")
        playService.onPlay()
    }

    @Synchronized
    override fun onPause() {
        super.onPause()
        LogUtil.d(TAG, "onPause()")
        playService.onPause()
    }

    @Synchronized
    override fun onStop() {
        super.onStop()
        LogUtil.d(TAG, "onStop()")
        playService.onStop()
    }

    @Synchronized
    override fun onFastForward() {
        super.onFastForward()
        LogUtil.d(TAG, "onFastForward()")
    }

    @Synchronized
    override fun onRewind() {
        super.onRewind()
        LogUtil.d(TAG, "onRewind()")
    }
}