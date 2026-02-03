package com.smile.u2bkaraoke

import androidx.fragment.app.Fragment
import com.smile.karaoke.utilities.LogUtil
import com.smile.u2bkaraoke.fragments.U2bKaOkFragment

open class U2bKaOkActivity : U2bKkBaseActivity() {

    private var mTAG : String = "U2bKaOkActivity"

    fun setTag(tag: String) {
        LogUtil.d(mTAG, "setTag.tag = $tag")
        mTAG = tag
    }

    override fun getFirstFragment(): Fragment {
        return U2bKaOkFragment()
    }
}
