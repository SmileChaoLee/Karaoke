package com.smile.u2bkktool.fragments

import android.content.Intent
import android.view.View
import android.widget.Toast
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.u2bkaraoke.fragments.SongListFragment
import com.smile.u2bplayer.U2bPlayerActivity
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
            U2bPlayerUtil.saveKeyword(act, song.songNa)
            Intent(act, U2bPlayerActivity::class.java).apply {
                startActivity(this@apply)
            }
        }
    }

    override fun setClickListeners() {
        super.setClickListeners()
        exitImageButton?.setOnClickListener {
            activity?.finish()
        }
    }
}