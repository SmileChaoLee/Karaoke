package com.smile.karaoke.exoplayer

import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.smile.karaoke.BaseActivity
import com.smile.karaoke.exoplayer.fragments.ExoPlayerFragment

private const val TAG : String = "ExoPlayerActivity"

@OptIn(UnstableApi::class)
class ExoPlayerActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
    }

    @OptIn(UnstableApi::class)
    override fun getFragment() = ExoPlayerFragment()
}