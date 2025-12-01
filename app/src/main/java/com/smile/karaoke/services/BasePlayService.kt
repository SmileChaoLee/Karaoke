package com.smile.karaoke.services

import android.app.Service
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.smile.karaoke.constants.PlayerConstants
import com.smile.karaoke.models.MySingleton.orderedSongs
import com.smile.karaoke.models.PlayingParameters
import com.smile.karaoke.models.SongInfo
import com.smile.karaoke.presenters.PlayerBasePresenter
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import com.google.android.gms.cast.framework.CastContext
import com.smile.karaoke.SmileAppBase
import com.smile.karaoke.chromecast.SetupChromeCast
import com.smile.karaoke.chromecast.WebServerAndCast
import com.smile.karaoke.utilities.LogUtil

@UnstableApi
abstract class BasePlayService : Service() {

    companion object {
        private const val TAG = "BasePlayService"
    }

    abstract fun initMediaCallback()
    abstract fun isPlaying(): Boolean
    abstract fun onPlay()
    abstract fun onPause()
    abstract fun onStop()
    abstract fun setPlayerTime(progress: Long)
    abstract fun isSeekable(): Boolean
    abstract fun setAudioVolume(volumeTmp: Float)
    abstract fun getMediaDuration(): Long
    abstract fun getCurrentPosition(): Long
    abstract fun getPlaybackState(): Int
    abstract fun specificPlayerReplayMedia(currentAudioPosition: Long)
    abstract fun switchDecoder()

    var mediaSessionCompat: MediaSessionCompat? = null
    var mediaControllerCompat: MediaControllerCompat? = null
    var isCastSessionAvailable = false
    val webServerAndCast = WebServerAndCast()
    var castContext: CastContext? = null
    private lateinit var setupCast: SetupChromeCast

    override fun onCreate() {
        LogUtil.i(TAG, "onCreate")
        castContext = (application as SmileAppBase).castContext
        stopCasting()
        setupCast = SetupChromeCast(this)
        initMediaSessionCompat()
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        LogUtil.i(TAG, "onStartCommand")
        // return super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onLowMemory() {
        LogUtil.i(TAG, "onLowMemory")
        super.onLowMemory()
    }

    override fun onTrimMemory(level: Int) {
        LogUtil.i(TAG, "onTrimMemory.level = $level")
        super.onTrimMemory(level)
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.i(TAG, "onDestroy")
        releaseMediaSessionCompat()
        stopCasting()
    }

    fun stopCasting() {
        LogUtil.i(TAG, "stopCasting")
        webServerAndCast.stopWebServer()
        castContext?.apply {
            // stop casting
            LogUtil.d(TAG, "stopCasting.endCurrentSession")
            sessionManager.endCurrentSession(true)
        }
        isCastSessionAvailable = false
    }

    private fun initMediaSessionCompat() {
        LogUtil.i(TAG, "initMediaSessionCompat")
        // Create a MediaSessionCompat
        mediaSessionCompat = MediaSessionCompat(this, PlayerConstants.LOG_TAG)
        LogUtil.d(TAG, "initMediaSessionCompat.mediaSessionCompat = $mediaSessionCompat")
        mediaSessionCompat?.apply {
            setMediaButtonReceiver(null)
            setActive(true) // might need to find better place to put
        }
    }

    private fun releaseMediaSessionCompat() {
        LogUtil.i(TAG, "releaseMediaSessionCompat")
        mediaSessionCompat?.apply {
            isActive = false
            release()
            mediaSessionCompat = null
        }
        mediaControllerCompat = null
    }

    private fun convertUriToHttpUri(medUri: Uri): Uri {
        val msgStr = "convertUriToHttpUri"
        LogUtil.i(TAG, msgStr)
        val mediaFileName = medUri.path
        LogUtil.d(TAG, "${medUri}.mediaFileName = $mediaFileName")
        if (mediaFileName.isNullOrEmpty()) {
            return medUri
        }
        // starting local web server
        webServerAndCast.startWebServer(mediaFileName)
        // must after startWebServer
        val localMediaUrl = webServerAndCast.getMediaUrl()
        LogUtil.d(TAG, "${msgStr}.localMediaUrl = $localMediaUrl")
        if (localMediaUrl.isEmpty()) {
            webServerAndCast.stopWebServer()
            return medUri
        }
        return localMediaUrl.toUri()
    }

