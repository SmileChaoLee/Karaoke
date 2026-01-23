package com.smile.u2bplayer.retrofit

import com.smile.karaoke.utilities.LogUtil
import com.smile.u2bplayer.models.VideoList
import retrofit2.Response

object U2bPyRestApiSync {

    private const val TAG = "U2bPyReApiSync"
    // Android Key: AIzaSyAaBRrr0Ei1lucLN0W5hlLNYnAXQslrKck
    private const val HTTP_OK = 200
    private const val API_KEY = "AIzaSyAaBRrr0Ei1lucLN0W5hlLNYnAXQslrKck"

    private fun getApiInstance(packageName: String): U2bPyApiInterface {
        LogUtil.d(TAG, "getApiInstance")
        return U2bPyRetrofitClient.getRetrofit(packageName).create(U2bPyApiInterface::class.java)
    }

    fun getVideoList(packageName: String, searchTerm: String, maxResult: Int): VideoList {
        val logStr = "getVideoList"
        LogUtil.d(TAG, "$logStr.searchTerm = $searchTerm")
        LogUtil.d(TAG, "$logStr.packageName = $packageName")
        var result = VideoList(items = emptyList())
        try {
            val response: Response<VideoList> = getApiInstance(packageName)
                .searchVideos(
                    API_KEY,
                    "snippet", // part
                    searchTerm, // query (q)
                    "video", // type
                    maxResult // maxResults    // used to be 25
                ).execute()
            LogUtil.d(TAG, "$logStr.Successful = ${response.isSuccessful}")
            val code = response.code()
            LogUtil.d(TAG, "$logStr.response.code() = $code")
            if (code == HTTP_OK) result = response.body() ?: VideoList(items = emptyList())
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
        }
        return result
    }
}

