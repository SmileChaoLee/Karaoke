package com.smile.u2bplayer.models

import com.google.gson.annotations.SerializedName

data class YTVideoList(
    @SerializedName("nextPageToken")
    val nextPageToken: String?,
    @SerializedName("ytVideos")
    val ytVideos: List<YouTubeVideo>
)