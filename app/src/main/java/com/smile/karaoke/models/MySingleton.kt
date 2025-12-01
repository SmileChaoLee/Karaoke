package com.smile.karaoke.models

object MySingleton {

    const val MAX_SONGS : Int = 100
    val favorites : ArrayList<SongDescription> = ArrayList(MAX_SONGS)
    val selectedFavorites : ArrayList<SongInfo> = ArrayList(MAX_SONGS)
    val backupSelectedId : ArrayList<Int> = ArrayList(MAX_SONGS)
    val orderedSongs : ArrayList<SongInfo> = ArrayList(MAX_SONGS)
    // moved from FileDesList
    const val MAX_FILES : Int = 500
    val fileList : ArrayList<FileDescription> = ArrayList(MAX_FILES)
    val rootPathSet : java.util.HashSet<String> = HashSet()
    var currentPath = "/"

    fun clearSingleton() {
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