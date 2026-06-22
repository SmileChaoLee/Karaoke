package com.smile.u2bplayer.retrofit

import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.retrofit.Client
import retrofit2.Retrofit

object U2bPlayerRetrofitClient {

    private const val TAG = "U2bPyRetClient"
    private const val CHAO_URL = "http://137.184.120.171/"
    // private const val CHAO_URL = "http://192.168.0.234:5000/"
    // private const val CHAO_URL = "http://172.28.234.155:5000/"


    fun getRetrofit(): Retrofit {
        val retrofit = Client.getInstance(CHAO_URL)
        LogUtil.d(TAG, "getRetrofit.retrofit = $retrofit")
        return retrofit
    }
}