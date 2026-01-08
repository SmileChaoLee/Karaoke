package com.smile.u2bkaraoke

import android.app.Application
import com.smile.karaoke.utilities.LogUtil
import com.smile.u2bkaraoke.dagger.interfaces.DaggerSongAppComponent

class U2bKaraokeApp : Application() {
    companion object {
        private const val TAG = "U2bKaraokeApp"
        val appCompBuilder = DaggerSongAppComponent.builder()!!
        val appComponent = appCompBuilder.build()
    }

    override fun onCreate() {
        LogUtil.d(TAG, "onCreate")
        super.onCreate()
    }

    override fun onTrimMemory(level: Int) {
        LogUtil.d(TAG, "onTrimMemory")
        super.onTrimMemory(level)
    }
}