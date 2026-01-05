package com.smile.androidsong.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Song(
    @SerializedName("id")
    var id: Int = 0,
    @SerializedName("songNo")
    var songNo: String = "",
    @SerializedName("songNa")
    var songNa: String = "",
    @SerializedName("sNumWord")
    var sNumWord: Int = 0,
    @SerializedName("numFw")
    var numFw: Int = 0,
    @SerializedName("numPw")
    var numPw: String = "0",
    @SerializedName("chor")
    var chor: String = "N",
    @SerializedName("nMpeg")
    var nMpeg: String = "11",
    @SerializedName("mMpeg")
    var mMpeg: String = "12",
    @SerializedName("vodYn")
    var vodYn: String = "Y",
    @SerializedName("vodNo")
    var vodNo: String = "",
    @SerializedName("pathname")
    var pathname: String = "",
    @SerializedName("ordNo")
    var ordNo: Int = 0,
    @SerializedName("orderNum")
    var orderNum: Int = 0,
    @SerializedName("ordOldN")
    var ordOldN: Int = 0,
    @SerializedName("languageId")
    var languageId: Int = 0,
    @SerializedName("languageNo")
    var languageNo: String = "",
    @SerializedName("languageNa")
    var languageNa: String = "",
    @SerializedName("singer1Id")
    var singer1Id: Int = 0,
    @SerializedName("singer1No")
    var singer1No: String = "",
    @SerializedName("singer1Na")
    var singer1Na: String = "",
    @SerializedName("singer2Id")
    var singer2Id: Int = 0,
    @SerializedName("singer2No")
    var singer2No: String = "",
    @SerializedName("singer2Na")
    var singer2Na: String = "",
    @SerializedName("inDate")
    var inDate: Date = Date()): Parcelable