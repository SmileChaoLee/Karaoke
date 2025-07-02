package com.smile.karaokeplayer.exoplayer.services

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.media3.cast.CastPlayer
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.ext.av1.Gav1Library
import com.google.android.exoplayer2.ext.ffmpeg.FfmpegLibrary
import com.google.android.exoplayer2.ext.flac.FlacLibrary
import com.google.android.exoplayer2.ext.opus.OpusLibrary
import com.google.android.exoplayer2.ext.vp9.VpxLibrary
import com.smile.karaokeplayer.constants.CommonConstants
import com.smile.karaokeplayer.exoplayer.audioProcessors.StereoVolumeAudioProcessor
import com.smile.karaokeplayer.exoplayer.callbacks.ExoMediaControllerCallback
import com.smile.karaokeplayer.exoplayer.callbacks.ExoMediaSessionCallback
import com.smile.karaokeplayer.exoplayer.exoRenderersFactory.MyRenderersFactory
import com.smile.karaokeplayer.exoplayer.listeners.ExoPlayerListener
import com.smile.karaokeplayer.exoplayer.presenters.ExoPlayerPresenter
import com.smile.karaokeplayer.services.BasePlayService
import java.util.Arrays

@UnstableApi
class ExoPlayService : BasePlayService() {

    var presenter: ExoPlayerPresenter? = null
    var exoPlayer: ExoPlayer? = null
    var castPlayer: CastPlayer? = null
    private var stereoVolumeAudioProcessor: StereoVolumeAudioProcessor? = null
    private var mediaSessionCallback: ExoMediaSessionCallback? = null
    private var controllerCallback: ExoMediaControllerCallback? = null
    private var exoPlayerListener: ExoPlayerListener? = null

