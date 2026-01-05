package com.smile.androidsong.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Language(
    @SerializedName("id")
    var id: Int = 0,
    @SerializedName("langNo")
    var langNo: String = "",
    @SerializedName("langNa")
    var langNa: String = "",
    @SerializedName("langEn")
    var langEn: String = ""): Parcelable