package com.smile.u2bkaraoke

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
import com.smile.u2bkaraoke.fragments.SongListFragment
import com.smile.u2bkaraoke.fragments.U2bKKBaseFragment
import com.smile.u2bkaraoke.fragments.U2bKaOkFragment
import com.smile.u2bkaraoke.utilities.U2bKaOkUtil
import com.smile.u2bplayer.fragments.U2bPlayFavFragment
import com.smile.u2bplayer.fragments.U2bPlayFragment

@OptIn(UnstableApi::class)
abstract class U2bKkBaseActivity : BaseActivity(), SongListFragment.U2bKkFunc {

    companion object {
        private const val TAG = "U2bKkBaseActivity"
        private const val FAV_FRAGMENT_TAG = "U2bPlayFavFragmentTag"
    }

    private var u2bPFFragment: U2bPlayFavFragment? = null
    private val nFragment = U2bKaOkFragment()
    var u2bPlayerFragment = U2bPlayFragment()
    private var fmContainerId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogUtil.d(TAG, "onCreate")
    }

    override fun onResume() {
        super.onResume()
        LogUtil.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        LogUtil.d(TAG, "onPause")
    }


    override fun onDestroy() {
        LogUtil.i(TAG, "onDestroy")
        super.onDestroy()
    }

    // implement abstract methods of BackActivity
    override fun getFragment(): U2bPlayFragment {
        LogUtil.d(TAG, "getFragment")
        return u2bPlayerFragment
    }

    override fun askPermissions(activity: Activity): Boolean {
        LogUtil.d(TAG, "askPermissions")
        return PermissionUtil.askPermissions(this@U2bKkBaseActivity,
            false)
    }
    // End of implementing abstract methods of BackActivity

    // implement interface, TablayoutFragment.TabFragmentFunc
    override fun setTabs(activity: FragmentActivity?, tabLayout: TabLayout, containerId: Int) {
        fmContainerId = containerId
        val tabText = arrayOf(getString(R.string.selectStr), getString(R.string.my_favorites))
        val act = activity ?: return
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    when (it.position) {
                        0-> {
                            LogUtil.d(TAG, "OnTabSelectedListener.onTabSelected.position = 0")
                            supportFragmentManager.apply {
                                val curF = findFragmentById(fmContainerId)
                                LogUtil.d(TAG, "OnTabSelectedListener.onTabSelected.position = 0.curF = $curF")
                                if (curF == null) {
                                    U2bKaOkUtil.beginTransaction(this@apply,
                                        fmContainerId, nFragment)
                                } else {
                                    if (curF is U2bPlayFavFragment) {   // it might be always
                                        U2bKaOkUtil.returnToPrevious(act)
                                    }
                                }
                            }
                        }
                        1-> {
                            LogUtil.d(TAG, "OnTabSelectedListener.onTabSelected.position = 1")
                            val existing = supportFragmentManager
                                .findFragmentByTag(FAV_FRAGMENT_TAG) as? U2bPlayFavFragment
                            u2bPFFragment = existing ?: U2bPlayFavFragment()
                            U2bKaOkUtil.beginTransaction(supportFragmentManager,
                                fmContainerId, u2bPFFragment!!,
                                FAV_FRAGMENT_TAG)
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
            it.isFocusable = true
            U2bKKBaseFragment.selectTab = it.newTab()
            U2bKKBaseFragment.selectTab?.text = tabText[0]
            U2bKKBaseFragment.selectTab?.view?.isFocusable = true
            it.addTab(U2bKKBaseFragment.selectTab!!, true)
            U2bKKBaseFragment.favoriteTab = it.newTab()
            U2bKKBaseFragment.favoriteTab?.text = tabText[1]
            U2bKKBaseFragment.favoriteTab?.view?.isFocusable = true
            it.addTab(U2bKKBaseFragment.favoriteTab!!)
        }

        /*
        // the following code must be called after setOnTabSelectedListener
        tabLayout.apply {
            // Set the height of the layout to match your indicator height
            // This makes the TabLayout "skinny"
            val indicatorHeight = ScreenUtil.dpToPixel(3f)
            layoutParams.height = indicatorHeight.toInt()
            // Remove the ripple and hide the indicator
            // Remove the ripple (grey circle) when clicking empty space
            tabRippleColor = ColorStateList.valueOf(Color.TRANSPARENT)
            // HIDE INDICATOR (Modern non-deprecated way)
            // Setting the indicator to null effectively removes it
            setSelectedTabIndicator(null)
            // setSelectedTabIndicatorHeight(0)

            // DISABLE FOCUS on the TabLayout itself, the focus will not go to indicator
            isFocusable = false
            isFocusableInTouchMode = false

            val tab = newTab()
            addTab(tab, true)
            // DISABLE FOCUS on the individual tab view, may not be necessary
            // This prevents the focus box from appearing on the empty space
            tab.view.isFocusable = false
            tab.view.isFocusableInTouchMode = false
        }
        */
    }

    override fun becomeVisible(tabLayout: TabLayout) {
        val index = tabLayout.selectedTabPosition
        LogUtil.d(TAG, "becomeVisible.index = $index")
        LogUtil.d(TAG, "becomeVisible.currentFocus = $currentFocus")
        val tabView = tabLayout.getTabAt(index)?.view
        tabView?.let { tab ->
            when (index) {
                0 -> {
                    LogUtil.d(TAG, "becomeVisible.index.0")
                    supportFragmentManager.findFragmentById(fmContainerId)?.let { curF ->
                        val exitButton = (curF as U2bKKBaseFragment).exitImageButton
                        exitButton?.post { exitButton.requestFocus() }
                    }
                }
                1 -> {
                    LogUtil.d(TAG, "becomeVisible.index.1")
                    tab.post { u2bPFFragment?.showVideoButton?.requestFocus() }
                }
            }
        }
    }

    override fun becomeInVisible() {
        LogUtil.d(TAG, "becomeInVisible")
    }
    // end of implementing interface, TablayoutFragment.TabFragmentFunc
}
