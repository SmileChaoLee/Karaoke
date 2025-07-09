package com.smile.karaokeplayer.vlcplayer.services

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.media3.common.util.UnstableApi
import com.smile.karaokeplayer.constants.CommonConstants
import com.smile.karaokeplayer.constants.PlayerConstants
import com.smile.karaokeplayer.services.BasePlayService
import com.smile.karaokeplayer.vlcplayer.Callbacks.VlcMediaControllerCallback
import com.smile.karaokeplayer.vlcplayer.Callbacks.VlcMediaSessionCallback
import com.smile.karaokeplayer.vlcplayer.Listeners.VlcPlayerListener
import com.smile.karaokeplayer.vlcplayer.Presenters.VlcPlayerPresenter
import com.smile.smilelibraries.utilities.ScreenUtil
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.interfaces.IMedia
import org.videolan.libvlc.util.VLCVideoLayout

@UnstableApi
class VlcPlayService : BasePlayService() {

    companion object {
        private const val TAG = "VlcPlayService"
    }

    var presenter : VlcPlayerPresenter? = null
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
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        detachPlayerViews()
        releaseVlcPlayer()
        mediaControllerCompat?.apply {
            controllerCallback?.let {
                unregisterCallback(it)
            }
        }
        mediaSessionCallback = null
    }

    fun initVlcPlayer() {
        Log.d(TAG, "initVlcPlayer.presenter = $presenter")
        presenter?.let {
            libVLC = LibVLC(it.activity)
            vlcPlayer = MediaPlayer(libVLC)
            vlcPlayer?.apply {
                setEventListener(VlcPlayerListener(this@VlcPlayService))
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

    fun attachPlayerViews(videoVLCPlayerView: VLCVideoLayout) {
        Log.d(TAG,"attachPlayerViews.vlcPlayer = $vlcPlayer")
        vlcPlayer?.apply {
            val isAttached = vlcVout.areViewsAttached()
            Log.d(TAG,"attachPlayerViews.areViewsAttached = $isAttached")
            if (!isAttached) {
                attachViews(videoVLCPlayerView, null, true, false)
            }
            videoVLCPlayerView.requestFocus()
        }
    }

    fun detachPlayerViews() {
        Log.d(TAG,"detachPlayerViews.vlcPlayer = $vlcPlayer")
        vlcPlayer?.apply {
            Log.d(TAG,"detachPlayerViews.areViewsAttached = ${vlcVout.areViewsAttached()}")
            if (vlcVout.areViewsAttached()) {
                detachViews()
            }
        }
    }

    fun setVideoWindowSize(videoVLCPlayerView: VLCVideoLayout) {
        val msgStr = "setVideoWindowSize"
        Log.d(TAG,msgStr)
        presenter?.let {
            attachPlayerViews(videoVLCPlayerView)   // must be the first statement
            Log.d(TAG,"${msgStr}.vlcPlayer = $vlcPlayer")
            Log.d(TAG,"${msgStr}.vlcPlayer.scale = ${vlcPlayer?.scale}")
            vlcPlayer?.scale = 0f
            Log.d(TAG,"${msgStr}.activity = ${it.activity}")
            it.activity?.let { actIt ->
                Log.d(TAG,"setVideoWindowSize.aspectRatio = ${vlcPlayer?.aspectRatio}")
                val screenSize = ScreenUtil.getScreenSize(actIt)
                Log.d(TAG,"setVideoWindowSize.screenSize = ${screenSize.x}, ${screenSize.y}")
                vlcPlayer?.vlcVout?.setWindowSize(screenSize.x, screenSize.y)
                if (actIt.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    Log.d(TAG, "setVideoWindowSize.ORIENTATION_LANDSCAPE")
                    vlcPlayer?.aspectRatio = "16:9"
                } else {
                    Log.d(TAG, "setVideoWindowSize.ORIENTATION_PORTRAIT")
                    vlcPlayer?.aspectRatio = "4:3"
                }
                Log.d(TAG,"setVideoWindowSize.aspectRatio = ${vlcPlayer?.aspectRatio}")
            }
        }
    }

    fun prepare(med: IMedia) {
        Log.d(TAG, "prepare.vlcPlayer = $vlcPlayer")
        vlcPlayer?.media = med
    }

    fun createMedia(uri: Uri): IMedia {
        return Media(libVLC, uri)
    }

    fun getAudioTrack(): Int {
        Log.d(TAG, "getAudioTrack")
        val tracks = vlcPlayer?.getTracks(IMedia.Track.Type.Audio)
        Log.d(TAG, "getAudioTrack.tracks")
        tracks?.also {
            Log.d(TAG, "getAudioTrack.tracks.size = ${it.size}")
            for (trackIndex in 0 until it.size) {
                if (it[trackIndex].selected) {
                    Log.d(TAG, "getAudioTrack.return trackIndex = $trackIndex")
                    return trackIndex
                }
            }
        }
        return -1
    }

    fun setAudioTrack(audioTrackId: Int) {
        Log.d(TAG, "setAudioTrack")
        val selectedTracks = vlcPlayer?.getTracks(IMedia.Track.Type.Audio)
        selectedTracks?.also {
            if (audioTrackId >= 0 && audioTrackId < it.size) {
                val track = it[audioTrackId]
                Log.d(TAG, "setAudioTrack.track = $track")
                vlcPlayer?.selectTrack(track.id)
            }
        }

    }

    fun getPlayingMediaInfo(audioTrackIndicesList: ArrayList<Int>):Int {
        val msgStr = "getPlayingMediaInfo"
        Log.d(TAG, msgStr)
        if (vlcPlayer == null) {
            Log.d(TAG, "${msgStr}.vlcPlayer is null")
        }
        val vPlayer = vlcPlayer!!
        var numOfVideoTracks = 0
        val videoDis = vPlayer.getTracks(IMedia.Track.Type.Video)
        var videoTrackId: String?
        var videoTrackName: String?
        videoDis?.also {
            // because it is null sometimes
            for (videoDi in it) {
                videoTrackId = videoDi.id
                Log.d(TAG, "${msgStr}.videoTrackId = $videoTrackId")
                videoTrackName = videoDi.name
                Log.d(TAG, "${msgStr}.videoTrackName = $videoTrackName")
                numOfVideoTracks++
            }
        }
        Log.d(TAG, "${msgStr}.numOfVideoTracks = " + numOfVideoTracks)

        var audioTrackId: String?
        var audioTrackName: String?
        audioTrackIndicesList.clear()
        val audioDis = vPlayer.getTracks(IMedia.Track.Type.Audio)
        audioDis?.also {
            // because it is null sometimes
            for (tackIndex in 0 until it.size) {
                val audioTrack: IMedia.AudioTrack = it[tackIndex] as IMedia.AudioTrack
                // info only
                val channels = audioTrack.channels
                Log.d(TAG, "${msgStr}.channels = $channels")
                //
                audioTrackId = audioTrack.id
                Log.d(TAG, "${msgStr}.audioTrackId = $audioTrackId")
                audioTrackName = audioTrack.name
                Log.d(TAG, "${msgStr}.audioTrackName = $audioTrackName")
                // exclude disabled
                audioTrackIndicesList.add(tackIndex)
            }
        }

        return numOfVideoTracks
    }

    override fun onPlay() {
        Log.d(TAG, "onPlay.vlcPlayer = $vlcPlayer")
        vlcPlayer?.play()
    }

    override fun onPause() {
        Log.d(TAG, "onPause.vlcPlayer = $vlcPlayer")
        vlcPlayer?.pause()
    }

    override fun onStop() {
        Log.d(TAG, "onStop.vlcPlayer = $vlcPlayer")
        vlcPlayer?.stop()
    }

    override fun initMediaCallback() {
        Log.d(TAG, "initMediaCallback")
        mediaSessionCallback = VlcMediaSessionCallback(this@VlcPlayService)
        mediaSessionCompat?.setCallback(mediaSessionCallback)
        controllerCallback = VlcMediaControllerCallback(this@VlcPlayService)
        mediaControllerCompat?.registerCallback(controllerCallback!!)
    }

    override fun isPlaying(): Boolean {
        val isPlaying = vlcPlayer?.isPlaying ?: false
        Log.d(TAG, "isPlaying.isPlaying = $isPlaying")
        return isPlaying
    }

    override fun setPlayerTime(progress: Long) {
        Log.d(TAG, "setPlayerTime.progress = $progress")
        vlcPlayer?.time = progress
        Log.d(TAG, "setPlayerTime.time = ${vlcPlayer?.time}")
    }

    override fun isSeekable(): Boolean {
        val isSeekable = vlcPlayer?.isSeekable ?: false
        Log.d(TAG, "isSeekable.isSeekable = $isSeekable")
        return isSeekable
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
                CommonConstants.LEFT_CHANNEL -> rightVolume = 0f
                CommonConstants.RIGHT_CHANNEL -> leftVolume = 0f
                CommonConstants.STEREO -> leftVolume = rightVolume
            }
            it.currentVolume = volumeTmp
            vlcPlayer?.volume = (volumeTmp * PlayerConstants.MAX_PROGRESS).toInt()
            return
        }
        Log.d(TAG, "setAudioVolume.presenter?.playingParam is null")
    }

    override fun getMediaDuration(): Long {
        val len = vlcPlayer?.length ?: 0
        Log.d(TAG, "getMediaDuration.len")
        return len
    }

    override fun getCurrentPosition(): Long {
        val time = vlcPlayer?.time ?: 0
        Log.d(TAG, "getCurrentPosition.time")
        return time
    }

    override fun getPlaybackState(): Int {
        val state = vlcPlayer?.playerState ?: PlayerConstants.PREPARE_MEDIA
        Log.d(TAG, "getPlaybackState.state")
        return state
    }

    override fun specificPlayerReplayMedia(currentAudioPosition: Long) {
        // song is playing, paused, or finished playing
        // switchAudioToVocal() // implement after VlcPlayer can be run
        vlcPlayer?.apply {
            time = currentAudioPosition // use time to set position
            if (!isPlaying) {
                play()
            }
        }
    }
}