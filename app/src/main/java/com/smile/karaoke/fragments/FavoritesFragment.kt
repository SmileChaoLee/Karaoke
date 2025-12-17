package com.smile.karaoke.fragments

import android.app.Activity.RESULT_CANCELED
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Build
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
import com.smile.karaoke.BaseSongDataActivity
import com.smile.karaoke.R
import com.smile.karaoke.adapters.FavoriteRecyclerViewAdapter
import com.smile.karaoke.adapters.MyLinearLayoutManager
import com.smile.karaoke.constants.CommonConstants
import com.smile.karaoke.constants.MyPlayerConstants
import com.smile.karaoke.interfaces.PlayMyFavorites
import com.smile.karaoke.models.MySingleton
import com.smile.karaoke.models.SongDescription
import com.smile.karaoke.models.SongInfo
import com.smile.karaoke.utilities.DatabaseUtil
import com.smile.karaoke.utilities.ImageUtil
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    private lateinit var editSongInfoLauncher: ActivityResultLauncher<Intent>
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

        editSongInfoLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            playMyFavorites?.restorePlayingState()
            if (result.resultCode == RESULT_CANCELED) return@registerForActivityResult
            val act = activity ?: return@registerForActivityResult
            result.data?.extras?.let {
                val action = it.getString(CommonConstants.CRUD_ACTION)
                LogUtil.d(TAG, "editSongInfoLauncher.action = $action")
                val song = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        it.getParcelable(MyPlayerConstants.SINGLE_SONG_INFO_STATE,
                            SongInfo::class.java)
                    else it.getParcelable(MyPlayerConstants.SINGLE_SONG_INFO_STATE)
                if (song == null) return@registerForActivityResult
                lifecycleScope.launch(Dispatchers.IO) {
                    if (action == CommonConstants.SAVE_ACTION) {
                        val updNum = DatabaseUtil.updateOneSongFromSongList(act, databaseName, song)
                        LogUtil.d(TAG, "editSongInfoLauncher.save.updNum = $updNum")
                    } else if (action == CommonConstants.DELETE_ACTION) {
                        val delNum = DatabaseUtil.deleteOneSongFromSongList(act, databaseName, song)
                        LogUtil.d(TAG, "editSongInfoLauncher.delete.delNum = $delNum")
                    }
                    LogUtil.d(TAG, "editSongInfoLauncher.searchFavorites()")
                    searchFavorites()
                }
            }
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
        searchFavorites()
        initFavoriteRecyclerView()

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        LogUtil.i(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        LogUtil.i(TAG, "onResume")
        setupSwitchDecoderButton()
    }

    override fun onPause() {
        super.onPause()
        LogUtil.i(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        LogUtil.i(TAG, "onStop")
        // clearFavoriteList()
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
        val song = MySingleton.favorites[position].song
        val act = activity?: return
        playMyFavorites?.let { playIt ->
            Intent(act, BaseSongDataActivity::class.java).apply {
                playIt.onSavePlayingState(component)
                MySingleton.backupSelectedFavorites()
                LogUtil.i(TAG, "startEditSongInfo.backupSelectedId.size" +
                        "= ${MySingleton.backupSelectedId.size}")
                putExtra(MyPlayerConstants.SINGLE_SONG_INFO_STATE, song)
                editSongInfoLauncher.launch(this@apply)
            }
        }
    }
    // end of implementing FavoriteRecyclerViewAdapter.FavItemListener

    fun clearFavoriteList() {
        LogUtil.i(TAG, "clearFavoriteList")
        MySingleton.favorites.clear()
        myRecyclerViewAdapter?.myNotifyDataSetChanged()
        myListRecyclerView?.visibility = View.GONE
    }

    fun searchFavorites() {
        val logStr = "searchFavorites"
        LogUtil.i(TAG, logStr)
        lifecycleScope.launch(Dispatchers.Main) {
            val act = activity?: return@launch
            searchCompleted = false
            myListRecyclerView?.visibility = View.GONE
            loadingMsgTextView?.visibility = View.VISIBLE
            var excessYn = false
            withContext(Dispatchers.IO) {
                val tempList: ArrayList<SongDescription> = ArrayList(MySingleton.MAX_SONGS)
                val songs = DatabaseUtil.readSavedFavorites(act, databaseName, false)
                var index = 0
                for (element in songs) {
                    LogUtil.d(TAG, "$logStr.element.included = ${element.included}")
                    LogUtil.d(TAG, "$logStr.element.filePath = ${element.filePath}")
                    var bm: Bitmap? = null
                    try {
                        // val path = File(element.filePath).path
                        // LogUtil.d(TAG, "$logStr.path = $path")
                        if(isDecoderVisible) {
                            mediaRetriever.setDataSource(element.filePath)
                            bm = mediaRetriever.getFrameAtTime(
                                0,
                                MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                            )?.scale(videoThumbnailsWidth, videoThumbnailsHeight)
                        } else {
                            // used by YouTubePlayer
                            LogUtil.d(TAG, "$logStr.element.YouTubePlayer")
                            bm = ImageUtil.getBitmapFromUri(act, element.bitmapUrl)
                        }
                    } catch (ex: Exception) {
                        LogUtil.e(
                            TAG, "$logStr.setDataSource.Exception:",
                            ex
                        )
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
                MySingleton.favorites.clear()
                MySingleton.favorites.addAll(tempList)
                LogUtil.d(TAG, "$logStr.MySingleTon.favorites.size = ${MySingleton.favorites.size}")
            }
            // Update the UI
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