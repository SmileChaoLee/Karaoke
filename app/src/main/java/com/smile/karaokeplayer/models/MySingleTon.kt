package com.smile.karaokeplayer.models

object MySingleTon {
    const val MAX_SONGS : Int = 100;
    val favorites : ArrayList<SongInfo> = ArrayList(MAX_SONGS)
    val selectedFavorites : ArrayList<SongInfo> = ArrayList(MAX_SONGS)
    val orderedSongs : ArrayList<SongInfo> = ArrayList(MAX_SONGS)
}