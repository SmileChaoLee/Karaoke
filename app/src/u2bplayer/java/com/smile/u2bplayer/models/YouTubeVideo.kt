package com.smile.u2bplayer.models

import com.google.gson.annotations.SerializedName

/**
 * Represents a single video result item in the list.
 */
data class YouTubeVideo(
    @SerializedName("Id")
    val id: String,
    @SerializedName("Title")
    val title: String,
    @SerializedName("Thumbnail")
    val thumbnail: String,
    @SerializedName("ChannelTitle")
    val channelTitle: String
)