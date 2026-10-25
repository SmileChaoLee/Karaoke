package com.smile.u2bplayer.retrofit

import com.smile.u2bplayer.models.YTVideoList
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface U2bPlayerApiInterface {

    @GET("api/U2b/{searchTerm}/{pageToken}")
    fun searchVideos(
        @Path("searchTerm") searchTerm: String,
        @Path("pageToken") pageToken: String
    ): Call<YTVideoList>
}