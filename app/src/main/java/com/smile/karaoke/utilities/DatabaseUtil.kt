package com.smile.karaoke.utilities

import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.smile.karaoke.R
import com.smile.karaoke.models.MySingleton
import com.smile.karaoke.models.SongInfo
import com.smile.karaoke.models.SongListSQLite
import com.smile.karaoke.roomdatabase.FavSongDatabase
import com.smile.smilelibraries.utilities.ScreenUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseUtil {
    private const val TAG = "DatabaseUtil"

    fun readSavedSongListSQLite(callingContext: Context, isIncluded: Boolean): ArrayList<SongInfo> {
        val playlist: ArrayList<SongInfo>
        val songListSQLite = SongListSQLite(callingContext)
        playlist = songListSQLite.readPlaylist(isIncluded)
        songListSQLite.closeDatabase()
        return playlist
    }

    suspend fun readSavedFavorites(act: Activity,
                                   dbName: String, isIncluded: Boolean): ArrayList<SongInfo> {
        LogUtil.d(TAG, "readSavedFavorites")
        val db = FavSongDatabase.getDatabase(act, dbName)
        val playlist = db.readPlaylist(isIncluded)
        db.close()
        return playlist
    }

    suspend fun addSongToFavorites(act: Activity, dbName: String,
                                   song: SongInfo): Boolean {
        val db = FavSongDatabase.getDatabase(act,dbName)
        // check if this file is already in database
        var added = false
        if (db.findOneSongByFilepath(song.filePath) == null) {
            song.included = "1"
            db.addSongToSongList(song)
            added = true
        }
        db.close()
        return added
    }

    suspend fun addSongsToFavorites(act: Activity, dbName: String,
                                    songs: ArrayList<SongInfo>,
                                    textFontSize: Float): Boolean {
        val msg = "addSongsToFavorites"
        LogUtil.d(TAG, msg)
        val res = act.resources
        val db = FavSongDatabase.getDatabase(act, dbName)
        var numRecords = db.recordsOfPlayList()
        LogUtil.d(TAG, "$msg.numRecords = $numRecords")
        var added = false
        for (song in songs) {
            if (numRecords < MySingleton.MAX_SONGS) {
                // check if this file is already in database
                if (db.findOneSongByFilepath(song.filePath) == null) {
                    song.included = "1"
                    db.addSongToSongList(song)
                    added = true
                    numRecords++
                    LogUtil.d(TAG, "$msg.numRecords = $numRecords")
                }
            } else {
                LogUtil.d(TAG, "$msg.excess the max")
                // excess max number of favorites
                withContext(Dispatchers.Main) {
                    ScreenUtil.showToast(act,
                        res.getString(R.string.excess_max) + ", ${MySingleton.MAX_SONGS}",
                        textFontSize,Toast.LENGTH_SHORT)
                }
                break
            }
        }
        db.close()
        return added
    }

    suspend fun updateOneSongFromSongList(act: Activity, dbName: String,
                                          song: SongInfo): Int {
        val db = FavSongDatabase.getDatabase(act, dbName)
        val numUpdated = db.updateOneSongFromSongList(song)
        db.close()
        return numUpdated
    }

    suspend fun deleteOneSongFromSongList(act: Activity, dbName: String,
                                          song: SongInfo): Int {
        val db = FavSongDatabase.getDatabase(act, dbName)
        val numDeleted = db.deleteOneSongFromSongList(song)
        db.close()
        return numDeleted
    }

    suspend fun getSongsToPlay(act: Activity, dbName: String
                               ,songs: ArrayList<SongInfo>) {
        val msg = "getSongsToPlay"
        LogUtil.d(TAG, msg)
        val db = FavSongDatabase.getDatabase(act, dbName)
        for (i in 0 until songs.size) {
            db.findOneSongByFilepath(songs[i].filePath)?.apply {
                LogUtil.d(TAG, "$msg.found")
                songs[i].included = "1"
            }
        }
        db.close()
    }
}
