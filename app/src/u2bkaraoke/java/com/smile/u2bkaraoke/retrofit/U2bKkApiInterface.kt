package com.smile.u2bkaraoke.retrofit

import com.smile.u2bkaraoke.model.LanguageList
import com.smile.u2bkaraoke.model.SingerAreaList
import com.smile.u2bkaraoke.model.SingerList
import com.smile.u2bkaraoke.model.SingerTypeList
import com.smile.u2bkaraoke.model.Song
import com.smile.u2bkaraoke.model.SongList
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface U2bKkApiInterface {
    @GET("api/Languages")
    fun getAllLanguages(): Call<LanguageList>

    @GET("api/Singareas")
    fun getAllSingerAreas(): Call<SingerAreaList>

    @GET("api/Singareas/SingerTypes")
    fun getAllSingerTypes(): Call<SingerTypeList>

    @GET("api/Singareas/{id}/Singers/{sex}/{pageSize}/{pageNo}/{orderBy}")
    fun getSingersBySingerTypeId(
        @Path("id") id: Int,
        @Path("sex") sex: String?,
        @Path("pageSize") pageSize: Int,
        @Path("pageNo") pageNo: Int,
        @Path("orderBy") orderBy: String?
    ): Call<SingerList>

    @GET("api/Singareas/{id}/Singers/{sex}/{pageSize}/{pageNo}/{orderBy}/{filter}")
    fun getSingersBySingerTypeIdWithFilter(
        @Path("id") id: Int,
        @Path("sex") sex: String?,
        @Path("pageSize") pageSize: Int,
        @Path("pageNo") pageNo: Int,
        @Path("orderBy") orderBy: String?,
        @Path("filter") filter: String?
    ): Call<SingerList>

    @GET("api/Songs/{pageSize}/{pageNo}/{orderBy}")
    fun getSongs(
        @Path("pageSize") pageSize: Int,
        @Path("pageNo") pageNo: Int,
        @Path("orderBy") orderBy: String?
    ): Call<SongList>

    @GET("api/Songs/{pageSize}/{pageNo}/{orderBy}/{numWords}")
    fun getSongsByNumWords(
        @Path("pageSize") pageSize: Int,
        @Path("pageNo") pageNo: Int,
        @Path("orderBy") orderBy: String?,
        @Path("numWords") numWords: String?
    ): Call<SongList>

    @GET("api/Songs/{pageSize}/{pageNo}/{orderBy}/{useFilter}/{filter}")
    fun getSongsWithFilter(
        @Path("pageSize") pageSize: Int,
        @Path("pageNo") pageNo: Int,
        @Path("orderBy") orderBy: String?,
        @Path("useFilter") useFilter: Boolean,
        @Path("filter") filter: String?
    ): Call<SongList>

    // [HttpGet("{id}/[Action]/{pageSize}/{pageNo}/{orderBy}")] in SingersController.cs
    @GET("api/Singers/{id}/Songs/{pageSize}/{pageNo}/{orderBy}")
    fun getSongsBySingerId(
        @Path("id") id: Int,
        @Path("pageSize") pageSize: Int,
        @Path("pageNo") pageNo: Int,
        @Path("orderBy") orderBy: String?
    ): Call<SongList>

    // [HttpGet("{id}/[Action]/{pageSize}/{pageNo}/{orderBy}/{filter}")]
    @GET("api/Singers/{id}/Songs/{pageSize}/{pageNo}/{orderBy}/{filter}")
    fun getSongsBySingerIdWithFilter(
        @Path("id") id: Int,
        @Path("pageSize") pageSize: Int,
        @Path("pageNo") pageNo: Int,
        @Path("orderBy") orderBy: String?,
        @Path("filter") filter: String?
    ): Call<SongList>

    @GET("api/Languages/{id}/Songs/{pageSize}/{pageNo}/{orderBy}")
    fun getSongsByLanguageIdOrderBy(
        @Path("id") id: Int,
        @Path("pageSize") pageSize: Int,
        @Path("pageNo") pageNo: Int,
        @Path("orderBy") orderBy: String?
    ): Call<SongList>

    @GET("api/Languages/{id}/Songs/{pageSize}/{pageNo}/{orderBy}/{filter}")
    fun getSongsByLanguageIdOrderByWithFilter(
        @Path("id") id: Int,
        @Path("pageSize") pageSize: Int,
        @Path("pageNo") pageNo: Int,
        @Path("orderBy") orderBy: String?,
        @Path("filter") filter: String?
    ): Call<SongList>

    @GET("api/Languages/{id}/{numOfWords}/Songs/{pageSize}/{pageNo}/{orderBy}")
    fun getSongsByLanguageIdNumOfWords(
        @Path("id") id: Int,
        @Path("numOfWords") numOfWords: Int,
        @Path("pageSize") pageSize: Int,
        @Path("pageNo") pageNo: Int,
        @Path("orderBy") orderBy: String?
    ): Call<SongList>

    @GET("api/Languages/{id}/{numOfWords}/Songs/{pageSize}/{pageNo}/{orderBy}/{filter}")
    fun getSongsByLanguageIdNumOfWordsWithFilter(
        @Path("id") id: Int,
        @Path("numOfWords") numOfWords: Int,
        @Path("pageSize") pageSize: Int,
        @Path("pageNo") pageNo: Int,
        @Path("orderBy") orderBy: String?,
        @Path("filter") filter: String?
    ): Call<SongList>

    // no order by, Only the date that the song came by descending order
    @GET("api/Languages/{id}/NewSongs/{pageSize}/{pageNo}")
    fun getNewSongsByLanguageId(
        @Path("id") id: Int,
        @Path("pageSize") pageSize: Int,
        @Path("pageNo") pageNo: Int
    ): Call<SongList>

    // no order by, Only the date that the song came by descending order
    @GET("api/Languages/{id}/NewSongs/{pageSize}/{pageNo}/{filter}")
    fun getNewSongsByLanguageIdWithFilter(
        @Path("id") id: Int,
        @Path("pageSize") pageSize: Int,
        @Path("pageNo") pageNo: Int,
        @Path("filter") filter: String?
    ): Call<SongList>

    // no order by, Only the number that the song is ordered by descending order
    @GET("api/Languages/{id}/HotSongs/{pageSize}/{pageNo}")
    fun getHotSongsByLanguageId(
        @Path("id") id: Int,
        @Path("pageSize") pageSize: Int,
        @Path("pageNo") pageNo: Int
    ): Call<SongList>

    // no order by, Only the number that the song is ordered by descending order
    @GET("api/Languages/{id}/HotSongs/{pageSize}/{pageNo}/{filter}")
    fun getHotSongsByLanguageIdWithFilter(
        @Path("id") id: Int,
        @Path("pageSize") pageSize: Int,
        @Path("pageNo") pageNo: Int,
        @Path("filter") filter: String?
    ): Call<SongList>

    @PUT("api/songs/{id}")
    fun updateOneSong(@Path("id") id: Int, @Body song: Song): Call<Int>
}
