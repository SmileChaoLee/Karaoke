package com.smile.karaoke.models

import android.os.Parcelable
import com.smile.karaoke.constants.CommonConstants
import com.smile.karaoke.constants.PlayerConstants
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlayingParameters (
        var softDecoderFirst: Boolean,
        var currentPlaybackState: Int,
        var isAutoPlay: Boolean,
        // preparedStatus = 0 --> No media is prepared
        // preparedStatus = 1 --> The  media was just prepared in onPrepareFromUri() in MediaSessionCallback
        // preparedStatus = 2 --> The media is being played, paused, or buffered
        // preparedStatus = 3 --> The app is in the background
        // preparedStatus = 4 --> just comes back from background
        var preparedStatus: Int,
        var isPlaySingleSong: Boolean,
        var isInSongList: Boolean,
        var musicAudioTrackIndex: Int,
        var vocalAudioTrackIndex: Int,
        var musicAudioChannel: Int,
        var vocalAudioChannel: Int,
        var currentAudioTrackIndexPlayed: Int,
        var currentChannelPlayed: Int,
        var currentAudioPosition: Long,
        var currentVolume: Float,
        var currentSongIndex: Int,
        var repeatStatus: Int,
        var isPlayerViewVisible : Boolean,
        var wentToFavorite: Boolean,
        var finishState: Int,
        // the following parameter is only for VlcPlayer
        // singleSongPlayingStatus = 0 --> no single song playing or playing single song closed
        // singleSongPlayingStatus = 1 --> just issued to play single song from BaseActivity.onReceiveFunc()
        // singleSongPlayingStatus = 2 --> single song prepared (will start soon)
        // from VlcMediaSessionCallback.onPrepareFromUri()
        var singleSongPlayingStatus: Int) : Parcelable {
        constructor() : this(false,
                PlayerConstants.PREPARE_MEDIA, false,
                0, false, false,
                1, 1, CommonConstants.LEFT_CHANNEL,
                CommonConstants.RIGHT_CHANNEL, 1, CommonConstants.LEFT_CHANNEL,
                0, 1.0f, -1,
                PlayerConstants.NoRepeatPlaying, true,
                false, PlayerConstants.FINISHED_NORMALLY, 0)
        // finishState = 0 --> playing finishes normally
        // finishState = 1 --> playing stopped by user
        // finishState = 2 --> finished by pressing playPreviousSong or PlayNextSong buttons
        constructor(playParam: PlayingParameters) : this(
                playParam.softDecoderFirst,
                playParam.currentPlaybackState, playParam.isAutoPlay, playParam.preparedStatus,
                playParam.isPlaySingleSong, playParam.isInSongList,
                playParam.musicAudioTrackIndex, playParam.vocalAudioTrackIndex,
                playParam.musicAudioChannel, playParam.vocalAudioChannel,
                playParam.currentAudioTrackIndexPlayed, playParam.currentChannelPlayed,
                playParam.currentAudioPosition, playParam.currentVolume, playParam.currentSongIndex,
                playParam.repeatStatus, playParam.isPlayerViewVisible,
                playParam.wentToFavorite,playParam.finishState,
                playParam.singleSongPlayingStatus)
}
