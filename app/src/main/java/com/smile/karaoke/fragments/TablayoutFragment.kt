package com.smile.karaoke.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.smile.karaoke.R
import com.smile.karaoke.SmileAppBase
import com.smile.karaoke.utilities.LogUtil
import com.smile.karaoke.utilities.MyBannerTool
import com.smile.smilelibraries.show_banner_ads.SetBannerAdView
import com.smile.smilelibraries.utilities.ScreenUtil

class TablayoutFragment : Fragment() {

    private var toastTextSize: Float = 0f
    private val openFragment = OpenFileFragment()
    private val safPickerFragment = SafPickerFragment()
    private val favoriteFragment = FavoritesFragment()
    private var bannerLayoutForTab: LinearLayout? = null
    private var myBannerAdView: SetBannerAdView? = null
    private var playTabLayout: TabLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
        LogUtil.i(TAG, "onCreate.finished")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        LogUtil.i(TAG, "onCreateView")
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tablayout,
            container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "onViewCreated")
        bannerLayoutForTab = view.findViewById(R.id.bannerLayoutForTab)
        activity?.let {actIt ->
            val textFontSize = ScreenUtil.getPxTextFontSizeNeeded(actIt)
            toastTextSize = textFontSize * 0.7f
            showBannerAd()
        }
        MyBannerTool.setVisible(bannerLayoutForTab, View.GONE)

        playTabLayout = view.findViewById(R.id.fragmentsTabLayout)
        val tabText = arrayOf(getString(R.string.open_files),
            getString(R.string.files_picker),
            getString(R.string.my_favorites))
        playTabLayout?.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    when (it.position) {
                        0-> {
                            LogUtil.d(TAG, "OnTabSelectedListener.onTabSelected.position = 0")
                            activity?.supportFragmentManager?.beginTransaction()?.apply {
                                replace(R.id.tablayout_container, openFragment,
                                    OPEN_FRAGMENT_TAG)
                                commit()
                            }
                        }
                        1-> {
                            LogUtil.d(TAG, "OnTabSelectedListener.onTabSelected.position = 1")
                            activity?.supportFragmentManager?.beginTransaction()?.apply {
                                replace(R.id.tablayout_container, safPickerFragment,
                                    PICKER_FRAGMENT_TAG)
                                commit()
                            }
                        }
                        2-> {
                            LogUtil.d(TAG, "OnTabSelectedListener.onTabSelected.position = 1")
                            activity?.supportFragmentManager?.beginTransaction()?.apply {
                                replace(R.id.tablayout_container, favoriteFragment,
                                    FAVORITE_FRAGMENT_TAG)
                                commit()
                            }
                        }
                        else->{
                            LogUtil.d(TAG, "OnTabSelectedListener.onTabSelected.others")
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                LogUtil.d(TAG, "OnTabSelectedListener.onTabUnselected")
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                LogUtil.d(TAG, "OnTabSelectedListener.onTabReselected")
            }

        })

        playTabLayout?.let {
            val openTab = it.newTab()
            openTab.text = tabText[0]
            it.addTab(openTab, true)
            val pickerTab = it.newTab()
            pickerTab.text = tabText[1]
            it.addTab(pickerTab)
            val favoriteTab = it.newTab()
            favoriteTab.text = tabText[2]
            it.addTab(favoriteTab)
        }

        LogUtil.i(TAG, "onViewCreated.finished")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        LogUtil.i(TAG, "onConfigurationChanged")
        super.onConfigurationChanged(newConfig)
        showBannerAd()
        MyBannerTool.setVisible(bannerLayoutForTab, View.GONE)
    }

    override fun onStart() {
        LogUtil.i(TAG, "onStart")
        super.onStart()
    }

    override fun onResume() {
        LogUtil.i(TAG, "onResume")
        super.onResume()
        myBannerAdView?.resume()
        MyBannerTool.setVisible(bannerLayoutForTab, View.GONE)
    }

    override fun onPause() {
        LogUtil.i(TAG, "onPause")
        super.onPause()
        myBannerAdView?.pause()
        bannerLayoutForTab?.visibility = View.GONE
    }

    override fun onStop() {
        LogUtil.i(TAG, "onStop")
        super.onStop()
    }

    override fun onDestroy() {
        LogUtil.i(TAG, "onDestroy")
        myBannerAdView?.destroy()
        super.onDestroy()
    }

    fun becomeVisible() {
        LogUtil.i(TAG, "becomeVisible")
        playTabLayout?.let {
            LogUtil.d(TAG, "becomeVisible.selectedTabPosition = ${it.selectedTabPosition}")
            if (it.selectedTabPosition==0) {
                openFragment.setupSwitchDecoderButton()
                openFragment.searchCurrentFolder()
            } else {
                favoriteFragment.setupSwitchDecoderButton()
                favoriteFragment.searchFavorites()
            }
        }
    }

    fun becomeInVisible() {
        LogUtil.i(TAG, "becomeInVisible")
        openFragment.clearFileList()
        favoriteFragment.clearFavoriteList()
    }

    private fun showBannerAd() {
        LogUtil.d(TAG, "showBannerAd")
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
        private const val PICKER_FRAGMENT_TAG : String = "FILES_PICKER"
        private const val FAVORITE_FRAGMENT_TAG : String = "MY_FAVORITES"
    }
}