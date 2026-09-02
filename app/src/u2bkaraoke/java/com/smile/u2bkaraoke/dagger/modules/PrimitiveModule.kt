package com.smile.u2bkaraoke.dagger.modules

import android.app.Activity
import androidx.fragment.app.Fragment
import com.smile.karaoke.interfaces.RecyclerItemListener
import com.smile.u2bkaraoke.model.Language
import com.smile.u2bkaraoke.model.Singer
import com.smile.u2bkaraoke.model.SingerArea
import com.smile.u2bkaraoke.model.Song
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class PrimitiveModule {
    @Provides
    @Named("Activity")
    fun activityProvider(@Named("PrimitiveModule")activity : Activity?) : Activity? {
        return activity
    }

    @Provides
    @Named("Fragment")
    fun fragmentProvider(@Named("PrimitiveModule")fragment : Fragment?) : Fragment? {
        return fragment
    }

    @Provides
    @Named("RecyclerItemListener")
    fun recyclerItemListenerProvider(@Named("PrimitiveModule")listener : RecyclerItemListener?)
    : RecyclerItemListener? {
        return listener
    }

    @Provides
    @Named("ArrayList<String>")
    fun arraylistProvider(@Named("PrimitiveModule")list : ArrayList<String>?) : ArrayList<String>? {
        return list
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
    @Named("SingerAreaArrayList")
    fun singerAreaArrayListProvider(@Named("PrimitiveModule")list :
                                    ArrayList<SingerArea>?) : ArrayList<SingerArea>? {
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