package com.smile.karaoke

import android.app.Activity
import androidx.annotation.OptIn
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.util.UnstableApi
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.smile.karaoke.fragments.ComFavFragment
import com.smile.karaoke.fragments.OpenFileFragment
import com.smile.karaoke.fragments.SafPickerFragment
import com.smile.karaoke.utilities.LogUtil
import com.smile.karaoke.utilities.PermissionUtil

@OptIn(UnstableApi::class)
abstract class UpBaseActivity: BaseActivity() {

    companion object {
        private const val TAG = "UpBaseActivity"
        private const val OPEN_FRAGMENT_TAG : String = "OPEN_FILES"
        private const val PICKER_FRAGMENT_TAG : String = "FILES_PICKER"
        private const val FAVORITE_FRAGMENT_TAG : String = "MY_FAVORITES"
    }

    abstract fun getOpenFileFragment(): OpenFileFragment
    abstract fun getFavoriteFragment(): ComFavFragment

    private val openFragment = getOpenFileFragment()
    private val safPickerFragment = SafPickerFragment()
    private val favoriteFragment = getFavoriteFragment()

    override fun askPermissions(activity: Activity): Boolean {
        LogUtil.d(TAG, "askPermissions")
        return PermissionUtil.askPermissions(this@UpBaseActivity,
            true)
    }

    override fun setTabs(activity: FragmentActivity?, tabLayout: TabLayout, containerId: Int) {
        val tabText = arrayOf(getString(R.string.open_files),
            getString(R.string.files_picker),
            getString(R.string.my_favorites))
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    when (it.position) {
                        0-> {
                            LogUtil.d(TAG, "OnTabSelectedListener.onTabSelected.position = 0")
                            activity?.supportFragmentManager?.beginTransaction()?.apply {
                                replace(containerId, openFragment,
                                    OPEN_FRAGMENT_TAG)
                                commit()
                            }
                        }
                        1-> {
                            LogUtil.d(TAG, "OnTabSelectedListener.onTabSelected.position = 1")
                            activity?.supportFragmentManager?.beginTransaction()?.apply {
                                replace(containerId, safPickerFragment,
                                    PICKER_FRAGMENT_TAG)
                                commit()
                            }
                        }
                        2-> {
                            LogUtil.d(TAG, "OnTabSelectedListener.onTabSelected.position = 1")
                            activity?.supportFragmentManager?.beginTransaction()?.apply {
                                replace(containerId, favoriteFragment,
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

        tabLayout.let {
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
    }

    override fun becomeVisible(tabLayout: TabLayout) {
        val index = tabLayout.selectedTabPosition
        LogUtil.d(TAG, "becomeVisible.index = $index")
        LogUtil.d(TAG, "becomeVisible.currentFocus = $currentFocus")
        val tabView = tabLayout.getTabAt(index)?.view
        tabView?.let {
            when (index) {
                1 -> {
                    it.post { safPickerFragment.showVideoButton?.requestFocus() }
                }
                2 -> {
                    it.post { favoriteFragment.showVideoButton?.requestFocus() }
                }
                else -> {
                    it.post { openFragment.showVideoButton?.requestFocus() }
                }
            }
        }
    }

    override fun becomeInVisible() {
        LogUtil.d(TAG, "becomeInVisible")
    }
}