package com.smile.youtube.models

import com.google.gson.annotations.SerializedName

/**
 * Represents a single video result item in the list.
 */
data class VideoItem(
    @SerializedName("id")
    val id: VideoId,
    @SerializedName("snippet")
    val snippet: VideoSnippet
)