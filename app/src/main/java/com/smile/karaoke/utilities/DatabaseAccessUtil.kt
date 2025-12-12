package com.smile.karaoke.utilities

import android.content.Context
import com.smile.karaoke.constants.CommonConstants
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

    suspend fun readSavedSongList(callingContext: Context, isIncluded: Boolean): ArrayList<SongInfo> {
        LogUtil.d(TAG, "readSavedSongList")
        val db = FavSongDatabase.getDatabase(callingContext, CommonConstants.FAVORITE_DB_NAME)
        val playlist = db.readPlaylist(isIncluded)
        LogUtil.d(TAG, "readSavedSongList.db.close()")
        db.close()
        return playlist
    }
}
