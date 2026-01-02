package com.smile.u2b.models

import com.google.gson.annotations.SerializedName

/**
 * Contains URLs for various thumbnail sizes.
 */
data class Thumbnails(
    @SerializedName("default")
    val default: ThumbnailDetails,
    @SerializedName("medium")
    val medium: ThumbnailDetails,
    @SerializedName("high")
    val high: ThumbnailDetails
)