package com.smile.androidsong.retrofit

import android.util.Log
import com.smile.androidsong.SongApplication
import com.smile.androidsong.model.Constants
import com.smile.androidsong.model.Language
import com.smile.androidsong.model.LanguageList
import com.smile.androidsong.model.Singer
import com.smile.androidsong.model.SingerList
import com.smile.androidsong.model.SingerType
import com.smile.androidsong.model.SingerTypeList
import com.smile.androidsong.model.SongList
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
            SongApplication.appCompBuilder.stringModule(Constants.CHAO_URL)
            .build().inject(this as RestApiAsync<Any>)
            return retrofit.create(ApiInterface::class.java)
            // return Client.getInstance(Constants.CHAO_URL).create(ApiInterface::class.java)
        }

    @Suppress("UNCHECKED_CAST")
    fun getAllSingerTypes() {
        Log.d(TAG, "getAllSingerTypes")
        // get Call from Retrofit Api
        apiInterface.allSingerTypes.enqueue(callback as Callback<SingerTypeList>)
    }

    @Suppress("UNCHECKED_CAST")
    fun getAllLanguages() {
        Log.d(TAG, "getAllLanguages")
        // get Call from Retrofit Api
        apiInterface.allLanguages.enqueue(callback as Callback<LanguageList>)
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
        Log.d(TAG, "getNewSongsByLanguage.no filter")
        val languageId = language.id
        // no order. Only the date that the song came in by descending order
        apiInterface.getNewSongsByLanguageId(languageId, pageSize, pageNo)
            .enqueue(callback as Callback<SongList>)
    }

    @Suppress("UNCHECKED_CAST")
    fun getNewSongsByLanguage(language: Language, pageSize: Int, pageNo: Int, filter: String) {
        Log.d(TAG, "getNewSongsByLanguage.filter not empty")
        val languageId = language.id
        // no order. Only the date that the song came in by descending order
        apiInterface.getNewSongsByLanguageIdWithFilter(languageId, pageSize, pageNo, filter)
            .enqueue(callback as Callback<SongList>)
    }

    @Suppress("UNCHECKED_CAST")
    fun getHotSongsByLanguage(language: Language, pageSize: Int, pageNo: Int) {
        Log.d(TAG, "getHotSongsByLanguage.no filter")
        val languageId = language.id
        // no order by. Only the number that the song is ordered by descending order
        apiInterface.getHotSongsByLanguageId(languageId, pageSize, pageNo)
            .enqueue(callback as Callback<SongList>)
    }

    @Suppress("UNCHECKED_CAST")
    fun getHotSongsByLanguage(language: Language, pageSize: Int, pageNo: Int, filter: String) {
        Log.d(TAG, "getHotSongsByLanguage.filter not empty")
        val languageId = language.id
        Log.d(TAG, "getNewSongsByLanguage.filter not empty.languageId = $languageId")
        Log.d(TAG, "getNewSongsByLanguage.filter not empty.pageSize = $pageSize")
        Log.d(TAG, "getNewSongsByLanguage.filter not empty.pageNo = $pageNo")
        Log.d(TAG, "getNewSongsByLanguage.filter not empty.filter = $filter")
        // no order by. Only the number that the song is ordered by descending order
        apiInterface.getHotSongsByLanguageIdWithFilter(languageId, pageSize, pageNo, filter)
            .enqueue(callback as Callback<SongList>)
    }

    @Suppress("UNCHECKED_CAST")
    fun getSongsByLanguage(language : Language, pageSize : Int, pageNo : Int) {
        Log.d(TAG, "getSongsByLanguage.no filter")
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
        Log.d(TAG, "getSongsByLanguage.filter not empty")
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
        Log.d(TAG, "getSongsByLanguageNumOfWords.no filter")
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
        Log.d(TAG, "getSongsByLanguageNumOfWords.filter not empty")
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
        Log.d(TAG, "getSingersBySingerType.no filter")
        val areaId = singerType.id
        val sex = singerType.sex
        val orderBy = "SingNa" // singer's name
        apiInterface.getSingersBySingerTypeId(areaId, sex, pageSize,
            pageNo, orderBy).enqueue(callback as Callback<SingerList>)
    }

    @Suppress("UNCHECKED_CAST")
    fun getSingersBySingerType(singerType: SingerType, pageSize: Int, pageNo: Int, filter: String) {
        Log.d(TAG, "getSingersBySingerType.filter not empty")
        val areaId = singerType.id
        val sex = singerType.sex
        val orderBy = "SingNa" // singer's name
        apiInterface.getSingersBySingerTypeIdWithFilter(areaId, sex, pageSize,
            pageNo, orderBy, filter).enqueue(callback as Callback<SingerList>)
    }
}