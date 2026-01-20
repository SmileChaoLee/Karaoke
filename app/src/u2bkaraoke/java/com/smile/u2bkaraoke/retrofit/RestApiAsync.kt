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
import com.smile.u2bkaraoke.model.SongList
import retrofit2.Callback
import retrofit2.Retrofit
import javax.inject.Inject

abstract class RestApiAsync<T> : Callback<T> {

    companion object {
        private const val TAG = "RestApiAsync"
    }

    private val callback : Callback<T>
        get() {
            return this
        }

    @Inject
    lateinit var retrofit : Retrofit

    // get Retrofit client and Retrofit Api
    @Suppress("UNCHECKED_CAST")
    private val apiInterface : ApiInterface
        get() {
            U2bKaraokeApp.appCompBuilder.stringModule(U2bKKConstants.CHAO_URL)
            .build().inject(this as RestApiAsync<Any>)
            return retrofit.create(ApiInterface::class.java)
            // return Client.getInstance(Constants.CHAO_URL).create(ApiInterface::class.java)
        }

    @Suppress("UNCHECKED_CAST")
    fun getAllSingerTypes() {
        LogUtil.d(TAG, "getAllSingerTypes")
        // get Call from Retrofit Api
        apiInterface.getAllSingerTypes().enqueue(callback as Callback<SingerTypeList>)
    }

    @Suppress("UNCHECKED_CAST")
    fun getAllLanguages() {
        LogUtil.d(TAG, "getAllLanguages")
        // get Call from Retrofit Api
        apiInterface.getAllLanguages().enqueue(callback as Callback<LanguageList>)
    }

    @Suppress("UNCHECKED_CAST")
    fun getSongsBySinger(singer: Singer, pageSize: Int, pageNo: Int) {
        val singerId = singer.id
        val orderBy = "SongNa" // order by song's name
        apiInterface.getSongsBySingerId(singerId, pageSize, pageNo, orderBy)
            .enqueue(callback as Callback<SongList>)
    }

    @Suppress("UNCHECKED_CAST")
    fun getSongsBySinger(singer: Singer, pageSize: Int, pageNo: Int, filter: String) {
        val singerId = singer.id
        val orderBy = "SongNa" // order by song's name
        apiInterface.getSongsBySingerIdWithFilter(singerId, pageSize, pageNo, orderBy, filter)
            .enqueue(callback as Callback<SongList>)
    }

    @Suppress("UNCHECKED_CAST")
    fun getNewSongsByLanguage(language: Language, pageSize: Int, pageNo: Int) {
        LogUtil.d(TAG, "getNewSongsByLanguage.no filter")
        val languageId = language.id
        // no order. Only the date that the song came in by descending order
        apiInterface.getNewSongsByLanguageId(languageId, pageSize, pageNo)
            .enqueue(callback as Callback<SongList>)
    }

    @Suppress("UNCHECKED_CAST")
    fun getNewSongsByLanguage(language: Language, pageSize: Int, pageNo: Int, filter: String) {
        LogUtil.d(TAG, "getNewSongsByLanguage.filter not empty")
        val languageId = language.id
        // no order. Only the date that the song came in by descending order
        apiInterface.getNewSongsByLanguageIdWithFilter(languageId, pageSize, pageNo, filter)
            .enqueue(callback as Callback<SongList>)
    }

    @Suppress("UNCHECKED_CAST")
    fun getHotSongsByLanguage(language: Language, pageSize: Int, pageNo: Int) {
        LogUtil.d(TAG, "getHotSongsByLanguage.no filter")
        val languageId = language.id
        // no order by. Only the number that the song is ordered by descending order
        apiInterface.getHotSongsByLanguageId(languageId, pageSize, pageNo)
            .enqueue(callback as Callback<SongList>)
    }

