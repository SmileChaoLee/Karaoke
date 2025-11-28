package youtube.models

import com.google.gson.annotations.SerializedName

/**
 * Contains the URL and dimensions of a specific thumbnail.
 */
data class ThumbnailDetails(
    @SerializedName("url")
    val url: String,
    @SerializedName("width")
    val width: Int,
    @SerializedName("height")
    val height: Int
)