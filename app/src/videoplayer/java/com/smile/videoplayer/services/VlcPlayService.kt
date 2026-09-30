package com.smile.videoplayer.services

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media3.common.util.UnstableApi
import com.smile.karaoke.callbacks.MediaControllerCallback
import com.smile.karaoke.constants.CommonConstants
import com.smile.karaoke.constants.MyPlayerConstants
import com.smile.karaoke.services.BasePlayService
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.interfaces.IMedia
import org.videolan.libvlc.util.VLCVideoLayout
import com.smile.videoplayer.callbacks.VlcMediaSessionCallback
import com.smile.videoplayer.native.VlcNativeBridge
import com.smile.videoplayer.listeners.VlcPlayerListener
import com.smile.videoplayer.presenters.VlcPlayerPresenter

@UnstableApi
class VlcPlayService : BasePlayService() {

    companion object {
        private const val TAG = "VlcPlayService"
    }

    // private lateinit var audioManager: AudioManager
    // private var curAudioVolume by Delegates.notNull<Int>()
    private var mediaSessionCallback: VlcMediaSessionCallback? = null
    var presenter : VlcPlayerPresenter? = null
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
        LogUtil.d(TAG, "onCreate")
        /*
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        curAudioVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        restoreAudioVolume()
        LogUtil.d(TAG, "onCreate.curAudioVolume = $curAudioVolume")
        */
    }

    override fun onBind(intent: Intent?): IBinder {
        LogUtil.d(TAG, "onBind.binder = $binder")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        LogUtil.d(TAG, "onUnbind.intent = $intent")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.d(TAG, "onDestroy")
        // restore the original audio volume before starting this app
        // restoreAudioVolume()
        //
        detachPlayerViews()
        releaseVlcPlayer()
        mediaControllerCompat?.apply {
            controllerCallback?.let {
                unregisterCallback(it)
            }
        }
        mediaSessionCallback = null
    }

    /*
    fun restoreAudioVolume() {
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            curAudioVolume,
            AudioManager.FLAG_SHOW_UI)  // Shows the volume slider UI
    }
    */

    fun initVlcPlayer() {
        LogUtil.d(TAG, "initVlcPlayer.presenter = $presenter")
        VlcNativeBridge.loadVlcBridge()
        presenter?.let {
            val options = ArrayList<String>()
            // options.add("-vvv") // Enables detailed logcat debugging
            options.add("--extraintf=")
            // 1 = Stereo
            // 2 = Reverse Stereo
            // 3 = Left Channel
            // 4 = Right Channel
            // 5 = Dolby
            options.add("--stereo-mode=1")  // works for changing the audio mode
            libVLC = LibVLC(it.getActivity(), options)
            vlcPlayer = MediaPlayer(libVLC)
            vlcPlayer?.apply {
                audioDelay = 0
                setEventListener(VlcPlayerListener(this@VlcPlayService))
            }
        }
    }

    private fun releaseVlcPlayer() {
        LogUtil.d(TAG, "releaseVlcPlayer.vlcPlayer = $vlcPlayer")
        vlcPlayer?.apply {
            stop()
            media?.release()
            detachViews()
            release()
            vlcPlayer = null
        }
        libVLC?.release()
        libVLC = null
    }

    fun attachPlayerViews(videoVLCPlayerView: VLCVideoLayout) {
        LogUtil.d(TAG,"attachPlayerViews.vlcPlayer = $vlcPlayer")
        vlcPlayer?.apply {
            val isAttached = vlcVout.areViewsAttached()
            LogUtil.d(TAG,"attachPlayerViews.areViewsAttached = $isAttached")
            if (!isAttached) {
                attachViews(videoVLCPlayerView, null, true, false)
            }
            videoVLCPlayerView.post { videoVLCPlayerView.requestFocus() }
        }
    }

    fun detachPlayerViews() {
        LogUtil.d(TAG,"detachPlayerViews.vlcPlayer = $vlcPlayer")
        vlcPlayer?.apply {
            LogUtil.d(TAG,"detachPlayerViews.areViewsAttached = ${vlcVout.areViewsAttached()}")
            if (vlcVout.areViewsAttached()) {
                detachViews()
            }
        }
    }

    fun setVideoWindowSize(videoVLCPlayerView: VLCVideoLayout) {
        val msgStr = "setVideoWindowSize"
        LogUtil.d(TAG,msgStr)
        presenter?.let {
            attachPlayerViews(videoVLCPlayerView)   // must be the first statement
            it.getActivity()?.let { actIt ->
                vlcPlayer?.apply {
                    val screenSize = ScreenUtil.getScreenSize(actIt)
                    LogUtil.d(TAG,"${msgStr}.screenSize = ${screenSize.x}, ${screenSize.y}")
                    LogUtil.d(TAG,"${msgStr}.aspectRatio = $aspectRatio")
                    scale = 0f
                    vlcVout.setWindowSize(screenSize.x, screenSize.y)
                    var scaleType = MediaPlayer.ScaleType.SURFACE_ORIGINAL
                    var nRatio = "4:3"
                    if (actIt.resources.configuration.orientation
                        == Configuration.ORIENTATION_LANDSCAPE) {
                        scaleType = MediaPlayer.ScaleType.SURFACE_FILL
                        nRatio = "16:9"
                    }
                    videoScale = scaleType
                    aspectRatio = nRatio
                    LogUtil.d(TAG,"${msgStr}.aspectRatio = $aspectRatio")
                }
            }
        }
    }

    fun prepare(med: IMedia) {
        LogUtil.d(TAG, "prepare.vlcPlayer = $vlcPlayer")
        vlcPlayer?.media = med
    }

    fun createMedia(uri: Uri): IMedia {
        LogUtil.d(TAG, "createMedia")
        val media = Media(libVLC, uri)
        // media.addOption(":stereo-mode=4") // does not work
        media.addOption(":audio-filter=remap")
        media.addOption(":remap-channel-left=1")  // Send Input Left to Output Left
        media.addOption(":remap-channel-right=1") // Send Input Left to Output Right (Mono Left Mode)
        return media
    }

    fun getAudioTrack(): Int {
        LogUtil.d(TAG, "getAudioTrack")
        /*
        val tracks = vlcPlayer?.getTracks(IMedia.Track.Type.Audio)
        LogUtil.d(TAG, "getAudioTrack.tracks.size = ${tracks?.size}")
        tracks?.also {
            for (trackIndex in 0 until it.size) {
                if (it[trackIndex].selected) {
                    LogUtil.d(TAG, "getAudioTrack.return trackIndex = $trackIndex")
                    return trackIndex
                }
            }
        }
        return -1
        */
        return vlcPlayer?.audioTrack ?: -1
    }

    fun setAudioTrack(audioTrackId: Int) {
        LogUtil.d(TAG, "setAudioTrack")
        /*
        val selectedTracks = vlcPlayer?.getTracks(IMedia.Track.Type.Audio)
        selectedTracks?.also {
            if (audioTrackId >= 0 && audioTrackId < it.size) {
                val track = it[audioTrackId]
                LogUtil.d(TAG, "setAudioTrack.track = $track")
                vlcPlayer?.selectTrack(track.id)
            }
        }
        */
        vlcPlayer?.apply {
            audioDelay = 0
            audioTrack = audioTrackId
        }
    }

    fun setAudioChannel(channel: Int) {
        LogUtil.d(TAG, "setAudioChannel.channel = $channel")
        vlcPlayer?.let { player ->
            player.audioDelay = 0
            // This instructs the native C++ audio sink to re-route structural layouts instantly
            // Mode 1: Plays both tracks cleanly (Stereo)
            // Mode 3: Routes Left stream data to both left and right ears
            // Mode 4: Routes Right stream data to both left and right ears
            val ch = when (channel) {
                CommonConstants.LEFT_CHANNEL -> VlcNativeBridge.LEFT_CHANNEL
                CommonConstants.RIGHT_CHANNEL -> VlcNativeBridge.RIGHT_CHANNEL
                else -> VlcNativeBridge.STEREO
            }

            val result = VlcNativeBridge.setNativeAudioChannel(player.instance, ch)
            LogUtil.d(TAG, "setAudioChannel.channel = $channel, result = $result")
        }
    }

    fun getPlayingMediaInfo(audioTrackIndicesList: ArrayList<Int>):Int {
        val msgStr = "getPlayingMediaInfo"
        LogUtil.d(TAG, msgStr)
        if (vlcPlayer == null) {
            LogUtil.d(TAG, "${msgStr}.vlcPlayer is null")
            return 0
        }
        val vPlayer = vlcPlayer!!

        var numOfVideoTracks = 0
        val videoDis = vPlayer.videoTracks
        // val videoDis = vPlayer.getTracks(IMedia.Track.Type.Video)
        var videoTrackId: Int?
        var videoTrackName: String?

        videoDis?.also {
            // because it is null sometimes
            for (videoDi in it) {
                videoTrackId = videoDi.id
                LogUtil.d(TAG, "${msgStr}.videoTrackId = $videoTrackId")
                videoTrackName = videoDi.name
                LogUtil.d(TAG, "${msgStr}.videoTrackName = $videoTrackName")
                numOfVideoTracks++
            }
        }
        LogUtil.d(TAG, "${msgStr}.numOfVideoTracks = " + numOfVideoTracks)

        var audioTrackId: Int?
        var audioTrackName: String?
        audioTrackIndicesList.clear()
        // val audioDis = vPlayer.getTracks(IMedia.Track.Type.Audio)
        val audioDis = vPlayer.audioTracks
        audioDis?.also {
            // because it is null sometimes
            for (audioDi in audioDis) {
                audioTrackId = audioDi.id
                audioTrackName = audioDi.name
                LogUtil.d(TAG, "${msgStr}.audioDis[i].id = $audioTrackId")
                LogUtil.d(TAG, "${msgStr}.audioDis[i].name = $audioTrackName")
                // exclude disabled
                if (audioTrackId >= 0) {
                    // enabled audio track
                    audioTrackIndicesList.add(audioTrackId)
                }
            }

            /*
            for (tackIndex in 0 until it.size) {
                val audioTrack: IMedia.AudioTrack = it[tackIndex] as IMedia.AudioTrack
                // info only
                val channels = audioTrack.channels
                LogUtil.d(TAG, "${msgStr}.channels = $channels")
                //
                audioTrackId = audioTrack.id
                LogUtil.d(TAG, "${msgStr}.audioTrackId = $audioTrackId")
                audioTrackName = audioTrack.name
                LogUtil.d(TAG, "${msgStr}.audioTrackName = $audioTrackName")
                // exclude disabled
                audioTrackIndicesList.add(tackIndex)
            }
            */
        }

        return numOfVideoTracks
    }

    override fun onPlay() {
        LogUtil.d(TAG, "onPlay.vlcPlayer = $vlcPlayer")
        vlcPlayer?.play()
    }

    override fun onPause() {
        LogUtil.d(TAG, "onPause.vlcPlayer = $vlcPlayer")
        vlcPlayer?.pause()
    }

    override fun onStop() {
        LogUtil.d(TAG, "onStop.vlcPlayer = $vlcPlayer")
        val playbackState = presenter?.playingParam?.currentPlaybackState
        LogUtil.d(TAG, "onStop.playbackState = $playbackState")
        if (playbackState == PlaybackStateCompat.STATE_PLAYING ||
            playbackState == PlaybackStateCompat.STATE_PAUSED) {
            LogUtil.d(TAG, "onStop.vlcPlayer?.stop()")
            vlcPlayer?.stop()
        }
    }

    override fun initMediaCallback() {
        LogUtil.d(TAG, "initMediaCallback")
        presenter?.let {
            mediaSessionCallback = VlcMediaSessionCallback(this@VlcPlayService)
            mediaSessionCompat?.setCallback(mediaSessionCallback)
            controllerCallback = MediaControllerCallback(it)
            mediaControllerCompat?.registerCallback(controllerCallback!!)
        }
    }

    override fun isPlaying(): Boolean {
        val isPlaying = vlcPlayer?.isPlaying ?: false
        LogUtil.d(TAG, "isPlaying.isPlaying = $isPlaying")
        return isPlaying
    }

    override fun setPlayerTime(progress: Long) {
        LogUtil.d(TAG, "setPlayerTime.progress = $progress")
        vlcPlayer?.time = progress
        LogUtil.d(TAG, "setPlayerTime.time = ${vlcPlayer?.time}")
    }

    override fun isSeekable(): Boolean {
        val isSeekable = vlcPlayer?.isSeekable ?: false
        LogUtil.d(TAG, "isSeekable.isSeekable = $isSeekable")
        return isSeekable
    }

    override fun setAudioVolume(volumeTmp: Float) {
        val logStr = "setAudioVolume"
        LogUtil.d(TAG, "$logStr.volumeTmp = $volumeTmp")
        presenter?.playingParam?.let {
            LogUtil.d(TAG, "$logStr..presenter?.playingParam is not null")
            it.currentVolume = volumeTmp
            vlcPlayer?.volume = (volumeTmp * MyPlayerConstants.MAX_PROGRESS).toInt()
            return
        }
        LogUtil.d(TAG, "$logStr..presenter?.playingParam is null")
    }

    override fun getMediaDuration(): Long {
        val len = vlcPlayer?.length ?: 0
        LogUtil.d(TAG, "getMediaDuration.len")
        return len
    }

    override fun getCurrentPosition(): Long {
        val time = vlcPlayer?.time ?: 0
        LogUtil.d(TAG, "getCurrentPosition.time")
        return time
    }

    override fun getPlaybackState(): Int {
        val state = vlcPlayer?.playerState ?: MyPlayerConstants.PREPARE_MEDIA
        LogUtil.d(TAG, "getPlaybackState.state")
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

    override fun switchDecoder() {
        // do nothing for now
    }
}