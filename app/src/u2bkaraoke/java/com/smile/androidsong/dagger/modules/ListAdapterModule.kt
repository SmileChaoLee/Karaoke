package com.smile.androidsong.dagger.modules

import android.app.Activity
import com.smile.androidsong.model.Language
import com.smile.androidsong.model.Singer
import com.smile.androidsong.model.SingerType
import com.smile.androidsong.model.Song
import com.smile.androidsong.view_adapter.LanguageListAdapter
import com.smile.androidsong.view_adapter.SingerListAdapter
import com.smile.androidsong.view_adapter.SingerTypeListAdapter
import com.smile.androidsong.view_adapter.SongListAdapter
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class ListAdapterModule {
    @Provides
    fun languageListAdapterProvider(@Named("Activity")activity : Activity?,
                                    @Named("LanguageArrayList")languages : ArrayList<Language>?,
                                    @Named("IntValue")orderedFrom : Int?,
                                    @Named("FloatValue")textFontSize : Float?
    ) : LanguageListAdapter {
        return LanguageListAdapter(activity!!, languages!!, orderedFrom!!, textFontSize!!)
    }

    @Provides
    fun songListAdapterProvider(@Named("Activity")activity : Activity?,
                                @Named("SongArrayList")songs : ArrayList<Song>?,
                                @Named("FloatValue")textFontSize : Float?
    ) : SongListAdapter {
        return SongListAdapter(activity!!, songs!!, textFontSize!!)
    }

    @Provides
    fun singerTypeListAdapterProvider(@Named("Activity")activity : Activity?,
                                @Named("SingerTypeArrayList")types : ArrayList<SingerType>?,
                                @Named("FloatValue")textFontSize : Float?
    ) : SingerTypeListAdapter {
        return SingerTypeListAdapter(activity!!, types!!, textFontSize!!)
    }

    @Provides
    fun singerListAdapterProvider(@Named("Activity")activity : Activity?,
                                  @Named("SingerArrayList")singers : ArrayList<Singer>?,
                                  @Named("FloatValue")textFontSize : Float?
    ) : SingerListAdapter {
        return SingerListAdapter(activity!!, singers!!, textFontSize!!)
    }
}