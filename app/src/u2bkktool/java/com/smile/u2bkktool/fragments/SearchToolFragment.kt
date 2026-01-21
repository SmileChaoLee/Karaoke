package com.smile.u2bkktool.fragments

import android.view.View
import android.widget.Toast
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.u2bplayer.fragments.SearchVideosFragment
import com.smile.u2bplayer.models.U2bSingleton

class SearchToolFragment : SearchVideosFragment() {

    companion object {
        private const val TAG : String = "SearchToolFragment"
    }

    override fun onItemClick(v: View?, position: Int) {
        LogUtil.d(TAG, "onItemClick.position = $position")
        if (position < 0) return
        val act = activity ?: return
        val songDesc = U2bSingleton.videos[position]
        songDesc.apply {
            ScreenUtil.showToast(act, song.songName,
                textFontSize,Toast.LENGTH_SHORT)
            // remove all selected songs because we just need one
            selectedSongs.clear()
            if (song.included == "1") {
                song.included = "0"
            } else {
                for (i in 0 until U2bSingleton.videos.size) {
                    val song = U2bSingleton.videos[i].song
                    if (song.included == "1") {
                        song.included = "0"
                        myRecyclerViewAdapter?.myNotifyItemChanged(i)
                    }
                }
                song.included = "1"
                selectedSongs.add(song)
            }
            myRecyclerViewAdapter?.myNotifyItemChanged(position)
        }
    }

    override fun addToFavoriteDatabase() {
        LogUtil.d(TAG, "addToFavoriteDatabase")
        // update the song database on the cloud
        // update the vodNo with video id and pathname with snippet.thumbnails.default.url
    }

    override fun setClickListeners() {
        super.setClickListeners()
        exitImageButton?.setOnClickListener {
            activity?.finish()
        }
    }
}