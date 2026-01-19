package com.smile.u2bplayer.models

import com.smile.karaoke.models.MySingleton
import com.smile.karaoke.models.SongDescription

object U2bSingleton {
    val videos : ArrayList<SongDescription> = ArrayList(MySingleton.MAX_SONGS)
}