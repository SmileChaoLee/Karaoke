package com.smile.karaoke.models

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.File

@Parcelize
data class FileDescription(var file: File,
                      var bm: Bitmap?,
                      var selected: Boolean): Parcelable