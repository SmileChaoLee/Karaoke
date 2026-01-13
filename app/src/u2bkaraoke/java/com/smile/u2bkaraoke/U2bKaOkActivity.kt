package com.smile.u2bkaraoke

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.util.UnstableApi
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.smile.karaoke.BaseActivity
import com.smile.karaoke.utilities.LogUtil
import com.smile.karaoke.utilities.PermissionUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.u2bkaraoke.fragments.U2bKKBaseFragment
import com.smile.u2bkaraoke.fragments.U2bKaOkFragment
import com.smile.u2bkaraoke.utilities.U2bKaOkUtil
import com.smile.u2bplayer.fragments.U2bPlayFragment

@OptIn(UnstableApi::class)
open class U2bKaOkActivity : BaseActivity() {

    private var mTAG : String = "U2bKaOkActivity"

    fun setTag(tag: String) {
        LogUtil.d(mTAG, "setTag.tag = $tag")
        mTAG = tag
    }

    private val u2bKKFragment = U2bKaOkFragment()
    private var fmContainerId: Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogUtil.d(mTAG, "onCreate")
    }

    override fun onDestroy() {
        LogUtil.i(mTAG, "onDestroy")
        super.onDestroy()
    }

    // implement abstract methods of BackActivity
    override fun getFragment(): U2bPlayFragment {
        LogUtil.d(mTAG, "getFragment")
        return U2bPlayFragment()
    }

    override fun askPermissions(activity: Activity): Boolean {
        LogUtil.d(mTAG, "askPermissions")
        return PermissionUtil.askPermissions(this@U2bKaOkActivity,
            false)
    }
    // End of implementing abstract methods of BackActivity

    // implement interface, TablayoutFragment.TabFragmentFunc
    override fun setTabs(activity: FragmentActivity?, tabLayout: TabLayout, containerId: Int) {
        fmContainerId = containerId
        val act = activity ?: return
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    when (it.position) {
                        0-> {
                            LogUtil.d(mTAG, "OnTabSelectedListener.onTabSelected.position = 0")
                            U2bKaOkUtil.beginTransaction(act.supportFragmentManager,
                                containerId, u2bKKFragment)
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
    }

    override fun becomeVisible(tabLayout: TabLayout) {
        val index = tabLayout.selectedTabPosition
        LogUtil.d(mTAG, "becomeVisible.index = $index")
        LogUtil.d(mTAG, "becomeVisible.currentFocus = $currentFocus")
        val tabView = tabLayout.getTabAt(index)?.view
        tabView?.let { tab ->
            when (index) {
                0 -> {
                    LogUtil.d(mTAG, "becomeVisible.index.0")
                    // tab.post { u2bKKFragment.showVideoButton?.requestFocus() }
                    fmContainerId?.let { cId ->
                        supportFragmentManager.findFragmentById(cId)?.let { curF ->
                            tab.post { (curF as U2bKKBaseFragment).showVideoButton?.requestFocus() }
                        }
                    }
                }
            }
        }
    }

    override fun becomeInVisible() {
        LogUtil.d(mTAG, "becomeInVisible")
    }
    // end of implementing interface, TablayoutFragment.TabFragmentFunc
}
