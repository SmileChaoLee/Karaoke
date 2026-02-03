package com.smile.u2bkktool.fragments

import android.app.Activity.RESULT_CANCELED
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.smile.karaoke.R
import com.smile.karaoke.models.SongInfo
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.u2bkaraoke.fragments.SongListFragment
import com.smile.u2bkaraoke.model.Song
import com.smile.u2bkktool.U2bKkPlayActivity
import com.smile.u2bkktool.u2bKktool_constants.U2bKkToConstants
import com.smile.u2bplayer.utilities.U2bPlayerUtil

class SongToolFragment : SongListFragment() {

    companion object {
        private const val TAG : String = "SongToolFragment"
    }

    private lateinit var searchToolLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        searchToolLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            val logStr = "searchToolLauncher.receive"
            LogUtil.d(TAG, "$logStr.result = $result")
            if (result.resultCode == RESULT_CANCELED) return@registerForActivityResult
            retrieveSongList()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        LogUtil.d(TAG, "onViewCreated")
        super.onViewCreated(view, savedInstanceState)
        searchEditText?.setHint(getString(R.string.numWords))
    }

    private fun songSearchTerm(song: Song): String {
        // val searchTerm = "intitle:" + "\"[" + song.songNa + " " + singer1 + " " + singer2 + "]\""
        var searchTerm = "\"${song.songNa.trim()}"
        if (song.singer1Na.isNotEmpty() && song.singer1Na.uppercase() != "UNKNOWN") {
            searchTerm = searchTerm + " " + song.singer1Na.trim()
        }
        if (song.singer2Na.isNotEmpty() && song.singer2Na.uppercase() != "UNKNOWN") {
            searchTerm = searchTerm + " " + song.singer2Na.trim()
        }
        searchTerm = "$searchTerm\""
        LogUtil.d(TAG, "songSearchTerm.searchTerm = $searchTerm")
        return searchTerm
    }

    override fun playSelectedSongList(songInfos: ArrayList<SongInfo>) {
        playSongs?.playSelectedSongList(songInfos, true)
    }

    override fun isAllowed(song: Song): Boolean {
        return true
    }

    override fun addToFavoriteDatabase() {
        val logStr = "addToFavoriteDatabase"
        LogUtil.d(TAG, logStr)
        val act = activity ?: return
        if (selectedSongs.isEmpty()) {
            ScreenUtil.showToast(activity,
                getString(R.string.noFilesSelectedString),
                textFontSize,Toast.LENGTH_SHORT)
            return
        }
        val song = selectedSongs[0].first
        val position = selectedSongs[0].second
        U2bPlayerUtil.saveKeyword(act, songSearchTerm(song))
        Intent(act,U2bKkPlayActivity::class.java).also { intIt ->
            intIt.putExtra(U2bKkToConstants.SONG_LIST_POSITION, position)
            intIt.putExtra(U2bKkToConstants.SEARCHED_SONG, song)
            searchToolLauncher.launch(intIt)
        }
    }

    override fun setClickListeners() {
        super.setClickListeners()
        exitImageButton?.setOnClickListener {
            activity?.finish()
        }
        showVideoButton?.visibility = View.VISIBLE
    }
}