    private fun playSingleSong(presenter: PlayerBasePresenter,
                               songInfo: SongInfo?) {
        val msgStr = "playSingleSong"
        LogUtil.i(TAG, msgStr)
        if (songInfo == null) {
            return
        }
        var filePath = songInfo.filePath ?: return
        filePath = filePath.trim { it <= ' ' }
        LogUtil.d(TAG, "${msgStr}.filePath = $filePath")
        if (filePath == "") {
            // skip this song
            LogUtil.d(TAG, "${msgStr}.send PlaybackStateCompat.STATE_STOPPED ")
            setMediaPlaybackState(PlaybackStateCompat.STATE_STOPPED)
            return
        }
        try {
            val contentResolver: ContentResolver? = presenter.activity?.contentResolver
            contentResolver?.let {
                for (perm in it.persistedUriPermissions) {
                    if (perm.uri == filePath.toUri()) {
                        LogUtil.d(TAG, "${msgStr}.has URI permission")
                        break
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        var mediaUri = filePath.toUri()
        LogUtil.d(TAG, "${msgStr}. = $mediaUri")
        if (Uri.EMPTY == mediaUri) {
            return
        }
        //
        if (isCastSessionAvailable) {
            val tempMediaUri = convertUriToHttpUri(mediaUri)
            if (tempMediaUri == mediaUri) {
                // no change, then skip this song
                LogUtil.d(TAG, "${msgStr}.send PlaybackStateCompat.STATE_STOPPED ")
                setMediaPlaybackState(PlaybackStateCompat.STATE_STOPPED)
                return
            }
            mediaUri = tempMediaUri
        }
        //
        presenter.mediaUri = mediaUri
        presenter.setPlayingParameters(songInfo)
        val playingParam: PlayingParameters? = presenter.playingParam
        playingParam?.apply {
            currentAudioPosition = 0
            currentPlaybackState = PlayerConstants.PREPARE_MEDIA
            preparedStatus = 0
            val param = this.copy()
            playMediaFromUri(presenter.mediaUri, param)
        }
    }

    fun initMediaControllerCompat(presenter: PlayerBasePresenter) {
        // Create a MediaControllerCompat
        LogUtil.i(TAG, "initMediaControllerCompat")
        presenter.activity.let {
            LogUtil.d(TAG, "initMediaControllerCompat.activity not null")
            mediaSessionCompat?.apply {
                mediaControllerCompat = MediaControllerCompat(it, this)
                LogUtil.d(TAG,"initMediaControllerCompat.mediaControllerCompat = $mediaControllerCompat")
                MediaControllerCompat.setMediaController(it, mediaControllerCompat)
                initMediaCallback()
            }
        }
    }

    fun setMediaPlaybackState(state: Int) {
        LogUtil.i(TAG, "setMediaPlaybackState = $state")
        val playbackStateBuilder = PlaybackStateCompat.Builder()
        if (state == PlaybackStateCompat.STATE_PLAYING) {
            playbackStateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_PAUSE)
        } else {
            playbackStateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_PLAY)
        }
        LogUtil.d(TAG, "setMediaPlaybackState.orderedSongs.size = ${orderedSongs.size}")
        playbackStateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0f)
        LogUtil.d(TAG, "setMediaPlaybackState.mediaSessionCompat = $mediaSessionCompat")
        mediaSessionCompat?.setPlaybackState(playbackStateBuilder.build())
    }

    fun playMediaFromUri(mediaUri: Uri?, playingParam: PlayingParameters) {
        LogUtil.i(TAG, "playMediaFromUri.mediaUri = $mediaUri")
        mediaUri?.let { mediaIt ->
            mediaSessionCompat?.let {
                it.controller?.transportControls?.apply {
                    LogUtil.d(TAG, "playMediaFromUri.mediaTransportControls is not null")
                    val playingParamOriginExtras = Bundle()
                    playingParamOriginExtras.putParcelable(PlayerConstants.PlayingParamOrigin,
                        PlayingParameters(playingParam))
                    prepareFromUri(mediaIt, playingParamOriginExtras)
                }
            }
        }
    }

    fun startAutoPlay(presenter: PlayerBasePresenter, isSelfFinished: Boolean): Boolean {
        val playingParam = presenter.playingParam
        val orderedSongsSize = orderedSongs.size
        LogUtil.i(TAG, "startAutoPlay.orderedSongs = $orderedSongsSize")
        var stillPlayNext = true
        val repeatStatus = playingParam.repeatStatus
        val currentSongIndex = playingParam.currentSongIndex
        var nextSongIndex = currentSongIndex + 1 // preparing the next
        LogUtil.d(TAG, "startAutoPlay.nextSongIndex = $nextSongIndex")
        if (orderedSongsSize == 0) {
            stillPlayNext = false // no more songs
        } else {
            when (repeatStatus) {
                PlayerConstants.NoRepeatPlaying ->                     // no repeat
                    if ((nextSongIndex >= orderedSongsSize) || (nextSongIndex < 0)) {
                        stillPlayNext = false // no more songs
                    }
                PlayerConstants.RepeatOneSong -> {
                    // repeat one song
                    LogUtil.d(TAG, "startAutoPlay.RepeatOneSong")
                    if (isSelfFinished && (nextSongIndex > 0) && (nextSongIndex <= orderedSongsSize)) {
                        nextSongIndex--
                        LogUtil.d(TAG, "startAutoPlay.RepeatOneSong.nextSongIndex = $nextSongIndex")
                    }
                }
                PlayerConstants.RepeatAllSongs ->                     // repeat all songs
                    if (nextSongIndex >= orderedSongsSize) {
                        nextSongIndex = 0
                    }
            }
        }

        if (stillPlayNext) {
            // still play the next song
            playSingleSong(presenter, orderedSongs[nextSongIndex])
            playingParam.currentSongIndex = nextSongIndex // set nextSongIndex to currentSongIndex
            LogUtil.d(TAG, "startAutoPlay.stillPlayNext.setCurrentSongIndex() = $nextSongIndex")
        }

        return stillPlayNext
    }

    fun replayMedia(presenter: PlayerBasePresenter) {
        LogUtil.i(TAG, "replayMedia")
        val mediaUri = presenter.mediaUri
        val playingParam = presenter.playingParam
        // val numberOfAudioTracks = presenter.numberOfAudioTracks
        // if ((mediaUri == null) || (Uri.EMPTY == mediaUri) || (numberOfAudioTracks <= 0)) {
        if ((mediaUri == null) || (Uri.EMPTY == mediaUri)) {
            return
        }
        LogUtil.d(TAG, "replayMedia.playingParam.preparedStatus = " +
                "${playingParam.preparedStatus}")
        playingParam.currentAudioPosition = 0
        if (playingParam.preparedStatus != 0) {
            // song is playing, paused, or finished playing
            // cannot do the following statement (exoPlayer.setPlayWhenReady(false); )
            // because it will send Play.STATE_ENDED event after the playing has finished
            // but the playing was stopped in the middle of playing then won't send
            // Play.STATE_ENDED event
            // exoPlayer.setPlayWhenReady(false);
            LogUtil.d(TAG, "replayMedia.specificPlayerReplayMedia")
            specificPlayerReplayMedia(0)
        } else {
            LogUtil.d(TAG, "replayMedia.playMediaFromUri")
            // playingParam.currentPlaybackState = PlaybackStateCompat.STATE_NONE
            playingParam.currentPlaybackState = PlayerConstants.PREPARE_MEDIA
            playMediaFromUri(mediaUri, playingParam)
        }
    }

    fun startPlay(presenter: PlayerBasePresenter) {
        val mediaUri = presenter.mediaUri
        val playingParam = presenter.playingParam
        val playbackState = playingParam.currentPlaybackState
        LogUtil.i(TAG, "startPlay.mediaUri = $mediaUri")
        LogUtil.d(TAG, "startPlay.playbackState = $playbackState")
        if (mediaUri != null && Uri.EMPTY != mediaUri) {
            mediaSessionCompat?.controller?.transportControls?.let {
                LogUtil.d(TAG, "startPlay.mediaTransportControls.play()")
                it.play()
            }
        }
    }

    fun startPlayWithParam(presenter: PlayerBasePresenter,
                  param: PlayingParameters) {
        val msgStr = "startPlayWithParam"
        LogUtil.i(TAG, msgStr)
        val mediaUri = presenter.mediaUri
        val playbackState = param.currentPlaybackState
        LogUtil.d(TAG, "${msgStr}.mediaUri = $mediaUri")
        LogUtil.d(TAG, "${msgStr}.playbackState = $playbackState")
        if (mediaUri != null && Uri.EMPTY != mediaUri) {
            param.currentPlaybackState = PlayerConstants.PREPARE_MEDIA
            playMediaFromUri(mediaUri, param)
        }
    }

    fun pausePlay() {
        LogUtil.i(TAG, "pausePlay")
        mediaSessionCompat?.controller?.transportControls?.let {
            LogUtil.d(TAG, "pausePlay.mediaTransportControls.pause().")
            it.pause()
        }
    }

    fun stopPlay() {
        LogUtil.i(TAG, "stopPlay")
        mediaSessionCompat?.controller?.transportControls?.let {
            LogUtil.d(TAG, "stopPlay.mediaTransportControls.stop().")
            it.stop()
        }
    }
}