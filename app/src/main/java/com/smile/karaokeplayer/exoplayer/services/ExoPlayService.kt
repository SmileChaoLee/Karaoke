package com.smile.karaokeplayer.exoplayer.services

// import com.google.android.gms.tasks.OnCompleteListener
// import com.google.android.gms.tasks.Task
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.mediarouter.media.MediaRouter
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.cast.CastPlayer
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.util.UnstableApi
import com.google.android.exoplayer2.ext.av1.Gav1Library
import com.google.android.exoplayer2.ext.ffmpeg.FfmpegLibrary
import com.google.android.exoplayer2.ext.flac.FlacLibrary
import com.google.android.exoplayer2.ext.opus.OpusLibrary
import com.google.android.exoplayer2.ext.vp9.VpxLibrary
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.TrackSelectionParameters
import com.google.android.gms.cast.framework.CastState
import com.smile.karaokeplayer.constants.CommonConstants
import com.smile.karaokeplayer.services.BasePlayService
import com.smile.karaokeplayer.exoplayer.audioProcessors.StereoVolumeAudioProcessor
import com.smile.karaokeplayer.exoplayer.callbacks.ExoMediaControllerCallback
import com.smile.karaokeplayer.exoplayer.callbacks.ExoMediaSessionCallback
import com.smile.karaokeplayer.exoplayer.exoRenderersFactory.MyRenderersFactory
import com.smile.karaokeplayer.exoplayer.listeners.ExoPlayerCastStateListener
import com.smile.karaokeplayer.exoplayer.listeners.ExoPlayerListener
import com.smile.karaokeplayer.exoplayer.presenters.ExoPlayerPresenter
import java.util.Arrays

@UnstableApi
class ExoPlayService : BasePlayService() {

    companion object {
        private const val TAG = "ExoPlayService"
    }

    var presenter : ExoPlayerPresenter? = null
    private var stereoVolumeAudioProcessor: StereoVolumeAudioProcessor? = null
    private var mediaSessionCallback: ExoMediaSessionCallback? = null
    private var controllerCallback: ExoMediaControllerCallback? = null
    private var exoPlayerListener: ExoPlayerListener? = null
    var currentPlayer: Player? = null
    private var exoPlayer: ExoPlayer? = null
    private var castPlayer: CastPlayer? = null
    private val isOnInternet = false
    private var castStateListener: ExoPlayerCastStateListener? = null
    private var sessionAvailabilityListener: SessionAvailabilityListener? = null
    var currentCastState: Int? = CastState.NO_DEVICES_AVAILABLE
        get() {
            val castContext = presenter?.castContext
            Log.d(TAG,"currentCastState.get().castContext?.castState = ${castContext?.castState}")
            return castContext?.castState
        }

