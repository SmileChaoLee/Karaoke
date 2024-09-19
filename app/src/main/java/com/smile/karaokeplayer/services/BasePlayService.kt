package com.smile.karaokeplayer.services

import android.app.Service
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.smile.karaokeplayer.constants.PlayerConstants
import com.smile.karaokeplayer.models.MySingleTon.orderedSongs
import com.smile.karaokeplayer.models.PlayingParameters
import com.smile.karaokeplayer.models.SongInfo
import com.smile.karaokeplayer.presenters.BasePlayerPresenter

abstract class BasePlayService : Service() {

    companion object {
        private const val TAG = "BasePlayService"
    }

    abstract fun initMediaCallback()
    abstract fun setPlayerTime(progress: Long)
    abstract fun isSeekable(): Boolean
    abstract fun setPlayerAudioVolume(volumeTmp: Float)
    abstract fun getMediaDuration(): Long
    abstract fun specificPlayerReplayMedia(currentAudioPosition: Long)
    // abstract fun setAudioVolumeInsideVolumeSeekBar(i: Int)
    // abstract fun getCurrentProgressForVolumeSeekBar(): Int

    var mediaSessionCompat: MediaSessionCompat? = null
    var mediaControllerCompat: MediaControllerCompat? = null

    override fun onCreate() {
        Log.d(TAG, "onCreate")
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
        Log.d(TAG, "onTrimMemory")
        super.onTrimMemory(level)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        releaseMediaSessionCompat()
        super.onDestroy()
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

    private fun playSingleSong(presenter: BasePlayerPresenter, songInfo: SongInfo?) {
        Log.d(TAG, "playSingleSong")
        if (songInfo == null) {
            return
        }
        var filePath = songInfo.filePath ?: return
        filePath = filePath.trim { it <= ' ' }
        if (filePath == "") {
            return
        }
        Log.d(TAG, "playSingleSong.filePath = $filePath")
        try {
            val contentResolver: ContentResolver? = presenter.activity?.contentResolver
            contentResolver?.let {
                for (perm in it.persistedUriPermissions) {
                    if (perm.uri == Uri.parse(filePath)) {
                        Log.d(TAG, "playSingleSong.has URI permission")
                        break
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        val mUri = Uri.parse(filePath)
        presenter.mediaUri = mUri
        Log.d(TAG, "playSingleSong.mediaUri = $mUri")
        if ((mUri == null) || (Uri.EMPTY == mUri)) {
            return
        }
        presenter.setPlayingParameters(songInfo)
        val playingParam: PlayingParameters? = presenter.playingParam
        playingParam?.apply {
            currentAudioPosition = 0
            currentPlaybackState = PlaybackStateCompat.STATE_NONE
            isMediaPrepared = false
            playMediaFromUri(mUri, this)
        }
    }

    fun initMediaControllerCompat(presenter: BasePlayerPresenter) {
        // Create a MediaControllerCompat
        Log.d(TAG, "initMediaControllerCompat")
        presenter.activity?.let {
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
            val playingParamOriginExtras = Bundle()
            playingParamOriginExtras.putParcelable(PlayerConstants.PlayingParamOrigin, playingParam)
            mediaSessionCompat?.let {
                it.controller?.transportControls?.apply {
                    Log.d(TAG, "playMediaFromUri.mediaTransportControls is not null")
                    prepareFromUri(mediaIt, playingParamOriginExtras)
                }
            }
        }
    }

    fun startAutoPlay(presenter: BasePlayerPresenter, isSelfFinished: Boolean): Boolean {
        val playingParam: PlayingParameters? = presenter.playingParam
        val orderedSongsSize = orderedSongs.size
        Log.d(TAG, "startAutoPlay.orderedSongs = $orderedSongsSize")
        var stillPlayNext = true
        val repeatStatus = playingParam?.repeatStatus
        val currentSongIndex = playingParam?.currentSongIndex
        var nextSongIndex = currentSongIndex!! + 1 // preparing the next
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

    fun replayMedia(presenter: BasePlayerPresenter) {
        Log.d(TAG, "replayMedia")
        val mediaUri = presenter.mediaUri
        val playingParam = presenter.playingParam
        val numberOfAudioTracks = presenter.numberOfAudioTracks
        if ((mediaUri == null) || (Uri.EMPTY == mediaUri) || (numberOfAudioTracks <= 0)) {
            return
        }
        if (playingParam.isMediaPrepared) {
            // song is playing, paused, or finished playing
            // cannot do the following statement (exoPlayer.setPlayWhenReady(false); )
            // because it will send Play.STATE_ENDED event after the playing has finished
            // but the playing was stopped in the middle of playing then won't send
            // Play.STATE_ENDED event
            // exoPlayer.setPlayWhenReady(false);
            val currentAudioPosition: Long = 0
            playingParam.currentAudioPosition = currentAudioPosition
            Log.d(TAG, "replayMedia.specificPlayerReplayMedia(currentAudioPosition)")
            specificPlayerReplayMedia(currentAudioPosition)
        } else {
            Log.d(TAG, "replayMedia.playMediaFromUri()")
            // song was stopped by user
            playingParam.currentPlaybackState = PlaybackStateCompat.STATE_NONE
            playMediaFromUri(mediaUri, playingParam)
        }
    }

    fun startPlay(presenter: BasePlayerPresenter) {
        val mediaUri = presenter.mediaUri
        val playingParam = presenter.playingParam
        val playbackState = playingParam.currentPlaybackState
        if ((mediaUri != null && Uri.EMPTY != mediaUri)
            && (playbackState != PlaybackStateCompat.STATE_PLAYING)) {
            // no media file opened or playing has been stopped
            if ((playbackState == PlaybackStateCompat.STATE_PAUSED)
                || (playbackState == PlaybackStateCompat.STATE_REWINDING)
                || (playbackState == PlaybackStateCompat.STATE_FAST_FORWARDING)
                || (playbackState == PlaybackStateCompat.STATE_BUFFERING)) {
                mediaSessionCompat?.controller?.transportControls?.let {
                    Log.d(TAG, "startPlay.mediaTransportControls.play()")
                    it.play()
                }
            }
        } else {
            // (playbackState == PlaybackStateCompat.STATE_STOPPED) or
            // (playbackState == PlaybackStateCompat.STATE_NONE)
            Log.d(TAG, "startPlay.replayMedia()")
            replayMedia(presenter)
        }
    }

    fun pausePlay(presenter: BasePlayerPresenter) {
        Log.d(TAG, "pausePlay()")
        val mediaUri = presenter.mediaUri
        val playingParam = presenter.playingParam
        if ((mediaUri != null && Uri.EMPTY != mediaUri)
            && (playingParam.currentPlaybackState != PlaybackStateCompat.STATE_PAUSED)) {
            mediaSessionCompat?.controller?.transportControls?.let {
                Log.d(TAG, "pausePlay().mediaTransportControls.pause().")
                it.pause()
            }
        }
    }

    fun stopPlay(presenter: BasePlayerPresenter) {
        Log.d(TAG, "stopPlay()")
        val mediaUri = presenter.mediaUri
        val playingParam = presenter.playingParam
        if ((mediaUri != null && Uri.EMPTY != mediaUri)
            && (playingParam.currentPlaybackState != PlaybackStateCompat.STATE_NONE)) {
            mediaSessionCompat?.controller?.transportControls?.let {
                Log.d(TAG, "stopPlay().mediaTransportControls.stop().")
                it.stop()
            }
        }
    }
}