package com.smile.karaoke.presenters

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import com.smile.karaoke.constants.CommonConstants
import com.smile.karaoke.constants.MyPlayerConstants
import com.smile.karaoke.models.MySingleton
import com.smile.karaoke.models.PlayingParameters
import com.smile.karaoke.models.SongInfo
import com.smile.karaoke.services.BasePlayService
import com.smile.karaoke.utilities.LogUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
abstract class PlayerBasePresenter(private val mPresentView: BasePresentView) {

    companion object {
        private const val TAG = "PlayerBasePresenter"
    }

    var mediaUri: Uri? = null
    open var numberOfVideoTracks: Int = 0
    lateinit var playingParam: PlayingParameters
        private set
    var singleSongInfo: SongInfo? = null // when playing single song in songs list
    private var mCanShowNotSupportedFormat = false

    interface BasePresentView {
        fun setImageButtonStatus()
        fun playButtonOnPauseButtonOff()
        fun playButtonOffPauseButtonOn()
        fun initPlayerDurationSeekbar(duration: Float)
        fun updateDurationTextView(duration: Float)
        fun onDurationSeekBarProgressChanged(progress: Int, fromUser: Boolean)
        fun updatePlayerDurationSeekbarProgress(progress: Int)
        fun updateVolumeSeekBarProgress()
        fun showNativeAndHideBannerAd(): Boolean
        fun hideNativeAd()
        fun showBufferingMessage()
        fun dismissBufferingMessage()
        fun buildAudioTrackMenuItem(audioTrackNumber: Int)
        fun setTimerToHideSupportAudioControl()
        fun showMusicAndVocalIsNotSet()
        fun hidePlayerView()
        fun showPlayerView()
        fun setCurrentPlayerToPlayerView()
        fun getPlayService(): BasePlayService?
        fun showToastNoFilesSelected()
        fun showToastNoPrevious()
        fun showToastNoNext()
        fun showToastNotSupported()
        fun isActivityFinishing(): Boolean
        fun getRunActivity(): AppCompatActivity?
    }

    abstract fun initializeVariables(
        savedInstanceState: Bundle?,
        callingIntent: Intent?,
        isAutoPlay: Boolean
    )

    abstract fun setAudioTrackAndChannel(audioTrackIndex: Int, audioChannel: Int)
    abstract fun switchAudioToMusic()
    abstract fun switchAudioToVocal()
    abstract fun startDurationBarHandler()
    abstract fun removeMsgFromDurationBarHandler()
    abstract fun setAudioActionSubMenu()
    abstract fun getNumberOfAudioTracks(): Int

    fun getActivity(): AppCompatActivity? {
        return mPresentView.getRunActivity()
    }

    @JvmField
    protected val durationSeekBarHandler: Handler = Handler(Looper.getMainLooper())
    @JvmField
    protected val durationSeekBarRunnable: Runnable = object : Runnable {
        val msgStr: String = "durationSeekBarRunnable"

        @Synchronized
        override fun run() {
            durationSeekBarHandler.removeCallbacksAndMessages(null)
            val playService: BasePlayService? = getBasePlayService()
            if (playService != null) {
                val playbackState = playingParam.currentPlaybackState
                LogUtil.d(TAG, "$msgStr.playbackState = $playbackState")
                if (playbackState == PlaybackStateCompat.STATE_PLAYING) {
                    // PlaybackStateCompat.STATE_PLAYING = 3
                    val currPosition = playService.getCurrentPosition().toInt()
                    LogUtil.d(TAG, "$msgStr + currPosition = $currPosition")
                    mPresentView.updatePlayerDurationSeekbarProgress(currPosition)
                }
                val duration = playService.getMediaDuration().toInt()
                LogUtil.d(TAG, "$msgStr.duration = $duration")
                mPresentView.updateDurationTextView(duration.toFloat())
            }
            durationSeekBarHandler.postDelayed(durationSeekBarRunnable, 1000)
        }
    }

