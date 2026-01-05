package com.smile.androidsong.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class LanguageList(
    @SerializedName("pageNo")
    var pageNo: Int = 0,
    @SerializedName("pageSize")
    var pageSize: Int = 0,
    @SerializedName("totalRecords")
    var totalRecords: Int = 0,
    @SerializedName("totalPages")
    var totalPages: Int = 0,
    @SerializedName("languages")
    var languages: ArrayList<Language> = ArrayList()): Parcelable