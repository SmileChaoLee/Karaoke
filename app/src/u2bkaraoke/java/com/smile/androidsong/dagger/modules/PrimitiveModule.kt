package com.smile.androidsong.dagger.modules

import android.app.Activity
import com.smile.androidsong.model.Language
import com.smile.androidsong.model.Singer
import com.smile.androidsong.model.SingerType
import com.smile.androidsong.model.Song
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Named

@Module
class PrimitiveModule {
    @Provides
    @Named("Activity")
    fun activityProvider(@Named("PrimitiveModule")activity : Activity?) : Activity? {
        return activity
    }

    @Provides
    @Named("LanguageArrayList")
    fun languageArrayListProvider(@Named("PrimitiveModule")list : ArrayList<Language>?) :
            ArrayList<Language>? {
        return list
    }

    @Provides
    @Named("SongArrayList")
    fun songArrayListProvider(@Named("PrimitiveModule")list : ArrayList<Song>?) :
            ArrayList<Song>? {
        return list
    }

    @Provides
    @Named("SingerTypeArrayList")
    fun singerTypeArrayListProvider(@Named("PrimitiveModule")list :
                                    ArrayList<SingerType>?) : ArrayList<SingerType>? {
        return list
    }

    @Provides
    @Named("SingerArrayList")
    fun singerArrayListProvider(@Named("PrimitiveModule")list :
                                    ArrayList<Singer>?) : ArrayList<Singer>? {
        return list
    }

    @Provides
    @Named("IntValue")
    fun intValueProvider(@Named("PrimitiveModule")intValue : Int?) : Int? {
        return intValue
    }

    @Provides
    @Named("FloatValue")
    fun floatValueProvider(@Named("PrimitiveModule")floatValue : Float?) : Float? {
        return floatValue
    }

    @Provides
    @Named("StringValue")
    fun stringValueProvider(@Named("PrimitiveModule")stringValue : String?) : String? {
        return stringValue
    }
}