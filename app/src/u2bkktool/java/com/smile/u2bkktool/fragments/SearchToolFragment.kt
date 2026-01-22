package com.smile.u2bkktool.fragments

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.smile.karaoke.R
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.u2bkaraoke.model.Song
import com.smile.u2bkaraoke.retrofit.U2bKkRestApiSync
import com.smile.u2bkktool.u2bKktool_constants.U2bKkToConstants
import com.smile.u2bplayer.fragments.SearchVideosFragment
import com.smile.u2bplayer.models.U2bSingleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchToolFragment : SearchVideosFragment() {

    companion object {
        private const val TAG : String = "SearchToolFragment"
    }

    private var dataSong: Song? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            dataSong = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                args.getParcelable(U2bKkToConstants.SEARCHED_SONG, Song::class.java)
            } else args.getParcelable(U2bKkToConstants.SEARCHED_SONG)
            LogUtil.d(TAG, "onCreate.dataSong = $dataSong")
        }
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
        val logStr = "addToFavoriteDatabase"
        LogUtil.d(TAG, logStr)
        val act = activity ?: return
        // update the song database on the cloud
        // update the vodNo with video id and pathname with snippet.thumbnails.default.url
        if (selectedSongs.isEmpty()) {
            // show message
            ScreenUtil.showToast(act,
                getString(R.string.noFilesSelectedString),
                textFontSize, Toast.LENGTH_SHORT)
            return
        }
        dataSong?.let { dSong ->
            lifecycleScope.launch(Dispatchers.IO) {
                dSong.vodNo = selectedSongs[0].filePath
                dSong.pathname = selectedSongs[0].bitmapUrl ?: ""
                val result = U2bKkRestApiSync.getApiSync().updateOneSong(dSong.id, dSong)
                LogUtil.d(TAG, "$logStr.result = $result")
                withContext(Dispatchers.Main) {
                    if (result == 1) {
                        ScreenUtil.showToast(
                            act,
                            getString(R.string.succeededMessage),
                            textFontSize, Toast.LENGTH_SHORT
                        )
                    } else {
                        ScreenUtil.showToast(
                            act,
                            getString(R.string.failedMessage),
                            textFontSize, Toast.LENGTH_SHORT
                        )
                    }
                }
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