package com.smile.u2bplayer.models

import com.google.gson.annotations.SerializedName

/**
 * Contains descriptive information about the video.
 */
data class VideoSnippet(
    @SerializedName("publishedAt")
    val publishedAt: String,
    @SerializedName("channelId")
    val channelId: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("thumbnails")
    val thumbnails: Thumbnails,
    @SerializedName("channelTitle")
    val channelTitle: String
)