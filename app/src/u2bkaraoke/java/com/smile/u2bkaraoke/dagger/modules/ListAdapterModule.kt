package com.smile.u2bkaraoke.dagger.modules

import android.app.Activity
import com.smile.karaoke.interfaces.RecyclerItemListener
import com.smile.u2bkaraoke.model.Language
import com.smile.u2bkaraoke.model.Singer
import com.smile.u2bkaraoke.model.SingerType
import com.smile.u2bkaraoke.model.Song
import com.smile.u2bkaraoke.adapters.LangListAdapter
import com.smile.u2bkaraoke.adapters.SingerListAdapter
import com.smile.u2bkaraoke.adapters.SingerTypeListAdapter
import com.smile.u2bkaraoke.adapters.SongListAdapter
import com.smile.u2bkaraoke.adapters.WordListAdapter
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class ListAdapterModule {
    @Provides
    fun languageListAdapterProvider(@Named("RecyclerItemListener")listener : RecyclerItemListener?,
                                    @Named("LanguageArrayList")languages : ArrayList<Language>?,
                                    @Named("FloatValue")textFontSize : Float?
    ) : LangListAdapter {
        return LangListAdapter(listener!!, languages!!, textFontSize!!)
    }

    @Provides
    fun wordListAdapterAdapterProvider(@Named("RecyclerItemListener")listener : RecyclerItemListener?,
                                    @Named("ArrayList<String>")arraylist : ArrayList<String>?,
                                    @Named("FloatValue")textFontSize : Float?
    ) : WordListAdapter {
        return WordListAdapter(listener!!, arraylist!!, textFontSize!!)
    }

    @Provides
    fun songListAdapterProvider(@Named("Activity")activity : Activity?,
                                @Named("SongArrayList")songs : ArrayList<Song>?,
                                @Named("FloatValue")textFontSize : Float?
    ) : SongListAdapter {
        return SongListAdapter(activity!!, songs!!, textFontSize!!)
    }

    @Provides
    fun singerTypeListAdapterProvider(@Named("RecyclerItemListener")listener : RecyclerItemListener?,
                                @Named("SingerTypeArrayList")types : ArrayList<SingerType>?,
                                @Named("FloatValue")textFontSize : Float?
    ) : SingerTypeListAdapter {
        return SingerTypeListAdapter(listener!!, types!!, textFontSize!!)
    }

    @Provides
    fun singerListAdapterProvider(@Named("Activity")activity : Activity?,
                                  @Named("SingerArrayList")singers : ArrayList<Singer>?,
                                  @Named("FloatValue")textFontSize : Float?
    ) : SingerListAdapter {
        return SingerListAdapter(activity!!, singers!!, textFontSize!!)
    }
}