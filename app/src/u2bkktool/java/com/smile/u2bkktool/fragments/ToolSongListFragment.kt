package com.smile.u2bkktool.fragments

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.smile.karaoke.models.SongInfo
import com.smile.karaoke.utilities.LogUtil
import com.smile.u2bkaraoke.fragments.SongListFragment
import com.smile.u2bkaraoke.model.Song
import com.smile.u2bkaraoke.model.SongList
import com.smile.u2bkaraoke.u2bkaok_constants.U2bKKConstants
import com.smile.u2bkktool.U2bKTPlayActivity
import com.smile.u2bplayer.utilities.U2bPlayerUtil

class ToolSongListFragment: SongListFragment() {

    companion object {
        private const val TAG = "ToolSongListFrag"
    }

    override fun inAddToFavoriteDatabase(act: FragmentActivity) {
        LogUtil.d(TAG,"inAddToFavoriteDatabase" )
        val song = selectedSongs[0].first
        val position = selectedSongs[0].second
        U2bPlayerUtil.saveKeyword(act, songSearchTerm(song))
        val nIntent = Intent(act, U2bKTPlayActivity::class.java)
        nIntent.putExtra(U2bKKConstants.SONG_LIST_POSITION, position)
        nIntent.putExtra(U2bKKConstants.SEARCHED_SONG, song)
        searchToolLauncher.launch(nIntent)
    }

    override fun inPlaySelectedSongList(songInfos: ArrayList<SongInfo>,
                                    songPs: ArrayList<Pair<Song, Int>>
    ) {
        LogUtil.d(TAG,"inPlaySelectedSongList" )
        playSongs?.playSelectedSongList(songInfos, true)
    }

    override fun isItemValid(act: FragmentActivity,
                         list: SongList,
                         song: Song
    ): Boolean {
        LogUtil.d(TAG,"isItemValid.true" )
        return true
    }

    override fun baseFilter(): String {
        LogUtil.d(TAG,"baseFilter.empty string" )
        return ""
    }
}