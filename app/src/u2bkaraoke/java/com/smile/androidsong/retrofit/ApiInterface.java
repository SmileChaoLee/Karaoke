package com.smile.androidsong.retrofit;

import com.smile.androidsong.model.*;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiInterface {
    //  from ASP.NET Core API

    @GET("api/Singareas/SingerTypes")
    Call<SingerTypeList> getAllSingerTypes();

    @GET("api/Singareas/{id}/Singers/{sex}/{pageSize}/{pageNo}/{orderBy}")
    Call<SingerList> getSingersBySingerTypeId(@Path("id") int id, @Path("sex") String sex, @Path("pageSize") int pageSize, @Path("pageNo") int pageNo, @Path("orderBy") String orderBy);

    @GET("api/Singareas/{id}/Singers/{sex}/{pageSize}/{pageNo}/{orderBy}/{filter}")
    Call<SingerList> getSingersBySingerTypeIdWithFilter(@Path("id") int id, @Path("sex") String sex, @Path("pageSize") int pageSize, @Path("pageNo") int pageNo, @Path("orderBy") String orderBy, @Path("filter") String filter);

    // [HttpGet("{id}/[Action]/{pageSize}/{pageNo}/{orderBy}")] in SingersController.cs
    @GET("api/Singers/{id}/Songs/{pageSize}/{pageNo}/{orderBy}")
    Call<SongList> getSongsBySingerId(@Path("id") int id, @Path("pageSize") int pageSize, @Path("pageNo") int pageNo, @Path("orderBy") String orderBy);

    // [HttpGet("{id}/[Action]/{pageSize}/{pageNo}/{orderBy}/{filter}")]
    @GET("api/Singers/{id}/Songs/{pageSize}/{pageNo}/{orderBy}/{filter}")
    Call<SongList> getSongsBySingerIdWithFilter(@Path("id") int id, @Path("pageSize") int pageSize, @Path("pageNo") int pageNo, @Path("orderBy") String orderBy, @Path("filter") String filter);

    @GET("api/Languages")
    Call<LanguageList> getAllLanguages();

    @GET("api/Languages/{id}/Songs/{pageSize}/{pageNo}/{orderBy}")
    Call<SongList> getSongsByLanguageIdOrderBy(@Path("id") int id, @Path("pageSize") int pageSize, @Path("pageNo") int pageNo, @Path("orderBy") String orderBy);

    @GET("api/Languages/{id}/Songs/{pageSize}/{pageNo}/{orderBy}/{filter}")
    Call<SongList> getSongsByLanguageIdOrderByWithFilter(@Path("id") int id, @Path("pageSize") int pageSize, @Path("pageNo") int pageNo, @Path("orderBy") String orderBy, @Path("filter") String filter);

    @GET("api/Languages/{id}/{numOfWords}/Songs/{pageSize}/{pageNo}/{orderBy}")
    Call<SongList> getSongsByLanguageIdNumOfWords(@Path("id") int id, @Path("numOfWords") int numOfWords, @Path("pageSize") int pageSize, @Path("pageNo") int pageNo, @Path("orderBy") String orderBy);

    @GET("api/Languages/{id}/{numOfWords}/Songs/{pageSize}/{pageNo}/{orderBy}/{filter}")
    Call<SongList> getSongsByLanguageIdNumOfWordsWithFilter(@Path("id") int id, @Path("numOfWords") int numOfWords, @Path("pageSize") int pageSize, @Path("pageNo") int pageNo, @Path("orderBy") String orderBy, @Path("filter") String filter);

    // no order by, Only the date that the song came by descending order
    @GET("api/Languages/{id}/NewSongs/{pageSize}/{pageNo}")
    Call<SongList> getNewSongsByLanguageId(@Path("id") int id, @Path("pageSize") int pageSize, @Path("pageNo") int pageNo);

    // no order by, Only the date that the song came by descending order
    @GET("api/Languages/{id}/NewSongs/{pageSize}/{pageNo}/{filter}")
    Call<SongList> getNewSongsByLanguageIdWithFilter(@Path("id") int id, @Path("pageSize") int pageSize, @Path("pageNo") int pageNo, @Path("filter") String filter);

    // no order by, Only the number that the song is ordered by descending order
    @GET("api/Languages/{id}/HotSongs/{pageSize}/{pageNo}")
    Call<SongList> getHotSongsByLanguageId(@Path("id") int id, @Path("pageSize") int pageSize, @Path("pageNo") int pageNo);

    // no order by, Only the number that the song is ordered by descending order
    @GET("api/Languages/{id}/HotSongs/{pageSize}/{pageNo}/{filter}")
    Call<SongList> getHotSongsByLanguageIdWithFilter(@Path("id") int id, @Path("pageSize") int pageSize, @Path("pageNo") int pageNo, @Path("filter") String filter);
}
