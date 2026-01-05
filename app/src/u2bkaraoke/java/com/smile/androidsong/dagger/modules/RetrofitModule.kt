package com.smile.androidsong.dagger.modules

import com.smile.smilelibraries.retrofit.Client
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Named

@Module
class RetrofitModule {
    @Provides
    fun retrofitProvider(@Named("StringValue") str: String?) : Retrofit {
        return Client.getInstance(str!!)
    }
}