    fun setPlayingParameters(songInfo: SongInfo) {
        val musicTrackNo = songInfo.musicTrackNo ?: 1
        val musicChannel = songInfo.musicChannel ?: CommonConstants.RIGHT_CHANNEL
        val vocalTrackNo = songInfo.vocalTrackNo ?: 1
        val vocalChannel = songInfo.vocalChannel ?: CommonConstants.LEFT_CHANNEL
        playingParam.isInSongList = songInfo.included == "1"
        playingParam.musicAudioTrackIndex = musicTrackNo
        playingParam.musicAudioChannel = musicChannel
        playingParam.vocalAudioTrackIndex = vocalTrackNo
        playingParam.vocalAudioChannel = vocalChannel
        if (songInfo !== singleSongInfo) {
            playingParam.currentAudioTrackIndexPlayed = vocalTrackNo
            playingParam.currentChannelPlayed = vocalChannel
            singleSongInfo = songInfo
        }
    }

    fun autoPlaySongList() {
        val logStr = "autoPlaySongList"
        LogUtil.d(TAG, "$logStr.orderedSongs.size = ${MySingleton.orderedSongs.size}")
        mCanShowNotSupportedFormat = true
        if (!MySingleton.orderedSongs.isEmpty()) {
            // next song that will be played, which the index is 0
            // start playing video from list
            playingParam.currentSongIndex = -1
            val playService = getBasePlayService()
            LogUtil.d(TAG, "$logStr.playService = $playService")
            val isPlaying = playService != null && playService.isPlaying()
            LogUtil.d(TAG, "$logStr.isPlaying = $isPlaying")
            if (isPlaying && playService.isSeekable()) {
                LogUtil.d(TAG, "$logStr.stopPlay(PlayerConstants.FINISHED_BY_PROGRAM)")
                stopPlay(MyPlayerConstants.FINISHED_BY_PROGRAM)
            } else {
                LogUtil.d(TAG, "$logStr.startAutoPlay(false)")
                startAutoPlay(false)
            }
        } else {
            mPresentView.showToastNoFilesSelected()
        }
    }

    private fun getBasePlayService(): BasePlayService? {
        return mPresentView.getPlayService()
    }

