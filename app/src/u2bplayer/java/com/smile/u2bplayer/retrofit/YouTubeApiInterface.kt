package com.smile.u2bplayer.retrofit

import com.smile.u2bplayer.models.VideoList
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApiInterface {
    /**
     * Searches for videos based on a query term.
     * @param apiKey Your YouTube Data API v3 key
     * @param part Required parameter (e.g., "snippet")
     * @param query The search terms
     * @param type Filter by resource type (e.g., "video")
     * @param maxResults Maximum number of items to return (1-50)
     * @return A Call object for the network request
     */
    @GET("search")
    fun searchVideos(
        @Query("key") apiKey: String?,
        @Query("part") part: String?,
        @Query("q") query: String?,
        @Query("type") type: String?,
        @Query("maxResults") maxResults: Int
    ): Call<VideoList>
}