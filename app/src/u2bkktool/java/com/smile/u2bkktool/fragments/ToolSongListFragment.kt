package com.smile.u2bkktool.fragments

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.smile.karaoke.R
import com.smile.karaoke.models.SongInfo
import com.smile.karaoke.utilities.LogUtil
import com.smile.u2bkaraoke.fragments.SongListFragment
import com.smile.u2bkaraoke.model.Song
import com.smile.u2bkaraoke.model.SongList
import com.smile.u2bkktool.adapters.ToolSongListAdapter
import com.smile.u2bkaraoke.u2bkaok_constants.U2bKKConstants
import com.smile.u2bkktool.U2bKTPlayActivity
import com.smile.u2bkktool.EditSongActivity
import com.smile.u2bkktool.retrofit.U2bKkToolRestApiSync
import com.smile.u2bplayer.utilities.U2bPlayerUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.view.isNotEmpty
import androidx.core.graphics.drawable.toDrawable
import com.smile.smilelibraries.utilities.ScreenUtil

class ToolSongListFragment: SongListFragment() {

    companion object {
        private const val TAG = "ToolSongListFrag"
    }

    private lateinit var editSongLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editSongLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) retrieveSongList()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myViewAdapter = ToolSongListAdapter(this, songList.songs, textFontSize)
        view.findViewById<RecyclerView>(R.id.songListRecyclerView).adapter = myViewAdapter
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

    override fun onItemLongClick(v: View?, position: Int): Boolean {
        val act = activity ?: return false
        if (position !in songList.songs.indices) return false

        val song = songList.songs[position]
        val dialog = Dialog(act)
        val buttonPadding = (16 * resources.displayMetrics.density).toInt()
        val buttonSpacing = (8 * resources.displayMetrics.density).toInt()
        val buttons = LinearLayout(act).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(buttonSpacing, buttonSpacing, buttonSpacing, buttonSpacing)
            setBackgroundColor(Color.WHITE)
        }

        fun addActionButton(label: Int, action: () -> Unit) {
            val button = Button(act).apply {
                text = getString(label)
                setTextColor(Color.BLACK)
                textSize = 20f
                minWidth = 0
                minHeight = 0
                setPadding(buttonPadding, buttonSpacing, buttonPadding, buttonSpacing)
                setOnClickListener {
                    dialog.dismiss()
                    action()
                }
            }
            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            if (buttons.isNotEmpty()) params.marginStart = buttonSpacing
            buttons.addView(button, params)
        }

        addActionButton(R.string.editString) {
            editSongLauncher.launch(
                Intent(act, EditSongActivity::class.java)
                    .putExtra(EditSongActivity.SONG_EXTRA, song)
            )
        }
        addActionButton(R.string.deleteString) {
            deleteSong(song.id)
        }

        dialog.setContentView(buttons)
        dialog.show()
        dialog.window?.apply {
            setBackgroundDrawable(Color.WHITE.toDrawable())
            setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        return true
    }

    private fun deleteSong(songId: Int) {
        val act = activity ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                U2bKkToolRestApiSync.getApiSync().deleteOneSong(songId)
            }
            var msg = act.getString(R.string.succeededMessage)
            if (result == 1) {
                retrieveSongList()
            } else {
                msg = act.getString(R.string.failedMessage)
            }
            ScreenUtil.showToast(
                act, msg,
                textFontSize, Toast.LENGTH_LONG
            )
        }
    }
}