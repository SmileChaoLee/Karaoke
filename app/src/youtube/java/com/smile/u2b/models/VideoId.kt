package com.smile.u2b.models

import com.google.gson.annotations.SerializedName

/**
 * Contains the unique ID information for the video.
 */
data class VideoId(
    // The "kind" field will usually be "youtube#video" for search results where type=video
    @SerializedName("kind")
    val kind: String,
    // **This is the ID you need for the android_youtube_player**
    @SerializedName("videoId")
    val videoId: String?
)