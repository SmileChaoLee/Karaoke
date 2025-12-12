package com.smile.karaoke.models

import android.os.Parcelable
import com.smile.karaoke.constants.CommonConstants
import kotlinx.parcelize.Parcelize

@Parcelize
class SongInfoOriginal constructor(var id : Int, var songName: String?, var filePath: String?,
                                   var musicTrackNo : Int, var musicChannel : Int,
                                   var vocalTrackNo : Int, var vocalChannel : Int,
                                   var included : String?) : Parcelable{
    constructor() : this(0, "", "", 1,
        CommonConstants.RIGHT_CHANNEL, 1, CommonConstants.LEFT_CHANNEL,
        "1")
    constructor(s: SongInfoOriginal) : this(s.id, s.songName, s.filePath, s.musicTrackNo, s.musicChannel,
            s.vocalTrackNo, s.vocalChannel, s.included) {
    }
}