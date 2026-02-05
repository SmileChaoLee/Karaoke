package com.smile.u2bkaraoke

import com.smile.karaoke.utilities.LogUtil

open class U2bKaOkActivity : U2bKkBaseActivity() {

    private var mTAG : String = "U2bKaOkActivity"

    fun setTag(tag: String) {
        LogUtil.d(mTAG, "setTag.tag = $tag")
        mTAG = tag
    }

    override fun isU2bKkTool(): Boolean {
        return false
    }
}
