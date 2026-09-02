package com.smile.u2bkaraoke.retrofit

import com.smile.karaoke.utilities.LogUtil
import com.smile.u2bkaraoke.U2bKaraokeApp
import com.smile.u2bkaraoke.u2bkaok_constants.U2bKKConstants
import com.smile.u2bkaraoke.model.Language
import com.smile.u2bkaraoke.model.LanguageList
import com.smile.u2bkaraoke.model.Singer
import com.smile.u2bkaraoke.model.SingerArea
import com.smile.u2bkaraoke.model.SingerAreaList
import com.smile.u2bkaraoke.model.SingerList
import com.smile.u2bkaraoke.model.SingerType
import com.smile.u2bkaraoke.model.SingerTypeList
import com.smile.u2bkaraoke.model.Song
import com.smile.u2bkaraoke.model.SongList
import retrofit2.Retrofit
import retrofit2.http.Path
import javax.inject.Inject
import java.util.Date

class U2bKkRestApiSync private constructor() {

    companion object {
        private const val TAG = "U2bKkRestApiSync"
        private const val HTTP_OK = 200
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
        var result:LanguageList? = null
        try {
            // get Call from Retrofit Api
            val response = apiInterface.getAllLanguages().execute()
            LogUtil.d(TAG, "$logStr.Successful = ${response.isSuccessful}")
            val code = response.code()
            LogUtil.d(TAG, "$logStr.response.code() = $code")
            if (code == HTTP_OK) result = response.body()
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
        }
        return removeNullFromLanguage(result)
    }

    fun getSingers(pageSize : Int, pageNo : Int, orderBy: String?, filter: String?): SingerList? {
        val logStr = "getAllLSingers"
        LogUtil.d(TAG, logStr)
        val orderByTmp = orderBy ?: ""
        val filterTmp = filter ?: ""
        var result:SingerList? = null
        try {
            // get Call from Retrofit Api
            val response = apiInterface.getSingers(pageSize, pageNo, orderByTmp, filterTmp).execute()
            LogUtil.d(TAG, "$logStr.Successful = ${response.isSuccessful}")
            val code = response.code()
            LogUtil.d(TAG, "$logStr.response.code() = $code")
            if (code == HTTP_OK) result = response.body()
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
        }
        return removeNullFromSinger(result)
    }

    fun getAllSingerAreas(): SingerAreaList? {
        val logStr = "getAllSingerAreas"
        LogUtil.d(TAG, logStr)
        var result:SingerAreaList? = null
        try {
            // get Call from Retrofit Api
            val response = apiInterface.getAllSingerAreas().execute()
            LogUtil.d(TAG, "$logStr.Successful = ${response.isSuccessful}")
            val code = response.code()
            LogUtil.d(TAG, "$logStr.response.code() = $code")
            if (code == HTTP_OK) result = response.body()
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
        }
        return removeNullFromSingerArea(result)
    }

    fun getAllSingerTypes(): SingerTypeList? {
        val logStr = "getAllSingerTypes"
        LogUtil.d(TAG, logStr)
        var result:SingerTypeList? = null
        try {
            // get Call from Retrofit Api
            // val response: Response<SingerTypeList> = apiInterface.getAllSingerTypes().execute()
            val response = apiInterface.getAllSingerTypes().execute()
            LogUtil.d(TAG, "$logStr.Successful = ${response.isSuccessful}")
            val code = response.code()
            LogUtil.d(TAG, "$logStr.response.code() = $code")
            if (code == HTTP_OK) result = response.body()
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
        }
        return removeNullFromSingerType(result)
    }

    fun getSongs(pageSize : Int, pageNo : Int): SongList? {
        val logStr = "getSongs"
        LogUtil.d(TAG, logStr)
        // order by (number of words + song's name)
        val orderBy = "NumWordsSongNa"
        var result:SongList? = null
        try {
            // get Call from Retrofit Api
            val response = apiInterface.getSongs(pageSize, pageNo, orderBy)
                .execute()
            LogUtil.d(TAG, "$logStr.Successful = ${response.isSuccessful}")
            val code = response.code()
            LogUtil.d(TAG, "$logStr.response.code() = $code")
            if (code == HTTP_OK) result = response.body()
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
        }
        return removeNullFromSong(result)
    }

    fun getSongs(pageSize : Int, pageNo : Int, numWords: String): SongList? {
        val logStr = "getSongs"
        LogUtil.d(TAG, logStr)
        // order by (number of words + song's name)
        val orderBy = "NumWordsSongNa"
        var result:SongList? = null
        try {
            // get Call from Retrofit Api
            val response = apiInterface.getSongsByNumWords(pageSize, pageNo, orderBy, numWords)
                .execute()
            LogUtil.d(TAG, "$logStr.Successful = ${response.isSuccessful}")
            val code = response.code()
            LogUtil.d(TAG, "$logStr.response.code() = $code")
            if (code == HTTP_OK) result = response.body()
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
        }
        return removeNullFromSong(result)
    }

