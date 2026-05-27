package com.smile.karaoke.ui_intents

import android.app.Activity
import com.smile.karaoke.interfaces.PlaySongs

sealed interface OpenFileUiIntent {
    data class SearchFiles(val activity: Activity?, val searchStr: String) : OpenFileUiIntent
    data class SearchCurrentFolder(
        val activity: Activity?,
        val videoThumbNailsWidth: Int,
        val videoThumbNailsHeight: Int
    ) : OpenFileUiIntent
    data class SongOnClicked(val position: Int) : OpenFileUiIntent
    data class AddToFavorites(val activity: Activity?) : OpenFileUiIntent
    data class StartPlaySelectedSong(
        val activity: Activity?,
        val playSongs: PlaySongs?,
    ) : OpenFileUiIntent
    object ClearSelectedSongs : OpenFileUiIntent
}