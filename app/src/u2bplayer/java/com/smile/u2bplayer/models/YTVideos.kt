package com.smile.u2bplayer.models

import com.google.gson.annotations.SerializedName

data class YTVideos(
    @SerializedName("Videos")
    val videos: List<YouTubeVideo>
)