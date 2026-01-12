package com.smile.videoplayer.fragments

import android.view.View
import com.smile.karaoke.fragments.ComFavFragment

class VlcFavFragment : ComFavFragment() {
    // overriding the methods of ComFavFragment
    override fun decoderButtonVisibility(): Int {
        return View.GONE
    }
    // end of overriding the methods of ComFavFragment

}