package com.smile.karaoke.interfaces

import com.smile.karaoke.models.SongInfo

interface PlaySongs {
    fun playSelectedSongList(songs : ArrayList<SongInfo>, isClearNeeded : Boolean = false)
    fun switchToPlayerView()
    fun isSoftDecoderFirst(): Boolean
    fun switchBetweenSoftAndHardDecoder()
    fun showSmileAppsActivity()
    fun returnToPrevious(isSingleSong : Boolean = false)
    fun isThereAnySongPlaying(): Boolean
}