    fun getSongsWithFilter(pageSize : Int, pageNo : Int, filter: String): SongList? {
        val logStr = "getSongs"
        LogUtil.d(TAG, logStr)
        // order by (number of words + song's name)
        val orderBy = "NumWordsSongNa"
        var result:SongList? = null
        try {
            // get Call from Retrofit Api
            val response = apiInterface.getSongsWithFilter(pageSize, pageNo, orderBy, true, filter)
                .execute()
            LogUtil.d(TAG, "$logStr.Successful = ${response.isSuccessful}")
            val code = response.code()
            LogUtil.d(TAG, "$logStr.response.code() = $code")
            if (code == HTTP_OK) result = response.body()
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
        }
        return removeNullFromSong(result)
    }

    fun getSongsBySinger(singer: Singer, pageSize: Int, pageNo: Int): SongList? {
        val logStr = "getSongsBySinger"
        LogUtil.d(TAG, logStr)
        val singerId = singer.id
        val orderBy = "SongNa" // order by song's name
        var result:SongList? = null
        try {
            val response = apiInterface.getSongsBySingerId(singerId,
                pageSize, pageNo, orderBy).execute()
            LogUtil.d(TAG, "$logStr.Successful = ${response.isSuccessful}")
            val code = response.code()
            LogUtil.d(TAG, "$logStr.response.code() = $code")
            if (code == HTTP_OK) result = response.body()
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
        }
        return removeNullFromSong(result)
    }

    fun getSongsBySinger(singer: Singer, pageSize: Int, pageNo: Int,
                         filter: String): SongList? {
        val logStr = "getSongsBySinger"
        LogUtil.d(TAG, "$logStr.filter = $filter")
        val singerId = singer.id
        val orderBy = "SongNa" // order by song's name
        var result:SongList? = null
        try {
            val response = apiInterface.getSongsBySingerIdWithFilter(singerId,
                pageSize, pageNo, orderBy, filter).execute()
            LogUtil.d(TAG, "$logStr.Successful = ${response.isSuccessful}")
            val code = response.code()
            LogUtil.d(TAG, "$logStr.response.code() = $code")
            if (code == HTTP_OK) result = response.body()
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
        }
        return removeNullFromSong(result)
    }

    fun getNewSongsByLanguage(language: Language, pageSize: Int, pageNo: Int): SongList? {
        val logStr = "getNewSongsByLanguage"
        LogUtil.d(TAG, logStr)
        val languageId = language.id
        var result:SongList? = null
        try {
            // no order. Only the date that the song came in by descending order
            val response = apiInterface.getNewSongsByLanguageId(languageId,
                pageSize, pageNo).execute()
            LogUtil.d(TAG, "$logStr.Successful = ${response.isSuccessful}")
            val code = response.code()
            LogUtil.d(TAG, "$logStr.response.code() = $code")
            if (code == HTTP_OK) result = response.body()
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
        }
        return removeNullFromSong(result)
    }

    fun getNewSongsByLanguage(language: Language, pageSize: Int, pageNo: Int,
                              filter: String): SongList? {
        val logStr = "getNewSongsByLanguage"
        LogUtil.d(TAG, "$logStr.filter = $filter")
        val languageId = language.id
        var result:SongList? = null
        try {
            // no order. Only the date that the song came in by descending order
            val response = apiInterface.getNewSongsByLanguageIdWithFilter(languageId,
                pageSize, pageNo, filter).execute()
            LogUtil.d(TAG, "$logStr.Successful = ${response.isSuccessful}")
            val code = response.code()
            LogUtil.d(TAG, "$logStr.response.code() = $code")
            if (code == HTTP_OK) result = response.body()
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
        }
        return removeNullFromSong(result)
    }

    fun getHotSongsByLanguage(language: Language, pageSize: Int, pageNo: Int): SongList? {
        val logStr = "getHotSongsByLanguage"
        LogUtil.d(TAG, logStr)
        val languageId = language.id
        var result:SongList? = null
        try {
            // no order by. Only the number that the song is ordered by descending order
            val response = apiInterface.getHotSongsByLanguageId(languageId,
                pageSize, pageNo).execute()
            LogUtil.d(TAG, "$logStr.Successful = ${response.isSuccessful}")
            val code = response.code()
            LogUtil.d(TAG, "$logStr.response.code() = $code")
            if (code == HTTP_OK) result = response.body()
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
        }
        return removeNullFromSong(result)
    }

