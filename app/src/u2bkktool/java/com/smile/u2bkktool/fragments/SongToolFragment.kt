package com.smile.u2bkktool.fragments

import android.app.Activity.RESULT_CANCELED
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.smile.karaoke.BuildConfig
import com.smile.karaoke.R
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.u2bkaraoke.fragments.SongListFragment
import com.smile.u2bkaraoke.model.Song
import com.smile.u2bkktool.U2bKkPlayActivity
import com.smile.u2bkktool.u2bKktool_constants.U2bKkToConstants
import com.smile.u2bkktool.utilities.U2bKkToUtil
import com.smile.u2bplayer.retrofit.U2bPyRestApiSync
import com.smile.u2bplayer.utilities.U2bPlayerUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
            val act = activity ?: return@registerForActivityResult
            result.data?.extras?.let {
                val position = it.getInt(U2bKkToConstants.SONG_LIST_POSITION, -1)
                LogUtil.d(TAG, "$logStr.position = $position")
                if (position == -1 || position >= songList.songs.size) return@registerForActivityResult
                val song = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    it.getParcelable(U2bKkToConstants.SEARCHED_SONG, Song::class.java)
                } else it.getParcelable(U2bKkToConstants.SEARCHED_SONG)
                songList.songs[position] = song ?: Song()
                myViewAdapter?.myNotifyItemChanged(position)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        LogUtil.d(TAG, "onViewCreated")
        super.onViewCreated(view, savedInstanceState)
        searchEditText?.setHint(getString(R.string.numWords))
    }

    private fun songSearchTerm(song: Song): String {
        // val searchTerm = "intitle:" + "\"[" + song.songNa + " " + singer1 + " " + singer2 + "]\""
        var searchTerm = "intitle:[\"${song.songNa.trim()}"
        if (song.singer1Na.isNotEmpty() && song.singer1Na.uppercase() != "UNKNOWN") {
            searchTerm = searchTerm + " " + song.singer1Na.trim()
        }
        if (song.singer2Na.isNotEmpty() && song.singer2Na.uppercase() != "UNKNOWN") {
            searchTerm = searchTerm + " " + song.singer2Na.trim()
        }
        searchTerm = "$searchTerm]\""
        LogUtil.d(TAG, "songSearchTerm.searchTerm = $searchTerm")
        return searchTerm
    }

    override fun addToFavoriteDatabase() {
        val logStr = "addToFavoriteDatabase"
        LogUtil.d(TAG, logStr)
        val act = activity ?: return
        val songs = songList.songs.ifEmpty { return }
        lifecycleScope.launch(Dispatchers.IO) {
            for (i in 0 until songs.size) {
                val song = songs[i]
                if (song.mMpeg == "00" && song.nMpeg == "00") continue // already done before
                val videos = U2bPyRestApiSync.getVideoList(BuildConfig.APPLICATION_ID,
                    songSearchTerm(song), 1)
                val videoId = if (videos.items.isEmpty()) null else videos.items[0].id.videoId
                if (videoId == null) {
                    LogUtil.d(TAG, "$logStr.did not find videoId")
                    withContext(Dispatchers.Main) {
                        ScreenUtil.showToast(act,
                            song.songNa + " " + act.getString(R.string.notFound),
                            textFontSize, Toast.LENGTH_SHORT)
                    }
                } else {
                    // update the song database on the cloud
                    LogUtil.d(TAG, "$logStr.videoId = $videoId")
                    song.vodNo = videoId
                    song.pathname = videos.items[0].snippet.thumbnails.default.url
                    val isSuccessful = U2bKkToUtil.updateOneSongToCloud(song)
                    withContext(Dispatchers.Main) {
                        if (isSuccessful) {
                            ScreenUtil.showToast(act,
                                song.songNa + " " + act.getString(R.string.succeededMessage),
                                textFontSize, Toast.LENGTH_SHORT)
                            myViewAdapter?.myNotifyItemChanged(i)
                        } else {
                            ScreenUtil.showToast(act,
                                song.songNa + " " + act.getString(R.string.failedMessage),
                                textFontSize, Toast.LENGTH_SHORT)
                        }
                    }
                }
                delay(100) // delay 0.1 seconds
            }
        }
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
            U2bPlayerUtil.saveKeyword(act, songSearchTerm(song))
            // val vIntent = Intent(act, U2bKkPlayActivity::class.java)
            // vIntent.putExtra(U2bKkToConstants.SEARCHED_SONG, song)
            // startActivity(vIntent)
            Intent(act,U2bKkPlayActivity::class.java).also { intIt ->
                intIt.putExtra(U2bKkToConstants.SONG_LIST_POSITION, position)
                intIt.putExtra(U2bKkToConstants.SEARCHED_SONG, song)
                searchToolLauncher.launch(intIt)
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