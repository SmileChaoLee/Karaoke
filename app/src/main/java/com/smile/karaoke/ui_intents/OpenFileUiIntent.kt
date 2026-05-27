package com.smile.karaoke.ui_intents

import android.app.Activity
import com.smile.karaoke.interfaces.PlaySongs
import com.smile.karaoke.models.SongInfo

sealed interface OpenFileUiIntent {
    data class SearchFiles(val activity: Activity?, val content: String) : OpenFileUiIntent
    data class SearchCurrentFolder(
        val activity: Activity?,
        val videoThumbNailsWidth: Int,
        val videoThumbNailsHeight: Int
    ) : OpenFileUiIntent
    data class SongOnClicked(
        val position: Int,
        val activity: Activity?,
        val videoThumbNailsWidth: Int,
        val videoThumbNailsHeight: Int
    ) : OpenFileUiIntent
    data class AddToFavorites(val activity: Activity?) : OpenFileUiIntent
    data class StartPlaySelectedSong(
        val activity: Activity?,
        val playSongs: PlaySongs?,
    ) : OpenFileUiIntent
    object ClearSelectedSongs : OpenFileUiIntent
}