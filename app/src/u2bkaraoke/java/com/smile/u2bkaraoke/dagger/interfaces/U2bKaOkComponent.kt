package com.smile.u2bkaraoke.dagger.interfaces

import android.app.Activity
import androidx.fragment.app.Fragment
import com.smile.karaoke.interfaces.RecyclerItemListener
import com.smile.u2bkaraoke.LanguageListActivity
import com.smile.u2bkaraoke.SingerListActivity
import com.smile.u2bkaraoke.SongListActivity
import com.smile.u2bkaraoke.WordListActivity
import com.smile.u2bkaraoke.retrofit.RestApiAsync
import com.smile.u2bkaraoke.retrofit.RestApiSync
import com.smile.u2bkaraoke.dagger.modules.ListAdapterModule
import com.smile.u2bkaraoke.dagger.modules.PrimitiveModule
import com.smile.u2bkaraoke.dagger.modules.RetrofitModule
import com.smile.u2bkaraoke.fragments.SingerTyListFragment
import com.smile.u2bkaraoke.model.Language
import com.smile.u2bkaraoke.model.Singer
import com.smile.u2bkaraoke.model.SingerType
import com.smile.u2bkaraoke.model.Song
import dagger.BindsInstance
import dagger.Component
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(modules = [PrimitiveModule::class,
    RetrofitModule::class,
    ListAdapterModule::class])
interface U2bKaOkComponent {
    fun inject(client: RestApiAsync<Any>)
    fun inject(client: RestApiSync)
    fun inject(fragment : SingerTyListFragment)
    fun inject(activity : SingerListActivity)
    fun inject(activity : LanguageListActivity)
    fun inject(activity : SongListActivity)
    fun inject(activity: WordListActivity)
    @Component.Builder
    interface  Builder {
        fun build() : U2bKaOkComponent
        @BindsInstance
        fun activityModule(@Named("PrimitiveModule") activity: Activity?) : Builder
        @BindsInstance
        fun fragmentModule(@Named("PrimitiveModule") fragment: Fragment?) : Builder
        @BindsInstance
        fun recyclerItemListenerModule(@Named("PrimitiveModule") listener: RecyclerItemListener?) : Builder
        @BindsInstance
        fun languageArrayListModule(@Named("PrimitiveModule") list :
                                    ArrayList<Language>?) : Builder
        @BindsInstance
        fun songArrayListModule(@Named("PrimitiveModule") list :
                            ArrayList<Song>?) : Builder
        @BindsInstance
        fun singerTypeArrayListModule(@Named("PrimitiveModule") list :
                                ArrayList<SingerType>?) : Builder
        @BindsInstance
        fun singerArrayListModule(@Named("PrimitiveModule") list :
                                      ArrayList<Singer>?) : Builder
        @BindsInstance
        fun intModule(@Named("PrimitiveModule") intValue : Int?) : Builder
        @BindsInstance
        fun floatModule(@Named("PrimitiveModule") floatValue : Float?) : Builder
        @BindsInstance
        fun stringModule(@Named("PrimitiveModule") stringValue : String?) : Builder
    }
}