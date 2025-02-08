package com.smile.karaokeplayer.vlcplayer

import android.os.Bundle
import android.util.Log
import com.smile.karaokeplayer.BaseActivity
import com.smile.karaokeplayer.vlcplayer.fragments.VlcPlayerFragment

private const val TAG : String = "VlcPlayerActivity"

class VlcPlayerActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
    }

    override fun getFragment() = VlcPlayerFragment()
}