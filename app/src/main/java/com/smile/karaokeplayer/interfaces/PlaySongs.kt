package com.smile.karaokeplayer.interfaces

import com.smile.karaokeplayer.models.SongInfo
import java.util.ArrayList

interface PlaySongs {
    fun playSelectedSongList(songs : ArrayList<SongInfo>)
}