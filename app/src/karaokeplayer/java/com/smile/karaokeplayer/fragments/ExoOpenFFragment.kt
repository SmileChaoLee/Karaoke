package com.smile.karaokeplayer.fragments

import android.view.View
import com.smile.karaoke.fragments.OpenFileFragment

class ExoOpenFFragment : OpenFileFragment() {
    // overriding the methods of OpenFileFragment
    override fun decoderButtonVisibility(): Int {
        return View.VISIBLE
    }
    // end of overriding the methods of OpenFileFragment
}