    fun getHotSongsByLanguage(language: Language, pageSize: Int, pageNo: Int,
                              filter: String): SongList? {
        val logStr = "getHotSongsByLanguage"
        LogUtil.d(TAG, "$logStr.filter = $filter")
        val languageId = language.id
        var result:SongList? = null
        try {
            // no order by. Only the number that the song is ordered by descending order
            val response = apiInterface.getHotSongsByLanguageIdWithFilter(
                languageId, pageSize, pageNo, filter).execute()
            LogUtil.d(TAG, "$logStr.Successful = ${response.isSuccessful}")
            val code = response.code()
            LogUtil.d(TAG, "$logStr.response.code() = $code")
            if (code == HTTP_OK) result = response.body()
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
        }
        return removeNullFromSong(result)
    }

    fun getSongsByLanguage(language : Language, pageSize : Int, pageNo : Int): SongList? {
        val logStr = "getSongsByLanguage"
        LogUtil.d(TAG, logStr)
        val languageId = language.id
        // order by (number of words + song's name)
        val orderBy = "NumWordsSongNa"
        var result:SongList? = null
        try {
            // get Call from Retrofit Api
            val response = apiInterface.getSongsByLanguageIdOrderBy(
                languageId, pageSize, pageNo, orderBy).execute()
            LogUtil.d(TAG, "$logStr.Successful = ${response.isSuccessful}")
            val code = response.code()
            LogUtil.d(TAG, "$logStr.response.code() = $code")
            if (code == HTTP_OK) result = response.body()
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
        }
        return removeNullFromSong(result)
    }

    fun getSongsByLanguage(language : Language, pageSize : Int, pageNo : Int,
                           filter: String): SongList? {
        // filter cannot be empty
        val logStr = "getHotSongsByLanguage"
        LogUtil.d(TAG, "$logStr.filter = $filter")
        val languageId = language.id
        // order by (number of words + song's name)
        val orderBy = "NumWordsSongNa"
        var result:SongList? = null
        try {
            // get Call from Retrofit Api
            val response = apiInterface.getSongsByLanguageIdOrderByWithFilter(
                languageId,
                pageSize,
                pageNo,
                orderBy,
                filter  // cannot be empty
            ).execute()
            LogUtil.d(TAG, "$logStr.Successful = ${response.isSuccessful}")
            val code = response.code()
            LogUtil.d(TAG, "$logStr.response.code() = $code")
            if (code == HTTP_OK) result = response.body()
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
        }
        return removeNullFromSong(result)
    }

    fun getSongsByLanguageNumOfWords(language: Language, numOfWords: Int,
                                     pageSize: Int, pageNo: Int): SongList? {
        val logStr = "getSongsByLanguage"
        LogUtil.d(TAG, logStr)
        val languageId = language.id
        val orderBy = "NumWordsSongNa" // order by (number of words + song's name)
        // get Call from Retrofit Api
        var result:SongList? = null
        try {
            val response = apiInterface.getSongsByLanguageIdNumOfWords(
                languageId,
                numOfWords,
                pageSize,
                pageNo,
                orderBy
            ).execute()
            LogUtil.d(TAG, "$logStr.Successful = ${response.isSuccessful}")
            val code = response.code()
            LogUtil.d(TAG, "$logStr.response.code() = $code")
            if (code == HTTP_OK) result = response.body()
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
        }
        return removeNullFromSong(result)
    }

