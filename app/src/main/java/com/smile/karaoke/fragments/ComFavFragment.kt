package com.smile.karaoke.fragments

import android.app.Activity.RESULT_CANCELED
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Bundle
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
import com.smile.karaoke.adapters.MyLayoutManager
import com.smile.karaoke.constants.CommonConstants
import com.smile.karaoke.constants.MyPlayerConstants
import com.smile.karaoke.interfaces.PlayMyFavorites
import com.smile.karaoke.models.MySingleton
import com.smile.karaoke.models.SongDescription
import com.smile.karaoke.models.SongInfo
import com.smile.karaoke.utilities.CommonUtil
import com.smile.karaoke.utilities.DatabaseUtil
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class ComFavFragment : ItemsBaseFragment(),
    FavoriteRecyclerViewAdapter.FavItemListener {

    companion object {
        private const val TAG: String = "ComFavFragment"
    }

    private val selectedSongs : ArrayList<SongInfo> = ArrayList()

    abstract fun decoderButtonVisibility(): Int
    // will be override by U2bPlayer
    open suspend fun getVideoThumbNail(song: SongInfo): Bitmap? {
        var bm: Bitmap? = null
        try {
            mediaRetriever.setDataSource(song.filePath)
            bm = mediaRetriever.getFrameAtTime(0,
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        } catch (ex: Exception) {
            LogUtil.e(TAG, "getVideoThumbNail.setDataSource.Exception:", ex)
        }
        return bm
    }
    // will be override by U2bPlayer
    open fun getFavDatabaseName(): String {
        return DatabaseUtil.getFavDatabaseName()
    }

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
                        val updNum = DatabaseUtil.updateOneSongFromSongList(act,
                            getFavDatabaseName(), song)
                        LogUtil.d(TAG, "editSongInfoLauncher.save.updNum = $updNum")
                    } else if (action == CommonConstants.DELETE_ACTION) {
                        val delNum = DatabaseUtil.deleteOneSongFromSongList(act,
                            getFavDatabaseName(), song)
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
        setProperFocus()
    }

    override fun onPause() {
        super.onPause()
        LogUtil.i(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        LogUtil.i(TAG, "onStop")
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

    private fun setProperFocus() {
        if (MySingleton.favorites.isEmpty()) {
            myListRecyclerView?.visibility = View.GONE
            showVideoButton?.post { showVideoButton?.requestFocus() }
        } else {
            myListRecyclerView?.visibility = View.VISIBLE
            myListRecyclerView?.post { myListRecyclerView?.requestFocus() }
        }
    }

    private fun updateRecyclerView() {
        myRecyclerViewAdapter?.myNotifyDataSetChanged()
        loadingMsgTextView?.visibility = View.GONE
        setProperFocus()
    }

    // implementing FavoriteRecyclerViewAdapter.FavItemListener
    override fun onItemClick(v: View?, position: Int) {
        LogUtil.i(TAG, "onItemClick.position = $position")
        val songDesc = MySingleton.favorites[position]
        songDesc.apply {
            var isUpdated = false
            if (song.included == "1") {
                song.included = "0"
                selectedSongs.remove(song)
                isUpdated = true
            } else {
                if (selectedSongs.size >= MySingleton.MAX_SONGS) {
                    ScreenUtil.showToast(
                        activity, getString(R.string.excess_max) +
                                " ${MySingleton.MAX_SONGS}", textFontSize,
                        Toast.LENGTH_SHORT
                    )
                } else {
                    song.included = "1"
                    selectedSongs.add(song)
                    isUpdated = true
                }
            }
            if (isUpdated) myRecyclerViewAdapter?.myNotifyItemChanged(position)
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
        selectedSongs.clear()
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
                val tempList: ArrayList<SongDescription> = ArrayList()
                val songs = DatabaseUtil.readSavedFavorites(act,
                    getFavDatabaseName(), false)
                var index = 0
                val fileBm = BitmapFactory.decodeResource(resources, R.drawable.video_image)
                for (element in songs) {
                    LogUtil.d(TAG, "$logStr.element.included = ${element.included}")
                    LogUtil.d(TAG, "$logStr.element.filePath = ${element.filePath}")
                    var bm = getVideoThumbNail(element)
                    if (bm == null) bm = fileBm
                    bm = bm?.scale(videoThumbNailsWidth, videoThumbNailsHeight)
                    element.included = "0"
                    tempList.add(SongDescription(element, bm))
                    index++
                    if (index >= MySingleton.MAX_FILES) {
                        // excess the max
                        withContext(Dispatchers.Main) {
                            ScreenUtil.showToast(
                                act, getString(R.string.excess_max) +
                                        " ${MySingleton.MAX_FILES}", textFontSize,
                                Toast.LENGTH_SHORT
                            )
                        }
                        excessYn = true
                        break
                    }
                }
                selectedSongs.clear()
                MySingleton.favorites.clear()
                MySingleton.favorites.addAll(tempList)
                LogUtil.d(TAG, "$logStr.MySingleTon.favorites.size = ${MySingleton.favorites.size}")
            }
            // Update the UI
            loadingMsgTextView?.visibility = View.GONE
            if (excessYn) {
                ScreenUtil.showToast(
                    act, getString(R.string.excess_max) +
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
                // myRecyclerViewAdapter?.myNotifyDataSetChanged()
                // myListRecyclerView?.visibility = View.VISIBLE
            } else {
                LogUtil.d(TAG, "$logStr.MySingleTon.favorites is empty")
                // myRecyclerViewAdapter?.myNotifyDataSetChanged()
                // myListRecyclerView?.visibility = View.GONE
                // Change the focus
                /*
                val keyEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)
                val isKeyDown: Boolean? = fragmentView?.dispatchKeyEvent(keyEvent)
                LogUtil.d(TAG, "$logStr.isKeyDown = $isKeyDown")
                showVideoButton?.requestFocus()
                */
            }
            updateRecyclerView()
            searchCompleted = true  // searching thread finished
        }
    }

    // overriding the methods of ItemsBaseFragment
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
            if (selectedSongs.isEmpty()) {
                ScreenUtil.showToast(activity,
                    getString(R.string.noFilesSelectedString),
                    textFontSize,Toast.LENGTH_SHORT)
            } else {
                val vSongs = ArrayList(selectedSongs.take(MySingleton.MAX_SONGS))
                playSongs?.playSelectedSongList(vSongs)
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

    // abstract method of ItemsBaseFragment
    override fun gridSpanCount(): Int {
        val act = activity ?: return 1
        return CommonUtil.gridSpanCount(act)
    }
    // end of overriding the methods of ItemsBaseFragment

    private fun initFavoriteRecyclerView() {
        LogUtil.i(TAG, "initFavoriteRecyclerView")
        activity?.let {
            myRecyclerViewAdapter = FavoriteRecyclerViewAdapter(this,
                MySingleton.favorites, textFontSize)
            myListRecyclerView?.itemAnimator = null
            myListRecyclerView?.adapter = myRecyclerViewAdapter
            myListRecyclerView?.layoutManager = MyLayoutManager(context, gridSpanCount())
            updateRecyclerView()
        }
    }

    private fun setupSwitchDecoderButton() {
        LogUtil.i(TAG, "setupSwitchDecoderButton")
        switchDecoderButton?.apply {
            visibility = decoderButtonVisibility()
            playSongs?.let {
                setImageResource(
                    if (it.isSoftDecoderFirst()) R.drawable.soft_decoder
                    else R.drawable.hard_decoder
                )
            }
        }
    }
}