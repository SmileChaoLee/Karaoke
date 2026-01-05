package com.smile.androidsong.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Singer(
    @SerializedName("id")
    var id: Int = 0,
    @SerializedName("singNo")
    var singNo: String = "",
    @SerializedName("singNa")
    var singNa: String = "",
    @SerializedName("sex")
    var sex: String = "",
    @SerializedName("chor")
    var chor: String = "",
    @SerializedName("hot")
    var hot: String = "",
    @SerializedName("numFw")
    var numFw: Int = 0,
    @SerializedName("numPw")
    var numPw: String = "",
    @SerializedName("picFile")
    var picFile: String = ""): Parcelable