    @Suppress("UNCHECKED_CAST")
    fun initializeVariablesBase(
        savedInstanceState: Bundle?,
        callingIntent: Intent?, isAutoPlay: Boolean) {
        val logStr = "initializeVariablesBase"
        LogUtil.i(TAG, "$logStr.savedInstanceState = $savedInstanceState")
        LogUtil.d(TAG, "$logStr.isAutoPlay = $isAutoPlay")
        if (savedInstanceState == null) {
            numberOfVideoTracks = 0
            mediaUri = null
            playingParam = PlayingParameters()
            mCanShowNotSupportedFormat = false
            singleSongInfo = null // default
            playingParam.let { pm ->
                pm.isPlaySingleSong = false // default
                if (callingIntent != null) {
                    val arguments = callingIntent.extras
                    if (arguments != null) {
                        pm.isPlaySingleSong = arguments
                            .getBoolean(MyPlayerConstants.IS_PLAY_SINGLE_SONG_STATE, true)
                        pm.currentVolume = arguments
                            .getFloat(MyPlayerConstants.SingleSongVolume, pm.currentVolume)
                        singleSongInfo =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) arguments.getParcelable(
                                MyPlayerConstants.SINGLE_SONG_INFO_STATE,
                                SongInfo::class.java
                            )
                            else arguments.getParcelable(MyPlayerConstants.SINGLE_SONG_INFO_STATE)
                        LogUtil.d(TAG, "$logStr.singleSongInfo = $singleSongInfo")
                    }
                }
            }
        } else {
            // needed to be set
            numberOfVideoTracks =
                savedInstanceState.getInt(MyPlayerConstants.NumberOfVideoTracksState, 0)
            val orderedSongs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                savedInstanceState.getSerializable(MyPlayerConstants.OrderedSongsState,
                    ArrayList::class.java) as ArrayList<SongInfo>?
            } else
                savedInstanceState.getSerializable(MyPlayerConstants.OrderedSongsState) as ArrayList<SongInfo>?
            LogUtil.d(TAG, "$logStr.orderedSongs = $orderedSongs")
            if (orderedSongs != null) {
                MySingleton.orderedSongs.clear()
                MySingleton.orderedSongs.addAll(orderedSongs)
            }
            mediaUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                savedInstanceState.getParcelable(MyPlayerConstants.MediaUriState,
                    Uri::class.java)
            } else savedInstanceState.getParcelable(MyPlayerConstants.MediaUriState)
            LogUtil.d(TAG, "$logStr.mediaUri = $mediaUri")
            val pm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                savedInstanceState.getParcelable(MyPlayerConstants.PlayingParamState,
                    PlayingParameters::class.java)
            } else
                savedInstanceState.getParcelable(MyPlayerConstants.PlayingParamState)
            LogUtil.d(TAG, "$logStr.pm = $pm")
            playingParam = pm ?: PlayingParameters()
            mCanShowNotSupportedFormat =
                savedInstanceState.getBoolean(MyPlayerConstants.CanShowNotSupportedFormatState)
            singleSongInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                savedInstanceState.getParcelable(MyPlayerConstants.SINGLE_SONG_INFO_STATE,
                    SongInfo::class.java)
            } else savedInstanceState.getParcelable(MyPlayerConstants.SINGLE_SONG_INFO_STATE)
            LogUtil.d(TAG, "initializeVariablesBase.singleSongInfo = $singleSongInfo")
        }
        playingParam.isAutoPlay = isAutoPlay
    }

    fun playLeftChannel() {
        LogUtil.d(TAG, "playLeftChannel")
        val playService = getBasePlayService() ?: return
        LogUtil.d(TAG, "playLeftChannel.playService = $playService")
        playingParam.currentChannelPlayed = CommonConstants.LEFT_CHANNEL
        playService.setAudioVolume(playingParam.currentVolume)
    }

    fun playRightChannel() {
        LogUtil.d(TAG, "playRightChannel")
        val playService = getBasePlayService() ?: return
        LogUtil.d(TAG, "playRightChannel.playService = $playService")
        playingParam.currentChannelPlayed = CommonConstants.RIGHT_CHANNEL
        playService.setAudioVolume(playingParam.currentVolume)
    }

    fun playStereoChannel() {
        LogUtil.d(TAG, "playStereoChannel")
        val playService = getBasePlayService() ?: return
        LogUtil.d(TAG, "playStereoChannel.playService = $playService")
        playingParam.currentChannelPlayed = CommonConstants.STEREO
        playService.setAudioVolume(playingParam.currentVolume)
    }

    fun startAutoPlay(isSelfFinished: Boolean) {
        LogUtil.d(TAG, "startAutoPlay")
        if (mPresentView.isActivityFinishing()) {
            // activity is being destroyed
            LogUtil.d(TAG, "startAutoPlay.activity is finishing")
            return
        }
        val playService = getBasePlayService() ?: return
        val stillPlayNext = playService.startAutoPlay(this, isSelfFinished)
        LogUtil.d(TAG, "startAutoPlay.stillPlayNext = $stillPlayNext")
        if (!stillPlayNext) {    // no more playing the next song
            mPresentView.showNativeAndHideBannerAd()
        }
        mPresentView.setImageButtonStatus()
    }

    fun setAutoPlayStatusAndAction(songs: ArrayList<SongInfo>): Boolean {
        LogUtil.d(TAG, "setAutoPlayStatusAndAction.songs.size() = ${songs.size}")
        var isAutoPlay = false
        if (!songs.isEmpty()) {
            MySingleton.orderedSongs.clear()
            MySingleton.orderedSongs.addAll(songs)
            playingParam.isAutoPlay = true
            autoPlaySongList()
            mPresentView.showPlayerView()
            mPresentView.setImageButtonStatus()
            isAutoPlay = true
        }
        return isAutoPlay
    }

    fun stopAutoPlay() {
        val playbackState = playingParam.currentPlaybackState
        if (playbackState != MyPlayerConstants.PREPARE_MEDIA
            && playbackState != PlaybackStateCompat.STATE_NONE
            && playbackState != PlaybackStateCompat.STATE_STOPPED) {
            // not the following: (has not started, stopped, or finished)
            stopPlay(MyPlayerConstants.STOPPED_BY_USER)
        }
        playingParam.isAutoPlay = false // must be the last in this block
        mPresentView.setImageButtonStatus()
    }

    fun playPreviousSong() {
        LogUtil.d(TAG, "playPreviousSong")
        val orderedSongsSize = MySingleton.orderedSongs.size
        if (orderedSongsSize <= 1) {
            LogUtil.d(TAG, "playPreviousSong.orderedSongsSize <= 1, only one song in the list")
            // only one file in the play list
            mPresentView.showToastNoPrevious()
            return
        }
        var currentIndex = playingParam.currentSongIndex
        val repeatStatus = playingParam.repeatStatus
        // because in startAutoPlay(), the next song will be current index + 1
        val lastPreviousIndex = currentIndex - 2
        when (repeatStatus) {
            MyPlayerConstants.NoRepeatPlaying, MyPlayerConstants.RepeatOneSong -> {
                if (currentIndex <= 0) {
                    LogUtil.d(TAG, "playPreviousSong.currentIndex <= 0, current is the first one.")
                    // no more previous
                    mPresentView.showToastNoPrevious()
                    return
                }
                // because in startAutoPlay(), the next song will be current index + 1
                currentIndex = lastPreviousIndex
            }

            MyPlayerConstants.RepeatAllSongs -> currentIndex = if (currentIndex <= 0) {
                // is going to play the last one
                orderedSongsSize - 2 // the last one
            } else {
                // because in startAutoPlay(), the next song will be current index + 1
                lastPreviousIndex
            }
        }
        playingParam.currentSongIndex = currentIndex
        if (playingParam.currentPlaybackState == PlaybackStateCompat.STATE_PLAYING
            || playingParam.currentPlaybackState == PlaybackStateCompat.STATE_PAUSED) {
            stopPlay(MyPlayerConstants.FINISHED_BY_PROGRAM)
        } else {
            startAutoPlay(false)
        }
    }

    fun playNextSong() {
        LogUtil.d(TAG, "playNextSong")
        val orderedSongsSize = MySingleton.orderedSongs.size
        LogUtil.d(TAG, "playNextSong.orderedSongsSize = $orderedSongsSize")
        val currentIndex = playingParam.currentSongIndex
        val repeatStatus = playingParam.repeatStatus
        if (orderedSongsSize <= 1) {
            // only one file in the play list
            LogUtil.d(TAG, "playNextSong.orderedSongsSize <= 1, only one song in the list")
            // no more next
            mPresentView.showToastNoNext()
            return
        }
        when (repeatStatus) {
            MyPlayerConstants.NoRepeatPlaying, MyPlayerConstants.RepeatOneSong ->
                if (currentIndex >= (orderedSongsSize - 1)) {
                LogUtil.d(TAG, "playPreviousSong.currentIndex >= (orderedSongsSize-1)," +
                            " current is the last one.")
                // no more next
                mPresentView.showToastNoNext()
                return
            }

            MyPlayerConstants.RepeatAllSongs -> {}
        }
        // mPlayingParam.setCurrentSongIndex(currentIndex); no need because it already is
        if (playingParam.currentPlaybackState == PlaybackStateCompat.STATE_PLAYING
            || playingParam.currentPlaybackState == PlaybackStateCompat.STATE_PAUSED) {
            stopPlay(MyPlayerConstants.FINISHED_BY_PROGRAM)
        } else {
            startAutoPlay(false)
        }
    }

    fun playSongPlayedBeforeActivityCreated() {
        val logStr = "playSongPlayedBeforeActivityCreated"
        val playService = getBasePlayService() ?: return
        LogUtil.i(TAG, "$logStr.isPlaySingleSong = ${playingParam.isPlaySingleSong}")
        LogUtil.d(TAG, "$logStr.preparedStatus = ${playingParam.preparedStatus}")
        mPresentView.updateVolumeSeekBarProgress()
        LogUtil.d(TAG, "$logStr.mediaUri = $mediaUri")
        if (mediaUri == null || Uri.EMPTY == mediaUri) {
            // No more playing single song
            /*
            if (playingParam.isPlaySingleSong) {
                // called by SongListActivity
                LogUtil.d(TAG, "$logStr.singleSongInfo = $singleSongInfo")
                if (singleSongInfo != null) {
                    playingParam.isAutoPlay = false
                    // added on 2020-12-08
                    // set orderedSongs that only contains song info from SongListActivity
                    orderedSongs.clear()
                    orderedSongs.add(singleSongInfo!!)
                    singleSongInfo = SongInfo() // reset for cycle playing
                    autoPlaySongList()
                }
            } else {
                playingParam.currentAudioPosition = 0
            }
            */
            playingParam.currentAudioPosition = 0
        } else {
            val playbackState = playingParam.currentPlaybackState
            LogUtil.d(TAG, "$logStr.playbackState = $playbackState")
            if (playbackState != MyPlayerConstants.PREPARE_MEDIA) {
                LogUtil.d(TAG, "$logStr.playService.playMediaFromUri()")
                playService.playMediaFromUri(mediaUri, playingParam)
            }
        }
        val currentPosition = playingParam.currentAudioPosition.toFloat()
        LogUtil.d(TAG, "$logStr.currentPosition = $currentPosition")
        mPresentView.onDurationSeekBarProgressChanged(currentPosition.toInt(), true)
        mPresentView.updatePlayerDurationSeekbarProgress(currentPosition.toInt())
    }

    fun setRepeatSongStatus() {
        LogUtil.d(TAG, "setRepeatSongStatus")
        val repeatStatus = playingParam.repeatStatus
        LogUtil.d(TAG, "setRepeatSongStatus.repeatStatus = $repeatStatus")
        when (repeatStatus) {
            MyPlayerConstants.NoRepeatPlaying ->                 // switch to repeat one song
                playingParam.repeatStatus = MyPlayerConstants.RepeatOneSong

            MyPlayerConstants.RepeatOneSong ->                 // switch to repeat song list
                playingParam.repeatStatus = MyPlayerConstants.RepeatAllSongs

            MyPlayerConstants.RepeatAllSongs ->                 // switch to no repeat
                playingParam.repeatStatus = MyPlayerConstants.NoRepeatPlaying
        }
        mPresentView.setImageButtonStatus()
    }

    fun startPlay() {
        LogUtil.i(TAG, "startPlay")
        val playService = getBasePlayService() ?: return
        LogUtil.d(TAG, "startPlay.playService.startPlay() ")
        playService.startPlay(this)
    }

    fun pausePlay() {
        LogUtil.i(TAG, "pausePlay")
        val playService = getBasePlayService() ?: return
        LogUtil.d(TAG, "pausePlay.playService.pausePlay() ")
        playService.pausePlay()
    }

    fun stopPlay(finishState: Int) {
        LogUtil.i(TAG, "stopPlay")
        val playService = getBasePlayService() ?: return
        LogUtil.i(TAG, "stopPlay.finishState = $finishState")
        val state = when (finishState) {
            MyPlayerConstants.FINISHED_NORMALLY -> {
                "FINISHED_NORMALLY"
            }
            MyPlayerConstants.STOPPED_BY_USER -> {
                "STOPPED_BY_USER"
            }
            else -> {
                // finishState == PlayerConstants.FINISHED_BY_PROGRAM
                "FINISHED_BY_PROGRAM"
            }
        }
        LogUtil.d(TAG, "stopPlay.finishState String = $state")
        LogUtil.d(TAG, "stopPlay.playService.stopPlay()")
        playingParam.finishState = finishState
        playService.stopPlay()
    }

    fun replayMedia() {
        LogUtil.i(TAG, "replayMedia")
        val playService = getBasePlayService() ?: return
        LogUtil.d(TAG, "replayMedia.playService.replayMedia() ")
        playService.replayMedia(this)
    }

    fun updateStatusAndUi(state: PlaybackStateCompat) {
        val msgStr = "updateStatusAndUi"
        LogUtil.d(TAG, msgStr)
        val playService = getBasePlayService() ?: return
        LogUtil.d(TAG, "$msgStr.playingParam.preparedStatus = ${playingParam.preparedStatus}")
        val currentState = state.state
        // update the playback state
        playingParam.currentPlaybackState = currentState
        if (playingParam.isPlaySingleSong && playingParam.singleSongPlayingStatus == 1) {
            LogUtil.d(TAG, "$msgStr.setSingleSongPlayingStatus(2)")
            playingParam.singleSongPlayingStatus = 2 // prepared and playing
        }
        if (currentState == PlaybackStateCompat.STATE_BUFFERING) {
            // Only for ExoPlayer
            LogUtil.d(TAG, "$msgStr.PlaybackStateCompat.STATE_BUFFERING")
            mPresentView.hideNativeAd()
            mPresentView.showBufferingMessage()
            return
        }
        mPresentView.dismissBufferingMessage()
        var playSong = false
        var isSelfFinished = false
        when (currentState) {
            PlaybackStateCompat.STATE_NONE -> {
                // 1. initial state
                // 2. exoPlayer is stopped by user
                // 3. vlcPlayer finished playing (Event.EndReached)
                // 4. vlcPlayer is stopped by user
                LogUtil.d(TAG, "$msgStr.PlaybackStateCompat.STATE_NONE")
                if (playingParam.preparedStatus == 1) {
                    // the first time of STATE_PLAYING means just prepared
                    // or just came back from background
                    setAudioActionSubMenu()
                }
                playingParam.preparedStatus = 0
                mPresentView.onDurationSeekBarProgressChanged(0, true)
                mPresentView.updatePlayerDurationSeekbarProgress(0)
                playingParam.currentAudioPosition = 0
                mPresentView.playButtonOnPauseButtonOff()
                removeMsgFromDurationBarHandler()
            }

            PlaybackStateCompat.STATE_PLAYING -> {
                // when playing
                LogUtil.d(TAG, "$msgStr.PlaybackStateCompat.STATE_PLAYING")
                LogUtil.d(TAG, "$msgStr.PlaybackStateCompat.STATE_PLAYING")
                if (playingParam.preparedStatus == 1) {
                    // the first time of STATE_PLAYING means just prepared
                    // or just came back from background
                    setAudioActionSubMenu()
                }
                playingParam.preparedStatus = 2 // has been prepared and playing
                playingParam.currentPlaybackState = PlaybackStateCompat.STATE_PLAYING
                startDurationBarHandler() // start updating duration seekbar
                // set up a timer for supportToolbar's visibility
                mPresentView.setTimerToHideSupportAudioControl()
                mPresentView.playButtonOffPauseButtonOn()
                mPresentView.hideNativeAd()
            }

            PlaybackStateCompat.STATE_PAUSED -> {
                // when playing is paused
                LogUtil.d(TAG, "$msgStr.PlaybackStateCompat.STATE_PAUSED")
                if (playingParam.preparedStatus == 1) {
                    // the first time of STATE_PLAYING means just prepared
                    // or just came back from background
                    setAudioActionSubMenu()
                }
                // new add, need to be tested more, especially ExoPlayer
                // VlcPlayer has already been tested but keep an eye on it
                playingParam.preparedStatus = 2
                mPresentView.playButtonOnPauseButtonOff()
            }

            PlaybackStateCompat.STATE_STOPPED -> {
                // 1. exoPlayer finished playing
                // 2. after vlcPlayer finished playing
                LogUtil.d(TAG, "$msgStr.PlaybackStateCompat.STATE_STOPPED")
                if (playingParam.preparedStatus == 1) {
                    // the first time of STATE_PLAYING means just prepared
                    // or just came back from background
                    setAudioActionSubMenu()
                }
                playingParam.preparedStatus = 0
                mPresentView.updatePlayerDurationSeekbarProgress(playService.getMediaDuration().toInt())
                playingParam.currentAudioPosition = 0
                mPresentView.playButtonOnPauseButtonOff()
                removeMsgFromDurationBarHandler()
                LogUtil.d(TAG, "$msgStr.playingParam.getFinishState() = ${playingParam.finishState}")
                // not finished by pressing playPreviousSong or PlayNextSong buttons
                isSelfFinished = playingParam.finishState != MyPlayerConstants.FINISHED_BY_PROGRAM
                playSong = true
                // startAutoPlay(isSelfFinished)
            }

            PlaybackStateCompat.STATE_ERROR -> {
                if (mCanShowNotSupportedFormat) {
                    // only show once
                    mCanShowNotSupportedFormat = false
                    mPresentView.showToastNotSupported()
                }
                playingParam.preparedStatus = 0
                mediaUri = null
                // remove the song that is unable to be played
                LogUtil.d(TAG,
                    "$msgStr.STATE_ERROR.orderedSongs.size() = ${MySingleton.orderedSongs.size}")
                val currentIndexOfList = playingParam.currentSongIndex
                LogUtil.d(TAG, "$msgStr.STATE_ERROR.currentIndexOfList = $currentIndexOfList")
                if (currentIndexOfList >= 0) {
                    MySingleton.orderedSongs.removeAt(currentIndexOfList)
                    playingParam.currentSongIndex = currentIndexOfList - 1
                }
                LogUtil.d(TAG, "$msgStr.STATE_ERROR.orderedSongs.size() = ${MySingleton.orderedSongs.size}")
                isSelfFinished = false
                playSong = true
                // startAutoPlay(false)
            }

            else -> LogUtil.d(TAG, "$msgStr.other PlaybackStateCompat")
        }
        val isShown = mPresentView.showNativeAndHideBannerAd()
        if (playSong) {
            if (getActivity() == null || !isShown) {
                startAutoPlay(isSelfFinished)
            } else {
                val act = getActivity()!!
                act.lifecycleScope.launch {
                    LogUtil.d(TAG, "$msgStr.delay(2000)")
                    delay(2000)
                    startAutoPlay(isSelfFinished)
                    // playingParam.finishState = MyPlayerConstants.FINISHED_NORMALLY
                }
            }
        }
        // reset the finish state
        playingParam.finishState = MyPlayerConstants.FINISHED_NORMALLY
    }

    open fun saveInstanceState(outState: Bundle) {
        LogUtil.d(TAG, "saveInstanceState")
        outState.putInt(MyPlayerConstants.NumberOfVideoTracksState, numberOfVideoTracks)
        val orderedSongs = ArrayList<SongInfo?>(MySingleton.orderedSongs)
        outState.putSerializable(MyPlayerConstants.OrderedSongsState, orderedSongs)
        outState.putParcelable(MyPlayerConstants.MediaUriState, mediaUri)
        outState.putParcelable(MyPlayerConstants.PlayingParamState, playingParam)
        outState.putBoolean(
            MyPlayerConstants.CanShowNotSupportedFormatState,
            mCanShowNotSupportedFormat
        )
        outState.putParcelable(MyPlayerConstants.SINGLE_SONG_INFO_STATE, singleSongInfo)
    }
}
