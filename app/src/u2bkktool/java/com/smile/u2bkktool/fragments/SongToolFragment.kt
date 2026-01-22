package com.smile.u2bkktool.fragments

import android.content.Intent
import android.view.View
import android.widget.Toast
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.u2bkaraoke.fragments.SongListFragment
import com.smile.u2bkktool.U2bKkPlayActivity
import com.smile.u2bkktool.u2bKktool_constants.U2bKkToConstants
import com.smile.u2bplayer.utilities.U2bPlayerUtil

class SongToolFragment : SongListFragment() {

    companion object {
        private const val TAG : String = "SongToolFragment"
    }

    override fun onItemClick(v: View?, position: Int) {
        LogUtil.d(TAG, "onItemClick.position = $position")
        if (position < 0) return
        val act = activity ?: return
        songList.let { list ->
            val song = list.songs[position]
            ScreenUtil.showToast(
                act, song.songNa,
                textFontSize, Toast.LENGTH_SHORT
            )
            val singer1 = if (song.singer1Na.isEmpty() ||
                song.singer1Na.uppercase() == "UNKNOWN") "" else song.singer1Na
            val singer2 = if (song.singer2Na.isEmpty() ||
                song.singer2Na.uppercase() == "UNKNOWN") "" else song.singer2Na
            val searchTerm = "intitle:" + "\"[" + song.songNa + " " + singer1 + " " + singer2 + "]\""
            U2bPlayerUtil.saveKeyword(act, searchTerm)
            val vIntent = Intent(act, U2bKkPlayActivity::class.java)
            vIntent.putExtra(U2bKkToConstants.SEARCHED_SONG, song)
            startActivity(vIntent)
        }
    }

    override fun setClickListeners() {
        super.setClickListeners()
        exitImageButton?.setOnClickListener {
            activity?.finish()
        }
    }
}