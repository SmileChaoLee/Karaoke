package com.smile.u2bkktool

import com.smile.karaoke.BuildConfig
import com.smile.u2bkktool.fragments.SearchToolFragment
import com.smile.u2bplayer.U2bBaseActivity
import com.smile.u2bplayer.u2bplay_constants.PrivateConstants

class U2bKTPlayActivity : U2bBaseActivity() {

    override fun getSearchFragment(): SearchToolFragment {
        val searchToolFragment = SearchToolFragment().apply {
            arguments = intent.extras
            applicationId = BuildConfig.APPLICATION_ID
            apiKey = when (applicationId) {
                "com.smile.u2bkktool" -> PrivateConstants.API_KEY2
                "com.smile.u2bkktool2" -> PrivateConstants.API_KEY3
                else -> PrivateConstants.API_KEY
            }
        }
        return searchToolFragment
    }
}