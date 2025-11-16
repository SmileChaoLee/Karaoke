package com.smile.karaoke.fragments

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.scale
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.smile.karaoke.BaseFavoriteListActivity
import com.smile.karaoke.BaseSongDataActivity
import com.smile.karaoke.R
import com.smile.karaoke.adapters.FavoriteRecyclerViewAdapter
import com.smile.karaoke.adapters.MyLinearLayoutManager
import com.smile.karaoke.constants.CommonConstants
import com.smile.karaoke.constants.PlayerConstants
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
    FavoriteRecyclerViewAdapter.FavItemListener {

    companion object {
        private const val TAG : String = "FavoritesFragment"
        private const val SEARCH_FAVORITES_COMPLETED = "SearchFavorites"
        private const val EXCESS_YN = "ExcessYN"
    }

    private var textFontSize = 0.0f
    private var videoThumbnailsWidth = 0
    private var videoThumbnailsHeight = 0
    private var fragmentView : View? = null
    private var playSongs: PlaySongs? = null
    private var playMyFavorites: PlayMyFavorites? = null
    private var myListRecyclerView : RecyclerView? = null
    private var myRecyclerViewAdapter : FavoriteRecyclerViewAdapter? = null
    private lateinit var editSongsActivityLauncher: ActivityResultLauncher<Intent>
    private lateinit var broadcastReceiver: BroadcastReceiver
    private var searchCompleted = true
    private lateinit var mediaRetriever: MediaMetadataRetriever
    private var selectAllButton: ImageButton? = null
    private var unselectButton: ImageButton? = null
    private var switchDecoderButton: ImageButton? = null
    private var playSelectedButton: ImageButton? = null
    private var showVideoButton: ImageButton? = null
    private var appsImageButton: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)

        arguments?.let {}
        activity?.let {
            textFontSize = ScreenUtil.getPxTextFontSizeNeeded(it)
            videoThumbnailsWidth = (textFontSize * 3.0f).toInt()
            videoThumbnailsHeight = (textFontSize * 2.0f).toInt()
            if (it is PlaySongs) playSongs = it
            LogUtil.d(TAG, "onCreate.playSongs = $playSongs")
            if (it is PlayMyFavorites) playMyFavorites = it
            LogUtil.d(TAG, "onCreate.playMyFavorites = $playMyFavorites")
        }

        mediaRetriever = MediaMetadataRetriever()

        editSongsActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            playMyFavorites?.restorePlayingState()
            searchFavorites()
        } // update the UI

        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                LogUtil.i(TAG, "BroadcastReceiver.onReceive")
                intent?.action?.let {
                    if (it == SEARCH_FAVORITES_COMPLETED) {
                        LogUtil.d(TAG, "BroadcastReceiver.onReceive.SearchFavorites")
                        if (intent.getBooleanExtra(EXCESS_YN, false)) {
                            ScreenUtil.showToast(
                                    activity, getString(R.string.excess_max) +
                                    " ${MySingleTon.MAX_SONGS}", textFontSize,
                                ScreenUtil.FontSize_Pixel_Type,
                                Toast.LENGTH_SHORT)
                        }
                        myRecyclerViewAdapter?.myNotifyDataSetChanged()
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
            val buttonWidth = (textFontSize * 1.5f).toInt()
            myListRecyclerView = it.findViewById(R.id.myListRecyclerView)
            myListRecyclerView?.setHasFixedSize(true)
            val linearParam = LinearLayout.LayoutParams(buttonWidth, buttonWidth)
            linearParam.setMargins(0, 0, 5, 0)
            selectAllButton = it.findViewById(R.id.favoriteSelectAllButton)
            selectAllButton?.setOnClickListener {
                if (!searchCompleted) return@setOnClickListener // searching
                for (i in 0 until MySingleTon.favorites.size) {
                    MySingleTon.favorites[i].run {
                        song.included = "1"
                        myRecyclerViewAdapter?.notifyItemChanged(i)
                    }
                }
            }
            unselectButton = it.findViewById(R.id.favoriteUnselectButton)
            unselectButton?.setOnClickListener {
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
                switchIt.setOnClickListener {
                    if (!searchCompleted) return@setOnClickListener // searching
                    playSongs?.switchBetweenSoftAndHardDecoder()
                    setupSwitchDecoderButton()
                }
            }
            playSelectedButton = it.findViewById(R.id.favoritePlaySelectedButton)
            playSelectedButton?.setOnClickListener {
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
                    ScreenUtil.showToast(activity, getString(R.string.noFilesSelectedString),
                        textFontSize,
                        ScreenUtil.FontSize_Pixel_Type,
                        Toast.LENGTH_SHORT)
                } else {
                    playSongs?.playSelectedSongList(ArrayList(songs))
                }
            }
            showVideoButton = it.findViewById(R.id.showVideoImageButton)
            showVideoButton?.visibility = View.VISIBLE
            showVideoButton?.setOnClickListener {
                if (!searchCompleted) return@setOnClickListener // searching
                playSongs?.switchToPlayerView()
            }
            it.isFocusable = true
            it.isFocusableInTouchMode = true
            it.requestFocus()
            appsImageButton = it.findViewById(R.id.appsImageButton)
            appsImageButton?.visibility = View.VISIBLE
            appsImageButton?.setOnClickListener {
                playSongs?.showSmileAppsActivity()
            }
            it.isFocusable = true
            it.isFocusableInTouchMode = true
        }

        setButtonsSize()

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

    override fun onConfigurationChanged(newConfig: Configuration) {
        LogUtil.i(TAG, "onConfigurationChanged")
        setButtonsSize()
        super.onConfigurationChanged(newConfig)
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

    // implementing FavoriteRecyclerViewAdapter.FavItemListener
    override fun onItemClick(v: View?, position: Int) {
        LogUtil.i(TAG, "onItemClick.position = $position")
        MySingleTon.favorites[position].apply {
            song.included = if (song.included == "1") "0" else "1"
            myRecyclerViewAdapter?.myNotifyItemChanged(position)
        }
    }

    override fun startEditSongInfo(position: Int) {
        LogUtil.i(TAG, "startEditSongInfo.position = $position")
        val listIt = listOf(MySingleTon.favorites[position].song)
        LogUtil.d(TAG, "editButton.listIt.size = ${listIt.size}")
        if (listIt.isNotEmpty()) {
            playMyFavorites?.let {playIt ->
                intentForFavoriteListActivity().apply {
                    playIt.onSavePlayingState(component)
                    MySingleTon.selectedFavorites.clear()
                    MySingleTon.selectedFavorites.addAll(listIt)
                    editSongsActivityLauncher.launch(this)
                }
            }
        }
    }
    // end of implementing FavoriteRecyclerViewAdapter.FavItemListener

    fun clearFavoriteList() {
        MySingleTon.favorites.clear()
        myRecyclerViewAdapter?.myNotifyDataSetChanged()
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
                                ?.scale(videoThumbnailsWidth, videoThumbnailsHeight)
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

    @SuppressLint("ClickableViewAccessibility")
    private fun setButtonsSize() {
        val buttonWidth = (textFontSize*1.5f).toInt()
        var percentWidth = 1.0f
        if (resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
            percentWidth = 0.6f
        }
        val buttonLayout = fragmentView?.findViewById<LinearLayout>(R.id.favoriteListButtonLayout)
        val constrainParam = buttonLayout?.layoutParams as ConstraintLayout.LayoutParams
        constrainParam.constrainedWidth = true
        constrainParam.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        constrainParam.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        constrainParam.matchConstraintPercentWidth = percentWidth
        buttonLayout.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            LogUtil.d(TAG, "setButtonsSize.setOnTouchListener.hasFocus() = $hasFocus")
        }

        var linearParam = selectAllButton?.layoutParams as LinearLayout.LayoutParams
        linearParam.width = buttonWidth
        linearParam.height = buttonWidth
        linearParam.setMargins(0, 0, 0, 0)
        unselectButton?.layoutParams = linearParam
        switchDecoderButton?.layoutParams = linearParam
        playSelectedButton?.layoutParams = linearParam
        showVideoButton?.layoutParams = linearParam

        linearParam = appsImageButton?.layoutParams as LinearLayout.LayoutParams
        linearParam.width = buttonWidth
        linearParam.height = buttonWidth
        linearParam.setMargins(0, 0, 0, 0)
    }

    private fun intentForFavoriteListActivity(): Intent {
        return Intent(activity, BaseFavoriteListActivity::class.java)
    }

    private fun initFavoriteRecyclerView() {
        LogUtil.i(TAG, "initFavoriteRecyclerView")
        activity?.let {
            myRecyclerViewAdapter = FavoriteRecyclerViewAdapter(
                    this, MySingleTon.favorites,
                resources.configuration.orientation,
                textFontSize,
                videoThumbnailsWidth, videoThumbnailsHeight)
            myListRecyclerView?.adapter = myRecyclerViewAdapter
            myListRecyclerView?.layoutManager = MyLinearLayoutManager(context)
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