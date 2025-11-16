package com.smile.karaoke.models

import android.util.Log

object MySingleTon {
    private const val TAG = "MySingleTon"

    const val MAX_SONGS : Int = 100;
    val favorites : ArrayList<SongDescription> = ArrayList(MAX_SONGS)
    val selectedFavorites : ArrayList<SongInfo> = ArrayList(MAX_SONGS)
    val backupSelectedId : ArrayList<Int> = ArrayList(MAX_SONGS)
    val orderedSongs : ArrayList<SongInfo> = ArrayList(MAX_SONGS)
    // moved from FileDesList
    const val maxFiles : Int = 500;
    val fileList : ArrayList<FileDescription> = ArrayList(maxFiles)
    val rootPathSet : java.util.HashSet<String> = HashSet()
    var currentPath = "/"

    fun clearSingleton() {
        Log.d(TAG, "clearSingleton()")
        favorites.clear()
        selectedFavorites.clear()
        backupSelectedId.clear()
        orderedSongs.clear()
        fileList.clear()
        rootPathSet.clear()
        currentPath = "/"
    }

    fun backupSelectedFavorites() {
        backupSelectedId.clear()
        for (fav in favorites) {
            if (fav.song.included == "1") {
                backupSelectedId.add(fav.song.id)
            }
        }
    }
}