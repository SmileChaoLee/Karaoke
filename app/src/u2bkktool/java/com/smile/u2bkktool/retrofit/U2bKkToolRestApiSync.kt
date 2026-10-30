package com.smile.u2bkktool.retrofit

import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.retrofit.Client
import com.smile.u2bkaraoke.model.Song
import com.smile.u2bkaraoke.retrofit.U2bKkRestApiSync
import com.smile.u2bkaraoke.u2bkaok_constants.U2bKKConstants

class U2bKkToolRestApiSync private constructor() {

    companion object {
        private const val TAG = "U2bKkToolRestApi"
        private const val HTTP_OK = 200
        private var instance: U2bKkToolRestApiSync? = null

        fun getApiSync(): U2bKkToolRestApiSync {
            if (instance == null) instance = U2bKkToolRestApiSync()
            return instance!!
        }
    }

    private val apiInterface: U2bKkToolApiInterface
        get() = Client.getInstance(U2bKKConstants.CHAO_URL)
            .create(U2bKkToolApiInterface::class.java)

    fun updateOneSong(song: Song): Int? =
        U2bKkRestApiSync.getApiSync().updateOneSong(song.id, song)

    fun deleteOneSong(id: Int): Int? {
        val logStr = "deleteOneSong"
        return try {
            val response = apiInterface.deleteOneSong(id).execute()
            LogUtil.d(TAG, "$logStr.response.code() = ${response.code()}")
            if (response.code() == HTTP_OK) response.body() else null
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
            null
        }
    }
}
