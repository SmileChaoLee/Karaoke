package com.smile.androidsong.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class SingerType(
    @SerializedName("id")
    var id: Int = 0,
    @SerializedName("areaNo")
    var areaNo: String = "",
    @SerializedName("areaNa")
    var areaNa: String = "",
    @SerializedName("areaEn")
    var areaEn: String = "",
    // 0-> not specified, 1->male, 2->female
    @SerializedName("sex")
    var sex: String = "0" ): Parcelable