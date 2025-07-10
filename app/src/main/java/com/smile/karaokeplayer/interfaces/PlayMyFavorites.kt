package com.smile.karaokeplayer.interfaces

import android.content.ComponentName

interface PlayMyFavorites {
    fun onSavePlayingState(compName : ComponentName?)
    fun restorePlayingState()
}