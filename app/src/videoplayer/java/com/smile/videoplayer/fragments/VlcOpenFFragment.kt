package com.smile.videoplayer.fragments

import android.view.View
import com.smile.karaoke.fragments.OpenFileFragment

class VlcOpenFFragment : OpenFileFragment() {
    // overriding the methods of OpenFileFragment
    override fun decoderButtonVisibility(): Int {
        return View.GONE
    }
    // end of overriding the methods of OpenFileFragment
}