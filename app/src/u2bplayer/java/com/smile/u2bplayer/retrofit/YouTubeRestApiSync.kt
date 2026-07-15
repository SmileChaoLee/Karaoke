/**
 * This class is used to sync the video list from YouTube.
 * It is only used by com.smile.u2bkktool and not used by other packages
 */
package com.smile.u2bplayer.retrofit

import com.smile.karaoke.utilities.LogUtil
import com.smile.u2bplayer.models.VideoList
import retrofit2.Response

object YouTubeRestApiSync {

    private const val TAG = "YTRestApiSync"
    private const val HTTP_OK = 200

    private fun getApiInstance(packageName: String): YouTubeApiInterface {
        LogUtil.d(TAG, "getApiInstance")
        return YouTubeRetrofitClient.getRetrofit(packageName).create(YouTubeApiInterface::class.java)
    }

    fun getVideoList(packageName: String, searchTerm: String, maxResult: Int,
                     apiKey: String): VideoList {
        val logStr = "getVideoList"
        LogUtil.d(TAG, "$logStr.searchTerm = $searchTerm")
        LogUtil.d(TAG, "$logStr.packageName = $packageName")
        LogUtil.d(TAG, "$logStr.maxResult = $maxResult")
        LogUtil.d(TAG, "$logStr.apiKey = $apiKey")
        var result = VideoList(items = emptyList())
        try {
            val response: Response<VideoList> = getApiInstance(packageName)
                .searchVideos(
                    apiKey,
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

