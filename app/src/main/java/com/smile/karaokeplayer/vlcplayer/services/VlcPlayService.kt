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
        Log.d(TAG,"setVideoWindowSize")
        presenter?.let {
            vlcPlayer?.scale = 0f
            it.activity.let { actIt ->
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
            attachPlayerViews(videoVLCPlayerView)
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
        return vlcPlayer?.audioTrack ?: -1
    }

    fun setAudioTrack(audioTrackId: Int) {
        // vlcPlayer?.setAudioTrack(audioTrackId)
        vlcPlayer?.audioTrack = audioTrackId
    }

    fun getPlayingMediaInfo(audioTrackIndicesList: ArrayList<Int>):Int {
        val msgStr = "getPlayingMediaInfo"
        Log.d(TAG, msgStr)
        if (vlcPlayer == null) {
            Log.d(TAG, "${msgStr}.vlcPlayer is null")
        }
        val vPlayer = vlcPlayer!!
        var numOfVideoTracks = 0
        val videoDis = vPlayer.videoTracks
        var videoTrackId: Int
        var videoTrackName: String?
        if (videoDis != null) {
            // because it is null sometimes
            for (videoDi in videoDis) {
                videoTrackId = videoDi.id
                videoTrackName = videoDi.name
                Log.d(TAG, "${msgStr}.videoTrackName = $videoTrackName")
                // exclude disabled
                if (videoTrackId >= 0) {
                    // enabled video track
                    numOfVideoTracks++
                }
            }
        }
        Log.d(TAG, "${msgStr}.numOfVideoTracks = " + numOfVideoTracks)

        var audioTrackId: Int
        var audioTrackName: String?
        audioTrackIndicesList.clear()
        val audioDis = vPlayer.audioTracks
        if (audioDis != null) {
            // because it is null sometimes
            for (audioDi in audioDis) {
                audioTrackId = audioDi.id
                audioTrackName = audioDi.name
                Log.d(TAG, "${msgStr}.audioTrackName = $audioTrackName")
                // exclude disabled
                if (audioTrackId >= 0) {
                    // enabled audio track
                    audioTrackIndicesList.add(audioTrackId)
                }
            }
        }

        /*
        // the following is just for the info
        val media = vPlayer.getMedia() // for version above 3.3.0
        media?.tracks?.let { mt ->
            val trackCount = mt.size
            Log.d(TAG, "${msgStr}.trackCount = " + trackCount)
            for (i in 0..<trackCount) {
                mt[i]?.let { track ->
                    Log.d(TAG, "${msgStr}.track.id = " + track.id)
                    if (track.type == IMedia.Track.Type.Audio) {
                        // audio
                        val audioTrack = track as IMedia.AudioTrack
                        Log.d(TAG,"${msgStr}.audioTrack.channels = "
                                + audioTrack.channels)
                    } else if (track.type == IMedia.Track.Type.Video) {
                        // video
                        val videoTrack = track as VideoTrack
                        val height = videoTrack.height
                        Log.d(TAG, "${msgStr}.videoTrack.height = " + height)
                        val width = videoTrack.width
                        Log.d(TAG, "${msgStr}.videoTrack.width = " + width)
                    }
                }
            }
        }
        */

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