package com.smile.androidsong

import android.app.Application
import android.util.Log
import com.smile.androidsong.dagger.interfaces.DaggerSongAppComponent

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