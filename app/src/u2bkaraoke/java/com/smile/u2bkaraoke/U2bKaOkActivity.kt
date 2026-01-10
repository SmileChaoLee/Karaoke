package com.smile.u2bkaraoke

import android.app.Activity
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.util.UnstableApi
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.smile.karaoke.BaseActivity
import com.smile.karaoke.utilities.LogUtil
import com.smile.karaoke.utilities.PermissionUtil
import com.smile.u2bkaraoke.fragments.U2bKaOkFragment
import com.smile.u2bplayer.fragments.U2bPlayFragment
import com.smile.u2bplayer.models.U2bSingleton

@OptIn(UnstableApi::class)
open class U2bKaOkActivity : BaseActivity() {

    companion object {
        private const val U2B_KK_FRAGMENT_TAG : String = "U2B_KK_FRAGMENT"
    }

    private var mTAG : String = "U2bKaOkActivity"

    fun setTag(tag: String) {
        LogUtil.d(mTAG, "setTag.tag = $tag")
        mTAG = tag
    }

    private val u2bKKFragment = U2bKaOkFragment()

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
        val act = activity ?: return
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    when (it.position) {
                        0-> {
                            LogUtil.d(mTAG, "OnTabSelectedListener.onTabSelected.position = 0")
                            val fm = act.supportFragmentManager
                            fm.beginTransaction().apply {
                                replace(containerId, u2bKKFragment)
                                addToBackStack(null)
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

        tabLayout.addTab(tabLayout.newTab(), true)
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
                    it.post { u2bKKFragment.singerOrderButton?.requestFocus() }
                }
            }
        }
    }

    override fun becomeInVisible() {
        LogUtil.d(mTAG, "becomeInVisible")
    }
    // end of implementing interface, TablayoutFragment.TabFragmentFunc
}