    fun getSongsByLanguageNumOfWords(language: Language, numOfWords: Int, pageSize: Int
                                     , pageNo: Int, filter: String): SongList? {
        val logStr = "getSongsByLanguageNumOfWords"
        LogUtil.d(TAG, "$logStr.filter = $filter")
        val languageId = language.id
        val orderBy = "NumWordsSongNa" // order by (number of words + song's name)
        var result:SongList? = null
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
            LogUtil.d(TAG, "$logStr.Successful = ${response.isSuccessful}")
            val code = response.code()
            LogUtil.d(TAG, "$logStr.response.code() = $code")
            if (code == HTTP_OK) result = response.body()
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
        }
        return removeNullFromSong(result)
    }

    fun getSingersBySingerType(singerType: SingerType, pageSize: Int, pageNo: Int): SingerList? {
        val logStr = "getSingersBySingerType"
        LogUtil.d(TAG, logStr)
        val areaId = singerType.id
        val sex = singerType.sex
        val orderBy = "SingNa" // singer's name
        var result:SingerList? = null
        try {
            val response = apiInterface.getSingersBySingerTypeId(
                areaId, sex, pageSize,
                pageNo, orderBy).execute()
            LogUtil.d(TAG, "$logStr.Successful = ${response.isSuccessful}")
            val code = response.code()
            LogUtil.d(TAG, "$logStr.response.code() = $code")
            if (code == HTTP_OK) result = response.body()
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
        }
        return removeNullFromSinger(result)
    }

    fun getSingersBySingerType(singerType: SingerType,
                               pageSize: Int, pageNo: Int,
                               filter: String?): SingerList? {
        val logStr = "getSingersBySingerType.filter = $filter"
        LogUtil.d(TAG, logStr)
        val areaId = singerType.id
        val sex = singerType.sex
        val orderBy = "SingNa" // singer's name
        var result:SingerList? = null
        try {
            val response = apiInterface.getSingersBySingerTypeIdWithFilter(
                areaId, sex, pageSize,
                pageNo, orderBy, filter).execute()
            LogUtil.d(TAG, "$logStr.Successful = ${response.isSuccessful}")
            val code = response.code()
            LogUtil.d(TAG, "$logStr.response.code() = $code")
            if (code == HTTP_OK) result = response.body()
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
        }
        return removeNullFromSinger(result)
    }

    fun updateOneSong(id: Int, song: Song): Int? {
        val logStr = "updateOneSong"
        var result: Int? = -1
        try {
            val response = apiInterface.updateOneSong(id, song).execute()
            LogUtil.d(TAG, "$logStr.Successful = ${response.isSuccessful}")
            val code = response.code()
            LogUtil.d(TAG, "$logStr.response.code() = $code")
            if (code == HTTP_OK) result = response.body()
        } catch (ex: Exception) {
            LogUtil.e(TAG, "$logStr.Exception", ex)
        }
        LogUtil.d(TAG, "$logStr.result = $result")
        return result
    }

    private fun removeNullFromSong(songList: SongList?): SongList? {
        // The data from cloud could have null element
        LogUtil.d(TAG, "removeNullFromSong")
        songList?.let {
            for (song: Song? in it.songs) {
                song?.apply {
                    id = id ?: 0
                    songNo = songNo ?: ""
                    songNa = songNa ?: ""
                    sNumWord = sNumWord ?: 0
                    numFw = numFw ?: 0
                    numPw = numPw ?: "0"
                    chor = chor ?: "N"
                    nMpeg = nMpeg ?: "11"
                    mMpeg = mMpeg ?: "12"
                    vodYn = vodYn ?: "Y"
                    vodNo = vodNo ?: ""
                    pathname = pathname ?: ""
                    ordNo = ordNo ?: 0
                    orderNum = orderNum ?: 0
                    ordOldN = ordOldN ?: 0
                    languageId = languageId ?: 0
                    languageNo = languageNo ?: ""
                    languageNa = languageNa ?: ""
                    singer1Id = singer1Id ?: 0
                    singer1No = singer1No ?: ""
                    singer1Na = singer1Na ?: ""
                    singer2Id = singer2Id ?: 0
                    singer2No = singer2No ?: ""
                    singer2Na = singer2Na ?: ""
                    inDate = inDate ?: Date()
                }
            }
        }
        return songList
    }

    private fun removeNullFromLanguage(languageList: LanguageList?): LanguageList? {
        // The data from cloud could have null element
        LogUtil.d(TAG, "removeNullFromLanguage")
        languageList?.let {
            for (language: Language? in it.languages) {
                language?.apply {
                    id = id ?: 0
                    langNo = langNo ?: ""
                    langNa = langNa ?: ""
                    langEn = langEn ?: ""
                }
            }
        }
        return languageList
    }

    private fun removeNullFromSinger(singerList: SingerList?): SingerList? {
        // The data from cloud could have null element
        LogUtil.d(TAG, "removeNullFromSinger")
        singerList?.let {
            for (singer: Singer? in it.singers) {
                singer?.apply {
                    id = id ?: 0
                    singNo = singNo ?: ""
                    singNa = singNa ?: ""
                    sex = sex ?: ""
                    chor = chor ?: ""
                    hot = hot ?: ""
                    numFw = numFw ?: 0
                    numPw = numPw ?: ""
                    picFile = picFile ?: ""
                }
            }
        }
        return singerList
    }

    private fun removeNullFromSingerArea(singerAreaList: SingerAreaList?): SingerAreaList? {
        // The data from cloud could have null element
        LogUtil.d(TAG, "removeNullFromSingerArea")
        singerAreaList?.let {
            for (singerArea: SingerArea? in it.singerAreas) {
                singerArea?.apply {
                    id = id ?: 0
                    areaNo = areaNo ?: ""
                    areaNa = areaNa ?: ""
                    areaEn = areaEn ?: ""
                }
            }
        }
        return singerAreaList
    }

    private fun removeNullFromSingerType(singerTypeList: SingerTypeList?): SingerTypeList? {
        // The data from cloud could have null element
        LogUtil.d(TAG, "removeNullFromSingerType")
        singerTypeList?.let {
            for (singerType: SingerType? in it.singerTypes) {
                singerType?.apply {
                    id = id ?: 0
                    areaNo = areaNo ?: ""
                    areaNa = areaNa ?: ""
                    areaEn = areaEn ?: ""
                }
            }
        }
        return singerTypeList
    }
}