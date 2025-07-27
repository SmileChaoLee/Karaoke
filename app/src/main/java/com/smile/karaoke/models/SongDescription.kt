package com.smile.karaoke.models

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SongDescription(val song: SongInfo,
                           var bm: Bitmap?): Parcelable