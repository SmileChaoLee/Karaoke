package com.smile.u2bkktool.retrofit

import com.smile.u2bkaraoke.retrofit.U2bKkApiInterface
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.Path

interface U2bKkToolApiInterface : U2bKkApiInterface {
    @DELETE("api/songs/{id}")
    fun deleteOneSong(@Path("id") id: Int): Call<Int>
}
