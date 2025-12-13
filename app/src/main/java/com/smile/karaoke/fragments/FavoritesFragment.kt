package com.smile.karaoke.fragments

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.scale
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.smile.karaoke.BaseFavoriteListActivity
import com.smile.karaoke.R
import com.smile.karaoke.adapters.FavoriteRecyclerViewAdapter
import com.smile.karaoke.adapters.MyLinearLayoutManager
import com.smile.karaoke.constants.CommonConstants
import com.smile.karaoke.interfaces.PlayMyFavorites
import com.smile.karaoke.models.MySingleton
import com.smile.karaoke.models.SongDescription
import com.smile.karaoke.models.SongInfo
import com.smile.karaoke.utilities.DatabaseUtil
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

open class FavoritesFragment : ItemsBaseFragment(),
    FavoriteRecyclerViewAdapter.FavItemListener {

    companion object {
        private const val TAG: String = "FavoritesFragment"
        private const val IS_DECODER_VISIBLE = "is_decoder_visible"
        private const val DATABASE_NAME = "database_name"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param isDecoderVisible Is switchDecoderButton visible
         * @param databaseName Name of the favorite database
         * @return A new instance of fragment MyDetailFragment.
         */
        fun newInstance(isDecoderVisible: Boolean, databaseName: String): FavoritesFragment {
            return FavoritesFragment().apply {
                Bundle().also {
                    it.putBoolean(IS_DECODER_VISIBLE, isDecoderVisible)
                    it.putString(DATABASE_NAME, databaseName)
                    arguments = it
                }
            }
        }
    }

    private var isDecoderVisible = true
    private var databaseName = CommonConstants.FAVORITE_DB_NAME
    private var playMyFavorites: PlayMyFavorites? = null
    private var myListRecyclerView : RecyclerView? = null
    private var loadingMsgTextView: TextView? = null
    private var myRecyclerViewAdapter : FavoriteRecyclerViewAdapter? = null
    private lateinit var editSongsActivityLauncher: ActivityResultLauncher<Intent>
    private var selectAllButton: ImageButton? = null
    private var unselectButton: ImageButton? = null
    private var switchDecoderButton: ImageButton? = null
    private var playSelectedButton: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)

        arguments?.let { bundle ->
            isDecoderVisible = bundle.getBoolean(IS_DECODER_VISIBLE, true)
            databaseName = bundle.getString(DATABASE_NAME,CommonConstants.FAVORITE_DB_NAME)
        }

        activity?.let {
            if (it is PlayMyFavorites) playMyFavorites = it
            LogUtil.d(TAG, "onCreate.playMyFavorites = $playMyFavorites")
        }

        editSongsActivityLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()){
            playMyFavorites?.restorePlayingState()
            searchFavorites()
        }

        LogUtil.d(TAG, "onCreate.FavoriteSingleTon.favoriteList.size = ${MySingleton.favorites.size}")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        LogUtil.i(TAG, "onCreateView")
        return inflater.inflate(R.layout.fragment_my_favorites,
            container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "onViewCreated")

        view.let {
            myListRecyclerView = it.findViewById(R.id.myListRecyclerView)
            myListRecyclerView?.setHasFixedSize(true)
            myListRecyclerView?.visibility = View.GONE
            loadingMsgTextView = it.findViewById(R.id.loadingMsgTextView)
            ScreenUtil.resizeTextSize(loadingMsgTextView, textFontSize * 2f)
            loadingMsgTextView?.visibility = View.GONE
            selectAllButton = it.findViewById(R.id.favoriteSelectAllButton)
            unselectButton = it.findViewById(R.id.favoriteUnselectButton)
            switchDecoderButton = it.findViewById(R.id.favoriteSwitchDecoderButton)
            setupSwitchDecoderButton()
            playSelectedButton = it.findViewById(R.id.favoritePlaySelectedButton)
            showVideoButton = it.findViewById(R.id.showVideoImageButton)
            showVideoButton?.visibility = View.VISIBLE
            exitImageButton = it.findViewById(R.id.exitImageButton)
            exitImageButton?.visibility = View.VISIBLE
        }
        initFavoriteRecyclerView()

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        LogUtil.i(TAG, "onStart")
        searchFavorites()   // has to be in onResume()
    }

    override fun onResume() {
        super.onResume()
        LogUtil.i(TAG, "onResume")
        setupSwitchDecoderButton()
        // searchFavorites()   // has to be in onResume()
    }

    override fun onPause() {
        super.onPause()
        LogUtil.i(TAG, "onPause")
        // clearFavoriteList()
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
        clearFavoriteList()
        mediaRetriever.release()
    }

    // implementing FavoriteRecyclerViewAdapter.FavItemListener
    override fun onItemClick(v: View?, position: Int) {
        LogUtil.i(TAG, "onItemClick.position = $position")
        MySingleton.favorites[position].apply {
            song.included = if (song.included == "1") "0" else "1"
            myRecyclerViewAdapter?.myNotifyItemChanged(position)
        }
    }

    override fun startEditSongInfo(position: Int) {
        LogUtil.i(TAG, "startEditSongInfo.position = $position")
        val listIt = listOf(MySingleton.favorites[position].song)
        LogUtil.d(TAG, "editButton.listIt.size = ${listIt.size}")
        if (listIt.isNotEmpty()) {
            playMyFavorites?.let {playIt ->
                intentForFavoriteListActivity().apply {
                    playIt.onSavePlayingState(component)
                    MySingleton.backupSelectedFavorites()
                    LogUtil.i(TAG, "startEditSongInfo.backupSelectedId.size" +
                            "= ${MySingleton.backupSelectedId.size}")
                    MySingleton.selectedFavorites.clear()
                    MySingleton.selectedFavorites.addAll(listIt)
                    editSongsActivityLauncher.launch(this)
                }
            }
        }
    }
    // end of implementing FavoriteRecyclerViewAdapter.FavItemListener

    fun clearFavoriteList() {
        MySingleton.favorites.clear()
        myRecyclerViewAdapter?.myNotifyDataSetChanged()
        myListRecyclerView?.visibility = View.GONE
    }

    fun searchFavorites() {
        val logStr = "searchFavorites"
        LogUtil.i(TAG, logStr)
        searchCompleted = false
        myListRecyclerView?.visibility = View.GONE
        loadingMsgTextView?.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            var excessYn = false
            val tempList: ArrayList<SongDescription> = ArrayList(MySingleton.MAX_SONGS)
            activity?.let {
                DatabaseUtil.readSavedFavorites(it,
                    databaseName, false).also { sqlIt ->
                    var index = 0
                    for (element in sqlIt) {
                        LogUtil.d(TAG, "$logStr.element.included = ${element.included}")
                        LogUtil.d(TAG, "$logStr.element.filePath = ${element.filePath}")
                        var bm: Bitmap? = null
                        try {
                            val path = File(element.filePath).path
                            LogUtil.d(TAG, "$logStr.path = $path")
                            mediaRetriever.setDataSource(element.filePath)
                            bm = mediaRetriever.getFrameAtTime(0,
                                MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                                ?.scale(videoThumbnailsWidth, videoThumbnailsHeight)
                        } catch (ex: Exception) {
                            LogUtil.e(TAG, "$logStr.setDataSource.Exception:",
                                ex)
                        }
                        element.included = "0"
                        tempList.add(SongDescription(element, bm))
                        index++
                        if (index >= MySingleton.MAX_SONGS) {
                            // excess the max
                            excessYn = true
                            break
                        }
                    }
                }
            }
            MySingleton.favorites.clear()
            MySingleton.favorites.addAll(tempList)
            LogUtil.d(TAG, "$logStr.MySingleTon.favorites.size = ${MySingleton.favorites.size}")

            // Update the UI
            withContext(Dispatchers.Main) {
                loadingMsgTextView?.visibility = View.GONE
                if (excessYn) {
                    ScreenUtil.showToast(
                        activity, getString(R.string.excess_max) +
                                " ${MySingleton.MAX_SONGS}", textFontSize,
                        Toast.LENGTH_SHORT)
                }
                if (MySingleton.favorites.isNotEmpty()) {
                    for (fav in MySingleton.favorites) {
                        LogUtil.d(TAG, "$logStr.fav.song.id = ${fav.song.id}")
                        if (MySingleton.backupSelectedId.contains(fav.song.id)) {
                            LogUtil.d(TAG, "$logStr.contains")
                            fav.song.included = "1"
                        }
                    }
                    myRecyclerViewAdapter?.myNotifyDataSetChanged()
                    myListRecyclerView?.visibility = View.VISIBLE
                } else {
                    LogUtil.d(TAG, "$logStr.MySingleTon.favorites is empty")
                    myRecyclerViewAdapter?.myNotifyDataSetChanged()
                    myListRecyclerView?.visibility = View.GONE
                    // Change the focus
                    val keyEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)
                    val isKeyDown: Boolean? = fragmentView?.dispatchKeyEvent(keyEvent)
                    LogUtil.d(TAG, "$logStr.isKeyDown = $isKeyDown")
                    showVideoButton?.requestFocus()
                }
                searchCompleted = true  // searching thread finished
            }
        }
    }

    override fun setClickListeners() {
        selectAllButton?.setOnClickListener {
            if (!searchCompleted) return@setOnClickListener // searching
            for (i in 0 until MySingleton.favorites.size) {
                MySingleton.favorites[i].run {
                    song.included = "1"
                    myRecyclerViewAdapter?.notifyItemChanged(i)
                }
            }
        }
        unselectButton?.setOnClickListener {
            if (!searchCompleted) return@setOnClickListener // searching
            for (i in 0 until MySingleton.favorites.size) {
                MySingleton.favorites[i].run {
                    song.included = "0"
                    myRecyclerViewAdapter?.notifyItemChanged(i)
                }
            }
        }
        switchDecoderButton?.let {switchIt ->
            switchIt.setOnClickListener {
                if (!searchCompleted) return@setOnClickListener // searching
                playSongs?.switchBetweenSoftAndHardDecoder()
                setupSwitchDecoderButton()
            }
        }
        playSelectedButton?.setOnClickListener {
            if (!searchCompleted) return@setOnClickListener // searching
            // open the files to play
            val songs = ArrayList<SongInfo>().also { songIt ->
                var index = 0
                for (i in 0 until MySingleton.favorites.size) {
                    if (MySingleton.favorites[i].song.included == "1") {
                        songIt.add(MySingleton.favorites[i].song)
                        index++
                        if (index >= MySingleton.MAX_SONGS) {
                            // excess the max
                            ScreenUtil.showToast(
                                activity, getString(R.string.excess_max) +
                                        " ${MySingleton.MAX_SONGS}", textFontSize,
                                Toast.LENGTH_SHORT)
                            break
                        }
                    }
                }
            }
            if (songs.isEmpty()) {
                ScreenUtil.showToast(activity, getString(R.string.noFilesSelectedString),
                    textFontSize,
                    Toast.LENGTH_SHORT)
            } else {
                playSongs?.playSelectedSongList(ArrayList(songs))
            }
        }

        super.setClickListeners()
    }

    override fun setButtonsSize() {
        buttonLayout = fragmentView?.findViewById(R.id.favoriteListButtonLayout)
        super.setButtonsSize()
        selectAllButton?.layoutParams = buttonParam
        unselectButton?.layoutParams = buttonParam
        switchDecoderButton?.layoutParams = buttonParam
        playSelectedButton?.layoutParams = buttonParam
    }

    private fun intentForFavoriteListActivity(): Intent {
        return Intent(activity, BaseFavoriteListActivity::class.java)
    }

    private fun initFavoriteRecyclerView() {
        LogUtil.i(TAG, "initFavoriteRecyclerView")
        activity?.let {
            myRecyclerViewAdapter = FavoriteRecyclerViewAdapter(
                    this, MySingleton.favorites,
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
            visibility = if (isDecoderVisible) View.VISIBLE else View.GONE
            playSongs?.let {
                setImageResource(
                    if (it.isSoftDecoderFirst()) R.drawable.soft_decoder
                    else R.drawable.hard_decoder
                )
            }
        }
    }
}