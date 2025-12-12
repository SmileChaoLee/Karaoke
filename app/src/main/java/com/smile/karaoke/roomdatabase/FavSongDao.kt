package com.smile.karaoke.roomdatabase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.smile.karaoke.models.SongInfo

@Dao
interface FavSongDao {
    @Insert
    suspend fun addSongToSongList(songInfo: SongInfo): Long

    @Query("SELECT * FROM ${FavSongDatabase.TABLE_NAME}")
    suspend fun getAllSongs(): List<SongInfo>

    @Query("SELECT COUNT(*) FROM ${FavSongDatabase.TABLE_NAME}")
    suspend fun recordsOfPlayList(): Int

    @Query("SELECT * FROM ${FavSongDatabase.TABLE_NAME} WHERE id = :songId")
    suspend fun getSongById(songId: Long): SongInfo?

    @Query("SELECT * FROM ${FavSongDatabase.TABLE_NAME} WHERE included = :included")
    suspend fun readPlaylist(included: String): List<SongInfo>

    @Update
    suspend fun updateOneSongFromSongList(vararg songInfo: SongInfo): Int

    @Query("SELECT * FROM ${FavSongDatabase.TABLE_NAME} WHERE filePath = :filePath")
    suspend fun findOneSongByFilepath(filePath: String): SongInfo?

    @Delete
    suspend fun deleteOneSongFromSongList(songInfo: SongInfo): Int

    @Query("DELETE FROM ${FavSongDatabase.TABLE_NAME}")
    suspend fun deleteAllSongList(): Int
}