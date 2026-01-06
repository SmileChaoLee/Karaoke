package com.smile.u2bkaraoke

import android.app.Application
import android.util.Log
import com.smile.u2bkaraoke.dagger.interfaces.DaggerSongAppComponent

class SongApplication : Application() {
    companion object {
        private const val TAG = "SongApplication"
        val appCompBuilder = DaggerSongAppComponent.builder()!!
        val appComponent = appCompBuilder.build()
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()
    }

    override fun onTrimMemory(level: Int) {
        Log.d(TAG, "onTrimMemory")
        super.onTrimMemory(level)
    }
}