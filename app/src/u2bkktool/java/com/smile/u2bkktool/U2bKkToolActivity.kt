package com.smile.u2bkktool

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.smile.u2bkaraoke.U2bKkBaseActivity
import com.smile.u2bkaraoke.u2bkaok_constants.U2bKKConstants
import com.smile.u2bkktool.fragments.SongToolFragment

class U2bKkToolActivity : U2bKkBaseActivity() {
    override fun getFirstFragment(): Fragment {
        return SongToolFragment().apply {
            arguments = Bundle().apply {
                putInt(U2bKKConstants.OrderedFrom, U2bKKConstants.ALL_SONG_ORDERED)
            }
        }
    }
}
