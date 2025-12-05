package com.smile.youtube.models

import com.smile.karaoke.models.SongDescription

object YouSingleton {
    const val MAX_SONGS : Int = 100
    val videos : ArrayList<SongDescription> = ArrayList(MAX_SONGS)
}