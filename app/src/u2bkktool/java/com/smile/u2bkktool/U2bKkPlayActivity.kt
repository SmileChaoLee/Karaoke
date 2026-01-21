package com.smile.u2bkktool

import com.smile.u2bkktool.fragments.SearchToolFragment
import com.smile.u2bplayer.U2bBaseActivity

class U2bKkPlayActivity : U2bBaseActivity() {

    override fun getSearchFragment() = SearchToolFragment()
}