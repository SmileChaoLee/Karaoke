package com.smile.karaoke.utilities

import android.content.Context
import com.smile.karaoke.models.SongInfo
import com.smile.karaoke.models.SongListSQLite
import com.smile.karaoke.roomdatabase.FavSongDatabase

object DatabaseAccessUtil {
    private const val TAG = "DatabaseAccessUtil"

    fun readSavedSongListSQLite(callingContext: Context, isIncluded: Boolean): ArrayList<SongInfo> {
        val playlist: ArrayList<SongInfo>
        val songListSQLite = SongListSQLite(callingContext)
        playlist = songListSQLite.readPlaylist(isIncluded)
        songListSQLite.closeDatabase()
        return playlist
    }

    suspend fun readSavedSongList(callingContext: Context,
                                  dbName: String, isIncluded: Boolean): ArrayList<SongInfo> {
        LogUtil.d(TAG, "readSavedSongList")
        val db = FavSongDatabase.getDatabase(callingContext, dbName)
        val playlist = db.readPlaylist(isIncluded)
        db.close()
        return playlist
    }
}