    // Binder given to clients.
    private val binder = LocalBinder()
    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods.
        fun getService(): ExoPlayService = this@ExoPlayService
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate.application = $application")
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
        releasePlayersAndListener()
        mediaControllerCompat?.apply {
            controllerCallback?.let {
                registerCallback(it)
            }
        }
        super.onDestroy()
    }

    fun initPlayersAndListeners() {
        Log.d(TAG, "initPlayersAndListeners")
        initExoPlayerListener()
        initExoPlayer()
        initCastPlayer()
    }

    private fun releasePlayersAndListener() {
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
            val trackSelector =
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
                if (exoPlayerListener != null) {
                    addListener(exoPlayerListener!!)
                }
                Log.d(TAG,"initExoPlayer.this = $this")
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
            if (exoPlayerListener != null) {
                removeListener(exoPlayerListener!!)
            }
            stop()
            release()
        }
        exoPlayer = null
    }

    private fun transferPlaybackToCast() {
        val msgString = "transferPlaybackToCast"
        Log.d(TAG, msgString)
        if (presenter == null) {
            Log.d(TAG, "${msgString}.presenter is null")
            return
        }
        castPlayer?.let { castP ->
            exoPlayer?.let { exoP ->
                Log.d(TAG, "${msgString}.castPlayer and exoPlayer not null")
                exoP.currentMediaItem?.let {
                    Log.d(TAG, "${msgString}.currentMediaItem = $it")
                    Log.d(TAG, "${msgString}.uri = ${it.localConfiguration?.uri}")
                    val mediaUri = presenter?.mediaUri
                    Log.d(TAG, "${msgString}.mediaUri = $mediaUri")
                    if (mediaUri == null) {
                        stopCasting()
                        return
                    }
                    val mediaFileName = presenter?.mediaUri?.path
                    Log.d(TAG, "${msgString}.mediaFileName = $mediaFileName")
                    if (mediaFileName == null) {
                        stopCasting()
                        return
                    }
                    val position = exoP.currentPosition
                    val playWhenReady = exoP.playWhenReady
                    exoP.stop()

                    // starting switching to castPlayer
                    webServerAndCast.startWebServer(mediaFileName)
                    // must after startWebServer
                    val localMediaUrl = webServerAndCast.getMediaUrl()
                    if (localMediaUrl.isEmpty()) {
                        Log.d(TAG, "${msgString}.localMediaUrl is empty")
                        stopCasting()
                        return
                    }
                    //
                    Log.d(TAG, "${msgString}.localMediaUrl = $localMediaUrl")
                    isCastSessionAvailable = true
                    /*
                    val lastSlash: Int = mediaFileName.lastIndexOf('/')
                    val mediaTitle = if (lastSlash >= 0) {
                        mediaFileName.substring(lastSlash + 1)
                    } else {
                        mediaFileName
                    }
                    val mediaMetadata = MediaMetadata.Builder()
                        .setTitle(mediaTitle)
                        .setMediaType(MediaMetadata.MEDIA_TYPE_MOVIE)
                        .build()
                    val mediaItem = MediaItem.Builder()
                        .setUri(localMediaUrl)
                        .setMediaMetadata(mediaMetadata)
                        .setMimeType(getMimeTypeForFile(mediaFileName))
                        .build()
                    castP.setMediaItem(mediaItem, position)
                    */
                    val mediaItem =  it.buildUpon().setUri(localMediaUrl).build()
                    Log.d(TAG, "${msgString}.position = $position")
                    // castP.setMediaItem(mediaItem, position)
                    setMediaItem(mediaItem, position)
                    Log.d(TAG, "${msgString}.playWhenReady = $playWhenReady")
                    // castP.playWhenReady = playWhenReady
                    setPlayWhenReady(playWhenReady)
                    // castP.prepare()
                    prepare()
                    presenter!!.setCurrentPlayerToPlayerView()
                    Log.d(TAG, "${msgString}.castPlayer.currentMediaItem.uri " +
                            "= ${castPlayer?.currentMediaItem?.localConfiguration?.uri}")
                }
            }
        }
    }

    private fun transferPlaybackToLocal() {
        val msgString = "transferPlaybackToLocal"
        Log.d(TAG, msgString)
        if (presenter == null) {
            Log.d(TAG, "${msgString}.presenter is null")
            return
        }
        exoPlayer?.let { exoP ->
            castPlayer?.let { castP ->
                Log.d(TAG, "${msgString}.castPlayer and exoPlayer not null")
                exoP.currentMediaItem?.let {
                    Log.d(TAG, "${msgString}.currentMediaItem = $it")
                    Log.d(TAG, "${msgString}.uri = ${it.localConfiguration?.uri}")
                    val position = castP.currentPosition
                    val playWhenReady = castP.playWhenReady
                    castP.stop()
                    stopCasting()   // isCastSessionAvailable -> false

                    // starting switching to exoPlayer.
                    // mediaUri need to be set to local uri?
                    // exoP.setMediaItem(it, position)
                    setMediaItem(it, position)
                    Log.d(TAG, "${msgString}.playWhenReady = $playWhenReady")
                    // exoP.playWhenReady = playWhenReady
                    setPlayWhenReady(playWhenReady)
                    // exoP.prepare()
                    prepare()
                    presenter!!.setCurrentPlayerToPlayerView()
                }
            }
        }
    }

    private fun initCastPlayer() {
        Log.d(TAG, "initCastPlayer")
        castContext?.let { castIt ->
            val sessionAvailabilityListener = object: SessionAvailabilityListener {
                override fun onCastSessionAvailable() {
                    Log.d(TAG,"onCastSessionAvailable")
                    transferPlaybackToCast()
                }
                override fun onCastSessionUnavailable() {
                    Log.d(TAG,"onCastSessionUnavailable")
                    transferPlaybackToLocal()
                }
            }
            castPlayer = CastPlayer(castIt)
            castPlayer?.apply {
                if (exoPlayerListener != null) {
                    addListener(exoPlayerListener!!)
                }
                setSessionAvailabilityListener(sessionAvailabilityListener)
            }
        }
    }

    private fun releaseCastPlayer() {
        Log.d(TAG, "releaseCastPlayer")
        castPlayer?.apply {
            if (exoPlayerListener != null) {
                removeListener(exoPlayerListener!!)
            }
            stop()
            release()
        }
        castPlayer = null
    }

    fun getCurrentPlayer(): Player? {
        return if (isCastSessionAvailable) {
            castPlayer
        } else {
            exoPlayer
        }
    }

    fun getPlayWhenReady(): Boolean {
        return if (isCastSessionAvailable) {
            castPlayer?.playWhenReady ?: false
        } else {
            exoPlayer?.playWhenReady ?: false
        }
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
        exoPlayer?.trackSelectionParameters = trackSelectionParam
        return trackSelectionParam
    }

    fun getPlayingMediaInfo(audioTrackIndicesList: ArrayList<Array<Int>>): Int {
        Log.d(TAG, "getPlayingMediaInfo()")
        var mNumberOfVideoTracks = 0
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

        var trackSelector: DefaultTrackSelector? = null
        exoPlayer?.let {
            trackSelector = (it.trackSelector) as DefaultTrackSelector
        }
        if (trackSelector == null) {
            Log.d(TAG, "getPlayingMediaInfo.trackSelector is null")
            return mNumberOfVideoTracks
        }
        trackSelector.let {
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
                    trackGroupArray.let { trackIt ->
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
        return if (isCastSessionAvailable) {
            castPlayer?.mediaItemCount
        } else {
            exoPlayer?.mediaItemCount
        }
    }
    fun setTrackSelectionParameters(trackSelParam: TrackSelectionParameters) {
        if (isCastSessionAvailable) {
            castPlayer?.trackSelectionParameters = trackSelParam
        } else {
            exoPlayer?.trackSelectionParameters = trackSelParam
        }
    }
    private fun setMediaItem(mediaItem: MediaItem, position: Long) {
        if (isCastSessionAvailable) {
            castPlayer?.setMediaItem(mediaItem, position)
        } else {
            exoPlayer?.setMediaItem(mediaItem, position)
        }
    }
    fun setMediaItem(mediaItem: MediaItem) {
        setMediaItem(mediaItem, 0)
    }
    fun prepare() {
        if (isCastSessionAvailable) {
            castPlayer?.prepare()
        } else {
            exoPlayer?.prepare()
        }
    }
    fun setPlayWhenReady(whenReady: Boolean) {
        if (isCastSessionAvailable) {
            castPlayer?.playWhenReady = whenReady
        } else {
            exoPlayer?.playWhenReady = whenReady
        }
    }
    override fun onPlay() {
        Log.d(TAG, "onPlay")
        if (isCastSessionAvailable) {
            castPlayer?.apply {
                presenter?.let {
                    if ((it.playingParam.currentPlaybackState == PlaybackStateCompat.STATE_NONE
                                || it.playingParam.currentPlaybackState == PlaybackStateCompat.STATE_STOPPED)) {
                        // stopped by user (Player.STATE_IDLE)
                        Log.d(TAG, "onPlay.currentPlaybackState = PlaybackStateCompat.STATE_NONE")
                        // or Playing was finished (Player.STATE_ENDED)
                        Log.d(TAG, "or PlaybackStateCompat.STATE_STOPPED")
                        // prepare() or
                        replayMedia(it)
                    } else {
                        play()
                    }
                }
            }
        } else {
            exoPlayer?.apply {
                presenter?.let {
                    if ((it.playingParam.currentPlaybackState == PlaybackStateCompat.STATE_NONE
                                || it.playingParam.currentPlaybackState == PlaybackStateCompat.STATE_STOPPED)) {
                        // stopped by user (Player.STATE_IDLE)
                        Log.d(TAG, "onPlay.currentPlaybackState = PlaybackStateCompat.STATE_NONE")
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
    }
    override fun onPause() {
        Log.d(TAG, "onPause")
        if (isCastSessionAvailable) {
            castPlayer?.pause()
        } else {
            exoPlayer?.pause()
        }
    }
    override fun onStop() {
        Log.d(TAG, "onStop")
        if (isCastSessionAvailable) {
            castPlayer?.stop()
        } else {
            exoPlayer?.stop()
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
        return if (isCastSessionAvailable) {
            castPlayer?.isPlaying ?: false
        } else {
            exoPlayer?.isPlaying ?: false
        }
    }

    override fun setPlayerTime(progress: Long) {
        Log.d(TAG, "setPlayerTime")
        if (isCastSessionAvailable) {
            castPlayer?.seekTo(progress)
        } else {
            exoPlayer?.seekTo(progress)
        }
    }

    override fun isSeekable(): Boolean {
        Log.d(TAG, "isSeekable")
        val seekAble: Boolean = if (isCastSessionAvailable) {
            castPlayer?.isCurrentMediaItemSeekable ?: false
        } else {
            exoPlayer?.isCurrentMediaItemSeekable ?: false
        }
        Log.d(TAG, "isSeekable.seekAble = $seekAble")
        return seekAble
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
                            CommonConstants.LEFT_CHANNEL -> {
                                volumeInput[StereoVolumeAudioProcessor.LEFT_SPEAKER] = volumeTmp
                                volumeInput[StereoVolumeAudioProcessor.RIGHT_SPEAKER] = 0.0f
                            }
                            CommonConstants.RIGHT_CHANNEL -> {
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
            Log.d(TAG, "setAudioVolume.useAudioProcessor = $useAudioProcessor")
            if (!useAudioProcessor) {
                // exoPlayer?.volume = volumeTmp
                Log.d(TAG, "setAudioVolume.volumeTmp = $volumeTmp")
                if (isCastSessionAvailable) {
                    castPlayer?.volume = volumeTmp
                } else {
                    exoPlayer?.volume = volumeTmp
                }
            }
            it.currentVolume = volumeTmp    // update presenter?.playingParam
            return
        }
        Log.d(TAG, "setAudioVolume.presenter?.playingParam is null")
    }

    override fun getMediaDuration(): Long {
        val duration: Long = if (isCastSessionAvailable) {
            castPlayer?.duration ?: 0
        } else {
            exoPlayer?.duration ?: 0
        }
        Log.d(TAG, "getMediaDuration.duration = $duration")
        return duration
    }

    override fun getCurrentPosition(): Long {
        val currPosition: Long = if (isCastSessionAvailable) {
            castPlayer?.currentPosition ?: 0
        } else {
            exoPlayer?.currentPosition ?: 0
        }
        Log.d(TAG, "getCurrentPosition.currPosition = $currPosition")
        return currPosition
    }

    override fun getPlaybackState(): Int {
        val state: Int = if (isCastSessionAvailable) {
            castPlayer?.playbackState ?: Player.STATE_IDLE
        } else {
            exoPlayer?.playbackState ?: Player.STATE_IDLE
        }
        Log.d(TAG, "getCurrentPosition.state = $state")
        return state
    }

    override fun specificPlayerReplayMedia(currentAudioPosition: Long) {
        // song is playing, paused, or finished playing
        // cannot do the following statement (currentPlayer.setPlayWhenReady(false) )
        // because it will send Play.STATE_ENDED event after the playing has finished
        // but the playing was stopped in the middle of playing then won't send
        // Play.STATE_ENDED event
        // currentPlayer.setPlayWhenReady(false)
        Log.d(TAG, "specificPlayerReplayMedia")
        if (isCastSessionAvailable) {
            castPlayer?.apply {
                Log.d(TAG,"specificPlayerReplayMedia.castPlayer.seekTo.")
                seekTo(currentAudioPosition)
                prepare()
                playWhenReady = true
            }
        } else {
            exoPlayer?.apply {
                Log.d(TAG,"specificPlayerReplayMedia.exoPlayer.seekTo.")
                seekTo(currentAudioPosition)
                prepare()
                playWhenReady = true
            }
        }
    }


    companion object {
        private const val TAG = "ExoPlayService"
    }
}