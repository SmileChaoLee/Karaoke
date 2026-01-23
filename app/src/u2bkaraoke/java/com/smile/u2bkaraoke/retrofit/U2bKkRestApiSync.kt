package com.smile.u2bkaraoke.retrofit

import com.smile.karaoke.utilities.LogUtil
import com.smile.u2bkaraoke.U2bKaraokeApp
import com.smile.u2bkaraoke.u2bkaok_constants.U2bKKConstants
import com.smile.u2bkaraoke.model.Language
import com.smile.u2bkaraoke.model.LanguageList
import com.smile.u2bkaraoke.model.Singer
import com.smile.u2bkaraoke.model.SingerList
import com.smile.u2bkaraoke.model.SingerType
import com.smile.u2bkaraoke.model.SingerTypeList
import com.smile.u2bkaraoke.model.Song
import com.smile.u2bkaraoke.model.SongList
import retrofit2.Retrofit
import javax.inject.Inject

class U2bKkRestApiSync private constructor() {

    companion object {
        private const val TAG = "U2bKkRestApiSync"
        private var instance: U2bKkRestApiSync? = null
        fun getApiSync(): U2bKkRestApiSync {
            if (instance == null) {
                instance = U2bKkRestApiSync()
            }
            return instance!!
        }
    }

    @Inject
    lateinit var retrofit : Retrofit

    // get Retrofit client and Retrofit Api
    private val apiInterface : U2bKkApiInterface
        get() {
            U2bKaraokeApp.appCompBuilder.stringModule(U2bKKConstants.CHAO_URL)
            .build().inject(this)
            return retrofit.create(U2bKkApiInterface::class.java)
            // return Client.getInstance(Constants.CHAO_URL).create(ApiInterface::class.java)
        }

    fun getAllLanguages(): LanguageList? {
        val logStr = "getAllLanguages"
        LogUtil.d(TAG, logStr)
        try {
            // get Call from Retrofit Api
            val response = apiInterface.getAllLanguages().execute()
            LogUtil.d(TAG, "$logStr.response = $response")
            return response.body()
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
            return null
        }
    }

    fun getAllSingerTypes(): SingerTypeList? {
        val logStr = "getAllSingerTypes"
        LogUtil.d(TAG, logStr)
        try {
            // get Call from Retrofit Api
            // val response: Response<SingerTypeList> = apiInterface.getAllSingerTypes().execute()
            val response = apiInterface.getAllSingerTypes().execute()
            LogUtil.d(TAG, "$logStr.response = $response")
            return response.body()
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
            return null
        }
    }

    fun getSongs(pageSize : Int, pageNo : Int): SongList? {
        val logStr = "getSongs"
        LogUtil.d(TAG, logStr)
        // order by (number of words + song's name)
        val orderBy = "NumWordsSongNa"
        try {
            // get Call from Retrofit Api
            val response = apiInterface.getSongs(pageSize, pageNo, orderBy)
                .execute()
            LogUtil.d(TAG, "$logStr.response = $response")
            return response.body()
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
            return null
        }
    }

    fun getSongsBySinger(singer: Singer, pageSize: Int, pageNo: Int): SongList? {
        val logStr = "getSongsBySinger"
        LogUtil.d(TAG, logStr)
        val singerId = singer.id
        val orderBy = "SongNa" // order by song's name
        try {
            val response = apiInterface.getSongsBySingerId(singerId,
                pageSize, pageNo, orderBy).execute()
            LogUtil.d(TAG, "$logStr.response = $response")
            return response.body()
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
            return null
        }
    }

    fun getSongsBySinger(singer: Singer, pageSize: Int, pageNo: Int,
                         filter: String): SongList? {
        val logStr = "getSongsBySinger"
        LogUtil.d(TAG, "$logStr.filter = $filter")
        val singerId = singer.id
        val orderBy = "SongNa" // order by song's name
        try {
            val response = apiInterface.getSongsBySingerIdWithFilter(singerId,
                pageSize, pageNo, orderBy, filter).execute()
            LogUtil.d(TAG, "$logStr.response = $response")
            return response.body()
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
            return null
        }
    }

    fun getNewSongsByLanguage(language: Language, pageSize: Int, pageNo: Int): SongList? {
        val logStr = "getNewSongsByLanguage"
        LogUtil.d(TAG, logStr)
        val languageId = language.id
        try {
            // no order. Only the date that the song came in by descending order
            val response = apiInterface.getNewSongsByLanguageId(languageId,
                pageSize, pageNo).execute()
            LogUtil.d(TAG, "$logStr.response = $response")
            return response.body()
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
            return null
        }
    }

    fun getNewSongsByLanguage(language: Language, pageSize: Int, pageNo: Int,
                              filter: String): SongList? {
        val logStr = "getNewSongsByLanguage"
        LogUtil.d(TAG, "$logStr.filter = $filter")
        val languageId = language.id
        try {
            // no order. Only the date that the song came in by descending order
            val response = apiInterface.getNewSongsByLanguageIdWithFilter(languageId,
                pageSize, pageNo, filter).execute()
            LogUtil.d(TAG, "$logStr.response = $response")
            return response.body()
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
            return null
        }
    }

