package com.smile.u2b.models

import com.google.gson.annotations.SerializedName

data class VideoList(
    // You can include other fields like kind, etag,
    // nextPageToken, regionCode, pageInfo if needed
    @SerializedName("items")
    val items: List<VideoItem>
)