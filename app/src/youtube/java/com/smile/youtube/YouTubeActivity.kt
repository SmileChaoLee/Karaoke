package com.smile.youtube

import androidx.annotation.OptIn
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.util.UnstableApi
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.smile.karaoke.BaseActivity
import com.smile.karaoke.R
import com.smile.karaoke.fragments.FavoritesFragment
import com.smile.karaoke.utilities.LogUtil
import com.smile.youtube.fragments.SearchVideosFragment
import com.smile.youtube.fragments.YouTubeFragment
import com.smile.youtube.yt_constants.YTConstants

@OptIn(UnstableApi::class)
class YouTubeActivity : BaseActivity() {

    companion object {
        private const val TAG : String = "YouTubeActivity"
        private const val SEARCH_FRAGMENT_TAG : String = "SEARCH_VIDEOS"
        private const val YT_FAV_FRAGMENT_TAG : String = "YT_FAVORITE"
    }

    private val searchFragment = SearchVideosFragment()
    private val ytFavFragment = FavoritesFragment.newInstance(
        false, YTConstants.YT_FAV_DB_NAME)

    override fun getFragment(): YouTubeFragment {
        LogUtil.d(TAG, "getFragment")
        return YouTubeFragment()
    }

    override fun setTabs(activity: FragmentActivity?, tabLayout: TabLayout, containerId: Int) {
        val tabText = arrayOf(getString(R.string.search_videos), getString(R.string.my_favorites))
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    when (it.position) {
                        0-> {
                            LogUtil.d(TAG, "OnTabSelectedListener.onTabSelected.position = 0")
                            activity?.supportFragmentManager?.beginTransaction()?.apply {
                                replace(containerId, searchFragment, SEARCH_FRAGMENT_TAG)
                                commit()
                            }
                        }
                        1-> {
                            LogUtil.d(TAG, "OnTabSelectedListener.onTabSelected.position = 1")
                            activity?.supportFragmentManager?.beginTransaction()?.apply {
                                replace(containerId, ytFavFragment, YT_FAV_FRAGMENT_TAG)
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

        tabLayout.let {
            val searchTab = it.newTab()
            searchTab.text = tabText[0]
            it.addTab(searchTab, true)
            val favoriteTab = it.newTab()
            favoriteTab.text = tabText[1]
            it.addTab(favoriteTab)
        }
    }

    override fun becomeVisible(tabLayout: TabLayout) {
        val index = tabLayout.selectedTabPosition
        LogUtil.d(TAG, "becomeVisible.index = $index")
        LogUtil.d(TAG, "becomeVisible.currentFocus = $currentFocus")
        val tabView = tabLayout.getTabAt(index)?.view
        tabView?.let {
            when (index) {
                0 -> {
                    LogUtil.d(TAG, "becomeVisible.index.0")
                    it.post { searchFragment.showVideoButton?.requestFocus() }
                }
                1 -> {
                    LogUtil.d(TAG, "becomeVisible.index.1")
                    it.post { ytFavFragment.showVideoButton?.requestFocus() }
                }
            }
        }
    }

    override fun becomeInVisible() {
        LogUtil.d(TAG, "becomeInVisible")
    }
}