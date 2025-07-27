package com.smile.karaoke.interfaces

import android.content.ComponentName

interface PlayMyFavorites {
    fun onSavePlayingState(compName : ComponentName?)
    fun restorePlayingState()
}