    // Binder given to clients.
    private val binder = LocalBinder()
    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods.
        fun getService(): ExoPlayService = this@ExoPlayService
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
        Log.d(TAG, "onDestroy")
        super.onDestroy()
        releaseCastPlayerAndExoPlayer()
        mediaControllerCompat?.apply {
            controllerCallback?.let {
                registerCallback(it)
            }
        }
    }

    fun initCastPlayerAndExoPlayer() {
        initExoPlayerListener()
        initCastPlayer()
        initExoPlayer()
    }

    private fun releaseCastPlayerAndExoPlayer() {
        releaseExoPlayer()
        releaseCastPlayer()
    }

    private fun initExoPlayerListener() {
        Log.d(TAG, "initExoPlayerListener")
        exoPlayerListener = ExoPlayerListener(this@ExoPlayService)
    }

    private fun initExoPlayer() {
        Log.d(TAG, "initExoPlayer.presenter = $presenter")
        presenter?.let {
            val trackSelectionParameters = it.trackSelectionParameters
            val trackSelector: DefaultTrackSelector =
                DefaultTrackSelector(applicationContext, AdaptiveTrackSelection.Factory())
            Log.d(TAG,"initExoPlayer.trackSelector = $trackSelector")
            trackSelector.setParameters(trackSelectionParameters!!)

            // EXTENSION_RENDERER_MODE_OFF, EXTENSION_RENDERER_MODE_ON, EXTENSION_RENDERER_MODE_PREFER
            val myRenderersFactory =
                MyRenderersFactory(applicationContext, DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
            stereoVolumeAudioProcessor = myRenderersFactory.stereoVolumeAudioProcessor

            val exoPlayerBuilder = ExoPlayer.Builder(applicationContext, myRenderersFactory)
            val extractorsFactory = DefaultExtractorsFactory().setConstantBitrateSeekingEnabled(true)
            exoPlayer = exoPlayerBuilder
                .setTrackSelector(trackSelector)
                .setMediaSourceFactory(DefaultMediaSourceFactory(applicationContext, extractorsFactory))
                .build()
            exoPlayer?.apply {
                Log.d(TAG,"initExoPlayer.exoPlayer = $this")
                addListener(exoPlayerListener!!)
                Log.d(TAG,"initExoPlayer.this = $this")
                currentPlayer = this // default is playing video on Android device
                Log.d(TAG,"initExoPlayer.currentPlayer = $currentPlayer")
            }
            Log.d(TAG,"initExoPlayer.FfmpegLibrary.isAvailable() = " + FfmpegLibrary.isAvailable())
            Log.d(TAG, "initExoPlayer.VpxLibrary.isAvailable() = " + VpxLibrary.isAvailable())
            Log.d(TAG, "initExoPlayer.FlacLibrary.isAvailable() = " + FlacLibrary.isAvailable())
            Log.d(TAG, "initExoPlayer.OpusLibrary.isAvailable() = " + OpusLibrary.isAvailable())
            Log.d(TAG, "initExoPlayer.Gav1Library.isAvailable() = " + Gav1Library.isAvailable())
        }
    }

    private fun releaseExoPlayer() {
        Log.d(TAG, "releaseExoPlayer")
        exoPlayer?.apply {
            removeListener(exoPlayerListener!!)
            stop()
            release()
        }
        exoPlayer = null
    }

    fun getPlayWhenReady(): Boolean {
        // exoPlayer?.apply {
        currentPlayer?.apply {
            return playWhenReady
        }
        return false
    }

    @SuppressLint("SuspiciousIndentation")
    fun initCastPlayer() {
        Log.d(TAG,"initCastPlayer.presenter = $presenter")
        currentCastState = CastState.NO_DEVICES_AVAILABLE
        if (com.smile.karaokeplayer.BuildConfig.DEBUG) {
            Log.d(TAG,"initCastPlayer.com.smile.karaokeplayer.BuildConfig.DEBUG")
            val castContext = presenter?.castContext
            Log.d(TAG, "initCastPlayer.castContext = $castContext")
            castContext?.let { castIt ->
                currentCastState = castIt.castState
                Log.d(TAG, "initCastPlayer.currentCastState = $currentCastState")
                presenter?.apply {
                    castStateListener = ExoPlayerCastStateListener(this)
                }
                castStateListener?.apply {
                    Log.d(TAG, "initCastPlayer.castStateListener = $this")
                    castIt.addCastStateListener(this)
                }
                castPlayer = CastPlayer(castIt)
                castPlayer?.addListener(exoPlayerListener!!)
                sessionAvailabilityListener = object : SessionAvailabilityListener {
                    @Synchronized
                    override fun onCastSessionAvailable() {
                        Log.d(TAG, "initCastPlayer.onCastSessionAvailable")
                        presenter?.let {
                            Log.d(TAG,"initCastPlayer.onCastSessionAvailable.mediaUri = ${it.mediaUri}")
                            Log.d(TAG,"initCastPlayer.onCastSessionAvailable.isOnInternet = $isOnInternet")
                            // if (it.mediaUri == null || !isOnInternet) {
                            if (it.mediaUri == null) {
                                val mRouter = MediaRouter.getInstance(applicationContext) // singleton
                                mRouter.unselect(MediaRouter.UNSELECT_REASON_STOPPED) // stop casting
                                return
                            }
                        }
                        Log.d(
                            TAG, "initCastPlayer.onCastSessionAvailable." +
                                "Set current player to castPlayer")
                        // currentPlayer = castPlayer
                        setPlayer(castPlayer)
                    }
                    override fun onCastSessionUnavailable() {
                        Log.d(
                            TAG,"initCastPlayer.onCastSessionUnavailable." +
                                "Set current player to exoPlayer")
                        // currentPlayer = exoPlayer
                        setPlayer(exoPlayer)
                    }
                }.also {
                    Log.d(TAG,"initCastPlayer.castPlayer.setSessionAvailabilityListener")
                    castPlayer?.setSessionAvailabilityListener(it)
                }
            }
        }
    }

    private fun releaseCastPlayer() {
        Log.d(TAG, "releaseCastPlayer")
        castPlayer?.apply {
            removeListener(exoPlayerListener!!)
            setSessionAvailabilityListener(null)
            stop()
            release()
        }
        castPlayer = null
        val castContext = presenter?.castContext
        Log.d(TAG, "initCastPlayer.castContext = $castContext")
        castContext?.apply {
            castStateListener?.let {
                Log.d(TAG,"releaseCastPlayer.removeCastStateListener(castStateListener)")
                removeCastStateListener(it)
            }
        }
    }

    private fun setPlayer(player: Player?) {
        Log.d(TAG, "setPlayer.player = $player")
        Log.d(TAG, "setPlayer.currentPlayer = $currentPlayer")
        if (player == null || player === currentPlayer) {
            Log.d(TAG, "setPlayer.player === currentPlayer")
            return
        }
        // Player state management.
        var playbackPositionMs = C.TIME_UNSET
        var windowIndex = C.INDEX_UNSET
        var playWhenReady = false
        // old currentPlayer
        currentPlayer?.let {
            // Save state from the previous player.
            val playbackState = it.playbackState
            if (playbackState != Player.STATE_ENDED) {
                Log.d(TAG, "setPlayer.playbackState != Player.STATE_ENDED")
                playWhenReady = it.playWhenReady
                windowIndex = it.currentMediaItemIndex
                if (windowIndex != presenter?.currentItemIndex) {
                    Log.d(TAG, "setPlayer.windowIndex != presenter?.currentItemIndex")
                    // playbackPositionMs = C.TIME_UNSET
                    // windowIndex = currentItemIndex;
                    presenter?.currentItemIndex = windowIndex
                }
            }
            // presenter?.stopPlay() // or pausePlay(); // do not use presenter, timing issue
            Log.d(TAG, "setPlayer.mediaSessionCallback.onStop()")
            mediaSessionCallback?.onPause()
            // for temporarily setting, it should be it.currentPosition
            // playbackPositionMs = exoPlayer?.currentPosition!!
            playbackPositionMs = it.currentPosition
            Log.d(TAG, "setPlayer.playbackPositionMs = $playbackPositionMs")
            presenter?.let { preIt ->
                preIt.playingParam?.currentAudioPosition = playbackPositionMs
                preIt.playingParam?.currentPlaybackState = PlaybackStateCompat.STATE_PAUSED
            }
        }

        currentPlayer = player
        presenter?.let {
            it.playingParam?.currentPlaybackState = PlaybackStateCompat.STATE_BUFFERING
            mediaSessionCallback?.onPrepareFromUri(it.mediaUri, null)
            currentPlayer?.let { playIt ->
                Log.d(TAG, "setPlayer.currentPosition = ${playIt.currentPosition}")
                playIt.repeatMode = it.playingParam.repeatStatus
                // playIt.seekTo(it.playingParam.currentAudioPosition)
                playIt.playWhenReady = true
            }
            it.setCurrentPlayerToPlayerView()
        }

        /*
        // Playback transition.
        currentPlayer?.let {
            if (it.currentTimeline.isEmpty && presenter?.mediaUri != null) {
                // has not play yet
                Log.d(TAG, "setPlayer.currentTimeline is Empty")
                val mediaItem: MediaItem = MediaItem.Builder()
                    .setUri(presenter?.mediaUri)
                    .setMediaMetadata(MediaMetadata.Builder().setTitle("Video Casted").build())
                    .setMimeType(MimeTypes.BASE_TYPE_VIDEO) // .setDrmConfiguration(null)
                    .build()
                Log.d(TAG, "setPlayer.windowIndex = $windowIndex")
                val mediaItems: MutableList<MediaItem> = ArrayList()
                mediaItems.add(mediaItem)
                it.setMediaItems(mediaItems, windowIndex, C.TIME_UNSET)
                it.repeatMode = presenter?.playingParam?.repeatStatus!!
                it.playWhenReady = playWhenReady
            } else {
                // already played before
                Log.d(TAG, "setPlayer.currentTimeline is not Empty or presenter?.mediaUri is null")
            }
            Log.d(TAG, "setPlayer.startPlay()")
            if (windowIndex != C.INDEX_UNSET) {
                Log.d(TAG, "setPlayer.windowIndex != C.INDEX_UNSET")
                Log.d(TAG, "setPlayer.playbackPositionMs = $playbackPositionMs")
                it.seekTo(playbackPositionMs)
                it.playWhenReady = playWhenReady
            }
            // Playback transition.
            // presenter?.startPlay()   // do not use this, timing issue
            Log.d(TAG, "setPlayer.mediaSessionCallback.onPrepareFromUri()")
            mediaSessionCallback?.onPrepareFromUri(presenter?.mediaUri!!, null)
            // Player View management.
            presenter?.setCurrentPlayerToPlayerView()
        }
        */
    }

    fun selectAudioTrack(trackIndicesCombination: Array<Int>?,
                         trackSelParam: TrackSelectionParameters): TrackSelectionParameters {
        Log.d(TAG, "selectAudioTrack")
        if (trackIndicesCombination == null) {
            Log.d(TAG, "selectAudioTrack.trackIndicesCombination = null")
            return trackSelParam
        }
        val trackSelector: DefaultTrackSelector? = exoPlayer?.trackSelector as DefaultTrackSelector
        if (trackSelector == null) {
            Log.d(TAG, "selectAudioTrack.trackSelector = null")
            return trackSelParam
        }
        val mappedTrackInfo = trackSelector.currentMappedTrackInfo
        if (mappedTrackInfo == null) {
            Log.d(TAG, "selectAudioTrack.mappedTrackInfo = null")
            return trackSelParam
        }
        val audioRendererIndex = trackIndicesCombination[0]
        Log.d(TAG,"selectAudioTrack.audioRendererIndex = $audioRendererIndex")
        val audioTrackGroupIndex = trackIndicesCombination[1]
        Log.d(TAG,"selectAudioTrack.audioTrackGroupIndex = $audioTrackGroupIndex")
        val audioTrackIndex = trackIndicesCombination[2]
        Log.d(TAG,"selectAudioTrack.audioTrackIndex = $audioTrackIndex")
        if (mappedTrackInfo.getTrackSupport(audioRendererIndex, audioTrackGroupIndex, audioTrackIndex)
            != C.FORMAT_HANDLED) {
            Log.d(TAG,"selectAudioTrack.!= C.FORMAT_HANDLED")
            return trackSelParam
        }
        Log.d(TAG,"selectAudioTrack.trackSelectorParameters = $trackSelParam")
        val parametersBuilder: TrackSelectionParameters.Builder =
            trackSelParam.buildUpon()
        val trackGroup = mappedTrackInfo.getTrackGroups(audioRendererIndex)[audioTrackGroupIndex]
        val override = TrackSelectionOverride(trackGroup, audioTrackIndex)
        val trackSelectionParam = parametersBuilder.setOverrideForType(override).build()
        // exoPlayer?.trackSelectionParameters = trackSelectionParam
        currentPlayer?.trackSelectionParameters = trackSelectionParam
        return trackSelectionParam
    }

    fun getPlayingMediaInfo(audioTrackIndicesList: ArrayList<Array<Int>>): Int {
        Log.d(TAG, "getPlayingMediaInfo()")
        var mNumberOfVideoTracks = 0;
        var numVideoRenderers = 0
        var numAudioRenderers = 0
        var numVideoTrackGroups = 0
        var numAudioTrackGroups = 0

        var trackIndicesCombination: Array<Int>
        var audioTrackIdPlayed = -1

        val videoPlayedFormat: Format? = exoPlayer?.videoFormat
        Log.d(TAG, "getPlayingMediaInfo.videoPlayedFormat = $videoPlayedFormat")
        videoPlayedFormat?.let {
            Log.d(TAG, "getPlayingMediaInfo.videoPlayedFormat.id = " + it.id)
        }
        val audioPlayedFormat: Format? = exoPlayer?.audioFormat
        Log.d(TAG, "getPlayingMediaInfo.audioPlayedFormat = $audioPlayedFormat")
        audioPlayedFormat?.let {
            Log.d(TAG, "getPlayingMediaInfo.audioPlayedFormat.id = " + it.id)
            val channelsNum = audioPlayedFormat.channelCount
            Log.d(TAG, "getPlayingMediaInfo.audioPlayedFormat.channelCount = $channelsNum")
            Log.d(TAG,"getPlayingMediaInfo.audioPlayedFormat.sampleRate = " + audioPlayedFormat.sampleRate)
            Log.d(TAG,"getPlayingMediaInfo.audioPlayedFormat.pcmEncoding = " + audioPlayedFormat.pcmEncoding)
        }

        val trackSelector = exoPlayer?.trackSelector as DefaultTrackSelector
        if (trackSelector == null) {
            Log.d(TAG, "getPlayingMediaInfo.trackSelector is null")
            return mNumberOfVideoTracks
        }
        trackSelector?.let {
            val mappedTrackInfo = it.currentMappedTrackInfo
            mappedTrackInfo?.let { mapIt ->
                val rendererCount = mapIt.rendererCount
                Log.d(TAG, "mappedTrackInfo.getRendererCount() = $rendererCount")
                for (rendererIndex in 0 until rendererCount) {
                    Log.d(TAG, "rendererIndex = $rendererIndex")
                    val rendererType = mapIt.getRendererType(rendererIndex)
                    when (rendererType) {
                        C.TRACK_TYPE_VIDEO -> numVideoRenderers++
                        C.TRACK_TYPE_AUDIO -> numAudioRenderers++
                    }
                    val trackGroupArray = mapIt.getTrackGroups(rendererIndex)
                    trackGroupArray?.let { trackIt ->
                        val arraySize = trackIt.length
                        Log.d(TAG,"trackGroupArray.length of renderer no ( $rendererIndex ) = $arraySize")
                        for (groupIndex in 0 until arraySize) {
                            Log.d(TAG, "trackGroupArray.index = $groupIndex")
                            when (rendererType) {
                                C.TRACK_TYPE_VIDEO -> numVideoTrackGroups++
                                C.TRACK_TYPE_AUDIO -> numAudioTrackGroups++
                            }
                            val trackGroup = trackIt[groupIndex]
                            val groupSize = trackGroup.length
                            Log.d(TAG,"trackGroup.length of trackGroup [ $groupIndex ] = $groupSize")
                            for (trackIndex in 0 until groupSize) {
                                val tempFormat = trackGroup.getFormat(trackIndex)
                                when (rendererType) {
                                    C.TRACK_TYPE_VIDEO -> {
                                        /*
                                        trackIndicesCombination = Array(3) {0}
                                        trackIndicesCombination[0] = rendererIndex
                                        trackIndicesCombination[1] = groupIndex
                                        trackIndicesCombination[2] = trackIndex
                                        */
                                        mNumberOfVideoTracks++
                                    }

                                    C.TRACK_TYPE_AUDIO -> {
                                        trackIndicesCombination = Array(3) {0}
                                        trackIndicesCombination[0] = rendererIndex
                                        trackIndicesCombination[1] = groupIndex
                                        trackIndicesCombination[2] = trackIndex
                                        audioTrackIndicesList.add(trackIndicesCombination)
                                        if (tempFormat == audioPlayedFormat) {
                                            audioTrackIdPlayed = audioTrackIndicesList.size
                                        }
                                    }
                                }
                                Log.d(TAG, "tempFormat = $tempFormat")
                            }
                        }
                    }
                }
            }
        }

        Log.d(TAG, "numVideoRenderer = $numVideoRenderers")
        Log.d(TAG, "numAudioRenderer = $numAudioRenderers")
        Log.d(TAG, "numVideoTrackGroups = $numVideoTrackGroups")
        Log.d(TAG, "numAudioTrackGroups = $numAudioTrackGroups")
        Log.d(TAG, "audioTrackIdPlayed = $audioTrackIdPlayed")

        return mNumberOfVideoTracks
    }

    // For ExoMediaSessionCallback.kt
    fun getMediaItemCount(): Int? {
        // return exoPlayer?.mediaItemCount
        return currentPlayer?.mediaItemCount
    }
    fun setTrackSelectionParameters(trackSelParam: TrackSelectionParameters) {
        // exoPlayer?.trackSelectionParameters = trackSelParam
        currentPlayer?.trackSelectionParameters = trackSelParam
    }
    fun setMediaItem(mediaItem: MediaItem) {
        // exoPlayer?.setMediaItem(mediaItem)
        currentPlayer?.setMediaItem(mediaItem)
    }
    fun prepare() {
        // exoPlayer?.prepare()
        currentPlayer?.prepare()
    }
    fun setPlayWhenReady(whenReady: Boolean) {
        // exoPlayer?.playWhenReady = whenReady
        currentPlayer?.playWhenReady = whenReady
    }
    override fun onPlay() {
        // exoPlayer?.apply {
        currentPlayer?.apply {
            Log.d(TAG, "onPlay().currentPlayer = $currentPlayer")
            Player.STATE_IDLE
            presenter?.let {
                if ((it.playingParam.currentPlaybackState == PlaybackStateCompat.STATE_NONE
                || it.playingParam.currentPlaybackState == PlaybackStateCompat.STATE_STOPPED)) {
                    // stopped by user (Player.STATE_IDLE)
                    Log.d(TAG, "onPlay().currentPlaybackState = PlaybackStateCompat.STATE_NONE")
                    // or Playing was finished (Player.STATE_ENDED)
                    Log.d(TAG, "or PlaybackStateCompat.STATE_STOPPED")
                    // prepare() or
                    replayMedia(it)
                } else {
                    play()
                }
            }
        }
    }
    override fun onPause() {
        // exoPlayer?.apply {
        currentPlayer?.apply {
            Log.d(TAG, "onPause().currentPlayer = $currentPlayer")
            pause()
        }
    }
    override fun onStop() {
        // exoPlayer?.apply {
        currentPlayer?.apply {
            Log.d(TAG, "onStop().currentPlayer = $currentPlayer")
            stop()
        }
    }

    override fun initMediaCallback() {
        Log.d(TAG, "initMediaCallback.presenter = $presenter")
        presenter?.let {
            mediaSessionCallback = ExoMediaSessionCallback(it, this@ExoPlayService)
            Log.d(TAG,"initMediaCallback.mediaSessionCallback = $mediaSessionCallback")
            mediaSessionCompat?.setCallback(mediaSessionCallback)
            controllerCallback = ExoMediaControllerCallback(it)
            Log.d(TAG,"initMediaCallback.controllerCallback = $controllerCallback")
            mediaControllerCompat?.registerCallback(controllerCallback!!)
        }
    }

    override fun isPlaying(): Boolean {
        // return exoPlayer?.isPlaying
        currentPlayer?.apply {
            return isPlaying
        }
        return false
    }

    override fun setPlayerTime(progress: Long) {
        Log.d(TAG, "setPlayerTime")
        // exoPlayer?.seekTo(progress)
        currentPlayer?.seekTo(progress)
    }

    override fun isSeekable(): Boolean {
        Log.d(TAG, "isSeekable")
        // exoPlayer?.apply {
        currentPlayer?.apply {
            Log.d(TAG, "isSeekable.isCurrentMediaItemSeekable")
            return isCurrentMediaItemSeekable
        }
        return false
    }

    override fun setAudioVolume(volumeTmp: Float) {
        Log.d(TAG, "setAudioVolume")
        presenter?.playingParam?.let {
            Log.d(TAG, "setAudioVolume.presenter?.playingParam is not null")
            // get current channel
            val currentChannelPlayed = it.currentChannelPlayed
            var useAudioProcessor = false
            stereoVolumeAudioProcessor?.apply {
                if (outputChannelCount >= 0) {
                    useAudioProcessor = true
                    val volumeInput = FloatArray(outputChannelCount)
                    if (outputChannelCount == 2) {
                        when (currentChannelPlayed) {
                            CommonConstants.LeftChannel -> {
                                volumeInput[StereoVolumeAudioProcessor.LEFT_SPEAKER] = volumeTmp
                                volumeInput[StereoVolumeAudioProcessor.RIGHT_SPEAKER] = 0.0f
                            }
                            CommonConstants.RightChannel -> {
                                volumeInput[StereoVolumeAudioProcessor.LEFT_SPEAKER] = 0.0f
                                volumeInput[StereoVolumeAudioProcessor.RIGHT_SPEAKER] = volumeTmp
                            }
                            else -> {
                                volumeInput[StereoVolumeAudioProcessor.LEFT_SPEAKER] = volumeTmp
                                volumeInput[StereoVolumeAudioProcessor.RIGHT_SPEAKER] = volumeTmp
                            }
                        }
                    } else {
                        Arrays.fill(volumeInput, volumeTmp)
                    }
                    volume = volumeInput
                }
            }
            if (!useAudioProcessor) {
                // exoPlayer?.volume = volumeTmp
                currentPlayer?.volume = volumeTmp
            }
            it.currentVolume = volumeTmp    // update presenter?.playingParam
            return
        }
        Log.d(TAG, "setAudioVolume.presenter?.playingParam is null")
    }

    override fun getMediaDuration(): Long {
        // exoPlayer?.apply {
        currentPlayer?.apply {
            return duration
        }
        return 0
    }

    override fun getCurrentPosition(): Long {
        // exoPlayer?.apply {
        currentPlayer?.apply {
            return currentPosition
        }
        return 0
    }

    override fun getPlaybackState(): Int {
        // exoPlayer?.apply {
        currentPlayer?.apply {
            return playbackState
        }
        return Player.STATE_IDLE
    }

    override fun specificPlayerReplayMedia(currentAudioPosition: Long) {
        // song is playing, paused, or finished playing
        // cannot do the following statement (currentPlayer.setPlayWhenReady(false); )
        // because it will send Play.STATE_ENDED event after the playing has finished
        // but the playing was stopped in the middle of playing then won't send
        // Play.STATE_ENDED event
        // currentPlayer.setPlayWhenReady(false);
        Log.d(TAG,"specificPlayerReplayMedia.currentPlayer.seekTo(currentAudioPosition).")
        // exoPlayer?.apply {
        currentPlayer?.apply {
            seekTo(currentAudioPosition)
            prepare() // replace currentPlayer.retry()
            playWhenReady = true
        }
    }
}