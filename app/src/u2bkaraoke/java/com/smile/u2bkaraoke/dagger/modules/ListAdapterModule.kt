package com.smile.u2bkaraoke.dagger.modules

import com.smile.karaoke.interfaces.RecyclerItemListener
import com.smile.u2bkaraoke.model.Language
import com.smile.u2bkaraoke.model.Singer
import com.smile.u2bkaraoke.model.Song
import com.smile.u2bkaraoke.adapters.LangListAdapter
import com.smile.u2bkaraoke.adapters.SingerListAdapter
import com.smile.u2bkaraoke.adapters.SingerAreaListAdapter
import com.smile.u2bkaraoke.adapters.SongListAdapter
import com.smile.u2bkaraoke.adapters.WordListAdapter
import com.smile.u2bkaraoke.model.SingerArea
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
    fun songListAdapterProvider(@Named("RecyclerItemListener")listener : RecyclerItemListener?,
                                @Named("SongArrayList")songs : ArrayList<Song>?,
                                @Named("FloatValue")textFontSize : Float?
    ) : SongListAdapter {
        return SongListAdapter(listener!!, songs!!, textFontSize!!)
    }

    @Provides
    fun singerAreaListAdapterProvider(@Named("RecyclerItemListener")listener : RecyclerItemListener?,
                                      @Named("SingerAreaArrayList")types : ArrayList<SingerArea>?,
                                      @Named("FloatValue")textFontSize : Float?
    ) : SingerAreaListAdapter {
        return SingerAreaListAdapter(listener!!, types!!, textFontSize!!)
    }

    @Provides
    fun singerListAdapterProvider(@Named("RecyclerItemListener")listener : RecyclerItemListener?,
                                  @Named("SingerArrayList")singers : ArrayList<Singer>?,
                                  @Named("FloatValue")textFontSize : Float?
    ) : SingerListAdapter {
        return SingerListAdapter(listener!!, singers!!, textFontSize!!)
    }
}