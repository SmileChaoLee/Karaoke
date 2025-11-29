package youtube

import android.os.Bundle
import androidx.annotation.OptIn
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.util.UnstableApi
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.smile.karaoke.BaseActivity
import com.smile.karaoke.R
import com.smile.karaoke.UpBaseActivity
import com.smile.karaoke.utilities.LogUtil
import youtube.fragments.SearchVideosFragment
import youtube.fragments.YouTubeFragment


@OptIn(UnstableApi::class)
class YouTubeActivity : BaseActivity() {

    companion object {
        private const val TAG : String = "YouTubeActivity"
        private const val SEARCH_FRAGMENT_TAG : String = "SEARCH_VIDEOS"
    }

    private val searchFragment = SearchVideosFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        LogUtil.d(TAG, "onCreate.finished")
    }

    override fun getFragment(): YouTubeFragment {
        LogUtil.d(TAG, "getFragment")
        return YouTubeFragment()
    }

    override fun setTabs(activity: FragmentActivity?, tabLayout: TabLayout, containerId: Int) {
        val tabText = arrayOf(getString(R.string.search_videos))
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
            }
        }
    }

    override fun becomeInVisible() {
        LogUtil.d(TAG, "becomeInVisible")
    }
}