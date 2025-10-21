package com.smile.karaoke.fragments

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.view.KeyEvent
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
import com.smile.karaoke.SmileAppBase
import com.smile.karaoke.adapters.FavoriteRecyclerViewAdapter
import com.smile.karaoke.interfaces.PlayMyFavorites
import com.smile.karaoke.interfaces.PlaySongs
import com.smile.karaoke.models.MySingleTon
import com.smile.karaoke.models.SongDescription
import com.smile.karaoke.models.SongInfo
import com.smile.karaoke.utilities.DatabaseAccessUtil
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import java.io.File

class FavoritesFragment : Fragment(),
    FavoriteRecyclerViewAdapter.OnRecyclerItemClickListener {

    companion object {
        private const val TAG : String = "FavoritesFragment"
        private const val SEARCH_FAVORITES_COMPLETED = "SearchFavorites"
        private const val EXCESS_YN = "ExcessYN"
    }

    private var fragmentView : View? = null
    private var playSongs: PlaySongs? = null
    private var playMyFavorites: PlayMyFavorites? = null
    private var myListRecyclerView : RecyclerView? = null
    private var myRecyclerViewAdapter : FavoriteRecyclerViewAdapter? = null
    private lateinit var editSongsActivityLauncher: ActivityResultLauncher<Intent>
    private lateinit var broadcastReceiver: BroadcastReceiver
    private var searchCompleted = true
    private lateinit var mediaRetriever: MediaMetadataRetriever
    private var showVideoButton: ImageButton? = null
    private var switchDecoderButton: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        arguments?.let {
        }

        mediaRetriever = MediaMetadataRetriever()

        SmileAppBase.videoThumbnailsWidth = (SmileAppBase.textFontSize * 3.0f).toInt()
        SmileAppBase.videoThumbnailsHeight = (SmileAppBase.textFontSize * 2.0f).toInt()

        activity?.let {
            if (it is PlaySongs) playSongs = it
            LogUtil.d(TAG, "onCreate.playSongs = $playSongs")
            if (it is PlayMyFavorites) playMyFavorites = it
            LogUtil.d(TAG, "onCreate.playMyFavorites = $playMyFavorites")
        }

        editSongsActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            playMyFavorites?.restorePlayingState()
            searchFavorites()
        } // update the UI }

        object : BroadcastReceiver() {
            @SuppressLint("NotifyDataSetChanged")
            override fun onReceive(context: Context?, intent: Intent?) {
                LogUtil.i(TAG, "BroadcastReceiver.onReceive")
                intent?.action?.let {
                    if (it == SEARCH_FAVORITES_COMPLETED) {
                        LogUtil.d(TAG, "BroadcastReceiver.onReceive.SearchFavorites")
                        if (intent.getBooleanExtra(EXCESS_YN, false)) {
                            ScreenUtil.showToast(
                                    activity, getString(R.string.excess_max) +
                                    " ${MySingleTon.MAX_SONGS}", SmileAppBase.textFontSize,
                                ScreenUtil.FontSize_Pixel_Type,
                                Toast.LENGTH_SHORT)
                        }
                        myRecyclerViewAdapter?.notifyDataSetChanged()
                        searchCompleted = true  // searching thread finished
                        if (MySingleTon.favorites.isEmpty()) {
                            LogUtil.d(TAG, "BroadcastReceiver.onReceive.MySingleTon.favorites is empty")
                            // Change the focus
                            val keyEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)
                            val isKeyDown: Boolean? = fragmentView?.dispatchKeyEvent(keyEvent)
                            LogUtil.d(TAG, "BroadcastReceiver.onReceive.isKeyDown = $isKeyDown")
                            showVideoButton?.requestFocus()
                        }
                    }
                }
            }
        }.also { broadcastReceiver = it }
        activity?.let {
            LocalBroadcastManager.getInstance(it).apply {
                LogUtil.d(TAG, "LocalBroadcastManager.registerReceiver")
                registerReceiver(broadcastReceiver, IntentFilter().apply {
                    addAction(SEARCH_FAVORITES_COMPLETED)
                })
            }
        }

        LogUtil.d(TAG, "onCreate.FavoriteSingleTon.favoriteList.size = ${MySingleTon.favorites.size}")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        LogUtil.i(TAG, "onCreateView")
        return inflater.inflate(R.layout.fragment_my_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "onViewCreated")
        super.onViewCreated(view, savedInstanceState)

        fragmentView = view
        fragmentView?.let {
            val buttonWidth = (SmileAppBase.textFontSize * 1.5f).toInt()
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
            switchDecoderButton = it.findViewById(R.id.favoriteSwitchDecoderButton)
            setupSwitchDecoderButton()
            switchDecoderButton?.let {switchIt ->
                layoutParams = switchIt.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.width = buttonWidth
                layoutParams.height = buttonWidth
                switchIt.layoutParams = layoutParams
                switchIt.setOnClickListener {
                    if (!searchCompleted) return@setOnClickListener // searching
                    playSongs?.switchBetweenSoftAndHardDecoder()
                    setupSwitchDecoderButton()
                }
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
                                        " ${MySingleTon.MAX_SONGS}", SmileAppBase.textFontSize,
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
                        SmileAppBase.textFontSize,
                        ScreenUtil.FontSize_Pixel_Type,
                        Toast.LENGTH_SHORT)
                } else {
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
                                LogUtil.d(TAG, "editButton.listIt.size = ${listIt.size}")
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
                                activity, getString(R.string.noFilesSelectedString), SmileAppBase.textFontSize,
                            ScreenUtil.FontSize_Pixel_Type,
                            Toast.LENGTH_SHORT)
                    }
                }
            }
            showVideoButton = it.findViewById(R.id.showVideoImageButton)
            layoutParams = showVideoButton?.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.width = buttonWidth
            layoutParams.height = buttonWidth
            showVideoButton?.layoutParams = layoutParams
            showVideoButton?.visibility = View.VISIBLE
            showVideoButton?.setOnClickListener {
                if (!searchCompleted) return@setOnClickListener // searching
                playSongs?.switchToPlayerView()
            }
            it.isFocusable = true
            it.isFocusableInTouchMode = true
            it.requestFocus()
        }

        initFavoriteRecyclerView()
    }

    override fun onStart() {
        super.onStart()
        LogUtil.i(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        LogUtil.i(TAG, "onResume")
        setupSwitchDecoderButton()
        searchFavorites()   // has to be in onResume()
    }

    override fun onPause() {
        super.onPause()
        LogUtil.i(TAG, "onPause")
        clearFavoriteList()
    }

    override fun onStop() {
        super.onStop()
        LogUtil.i(TAG, "onStop")
        clearFavoriteList()
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.i(TAG, "onStop")
        activity?.let {
            LocalBroadcastManager.getInstance(it).apply {
                unregisterReceiver(broadcastReceiver)
            }
        }
        mediaRetriever.release()
    }

    override fun onRecyclerItemClick(v: View?, position: Int) {
        LogUtil.i(TAG, "onRecyclerItemClick.position = $position")
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
        LogUtil.i(TAG, "searchFavorites")
        searchCompleted = false
        Thread {
            var excessYn = false
            val tempList: ArrayList<SongDescription> = ArrayList(MySingleTon.MAX_SONGS)
            activity?.let {
                DatabaseAccessUtil.readSavedSongList(it, false)?.also { sqlIt ->
                    var index = 0
                    for (element in sqlIt) {
                        LogUtil.d(TAG, "searchFavorites.element.included = ${element.included}")
                        LogUtil.d(TAG, "searchFavorites.element.filePath = ${element.filePath}")
                        var bm: Bitmap? = null
                        try {
                            val path = File(element.filePath!!).path
                            LogUtil.d(TAG, "searchFavorites.path = $path")
                            mediaRetriever.setDataSource(element.filePath)
                            bm = mediaRetriever.getFrameAtTime(0,
                                MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                                ?.scale(SmileAppBase.videoThumbnailsWidth, SmileAppBase.videoThumbnailsHeight)
                        } catch (ex: Exception) {
                            LogUtil.e(TAG, "searchFavorites.setDataSource.Exception:",
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
            LogUtil.d(TAG, "searchFavorites.MySingleTon.favorites.size = ${MySingleTon.favorites.size}")

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
        LogUtil.i(TAG, "initFavoriteRecyclerView")
        activity?.let {
            val tColor = ContextCompat.getColor(it, R.color.gnt_green)
            val transparentLightGray = ContextCompat.getColor(it,
                R.color.transparentLightGray)

            myRecyclerViewAdapter = FavoriteRecyclerViewAdapter.getInstance(
                    this, MySingleTon.favorites,
                tColor, transparentLightGray)

            myListRecyclerView?.adapter = myRecyclerViewAdapter
            myListRecyclerView?.layoutManager = object : LinearLayoutManager(context) {
                override fun isAutoMeasureEnabled(): Boolean {
                    return false
                }
            }
        }
    }

    fun setupSwitchDecoderButton() {
        LogUtil.i(TAG, "setupSwitchDecoderButton")
        switchDecoderButton?.apply {
            playSongs?.let {
                setImageResource(
                    if (it.isSoftDecoderFirst()) R.drawable.soft_decoder
                    else R.drawable.hard_decoder
                )
            }
        }
    }
}