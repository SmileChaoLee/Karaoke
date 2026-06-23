package com.smile.u2bplayer.retrofit

import com.smile.karaoke.utilities.LogUtil
import com.smile.u2bplayer.models.YouTubeVideo
import retrofit2.Call
import retrofit2.Response

object U2bPlayerRestApiSync {

    private const val TAG = "U2bPyRestApiSync"
    private const val HTTP_OK = 200

    private fun getApiInstance(): U2bPlayerApiInterface {
        LogUtil.d(TAG, "getApiInstance")
        return U2bPlayerRetrofitClient.getRetrofit().create(U2bPlayerApiInterface::class.java)
    }

    fun getVideos(searchTerm: String):  ArrayList<YouTubeVideo> {
        val logStr = "getVideos"
        LogUtil.d(TAG, "$logStr.searchTerm = $searchTerm")
        var result = ArrayList<YouTubeVideo>()
        try {
            val response: Response<ArrayList<YouTubeVideo>> = getApiInstance()
                .searchVideos(searchTerm).execute()
            LogUtil.d(TAG, "$logStr.Successful = ${response.isSuccessful}")
            val code = response.code()
            LogUtil.d(TAG, "$logStr.response.code() = $code")
            if (code == HTTP_OK) {
                result = response.body() ?: ArrayList()
            }
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
        }
        return result
    }
}