    fun getHotSongsByLanguage(language: Language, pageSize: Int, pageNo: Int): SongList? {
        val logStr = "getHotSongsByLanguage"
        LogUtil.d(TAG, logStr)
        val languageId = language.id
        try {
            // no order by. Only the number that the song is ordered by descending order
            val response = apiInterface.getHotSongsByLanguageId(languageId,
                pageSize, pageNo).execute()
            LogUtil.d(TAG, "$logStr.response = $response")
            return response.body()
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
            return null
        }
    }

    fun getHotSongsByLanguage(language: Language, pageSize: Int, pageNo: Int,
                              filter: String): SongList? {
        val logStr = "getHotSongsByLanguage"
        LogUtil.d(TAG, "$logStr.filter = $filter")
        val languageId = language.id
        try {
            // no order by. Only the number that the song is ordered by descending order
            val response = apiInterface.getHotSongsByLanguageIdWithFilter(
                languageId, pageSize, pageNo, filter).execute()
            LogUtil.d(TAG, "$logStr.response = $response")
            return response.body()
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
            return null
        }
    }

    fun getSongsByLanguage(language : Language, pageSize : Int, pageNo : Int): SongList? {
        val logStr = "getSongsByLanguage"
        LogUtil.d(TAG, logStr)
        val languageId = language.id
        // order by (number of words + song's name)
        val orderBy = "NumWordsSongNa"
        try {
            // get Call from Retrofit Api
            val response = apiInterface.getSongsByLanguageIdOrderBy(
                languageId, pageSize, pageNo, orderBy).execute()
            LogUtil.d(TAG, "$logStr.response = $response")
            return response.body()
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
            return null
        }
    }

    fun getSongsByLanguage(language : Language, pageSize : Int, pageNo : Int,
                           filter: String): SongList? {
        // filter cannot be empty
        val logStr = "getHotSongsByLanguage"
        LogUtil.d(TAG, "$logStr.filter = $filter")
        val languageId = language.id
        // order by (number of words + song's name)
        val orderBy = "NumWordsSongNa"
        try {
            // get Call from Retrofit Api
            val response = apiInterface.getSongsByLanguageIdOrderByWithFilter(
                languageId,
                pageSize,
                pageNo,
                orderBy,
                filter  // cannot be empty
            ).execute()
            LogUtil.d(TAG, "$logStr.response = $response")
            return response.body()
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
            return null
        }
    }

    fun getSongsByLanguageNumOfWords(language: Language, numOfWords: Int,
                                     pageSize: Int, pageNo: Int): SongList? {
        val logStr = "getSongsByLanguage"
        LogUtil.d(TAG, logStr)
        val languageId = language.id
        val orderBy = "NumWordsSongNa" // order by (number of words + song's name)
        // get Call from Retrofit Api
        try {
            val response = apiInterface.getSongsByLanguageIdNumOfWords(
                languageId,
                numOfWords,
                pageSize,
                pageNo,
                orderBy
            ).execute()
            LogUtil.d(TAG, "$logStr.response = $response")
            return response.body()
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
            return null
        }
    }

    fun getSongsByLanguageNumOfWords(language: Language, numOfWords: Int, pageSize: Int
                                     , pageNo: Int, filter: String): SongList? {
        val logStr = "getSongsByLanguageNumOfWords"
        LogUtil.d(TAG, "$logStr.filter = $filter")
        val languageId = language.id
        val orderBy = "NumWordsSongNa" // order by (number of words + song's name)
        try {
            // get Call from Retrofit Api
            val response = apiInterface.getSongsByLanguageIdNumOfWordsWithFilter(
                languageId,
                numOfWords,
                pageSize,
                pageNo,
                orderBy,
                filter
            ).execute()
            LogUtil.d(TAG, "$logStr.response = $response")
            return response.body()
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
            return null
        }
    }

    fun getSingersBySingerType(singerType: SingerType, pageSize: Int, pageNo: Int): SingerList? {
        val logStr = "getSingersBySingerType"
        LogUtil.d(TAG, logStr)
        val areaId = singerType.id
        val sex = singerType.sex
        val orderBy = "SingNa" // singer's name
        try {
            val response = apiInterface.getSingersBySingerTypeId(
                areaId, sex, pageSize,
                pageNo, orderBy).execute()
            LogUtil.d(TAG, "$logStr.response = $response")
            return response.body()
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
            return null
        }
    }

    fun getSingersBySingerType(singerType: SingerType,
                               pageSize: Int, pageNo: Int,
                               filter: String): SingerList? {
        val logStr = "getSingersBySingerType.filter = $filter"
        LogUtil.d(TAG, logStr)
        val areaId = singerType.id
        val sex = singerType.sex
        val orderBy = "SingNa" // singer's name
        try {
            val response = apiInterface.getSingersBySingerTypeIdWithFilter(
                areaId, sex, pageSize,
                pageNo, orderBy, filter).execute()
            LogUtil.d(TAG, "$logStr.response = $response")
            return response.body()
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
            return null
        }
    }

    fun updateOneSong(id: Int, song: Song): Int? {
        val logStr = "updateOneSong"
        var result: Int? = -1
        try {
            val response = apiInterface.updateOneSong(id, song).execute()
            LogUtil.d(TAG, "$logStr.Successful = ${response.isSuccessful}")
            val code = response.code()
            LogUtil.d(TAG, "$logStr.response.code() = $code")
            if (code == 200) result = response.body()
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
        }
        LogUtil.d(TAG, "$logStr.result = $result")
        return result
    }
}