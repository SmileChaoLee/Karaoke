package com.smile.u2bplayer

import android.app.Activity
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.util.UnstableApi
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.smile.karaoke.BaseActivity
import com.smile.karaoke.R
import com.smile.karaoke.utilities.LogUtil
import com.smile.karaoke.utilities.PermissionUtil
import com.smile.u2bplayer.fragments.SearchVideosFragment
import com.smile.u2bplayer.fragments.U2bPlayFavFragment
import com.smile.u2bplayer.fragments.U2bPlayFragment
import com.smile.u2bplayer.models.U2bSingleton

@OptIn(UnstableApi::class)
open class U2bPlayerActivity : BaseActivity() {

    companion object {
        private const val SEARCH_FRAGMENT_TAG : String = "SEARCH_VIDEOS"
        private const val U2B_FAV_FRAGMENT_TAG : String = "U2B_FAVORITE"
    }
    private var mTAG : String = "U2bPlayerActivity"

    fun setTag(tag: String) {
        LogUtil.d(mTAG, "setTag.tag = $tag")
        mTAG = tag
    }

    private val searchFragment = SearchVideosFragment()
    private val u2bPFFragment = U2bPlayFavFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogUtil.d(mTAG, "onCreate")
        U2bSingleton.videos.clear() // moved from SearchVideosFragment
    }
    override fun onDestroy() {
        LogUtil.i(mTAG, "onDestroy")
        U2bSingleton.videos.clear() // moved from SearchVideosFragment
        super.onDestroy()
    }

    // implement abstract methods of BackActivity
    override fun getFragment(): U2bPlayFragment {
        LogUtil.d(mTAG, "getFragment")
        return U2bPlayFragment()
    }

    override fun askPermissions(activity: Activity): Boolean {
        LogUtil.d(mTAG, "askPermissions")
        return PermissionUtil.askPermissions(this@U2bPlayerActivity,
            false)
    }
    // End of implementing abstract methods of BackActivity

    override fun setTabs(activity: FragmentActivity?, tabLayout: TabLayout, containerId: Int) {
        val tabText = arrayOf(getString(R.string.search_videos), getString(R.string.my_favorites))
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    when (it.position) {
                        0-> {
                            LogUtil.d(mTAG, "OnTabSelectedListener.onTabSelected.position = 0")
                            activity?.supportFragmentManager?.beginTransaction()?.apply {
                                replace(containerId, searchFragment, SEARCH_FRAGMENT_TAG)
                                commit()
                            }
                        }
                        1-> {
                            LogUtil.d(mTAG, "OnTabSelectedListener.onTabSelected.position = 1")
                            activity?.supportFragmentManager?.beginTransaction()?.apply {
                                replace(containerId, u2bPFFragment, U2B_FAV_FRAGMENT_TAG)
                                commit()
                            }
                        }
                        else->{
                            LogUtil.d(mTAG, "OnTabSelectedListener.onTabSelected.others")
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                LogUtil.d(mTAG, "OnTabSelectedListener.onTabUnselected")
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                LogUtil.d(mTAG, "OnTabSelectedListener.onTabReselected")
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
        LogUtil.d(mTAG, "becomeVisible.index = $index")
        LogUtil.d(mTAG, "becomeVisible.currentFocus = $currentFocus")
        val tabView = tabLayout.getTabAt(index)?.view
        tabView?.let {
            when (index) {
                0 -> {
                    LogUtil.d(mTAG, "becomeVisible.index.0")
                    it.post { searchFragment.showVideoButton?.requestFocus() }
                }
                1 -> {
                    LogUtil.d(mTAG, "becomeVisible.index.1")
                    it.post { u2bPFFragment.showVideoButton?.requestFocus() }
                }
            }
        }
    }

    override fun becomeInVisible() {
        LogUtil.d(mTAG, "becomeInVisible")
    }
}