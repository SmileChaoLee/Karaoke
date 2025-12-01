package com.smile.karaoke.constants

object MyPlayerConstants {
    const val LOG_TAG = "MediaSessionCompatTag"
    const val PlayingParamOrigin = "PlayingParamOrigin"
    const val PlayingParamState = "PlayingParam"
    const val TrackSelectionParametersState = "TrackSelectorParameters"
    const val NumberOfVideoTracksState = "NumberOfVideoTracks"
    const val OrderedSongsState = "OrderedSongList"
    const val MediaUriState = "MediaUri"
    const val CanShowNotSupportedFormatState = "CanShowNotSupportedFormat"
    const val AudioTrackIndicesListState = "AudioTrackIndicesList"
    const val PrivacyPolicyActivityRequestCode = 10
    const val PlayerView_Timeout = 10000 //  10 seconds
    const val NoAudioTrack = 0
    const val NoAudioChannel = 0
    const val MAX_PROGRESS = 100
    const val NoRepeatPlaying = 0 // Player.REPEAT_MODE_OFF
    const val RepeatOneSong = 1 // Player.REPEAT_MODE_ONE
    const val RepeatAllSongs = 2 // Player.REPEAT_MODE_ALL
    const val PREPARE_MEDIA = -10
    const val FINISHED_NORMALLY = 0
    const val STOPPED_BY_USER = 1
    const val FINISHED_BY_PROGRAM = 2

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    const val IS_PLAY_SINGLE_SONG_STATE = "IsPlaySingleSong"
    const val SINGLE_SONG_INFO_STATE = "SingleSongInfo"
    const val IS_AUTOPLAY_STATE = "isAutoPlay"

    const val MyFavoriteListState = "MyFavoriteList"
    const val PlaySingleSongAction = "PlaySingleSongAction"
    const val SingleSongVolume = "SingleSongVolume"
    const val BackToBaseActivity = "BackToBaseActivity"

    const val VLC_PLAYER = 1
    const val EXO_PLAYER = 2
}