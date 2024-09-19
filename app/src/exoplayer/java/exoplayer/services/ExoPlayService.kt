package exoplayer.services

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.av1.Gav1Library
import com.google.android.exoplayer2.ext.ffmpeg.FfmpegLibrary
import com.google.android.exoplayer2.ext.flac.FlacLibrary
import com.google.android.exoplayer2.ext.opus.OpusLibrary
import com.google.android.exoplayer2.ext.vp9.VpxLibrary
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionOverride
import com.google.android.exoplayer2.trackselection.TrackSelectionParameters
import com.smile.karaokeplayer.constants.CommonConstants
import com.smile.karaokeplayer.services.BasePlayService
import exoplayer.audioProcessors.StereoVolumeAudioProcessor
import exoplayer.callbacks.ExoMediaControllerCallback
import exoplayer.callbacks.ExoMediaSessionCallbackNew
import exoplayer.exoRenderersFactory.MyRenderersFactory
import exoplayer.listeners.ExoPlayerListenerNew
import exoplayer.presenters.ExoPlayerPresenter
import java.util.Arrays

class ExoPlayService : BasePlayService() {

    companion object {
        private const val TAG = "ExoPlayService"
    }

    private var presenter : ExoPlayerPresenter? = null
    private var stereoVolumeAudioProcessor: StereoVolumeAudioProcessor? = null
    private var mediaSessionCallback: ExoMediaSessionCallbackNew? = null
    private var controllerCallback: ExoMediaControllerCallback? = null
    private var exoPlayerListener: ExoPlayerListenerNew? = null
    var currentPlayer: Player? = null
    var exoPlayer: ExoPlayer? = null

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
        Log.d(TAG, "onBind.binder= $binder")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind.intent = $intent")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
        releaseExoPlayer()
    }

    fun setPresenter(presenter: ExoPlayerPresenter) {
        this.presenter = presenter
    }

    fun initExoPlayer() {
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
                Log.d(TAG,"initExoPlayer.exoPlayer = $exoPlayer")
                exoPlayerListener = ExoPlayerListenerNew(this@ExoPlayService)
                addListener(exoPlayerListener!!)
                currentPlayer = this // default is playing video on Android device
            }
            Log.d(TAG,"initExoPlayer.FfmpegLibrary.isAvailable() = " + FfmpegLibrary.isAvailable())
            Log.d(TAG, "initExoPlayer.VpxLibrary.isAvailable() = " + VpxLibrary.isAvailable())
            Log.d(TAG, "initExoPlayer.FlacLibrary.isAvailable() = " + FlacLibrary.isAvailable())
            Log.d(TAG, "initExoPlayer.OpusLibrary.isAvailable() = " + OpusLibrary.isAvailable())
            Log.d(TAG, "initExoPlayer.Gav1Library.isAvailable() = " + Gav1Library.isAvailable())
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

    private fun releaseExoPlayer() {
        Log.d(TAG, "releaseExoPlayer")
        exoPlayer?.apply {
            removeListener(exoPlayerListener!!)
            stop()
            release()
        }
        exoPlayer = null
    }

    override fun initMediaCallback() {
        Log.d(TAG, "initMediaCallback.presenter = $presenter")
        presenter?.let {
            mediaSessionCallback = ExoMediaSessionCallbackNew(it, this@ExoPlayService)
            Log.d(TAG,"initMediaCallback.mediaSessionCallback = $mediaSessionCallback")
            mediaSessionCompat?.setCallback(mediaSessionCallback)
            controllerCallback = ExoMediaControllerCallback(it)
            Log.d(TAG,"initMediaCallback.controllerCallback = $controllerCallback")
            mediaControllerCompat?.registerCallback(controllerCallback!!)
        }
    }

    override fun setPlayerTime(progress: Long) {
        Log.d(TAG, "setPlayerTime")
        exoPlayer?.seekTo(progress)
    }

    override fun isSeekable(): Boolean {
        Log.d(TAG, "isSeekable")
        exoPlayer?.apply {
            return isCurrentMediaItemSeekable
        }
        return false
    }

    override fun setPlayerAudioVolume(volumeTmp: Float) {
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
                exoPlayer?.volume = volumeTmp
            }
            // No update, presenter?.playingParam should be updated in presenter
            // it.currentVolume = volumeTmp    // update presenter?.playingParam
            return
        }
        Log.d(TAG, "setAudioVolume.presenter?.playingParam is null")
    }

    override fun getMediaDuration(): Long {
        exoPlayer?.apply {
            return duration
        }
        return 0
    }

    /*  commented out for testing
    @Override
    public boolean isSeekable() {
        return getExoPlayer().isCurrentMediaItemSeekable();
    }
    */
    /*  commented out for testing
    @Override
    public void initMediaCallback() {
        Log.d(TAG, "initMediaCallback");
        mediaSessionCallback = new ExoMediaSessionCallback(mActivity,this);
        controllerCallback = new ExoMediaControllerCallback(this);
        Log.d(TAG, "initMediaCallback.getPlayService() = " + getPlayService());
        if (getPlayService() != null) {
            Log.d(TAG, "initMediaCallback.mediaSessionCallback = " + mediaSessionCallback);
            getPlayService().getMediaSessionCompat().setCallback(mediaSessionCallback);
            Log.d(TAG, "initMediaCallback.controllerCallback = " + controllerCallback);
            getPlayService().getMediaControllerCompat().registerCallback(controllerCallback);
        }
    }
    */
    override fun specificPlayerReplayMedia(currentAudioPosition: Long) {
        // song is playing, paused, or finished playing
        // cannot do the following statement (exoPlayer.setPlayWhenReady(false); )
        // because it will send Play.STATE_ENDED event after the playing has finished
        // but the playing was stopped in the middle of playing then won't send
        // Play.STATE_ENDED event
        // exoPlayer.setPlayWhenReady(false);
        Log.d(TAG,"specificPlayerReplayMedia.exoPlayer.seekTo(currentAudioPosition).")
        exoPlayer?.apply {
            seekTo(currentAudioPosition)
            prepare() // replace exoPlayer.retry()
            playWhenReady = true
        }
    }

    /*
    override fun setAudioVolumeInsideVolumeSeekBar(i: Int) {
        var currentVolume = 1.0f;
        if (i < PlayerConstants.MaxProgress) {
            val log1 = ln(PlayerConstants.MaxProgress.toDouble() - i.toDouble()).toFloat()
            val log2 = ln(PlayerConstants.MaxProgress.toDouble()).toFloat()
            currentVolume = 1.0f - (log1 / log2)
        }
        setAudioVolume(currentVolume);
    }
    */
    /*
    override fun getCurrentProgressForVolumeSeekBar(): Int {
        var currentProgress = PlayerConstants.MaxProgress
        presenter?.playingParam?.let {
            val currentVolume = it.currentVolume
            if (currentVolume < 1.0f) {
                currentProgress = PlayerConstants.MaxProgress -
                        (PlayerConstants.MaxProgress.toDouble().pow((1 - currentVolume).toDouble())).toInt()
                if (currentProgress < 0 ) currentProgress = 0
            }
        }
        return currentProgress;
    }
    */
}