package com.smile.karaoke.services

import android.app.Service
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.smile.karaoke.constants.PlayerConstants
import com.smile.karaoke.models.MySingleTon.orderedSongs
import com.smile.karaoke.models.PlayingParameters
import com.smile.karaoke.models.SongInfo
import com.smile.karaoke.presenters.PlayerBasePresenter
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import com.google.android.gms.cast.framework.CastContext
import com.smile.karaoke.SmileAppBase
import com.smile.karaoke.googlecast.WebServerAndCast

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

    var mediaSessionCompat: MediaSessionCompat? = null
    var mediaControllerCompat: MediaControllerCompat? = null
    var isCastSessionAvailable = false
    val webServerAndCast = WebServerAndCast()
    protected var castContext: CastContext? = null
    // private lateinit var setupCast: SetupChromeCast

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        castContext = (application as SmileAppBase).castContext
        stopCasting()
        // setupCast = SetupChromeCast(this, castContext)
        initMediaSessionCompat()
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        // return super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onLowMemory() {
        Log.d(TAG, "onLowMemory")
        super.onLowMemory()
    }

    override fun onTrimMemory(level: Int) {
        Log.d(TAG, "onTrimMemory.level = $level")
        super.onTrimMemory(level)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        releaseMediaSessionCompat()
        stopCasting()
    }

    fun stopCasting() {
        Log.d(TAG, "stopCasting")
        webServerAndCast.stopWebServer()
        castContext?.apply {
            // stop casting
            Log.d(TAG, "stopCasting.endCurrentSession")
            sessionManager.endCurrentSession(true)
        }
        isCastSessionAvailable = false
    }

    private fun initMediaSessionCompat() {
        Log.d(TAG, "initMediaSessionCompat")
        // Create a MediaSessionCompat
        mediaSessionCompat = MediaSessionCompat(this, PlayerConstants.LOG_TAG)
        Log.d(TAG, "initMediaSessionCompat.mediaSessionCompat = $mediaSessionCompat")
        mediaSessionCompat?.apply {
            setMediaButtonReceiver(null)
            setActive(true) // might need to find better place to put
        }
    }

    private fun releaseMediaSessionCompat() {
        Log.d(TAG, "releaseMediaSessionCompat")
        mediaSessionCompat?.apply {
            isActive = false
            release()
            mediaSessionCompat = null
        }
        mediaControllerCompat = null
    }

    private fun convertUriToHttpUri(medUri: Uri): Uri {
        val msgStr = "convertUriToHttpUri"
        Log.d(TAG, msgStr)
        val mediaFileName = medUri.path
        Log.d(TAG, "${medUri}.mediaFileName = $mediaFileName")
        if (mediaFileName.isNullOrEmpty()) {
            return medUri
        }
        // starting local web server
        webServerAndCast.startWebServer(mediaFileName)
        // must after startWebServer
        val localMediaUrl = webServerAndCast.getMediaUrl()
        Log.d(TAG, "${msgStr}.localMediaUrl = $localMediaUrl")
        if (localMediaUrl.isEmpty()) {
            webServerAndCast.stopWebServer()
            return medUri
        }
        return localMediaUrl.toUri()
    }

    private fun playSingleSong(presenter: PlayerBasePresenter,
                               songInfo: SongInfo?) {
        val msgStr = "playSingleSong"
        Log.d(TAG, msgStr)
        if (songInfo == null) {
            return
        }
        var filePath = songInfo.filePath ?: return
        filePath = filePath.trim { it <= ' ' }
        Log.d(TAG, "${msgStr}.filePath = $filePath")
        if (filePath == "") {
            // skip this song
            Log.d(TAG, "${msgStr}.send PlaybackStateCompat.STATE_STOPPED ")
            setMediaPlaybackState(PlaybackStateCompat.STATE_STOPPED)
            return
        }
        try {
            val contentResolver: ContentResolver? = presenter.activity?.contentResolver
            contentResolver?.let {
                for (perm in it.persistedUriPermissions) {
                    if (perm.uri == filePath.toUri()) {
                        Log.d(TAG, "${msgStr}.has URI permission")
                        break
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        var mediaUri = filePath.toUri()
        Log.d(TAG, "${msgStr}. = $mediaUri")
        if (Uri.EMPTY == mediaUri) {
            return
        }
        //
        if (isCastSessionAvailable) {
            val tempMediaUri = convertUriToHttpUri(mediaUri)
            if (tempMediaUri == mediaUri) {
                // no change, then skip this song
                Log.d(TAG, "${msgStr}.send PlaybackStateCompat.STATE_STOPPED ")
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
        Log.d(TAG, "initMediaControllerCompat")
        presenter.activity.let {
            Log.d(TAG, "initMediaControllerCompat.activity not null")
            mediaSessionCompat?.apply {
                mediaControllerCompat = MediaControllerCompat(it, this)
                Log.d(TAG,"initMediaControllerCompat.mediaControllerCompat = $mediaControllerCompat")
                MediaControllerCompat.setMediaController(it, mediaControllerCompat)
                initMediaCallback()
            }
        }
    }

    fun setMediaPlaybackState(state: Int) {
        Log.d(TAG, "setMediaPlaybackState = $state")
        val playbackStateBuilder = PlaybackStateCompat.Builder()
        if (state == PlaybackStateCompat.STATE_PLAYING) {
            playbackStateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_PAUSE)
        } else {
            playbackStateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_PLAY)
        }
        Log.d(TAG, "setMediaPlaybackState.orderedSongs.size = ${orderedSongs.size}")
        playbackStateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0f)
        Log.d(TAG, "setMediaPlaybackState.mediaSessionCompat = $mediaSessionCompat")
        mediaSessionCompat?.setPlaybackState(playbackStateBuilder.build())
    }

    fun playMediaFromUri(mediaUri: Uri?, playingParam: PlayingParameters) {
        Log.d(TAG, "playMediaFromUri.mediaUri = $mediaUri")
        mediaUri?.let { mediaIt ->
            mediaSessionCompat?.let {
                it.controller?.transportControls?.apply {
                    Log.d(TAG, "playMediaFromUri.mediaTransportControls is not null")
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
        Log.d(TAG, "startAutoPlay.orderedSongs = $orderedSongsSize")
        var stillPlayNext = true
        val repeatStatus = playingParam.repeatStatus
        val currentSongIndex = playingParam.currentSongIndex
        var nextSongIndex = currentSongIndex + 1 // preparing the next
        Log.d(TAG, "startAutoPlay.nextSongIndex = $nextSongIndex")
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
                    Log.d(TAG, "startAutoPlay.RepeatOneSong")
                    if (isSelfFinished && (nextSongIndex > 0) && (nextSongIndex <= orderedSongsSize)) {
                        nextSongIndex--
                        Log.d(TAG, "startAutoPlay.RepeatOneSong.nextSongIndex = $nextSongIndex")
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
            Log.d(TAG, "startAutoPlay.stillPlayNext.setCurrentSongIndex() = $nextSongIndex")
        }

        return stillPlayNext
    }

    fun replayMedia(presenter: PlayerBasePresenter) {
        Log.d(TAG, "replayMedia")
        val mediaUri = presenter.mediaUri
        val playingParam = presenter.playingParam
        // val numberOfAudioTracks = presenter.numberOfAudioTracks
        // if ((mediaUri == null) || (Uri.EMPTY == mediaUri) || (numberOfAudioTracks <= 0)) {
        if ((mediaUri == null) || (Uri.EMPTY == mediaUri)) {
            return
        }
        Log.d(TAG, "replayMedia.playingParam.preparedStatus = " +
                "${playingParam.preparedStatus}")
        playingParam.currentAudioPosition = 0
        if (playingParam.preparedStatus != 0) {
            // song is playing, paused, or finished playing
            // cannot do the following statement (exoPlayer.setPlayWhenReady(false); )
            // because it will send Play.STATE_ENDED event after the playing has finished
            // but the playing was stopped in the middle of playing then won't send
            // Play.STATE_ENDED event
            // exoPlayer.setPlayWhenReady(false);
            Log.d(TAG, "replayMedia.specificPlayerReplayMedia")
            specificPlayerReplayMedia(0)
        } else {
            Log.d(TAG, "replayMedia.playMediaFromUri")
            // playingParam.currentPlaybackState = PlaybackStateCompat.STATE_NONE
            playingParam.currentPlaybackState = PlayerConstants.PREPARE_MEDIA
            playMediaFromUri(mediaUri, playingParam)
        }
    }

    fun startPlay(presenter: PlayerBasePresenter) {
        val mediaUri = presenter.mediaUri
        val playingParam = presenter.playingParam
        val playbackState = playingParam.currentPlaybackState
        Log.d(TAG, "startPlay.mediaUri = $mediaUri")
        Log.d(TAG, "startPlay.playbackState = $playbackState")
        if (mediaUri != null && Uri.EMPTY != mediaUri) {
            mediaSessionCompat?.controller?.transportControls?.let {
                Log.d(TAG, "startPlay.mediaTransportControls.play()")
                it.play()
            }
        }
    }

    fun startPlayWithParam(presenter: PlayerBasePresenter,
                  param: PlayingParameters) {
        val msgStr = "startPlayWithParam"
        Log.d(TAG, msgStr)
        val mediaUri = presenter.mediaUri
        val playbackState = param.currentPlaybackState
        Log.d(TAG, "${msgStr}.mediaUri = $mediaUri")
        Log.d(TAG, "${msgStr}.playbackState = $playbackState")
        if (mediaUri != null && Uri.EMPTY != mediaUri) {
            param.currentPlaybackState = PlayerConstants.PREPARE_MEDIA
            playMediaFromUri(mediaUri, param)
        }
    }

    fun pausePlay() {
        Log.d(TAG, "pausePlay")
        mediaSessionCompat?.controller?.transportControls?.let {
            Log.d(TAG, "pausePlay.mediaTransportControls.pause().")
            it.pause()
        }
    }

    fun stopPlay() {
        Log.d(TAG, "stopPlay")
        mediaSessionCompat?.controller?.transportControls?.let {
            Log.d(TAG, "stopPlay.mediaTransportControls.stop().")
            it.stop()
        }
    }
}