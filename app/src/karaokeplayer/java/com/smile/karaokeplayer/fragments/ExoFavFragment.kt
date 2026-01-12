package com.smile.karaokeplayer.fragments

import android.view.View
import com.smile.karaoke.fragments.ComFavFragment

class ExoFavFragment : ComFavFragment() {
    // overriding the methods of ComFavFragment
    override fun decoderButtonVisibility(): Int {
        return View.VISIBLE
    }
    // end of overriding the methods of ComFavFragment
}