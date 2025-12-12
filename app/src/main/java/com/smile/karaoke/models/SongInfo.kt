package com.smile.karaoke.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.smile.karaoke.constants.CommonConstants
import com.smile.karaoke.roomdatabase.FavSongDatabase
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = FavSongDatabase.TABLE_NAME)
data class SongInfo(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id", typeAffinity = ColumnInfo.INTEGER)
    var id: Int? = null,

    @ColumnInfo(name = "songName")
    var songName: String = "",

    @ColumnInfo(name = "filePath")
    var filePath: String = "",

    @ColumnInfo(name = "musicTrackNo")
    var musicTrackNo : Int? = 1,

    @ColumnInfo(name = "musicChannel")
    var musicChannel : Int? = CommonConstants.RIGHT_CHANNEL,

    @ColumnInfo(name = "vocalTrackNo")
    var vocalTrackNo : Int? = 1,

    @ColumnInfo(name = "vocalChannel")
    var vocalChannel : Int? = CommonConstants.LEFT_CHANNEL,

    @ColumnInfo(name = "included")
    var included : String = "1",

    @ColumnInfo(name = "bitmapUrl")
    var bitmapUrl: String? = null
): Parcelable