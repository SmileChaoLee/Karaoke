package com.smile.karaoke.roomdatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.smile.karaoke.models.SongInfo
import com.smile.karaoke.utilities.LogUtil
import kotlin.jvm.java

@Database(entities = [SongInfo::class], version = 5)
abstract class FavSongDatabase : RoomDatabase() {

    abstract fun favSongDao(): FavSongDao

    companion object {
        private const val TAG = "FavSongDatabase"
        const val TABLE_NAME = "songList"
        val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Example: Add a new column to the 'users' table
                LogUtil.i(TAG, "MIGRATION_4_5.ALTER TABLE")
                val sqlString = "ALTER TABLE $TABLE_NAME ADD COLUMN bitmapUrl TEXT"
                db.execSQL(sqlString)
            }
        }
        fun getDatabase(context: Context, databaseName: String): FavSongDatabase {
            LogUtil.i(TAG, "getDatabase.databaseName = $databaseName")
            return synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FavSongDatabase::class.java,
                    databaseName)
                    // IMPORTANT: Add migration strategies here if your schema changes
                    // .addMigrations(MIGRATION_1_2, MIGRATION_2_3) // Example
                    .addMigrations(MIGRATION_4_5)
                    .build()
                instance
            }
        }
    }

    suspend fun addSongToSongList(songInfo: SongInfo): Long {
        LogUtil.i(TAG, "addSongToSongList")
        val tempScore = songInfo.copy(  // use the default value, null for id
            id = null)
        try {
            return favSongDao().addSongToSongList(tempScore)
        } catch (ex: Exception) {
            LogUtil.e(TAG, "addSongToSongList.Exception: ", ex)
            return -1L  // Return -1 to indicate an error
        }
    }

    suspend fun getAllSongs(): ArrayList<SongInfo> {
        LogUtil.i(TAG, "getAllSongs")
        try {
            return ArrayList(favSongDao().getAllSongs())
        } catch (ex: Exception) {
            LogUtil.e(TAG, "getAllSongs.Exception: ", ex)
            return ArrayList()
        }
    }

    suspend fun getSongById(songId: Long): SongInfo? {
        LogUtil.i(TAG, "getSongById")
        try {
            return favSongDao().getSongById(songId)
        } catch (ex: Exception) {
            LogUtil.e(TAG, "getSongById.Exception: ", ex)
            return null
        }
    }

    suspend fun readPlaylist(isIncluded: Boolean): ArrayList<SongInfo> {
        LogUtil.i(TAG, "readPlaylist.isIncluded = $isIncluded")
        var list = ArrayList<SongInfo>()
        try {
            list = if (isIncluded) {
                ArrayList(favSongDao().readPlaylist("1"))
            } else {
                ArrayList(favSongDao().getAllSongs())
            }
        } catch (ex: Exception) {
            LogUtil.e(TAG, "readPlaylist.Exception: ", ex)
        }
        return list
    }

    suspend fun updateOneSongFromSongList(songInfo: SongInfo): Int {
        LogUtil.i(TAG, "updateOneSongFromSongList")
        try {
            return favSongDao().updateOneSongFromSongList(songInfo)
        } catch (ex: Exception) {
            LogUtil.e(TAG, "updateOneSongFromSongList.Exception: ", ex)
            return -1  // Return -1 to indicate an error
        }
    }

    suspend fun findOneSongByFilepath(filePath: String): SongInfo? {
        LogUtil.i(TAG, "findOneSongByFilepath.filePath = $filePath")
        try {
            return favSongDao().findOneSongByFilepath(filePath)
        } catch (ex: Exception) {
            LogUtil.e(TAG, "findOneSongByFilepath.Exception: ", ex)
            return null
        }
    }

    suspend fun deleteOneSongFromSongList(songInfo: SongInfo): Int {
        LogUtil.i(TAG, "deleteOneSongFromSongList")
        try {
            return favSongDao().deleteOneSongFromSongList(songInfo)
        } catch (ex: Exception) {
            LogUtil.e(TAG, "deleteOneSongFromSongList.Exception: ", ex)
            return -1
        }
    }

    suspend fun deleteAllSongList(): Int {
        LogUtil.i(TAG, "deleteAllSongList")
        try {
            return favSongDao().deleteAllSongList()
        } catch (ex: Exception) {
            LogUtil.e(TAG, "deleteAllSongList.Exception: ", ex)
            return -1
        }
    }
}
