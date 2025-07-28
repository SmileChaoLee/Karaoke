package com.smile.karaoke.fragments

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.smile.karaoke.R
import com.smile.karaoke.SmileAppBase
import com.smile.karaoke.utilities.MyBannerTool
import com.smile.smilelibraries.show_banner_ads.SetBannerAdView
import com.smile.smilelibraries.utilities.ScreenUtil

class TablayoutFragment : Fragment() {

    private var toastTextSize: Float = 0f
    private val openFragment = OpenFileFragment()
    private val favoriteFragment = MyFavoritesFragment()
    private var bannerLayoutForTab: LinearLayout? = null
    private var myBannerAdView: SetBannerAdView? = null
    private var playTabLayout: TabLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate() is called.")
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView()")
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tablayout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated")
        bannerLayoutForTab = view.findViewById(R.id.bannerLayoutForTab)
        activity?.let {actIt ->
            val defaultTextFontSize = ScreenUtil.getDefaultTextSizeFromTheme(actIt,
                ScreenUtil.FontSize_Pixel_Type, null)
            toastTextSize = 0.7f * ScreenUtil.suitableFontSize(actIt, defaultTextFontSize,
                ScreenUtil.FontSize_Pixel_Type, 0.0f)
            showBannerAd()
        }
        MyBannerTool.setVisible(bannerLayoutForTab, View.GONE)

        playTabLayout = view.findViewById(R.id.fragmentsTabLayout)
        val tabText = arrayOf(getString(R.string.open_files), getString(R.string.my_favorites))
        playTabLayout?.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    when (it.position) {
                        0-> {
                            Log.d(TAG, "OnTabSelectedListener.onTabSelected.position = 0")
                            activity?.supportFragmentManager?.beginTransaction()?.apply {
                                replace(R.id.tablayout_container, openFragment, OPEN_FRAGMENT_TAG)
                                commit()
                            }
                        }
                        1-> {
                            Log.d(TAG, "OnTabSelectedListener.onTabSelected.position = 1")
                            activity?.supportFragmentManager?.beginTransaction()?.apply {
                                replace(R.id.tablayout_container, favoriteFragment, FAVORITE_FRAGMENT_TAG)
                                commit()
                            }
                        }
                        else->{
                            Log.d(TAG, "OnTabSelectedListener.onTabSelected.others")
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                Log.d(TAG, "OnTabSelectedListener.onTabUnselected")
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                Log.d(TAG, "OnTabSelectedListener.onTabReselected")
            }

        })

        playTabLayout?.let {
            val openTab = it.newTab()
            openTab.text = tabText[0]
            it.addTab(openTab, true)
            val favoriteTab = it.newTab()
            favoriteTab.text = tabText[1]
            it.addTab(favoriteTab)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        Log.d(TAG, "onConfigurationChanged()")
        super.onConfigurationChanged(newConfig)
        showBannerAd()
        MyBannerTool.setVisible(bannerLayoutForTab, View.GONE)
    }

    override fun onStart() {
        Log.d(TAG, "onStart")
        super.onStart()
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()
        myBannerAdView?.resume()
        MyBannerTool.setVisible(bannerLayoutForTab, View.GONE)
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
        myBannerAdView?.pause()
        bannerLayoutForTab?.visibility = View.GONE
    }

    override fun onStop() {
        Log.d(TAG, "onStop")
        super.onStop()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        myBannerAdView?.destroy()
        super.onDestroy()
    }

    fun becomeVisible() {
        Log.d(TAG, "becomeVisible")
        playTabLayout?.let {
            Log.d(TAG, "becomeVisible.selectedTabPosition = ${it.selectedTabPosition}")
            if (it.selectedTabPosition==0) openFragment.searchCurrentFolder()
            else favoriteFragment.searchFavorites()
        }
    }

    fun becomeInVisible() {
        Log.d(TAG, "becomeInVisible")
        openFragment.clearFileList()
        favoriteFragment.clearFavoriteList()
    }

    private fun showBannerAd() {
        Log.d(TAG, "showBannerAd")
        activity?.let { actIt ->
            myBannerAdView?.destroy()
            myBannerAdView = (actIt.application as SmileAppBase)
                .showBannerAd(actIt, bannerLayoutForTab)
            myBannerAdView?.showBannerAdView(0) // AdMob first
        }
    }

    companion object {
        private const val TAG : String = "TablayoutFragment"
        private const val OPEN_FRAGMENT_TAG : String = "OPEN_FILES"
        private const val FAVORITE_FRAGMENT_TAG : String = "MY_FAVORITES"
    }
}