    @Suppress("UNCHECKED_CAST")
    fun getHotSongsByLanguage(language: Language, pageSize: Int, pageNo: Int, filter: String) {
        LogUtil.d(TAG, "getHotSongsByLanguage.filter not empty")
        val languageId = language.id
        LogUtil.d(TAG, "getNewSongsByLanguage.filter not empty.languageId = $languageId")
        LogUtil.d(TAG, "getNewSongsByLanguage.filter not empty.pageSize = $pageSize")
        LogUtil.d(TAG, "getNewSongsByLanguage.filter not empty.pageNo = $pageNo")
        LogUtil.d(TAG, "getNewSongsByLanguage.filter not empty.filter = $filter")
        // no order by. Only the number that the song is ordered by descending order
        apiInterface.getHotSongsByLanguageIdWithFilter(languageId, pageSize, pageNo, filter)
            .enqueue(callback as Callback<SongList>)
    }

    @Suppress("UNCHECKED_CAST")
    fun getSongsByLanguage(language : Language, pageSize : Int, pageNo : Int) {
        LogUtil.d(TAG, "getSongsByLanguage.no filter")
        val languageId = language.id
        // order by (number of words + song's name)
        val orderBy = "NumWordsSongNa"
        // get Call from Retrofit Api
        apiInterface.getSongsByLanguageIdOrderBy(languageId, pageSize, pageNo, orderBy)
            .enqueue(callback as Callback<SongList>)
    }

    @Suppress("UNCHECKED_CAST")
    fun getSongsByLanguage(language : Language, pageSize : Int, pageNo : Int, filter: String) {
        // filter cannot be empty
        LogUtil.d(TAG, "getSongsByLanguage.filter not empty")
        val languageId = language.id
        // order by (number of words + song's name)
        val orderBy = "NumWordsSongNa"
        // get Call from Retrofit Api
        apiInterface.getSongsByLanguageIdOrderByWithFilter(
            languageId,
            pageSize,
            pageNo,
            orderBy,
            filter  // cannot be empty
        ).enqueue(callback as Callback<SongList>)
    }

    @Suppress("UNCHECKED_CAST")
    fun getSongsByLanguageNumOfWords(language: Language, numOfWords: Int, pageSize: Int
                                       , pageNo: Int) {
        LogUtil.d(TAG, "getSongsByLanguageNumOfWords.no filter")
        val languageId = language.id
        val orderBy = "NumWordsSongNa" // order by (number of words + song's name)
        // get Call from Retrofit Api
        apiInterface.getSongsByLanguageIdNumOfWords(
            languageId,
            numOfWords,
            pageSize,
            pageNo,
            orderBy).enqueue(callback as Callback<SongList>)
    }

    @Suppress("UNCHECKED_CAST")
    fun getSongsByLanguageNumOfWords(language: Language, numOfWords: Int, pageSize: Int
                                     , pageNo: Int, filter: String) {
        LogUtil.d(TAG, "getSongsByLanguageNumOfWords.filter not empty")
        val languageId = language.id
        val orderBy = "NumWordsSongNa" // order by (number of words + song's name)
        // get Call from Retrofit Api
        apiInterface.getSongsByLanguageIdNumOfWordsWithFilter(
            languageId,
            numOfWords,
            pageSize,
            pageNo,
            orderBy,
            filter).enqueue(callback as Callback<SongList>)
    }

    @Suppress("UNCHECKED_CAST")
    fun getSingersBySingerType(singerType: SingerType, pageSize: Int, pageNo: Int) {
        LogUtil.d(TAG, "getSingersBySingerType.no filter")
        val areaId = singerType.id
        val sex = singerType.sex
        val orderBy = "SingNa" // singer's name
        apiInterface.getSingersBySingerTypeId(areaId, sex, pageSize,
            pageNo, orderBy).enqueue(callback as Callback<SingerList>)
    }

    @Suppress("UNCHECKED_CAST")
    fun getSingersBySingerType(singerType: SingerType, pageSize: Int, pageNo: Int, filter: String) {
        LogUtil.d(TAG, "getSingersBySingerType.filter not empty")
        val areaId = singerType.id
        val sex = singerType.sex
        val orderBy = "SingNa" // singer's name
        apiInterface.getSingersBySingerTypeIdWithFilter(areaId, sex, pageSize,
            pageNo, orderBy, filter).enqueue(callback as Callback<SingerList>)
    }
}