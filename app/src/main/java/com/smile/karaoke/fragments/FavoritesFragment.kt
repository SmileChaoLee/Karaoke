package com.smile.karaoke.fragments

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.scale
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smile.karaoke.BaseFavoriteListActivity
import com.smile.karaoke.R
import com.smile.karaoke.adapters.FavoriteRecyclerViewAdapter
import com.smile.karaoke.interfaces.PlayMyFavorites
import com.smile.karaoke.interfaces.PlaySongs
import com.smile.karaoke.models.MySingleTon
import com.smile.karaoke.models.SongDescription
import com.smile.karaoke.models.SongInfo
import com.smile.karaoke.utilities.DatabaseAccessUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import java.io.File

class FavoritesFragment : Fragment(),
    FavoriteRecyclerViewAdapter.OnRecyclerItemClickListener {

    companion object {
        private const val TAG : String = "FavoritesFragment"
        private const val SEARCH_FAVORITES_COMPLETED = "SearchFavorites"
        private const val EXCESS_YN = "ExcessYN"
    }

    private var textFontSize = 0f
    private var fontScale = 0f
    private var playSongs: PlaySongs? = null
    private var playMyFavorites: PlayMyFavorites? = null
    private var myListRecyclerView : RecyclerView? = null
    private var myRecyclerViewAdapter : FavoriteRecyclerViewAdapter? = null
    private lateinit var editSongsActivityLauncher: ActivityResultLauncher<Intent>
    private lateinit var broadcastReceiver: BroadcastReceiver
    private var searchCompleted = true
    private lateinit var mediaRetriever: MediaMetadataRetriever

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate() is called")
        super.onCreate(savedInstanceState)
        arguments?.let {
        }

        mediaRetriever = MediaMetadataRetriever()

        val defaultTextFontSize = ScreenUtil.getDefaultTextSizeFromTheme(activity,
            ScreenUtil.FontSize_Pixel_Type, null)
        textFontSize = ScreenUtil.suitableFontSize(activity, defaultTextFontSize,
            ScreenUtil.FontSize_Pixel_Type,0.0f)
        fontScale = ScreenUtil.suitableFontScale(activity,
            ScreenUtil.FontSize_Pixel_Type, 0.0f)

        activity?.let {
            if (it is PlaySongs) playSongs = it
            Log.d(TAG, "onCreate.playSongs = $playSongs")
            if (it is PlayMyFavorites) playMyFavorites = it
            Log.d(TAG, "onCreate.playMyFavorites = $playMyFavorites")
        }

        editSongsActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            playMyFavorites?.restorePlayingState()
            searchFavorites()
        } // update the UI }

        object : BroadcastReceiver() {
            @SuppressLint("NotifyDataSetChanged")
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d(TAG, "BroadcastReceiver.onReceive")
                intent?.action?.let {
                    if (it == SEARCH_FAVORITES_COMPLETED) {
                        Log.d(TAG, "BroadcastReceiver.onReceive.SearchFavorites")
                        if (intent.getBooleanExtra(EXCESS_YN, false)) {
                            ScreenUtil.showToast(
                                    activity, getString(R.string.excess_max) +
                                    " ${MySingleTon.MAX_SONGS}", textFontSize,
                                ScreenUtil.FontSize_Pixel_Type,
                                Toast.LENGTH_SHORT)
                        }
                        myRecyclerViewAdapter?.notifyDataSetChanged()
                        searchCompleted = true  // searching thread finished
                    }
                }
            }
        }.also { broadcastReceiver = it }
        activity?.let {
            LocalBroadcastManager.getInstance(it).apply {
                Log.d(TAG, "LocalBroadcastManager.registerReceiver")
                registerReceiver(broadcastReceiver, IntentFilter().apply {
                    addAction(SEARCH_FAVORITES_COMPLETED)
                })
            }
        }

        Log.d(TAG, "onCreate.FavoriteSingleTon.favoriteList.size = ${MySingleTon.favorites.size}")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        Log.d(TAG, "onCreateView() is called")
        return inflater.inflate(R.layout.fragment_my_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated() is called.")
        super.onViewCreated(view, savedInstanceState)

        view.let {
            val buttonWidth = (textFontSize * 1.5f).toInt()
            myListRecyclerView = it.findViewById(R.id.myListRecyclerView)
            myListRecyclerView?.setHasFixedSize(true)
            val selectAllButton: ImageButton = it.findViewById(R.id.favoriteSelectAllButton)
            var layoutParams: ViewGroup.MarginLayoutParams = selectAllButton.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.width = buttonWidth
            layoutParams.height = buttonWidth
            selectAllButton.layoutParams = layoutParams
            selectAllButton.setOnClickListener {
                if (!searchCompleted) return@setOnClickListener // searching
                for (i in 0 until MySingleTon.favorites.size) {
                    MySingleTon.favorites[i].run {
                        song.included = "1"
                        myRecyclerViewAdapter?.notifyItemChanged(i)
                    }
                }
            }
            val unselectButton: ImageButton = it.findViewById(R.id.favoriteUnselectButton)
            layoutParams = unselectButton.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.width = buttonWidth
            layoutParams.height = buttonWidth
            unselectButton.layoutParams = layoutParams
            unselectButton.setOnClickListener {
                if (!searchCompleted) return@setOnClickListener // searching
                for (i in 0 until MySingleTon.favorites.size) {
                    MySingleTon.favorites[i].run {
                        song.included = "0"
                        myRecyclerViewAdapter?.notifyItemChanged(i)
                    }
                }
            }
            val refreshButton: ImageButton = it.findViewById(R.id.favoriteRefreshButton)
            layoutParams = refreshButton.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.width = buttonWidth
            layoutParams.height = buttonWidth
            refreshButton.layoutParams = layoutParams
            refreshButton.setOnClickListener {
                if (!searchCompleted) return@setOnClickListener // searching
                searchFavorites()
            }
            val playSelectedButton: ImageButton = it.findViewById(R.id.favoritePlaySelectedButton)
            layoutParams = playSelectedButton.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.width = buttonWidth
            layoutParams.height = buttonWidth
            playSelectedButton.layoutParams = layoutParams
            playSelectedButton.setOnClickListener {
                if (!searchCompleted) return@setOnClickListener // searching
                // open the files to play
                val songs = ArrayList<SongInfo>().also { songIt ->
                    var index = 0
                    for (i in 0 until MySingleTon.favorites.size) {
                        if (MySingleTon.favorites[i].song.included == "1") {
                            songIt.add(MySingleTon.favorites[i].song)
                            index++
                            if (index >= MySingleTon.MAX_SONGS) {
                                // excess the max
                                ScreenUtil.showToast(
                                        activity, getString(R.string.excess_max) +
                                        " ${MySingleTon.MAX_SONGS}", textFontSize,
                                    ScreenUtil.FontSize_Pixel_Type,
                                    Toast.LENGTH_SHORT)
                                break
                            }
                        }
                    }
                }
                if (songs.isEmpty()) {
                    ScreenUtil.showToast(
                            activity, getString(R.string.noFilesSelectedString),
                        textFontSize,
                        ScreenUtil.FontSize_Pixel_Type,
                        Toast.LENGTH_SHORT)
                } else {
                    // playSongs?.choosePlayerToPlaySelectedSongs(ArrayList(songs))
                    playSongs?.playSelectedSongList(ArrayList(songs))
                }
            }
            val editButton: ImageButton = it.findViewById(R.id.favoriteEditButton)
            layoutParams = editButton.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.width = buttonWidth
            layoutParams.height = buttonWidth
            editButton.layoutParams = layoutParams
            editButton.setOnClickListener {
                if (!searchCompleted) return@setOnClickListener // searching
                ArrayList<SongInfo>().also {listIt ->
                    for (element in MySingleTon.favorites) {
                        if (element.song.included == "1") listIt.add(element.song)
                    }
                    if (listIt.isNotEmpty()) {
                        playMyFavorites?.let {playIt ->
                            intentForFavoriteListActivity().apply {
                                Log.d(TAG, "editButton.listIt.size = ${listIt.size}")
                                playIt.onSavePlayingState(component)
                                // putExtra(PlayerConstants.MyFavoriteListState, listIt)
                                MySingleTon.selectedFavorites.clear()
                                MySingleTon.selectedFavorites.addAll(listIt)
                                Runtime.getRuntime().gc()
                                editSongsActivityLauncher.launch(this)
                            }
                        }
                    } else {
                        ScreenUtil.showToast(
                                activity, getString(R.string.noFilesSelectedString), textFontSize,
                            ScreenUtil.FontSize_Pixel_Type,
                            Toast.LENGTH_SHORT)
                    }
                }
            }
            val showVideoButton: ImageButton = it.findViewById(R.id.showVideoImageButton)
            layoutParams = showVideoButton.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.width = buttonWidth
            layoutParams.height = buttonWidth
            showVideoButton.layoutParams = layoutParams
            showVideoButton.visibility = View.VISIBLE
            showVideoButton.setOnClickListener {
                if (!searchCompleted) return@setOnClickListener // searching
                playSongs?.switchToPlayerView()
            }
        }

        initFavoriteRecyclerView()
    }

    override fun onStart() {
        Log.d(TAG, "onStart()")
        super.onStart()
    }

    override fun onResume() {
        Log.d(TAG, "onResume()")
        searchFavorites()   // has to be in onResume()
        super.onResume()
    }

    override fun onPause() {
        Log.d(TAG, "onPause()")
        clearFavoriteList()
        super.onPause()
    }

    override fun onStop() {
        Log.d(TAG, "onStop()")
        clearFavoriteList()
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.let {
            LocalBroadcastManager.getInstance(it).apply {
                unregisterReceiver(broadcastReceiver)
            }
        }
        mediaRetriever.release()
    }

    override fun onRecyclerItemClick(v: View?, position: Int) {
        Log.d(TAG, "onRecyclerItemClick.position = $position")
        MySingleTon.favorites[position].apply {
            song.included = if (song.included == "1") "0" else "1"
            myRecyclerViewAdapter?.myNotifyItemChanged(position)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearFavoriteList() {
        MySingleTon.favorites.clear()
        myRecyclerViewAdapter?.notifyDataSetChanged()
    }

    fun searchFavorites() {
        Log.d(TAG, "searchFavorites")
        searchCompleted = false
        Thread {
            var excessYn = false
            val tempList: ArrayList<SongDescription> = ArrayList(MySingleTon.MAX_SONGS)
            activity?.let {
                DatabaseAccessUtil.readSavedSongList(it, false)?.also { sqlIt ->
                    var index = 0
                    val imageWidth = (textFontSize * 3.0f).toInt()
                    val imageHeight = (textFontSize * 3.0f).toInt()
                    for (element in sqlIt) {
                        Log.d(TAG, "searchFavorites.element.included = ${element.included}")
                        Log.d(TAG, "searchFavorites.element.filePath = ${element.filePath}")
                        var bm: Bitmap? = null
                        try {
                            val path = File(element.filePath!!).path
                            Log.d(TAG, "searchFavorites.path = $path")
                            mediaRetriever.setDataSource(element.filePath)
                            bm = mediaRetriever.getFrameAtTime(0,
                                MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                                ?.scale(imageWidth, imageHeight)
                        } catch (ex: Exception) {
                            Log.e(TAG, "searchFavorites.setDataSource.Exception:",
                                ex)
                        }
                        element.included = "0"
                        tempList.add(SongDescription(element, bm))
                        index++
                        if (index >= MySingleTon.MAX_SONGS) {
                            // excess the max
                            excessYn = true
                            break
                        }
                    }
                }
            }
            MySingleTon.favorites.clear()
            MySingleTon.favorites.addAll(tempList)
            Log.d(TAG, "searchFavorites.MySingleTon.favorites.size = ${MySingleTon.favorites.size}")

            activity?.let {
                LocalBroadcastManager.getInstance(it).apply {
                    sendBroadcast(Intent().apply {
                        action = SEARCH_FAVORITES_COMPLETED
                        putExtra(EXCESS_YN,excessYn)
                    })
                }
            }

        }.start()
    }

    private fun intentForFavoriteListActivity(): Intent {
        return Intent(activity, BaseFavoriteListActivity::class.java)
    }

    private fun initFavoriteRecyclerView() {
        Log.d(TAG, "initFavoriteRecyclerView() is called")
        activity?.let {
            val tColor = ContextCompat.getColor(it, R.color.gnt_green)
            val transparentLightGray = ContextCompat.getColor(it,
                R.color.transparentLightGray)

            myRecyclerViewAdapter = FavoriteRecyclerViewAdapter.getInstance(
                    this, textFontSize, MySingleTon.favorites,
                tColor, transparentLightGray)

            myListRecyclerView?.adapter = myRecyclerViewAdapter
            myListRecyclerView?.layoutManager = object : LinearLayoutManager(context) {
                override fun isAutoMeasureEnabled(): Boolean {
                    return false
                }
            }
        }
    }
}