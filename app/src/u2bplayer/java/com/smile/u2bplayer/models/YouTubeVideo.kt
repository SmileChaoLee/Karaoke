package com.smile.u2bplayer.models

import com.google.gson.annotations.SerializedName

/**
 * Represents a single video result item in the list.
 */
data class YouTubeVideo(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("thumbnail")
    val thumbnail: String,
    @SerializedName("channelTitle")
    val channelTitle: String
)