package com.smile.u2bplayer.retrofit

import com.smile.u2bplayer.models.YouTubeVideo
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface U2bPlayerApiInterface {

    @GET("api/U2b/{searchTerm}")
    fun searchVideos(
        @Path("searchTerm") searchTerm: String?
    ): Call<ArrayList<YouTubeVideo>>
}