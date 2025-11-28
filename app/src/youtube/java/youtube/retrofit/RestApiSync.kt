package youtube.retrofit

import com.smile.karaoke.utilities.LogUtil
import retrofit2.Response
import youtube.models.VideoList

object RestApiSync {

    private const val TAG = "RestApiSync"
    // Android Key: AIzaSyAaBRrr0Ei1lucLN0W5hlLNYnAXQslrKck
    private const val API_KEY = "AIzaSyAaBRrr0Ei1lucLN0W5hlLNYnAXQslrKck"

    private fun getApiInstance(packageName: String): ApiInterface {
        LogUtil.d(TAG, "getApiInstance")
        return RetrofitClient.getRetrofit(packageName).create(ApiInterface::class.java)
    }

    fun getVideoList(packageName: String, searchTerm: String): VideoList {
        LogUtil.d(TAG, "getVideoList.searchTerm = $searchTerm")
        LogUtil.d(TAG, "getVideoList.packageName = $packageName")
        try {
            val response: Response<VideoList> = getApiInstance(packageName)
                .searchVideos(
                    API_KEY,
                    "snippet", // part
                    searchTerm, // query (q)
                    "video", // type
                    25 // maxResults
                ).execute()
            LogUtil.e(TAG, "getVideoList.response = $response")
            return response.body() ?: VideoList(items = emptyList())
        } catch (ex: Exception) {
            LogUtil.e(TAG, "getVideoList.Exception", ex)
            return VideoList(items = emptyList())
        }
    }
}

