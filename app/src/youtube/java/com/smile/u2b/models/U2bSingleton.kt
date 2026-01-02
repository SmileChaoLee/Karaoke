package com.smile.u2b.models

import com.smile.karaoke.models.SongDescription

object U2bSingleton {
    const val MAX_SONGS : Int = 100
    val videos : ArrayList<SongDescription> = ArrayList(MAX_SONGS)
}