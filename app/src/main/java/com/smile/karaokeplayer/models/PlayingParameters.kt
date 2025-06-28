package com.smile.karaokeplayer.models

import android.os.Parcelable
import com.smile.karaokeplayer.constants.CommonConstants
import com.smile.karaokeplayer.constants.PlayerConstants
import kotlinx.parcelize.Parcelize

@Parcelize
class PlayingParameters (
        // preparedStatus = 0 --> No media is prepared
        // preparedStatus = 1 --> The  media was just prepared in onPrepareFromUri() in MediaSessionCallback
        // preparedStatus = 2 --> The media is being played, paused, or buffered
        // preparedStatus = 3 --> The app is in the background
        // preparedStatus = 4 --> just comes back from background
        // the following parameter is only for VlcPlayer
        // singleSongPlayingStatus = 0 --> no single song playing or playing single song closed
        // singleSongPlayingStatus = 1 --> just issued to play single song from BaseActivity.onReceiveFunc()
        // singleSongPlayingStatus = 2 --> single song prepared (will start soon)
        // from VlcMediaSessionCallback.onPrepareFromUri()
        //
        var currentPlaybackState: Int, var isAutoPlay: Boolean, var preparedStatus: Int,
        var isPlaySingleSong: Boolean, var isInSongList: Boolean,
        var musicAudioTrackIndex: Int, var vocalAudioTrackIndex: Int,
        var musicAudioChannel: Int, var vocalAudioChannel: Int,
        var currentAudioTrackIndexPlayed: Int, var currentChannelPlayed: Int,
        var currentAudioPosition: Long, var currentVolume: Float, var currentSongIndex: Int,
        var repeatStatus: Int, var isPlayerViewVisible : Boolean,
        var wentToFavorite: Boolean, var finishState: Int, var numPlayed: Int,
        var singleSongPlayingStatus: Int) : Parcelable {
        constructor() : this(PlayerConstants.PREPARE_MEDIA, false,
                0, false, false,
                1, 1, CommonConstants.LeftChannel,
                CommonConstants.RightChannel, 1, CommonConstants.LeftChannel,
                0, 1.0f, -1,
                PlayerConstants.NoRepeatPlaying, true,
                false, PlayerConstants.FINISHED_NORMALLY,
                0, 0)
        // finishState = 0 --> playing finishes normally
        // finishState = 1 --> playing stopped by user
        // finishState = 2 --> finished by pressing playPreviousSong or PlayNextSong buttons
        constructor(playParam: PlayingParameters) : this(
                playParam.currentPlaybackState, playParam.isAutoPlay, playParam.preparedStatus,
                playParam.isPlaySingleSong, playParam.isInSongList,
                playParam.musicAudioTrackIndex, playParam.vocalAudioTrackIndex,
                playParam.musicAudioChannel, playParam.vocalAudioChannel,
                playParam.currentAudioTrackIndexPlayed, playParam.currentChannelPlayed,
                playParam.currentAudioPosition, playParam.currentVolume, playParam.currentSongIndex,
                playParam.repeatStatus, playParam.isPlayerViewVisible,
                playParam.wentToFavorite,playParam.finishState,
                playParam.numPlayed,playParam.singleSongPlayingStatus)
}
