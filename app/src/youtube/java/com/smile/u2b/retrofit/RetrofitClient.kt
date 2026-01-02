package com.smile.u2b.retrofit

import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val TAG = "RetrofitClient"
    private const val YOUTUBE_URL = "https://www.googleapis.com/youtube/v3/"

    fun getRetrofit(packageName: String): Retrofit {
        Log.d(TAG, "getInstance.url = $YOUTUBE_URL")

        val debugSHA1 = "BC6C274D1114EAFB5DA81CBF038A066000A084F6"
        val releaseSHA1 = "942646C07D3B482A1C7EB55A1FAFE900CDDFC3A9"
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS // Logs only headers, not the body
        }

        // Create the OkHttpClient instance
        val okClient: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest: Request = chain.request()
                // Build the new request with the required header
                val newRequest: Request = originalRequest.newBuilder()
                    // This is the essential header for restricted Android keys
                    .header("X-Android-Package", packageName)
                    // .header("X-Android-Cert", debugSHA1)
                    .header("X-Android-Cert", releaseSHA1)
                    // Note: The X-Android-Cert header is typically handled automatically
                    // by the underlying Google Play Services SDK or handled implicitly
                    // by Google's servers based on the SSL certificate handshake.
                    // The Package header is the one you usually need to manually add.
                    .build()

                // Proceed with the new request
                chain.proceed(newRequest)
            }
            .addInterceptor(loggingInterceptor)
            .connectTimeout(1, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()

        val gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create()
        val retrofit = Retrofit.Builder()
            .baseUrl(YOUTUBE_URL)
            .client(okClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        Log.d(TAG, "getRetrofit.retrofit = $retrofit")
        return retrofit
    }
}