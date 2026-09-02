package com.smile.u2bkaraoke.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class SingerArea(
    @SerializedName("id")
    var id: Int = 0,
    @SerializedName("areaNo")
    var areaNo: String = "",
    @SerializedName("areaNa")
    var areaNa: String = "",
    @SerializedName("areaEn")
    var areaEn: String = "") : Parcelable