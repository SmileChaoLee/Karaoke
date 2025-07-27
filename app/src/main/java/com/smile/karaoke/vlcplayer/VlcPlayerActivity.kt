package com.smile.karaoke.vlcplayer

import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.smile.karaoke.BaseActivity
import com.smile.karaoke.vlcplayer.fragments.VlcPlayerFragment

private const val TAG : String = "VlcPlayerActivity"

@OptIn(UnstableApi::class)
class VlcPlayerActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
    }

    override fun getFragment() = VlcPlayerFragment()
}