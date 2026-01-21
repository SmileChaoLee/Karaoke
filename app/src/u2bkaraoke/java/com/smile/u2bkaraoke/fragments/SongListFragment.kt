package com.smile.u2bkaraoke.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smile.karaoke.R
import com.smile.karaoke.interfaces.RecyclerItemListener
import com.smile.karaoke.models.MySingleton
import com.smile.karaoke.models.SongInfo
import com.smile.karaoke.utilities.DatabaseUtil
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.u2bkaraoke.U2bKaraokeApp.Companion.appCompBuilder
import com.smile.u2bkaraoke.u2bkaok_constants.U2bKKConstants
import com.smile.u2bkaraoke.model.Language
import com.smile.u2bkaraoke.model.Singer
import com.smile.u2bkaraoke.model.SongList
import com.smile.u2bkaraoke.retrofit.RestApiAsync
import com.smile.u2bkaraoke.adapters.SongListAdapter
import com.smile.u2bkaraoke.model.Song
import com.smile.u2bkaraoke.retrofit.RestApiSync
import com.smile.u2bplayer.u2bplay_constants.U2bPlayConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Response
import javax.inject.Inject

open class SongListFragment : U2bKKBaseFragment(), RecyclerItemListener {

    companion object {
        private const val TAG = "SongListFragment"
    }

    @JvmField
    @Inject
    var myViewAdapter: SongListAdapter? = null
    private var mRecyclerView: RecyclerView? = null
    private var searchCompleted = true
    private var searchEditText: EditText? = null
    private var filterString: String? = null
    private var songListEmptyTextView: TextView? = null
    private var firstPageButton: Button? = null
    private var previousPageButton: Button? = null
    private var nextPageButton: Button? = null
    private var lastPageButton: Button? = null
    private var pageNoTextView: TextView? = null
    private var unselectButton: ImageButton? = null
    private var playSelectedButton: ImageButton? = null
    private var addToFavoriteButton: ImageButton? = null
    lateinit var songList: SongList
    private var singer: Singer? = null
    private var language: Language? = null
    private var objectPassed: Any? = null
    private var orderedFrom = 0
    private var activityTitle = ""
    private var numOfWords = 0
    private var pageNo = 1
    private var pageSize = 10
    private var totalPages = 0
    private val selectedSongs : ArrayList<SongInfo> = ArrayList()
    // private var restApi: MyRestApi? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        orderedFrom = 0 // default value
        numOfWords = 0
        arguments?.let { args ->
            orderedFrom = args.getInt(U2bKKConstants.OrderedFrom, 0)
            activityTitle = args.getString(U2bKKConstants.SongListTitle, "").trim()
            when (orderedFrom) {
                U2bKKConstants.SingerOrdered -> {
                    singer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        args.getParcelable(U2bKKConstants.SingerParcelable, Singer::class.java)
                    } else args.getParcelable(U2bKKConstants.SingerParcelable)
                    objectPassed = singer
                }
                // Constants.NewSongOrdered -> objectPassed = language
                U2bKKConstants.NewSongLanguageOrdered, U2bKKConstants.HotSongLanguageOrdered -> {
                    language = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        args.getParcelable(U2bKKConstants.LanguageParcelable, Language::class.java)
                    } else args.getParcelable(U2bKKConstants.LanguageParcelable)
                    objectPassed = language
                    LogUtil.i(TAG, "onCreate.NewSongLanguageOrdered.language = $language")
                }
                // Constants.HotSongOrdered -> objectPassed = null
                U2bKKConstants.LanguageOrdered, U2bKKConstants.LanguageWordsOrdered -> {
                    language = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        args.getParcelable(U2bKKConstants.LanguageParcelable, Language::class.java)
                    } else args.getParcelable(U2bKKConstants.LanguageParcelable)
                    objectPassed = language
                    numOfWords = args.getInt(U2bKKConstants.NumOfWords)
                }
            }
        }
        if (orderedFrom == U2bKKConstants.ALL_SONG_ORDERED) {
            activityTitle = getString(R.string.allSongsString)
        }
        LogUtil.d(TAG, "onCreate.orderedFrom = $orderedFrom")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        LogUtil.i(TAG, "onCreateView")
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_song_list,
            container, false)
        return view
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        LogUtil.d(TAG, "onViewCreated")
        textFontSize = ScreenUtil.getPxTextFontSizeNeeded(activity)
        songList = SongList()

        view.apply {
            val songsListMenuTextView = findViewById<TextView>(R.id.songsListMenuTextView)
            ScreenUtil.resizeTextSize(songsListMenuTextView, textFontSize)
            songsListMenuTextView.text = activityTitle

            filterString = ""
            searchEditText = findViewById(R.id.songSearchEditText)
            searchEditText?.let { sEt ->
                ScreenUtil.resizeTextSize(sEt, textFontSize)
                sEt.setText(filterString)
                sEt.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {
                        LogUtil.d(TAG, "addTextChangedListener.beforeTextChanged")
                    }
                    override fun onTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {
                        LogUtil.d(TAG, "addTextChangedListener.onTextChanged")
                    }
                    override fun afterTextChanged(editable: Editable) {
                        LogUtil.d(TAG, "addTextChangedListener.afterTextChanged")
                        val content = editable.toString().trim()
                        filterString = if (content.isEmpty()) "" else "SongNa+$content"
                        LogUtil.d(TAG, "addTextChangedListener.afterTextChanged.filterString = $filterString")
                        pageNo = 1
                        retrieveSongList(true)
                    }
                })
            }
            mRecyclerView = findViewById(R.id.songListRecyclerView)
            songListEmptyTextView = findViewById(R.id.songListEmptyTextView)
            ScreenUtil.resizeTextSize(songListEmptyTextView, textFontSize)
            songListEmptyTextView?.visibility = View.GONE
            val smallButtonFontSize = textFontSize * 0.7f
            firstPageButton = findViewById(R.id.firstPageButton)
            ScreenUtil.resizeTextSize(firstPageButton, smallButtonFontSize)
            previousPageButton = findViewById(R.id.previousPageButton)
            ScreenUtil.resizeTextSize(previousPageButton, smallButtonFontSize)
            nextPageButton = findViewById(R.id.nextPageButton)
            ScreenUtil.resizeTextSize(nextPageButton, smallButtonFontSize)
            lastPageButton = findViewById(R.id.lastPageButton)
            ScreenUtil.resizeTextSize(lastPageButton, smallButtonFontSize)
            pageNoTextView = findViewById(R.id.pageNoTotal)
            ScreenUtil.resizeTextSize(pageNoTextView, smallButtonFontSize)
            unselectButton = findViewById(R.id.songUnselectButton)
            playSelectedButton = findViewById(R.id.songPlaySelectedButton)
            addToFavoriteButton = findViewById(R.id.songAddToFavButton)
        }

        super.onViewCreated(view, savedInstanceState)

        firstPageButton?.nextFocusUpId = R.id.songListRecyclerView
        previousPageButton?.nextFocusUpId = R.id.songListRecyclerView
        nextPageButton?.nextFocusUpId = R.id.songListRecyclerView
        lastPageButton?.nextFocusUpId = R.id.songListRecyclerView
        setFucusDirection()

        appCompBuilder
            .recyclerItemListenerModule(this@SongListFragment)
            .songArrayListModule(songList.songs)
            .floatModule(textFontSize).build()
            .inject(this@SongListFragment)
        mRecyclerView?.apply {
            setAdapter(myViewAdapter)
            setLayoutManager(LinearLayoutManager(requireContext()))
        }

        // restApi = MyRestApi()
        retrieveSongList()
    }

    private fun setFucusDirection() {
        val showVisible = showVideoButton?.isVisible ?: false
        exitImageButton?.let { exitB ->
            exitB.nextFocusUpId = R.id.nextPageButton
            if (showVisible) exitB.nextFocusRightId = R.id.u2bKShowVideoButton
            else exitB.nextFocusRightId = R.id.songUnselectButton
        }
        showVideoButton?.let { showB ->
            showB.nextFocusUpId = R.id.nextPageButton
            showB.nextFocusRightId = R.id.songUnselectButton
            showB.nextFocusLeftId = R.id.u2bKExitButton
        }
        unselectButton?.let { unB ->
            if (showVisible) unB.nextFocusLeftId = R.id.u2bKShowVideoButton
            else unB.nextFocusLeftId = R.id.u2bKExitButton
        }
    }

    override fun onDestroy() {
        LogUtil.i(TAG, "onDestroy")
        selectedSongs.clear()
        songList.songs.clear()
        super.onDestroy()
    }

    override fun setClickListeners() {
        firstPageButton?.setOnClickListener { firstPage() }
        previousPageButton?.setOnClickListener { previousPage() }
        nextPageButton?.setOnClickListener { nextPage() }
        lastPageButton?.setOnClickListener { lastPage() }
        unselectButton?.setOnClickListener {
            val logStr = "unselectButton.setOnClickListener"
            LogUtil.d(TAG, "$logStr.searchCompleted = $searchCompleted")
            if (!searchCompleted) return@setOnClickListener // searching
            songList.let { list ->
                val songs = list.songs
                for (i in 0 until songs.size) {
                    if (songs[i].vodYn.uppercase() == "Y") {
                        songs[i].vodYn = "N"
                        myViewAdapter?.myNotifyItemChanged(i)
                    }
                }
            }
        }
        playSelectedButton?.setOnClickListener {
            val logStr = "playSelectedButton.setOnClickListener"
            LogUtil.d(TAG, "$logStr.searchCompleted = $searchCompleted")
            if (!searchCompleted) return@setOnClickListener // searching
            val act = activity?: return@setOnClickListener
            LogUtil.d(TAG, "$logStr.songs.size = ${selectedSongs.size}")
            if (selectedSongs.isEmpty()) {
                ScreenUtil.showToast(act,
                    getString(R.string.noFilesSelectedString),
                    textFontSize, Toast.LENGTH_SHORT)
            } else {
                val vSongs = ArrayList(selectedSongs.take(MySingleton.MAX_SONGS))
                playSongs?.playSelectedSongList(vSongs)
                setShowVideoButtonVisibility()
                setFucusDirection()
            }
        }
        addToFavoriteButton?.setOnClickListener {
            val logStr = "addToFavoriteButton.setOnClickListener"
            LogUtil.d(TAG, "$logStr.searchCompleted = $searchCompleted")
            if (!searchCompleted) return@setOnClickListener // searching
            val act = activity?: return@setOnClickListener
            if (selectedSongs.isEmpty()) {
                ScreenUtil.showToast( act,
                    getString(R.string.noFilesSelectedString),
                    textFontSize,
                    Toast.LENGTH_SHORT)
            } else {
                lifecycleScope.launch(Dispatchers.IO) {
                    if (DatabaseUtil.addSongsToFavorites(act,
                            U2bPlayConstants.U2B_FAV_DB_NAME,
                            selectedSongs)) {
                        withContext(Dispatchers.Main) {
                            ScreenUtil.showToast(
                                act,
                                getString(R.string.add_to_favorites),
                                textFontSize, Toast.LENGTH_SHORT
                            )
                        }
                    }
                }
            }
        }
        super.setClickListeners()
    }

    override fun setButtonsSize() {
        super.setButtonsSize()
        unselectButton?.layoutParams = buttonParam
        playSelectedButton?.layoutParams = buttonParam
        addToFavoriteButton?.layoutParams = buttonParam
    }

    override fun onItemClick(v: View?, position: Int) {
        LogUtil.i(TAG, "onItemClick.position = $position")
        if (position < 0) return
        val act = activity ?: return
        // val fragContainerId = this.id   // container id of the fragment
        // val fragManager = act.supportFragmentManager
        songList.let { list ->
            val song = list.songs[position]
            ScreenUtil.showToast(act, song.songNa,
                textFontSize, Toast.LENGTH_SHORT)
            val songInfo = dataSongToSongInfo(song)
            var isUpdated = false
            if (song.vodYn.uppercase() == "Y") {
                song.vodYn = "N"
                selectedSongs.remove(songInfo)
                isUpdated = true
            } else {
                if (selectedSongs.size >= MySingleton.MAX_SONGS) {
                    ScreenUtil.showToast(
                        activity, getString(R.string.excess_max) +
                                " ${MySingleton.MAX_SONGS}", textFontSize,
                        Toast.LENGTH_SHORT)
                } else {
                    song.vodYn = "Y"
                    selectedSongs.add(songInfo)
                    isUpdated = true
                }
            }
            if (isUpdated) myViewAdapter?.notifyItemChanged(position)
        }
    }

    override fun myBackgroundColor(position: Int): Int {
        if (position < 0) return super.myBackgroundColor(position)
        if (position >= songList.songs.size) return super.myBackgroundColor(position)
        val song = songList.songs[position]
        return if (song.vodYn == "Y") Color.rgb(0x00, 0xff, 0x00)
            else super.myBackgroundColor(position)
    }

    @SuppressLint("SetTextI18n")
    private fun retrieveSongList(isSearch: Boolean = false) {
        val logStr = "retrieveSingerList"
        LogUtil.d(TAG, "$logStr.orderedFrom = $orderedFrom")
        LogUtil.d(TAG, "$logStr.filterString = $filterString")
        val act = activity ?: return
        act.lifecycleScope.launch(Dispatchers.Main) {
            searchCompleted = false
            mRecyclerView?.visibility = View.GONE
            songListEmptyTextView?.visibility = View.VISIBLE
            songListEmptyTextView?.text = act.getString(R.string.loadingString)
            var tempList: SongList? = null
            withContext(Dispatchers.IO) {
                val restApi = RestApiSync.getApiSync()
                when (orderedFrom) {
                    U2bKKConstants.ALL_SONG_ORDERED -> {
                        LogUtil.d(TAG, "$logStr.ALL_SONG_ORDERED")
                        restApi.let { rApi ->
                            val song = objectPassed as? Song ?: Song()
                            tempList = if (filterString.isNullOrEmpty()) {
                                rApi.getSongs(pageSize, pageNo)
                            } else {
                                // rApi.getSongs(pageSize, pageNo, filterString!!)
                                rApi.getSongs(pageSize, pageNo)
                            }
                            objectPassed = song
                        }
                    }
                    U2bKKConstants.SingerOrdered -> {
                        LogUtil.d(TAG, "$logStr.SingerOrdered")
                        restApi.let { rApi ->
                            val singer = objectPassed as? Singer ?: Singer()
                            tempList = if (filterString.isNullOrEmpty()) {
                                rApi.getSongsBySinger(singer, pageSize, pageNo)
                            } else {
                                rApi.getSongsBySinger(singer, pageSize, pageNo, filterString!!)
                            }
                            objectPassed = singer
                        }
                    }
                    U2bKKConstants.NewSongLanguageOrdered -> {
                        LogUtil.d(TAG, "$logStr.NewSongLanguageOrdered")
                        restApi.let { rApi ->
                            val language = objectPassed as? Language ?: Language()
                            tempList = if (filterString.isNullOrEmpty()) {
                                rApi.getNewSongsByLanguage(language, pageSize, pageNo)
                            } else {
                                rApi.getNewSongsByLanguage(
                                    language,
                                    pageSize,
                                    pageNo,
                                    filterString!!
                                )
                            }
                            objectPassed = language
                        }
                    }
                    U2bKKConstants.HotSongLanguageOrdered -> {
                        LogUtil.d(TAG, "$logStr.HotSongLanguageOrdered")
                        restApi.let { rApi ->
                            val language = objectPassed as? Language ?: Language()
                            tempList = if (filterString.isNullOrEmpty()) {
                                rApi.getHotSongsByLanguage(language, pageSize, pageNo)
                            } else {
                                rApi.getHotSongsByLanguage(
                                    language,
                                    pageSize,
                                    pageNo,
                                    filterString!!
                                )
                            }
                            objectPassed = language
                        }
                    }
                    U2bKKConstants.LanguageOrdered -> {
                        LogUtil.d(TAG, "$logStr.LanguageOrdered")
                        restApi.let { rApi ->
                            val language = objectPassed as? Language ?: Language()
                            tempList = if (filterString.isNullOrEmpty()) {
                                rApi.getSongsByLanguage(language, pageSize, pageNo)
                            } else {
                                rApi.getSongsByLanguage(language, pageSize, pageNo, filterString!!)
                            }
                            objectPassed = language
                        }
                    }
                    U2bKKConstants.LanguageWordsOrdered -> {
                        LogUtil.d(TAG, "$logStr.LanguageWordsOrdered")
                        restApi.let { rApi ->
                            val language = objectPassed as? Language ?: Language()
                            tempList = if (filterString.isNullOrEmpty()) {
                                rApi.getSongsByLanguageNumOfWords(
                                    language,
                                    numOfWords,
                                    pageSize,
                                    pageNo
                                )
                            } else {
                                rApi.getSongsByLanguageNumOfWords(
                                    language,
                                    numOfWords,
                                    pageSize,
                                    pageNo,
                                    filterString!!
                                )
                            }
                            objectPassed = language
                        }
                    }
                }   // end of when(), finished the retrieving song list from server
            }
            // update the UI
            withContext(Dispatchers.Main) {
                tempList?.let { sList ->
                    if (sList.songs.isEmpty()) {
                        songListEmptyTextView?.text = act.getString(R.string.noResultString)
                        songListEmptyTextView?.visibility = View.VISIBLE
                    } else {
                        songListEmptyTextView?.visibility = View.GONE
                        for (song in sList.songs) {
                            // use song.vodYn for the selection by user
                            song.vodYn = "N"    // set to unselected
                        }
                    }
                } ?: run{
                    tempList = SongList()
                    songListEmptyTextView?.text = act.getString(R.string.failedMessage)
                    songListEmptyTextView?.visibility = View.VISIBLE
                }
                LogUtil.d(TAG, "$logStr.tempList.songs.size = ${tempList?.songs?.size}")
                selectedSongs.clear()
                songList.songs.clear()
                tempList?.let { tempList ->
                    songList.pageNo = tempList.pageNo
                    songList.pageSize = tempList.pageSize
                    songList.totalRecords = tempList.totalRecords
                    songList.totalPages = tempList.totalPages
                    songList.songs.addAll(tempList.songs)
                }
                myViewAdapter?.myNotifyDataSetChanged()
                updateRecyclerView()
                pageNo = songList.pageNo
                pageSize = songList.pageSize
                totalPages = songList.totalPages
                pageNoTextView?.text = "$pageNo/$totalPages"
                if (isSearch) searchEditText?.post { searchEditText?.requestFocus() }
            }
            searchCompleted = true
        }
    }

    private fun updateRecyclerView() {
        LogUtil.d(TAG, "updateRecyclerView")
        songListEmptyTextView?.visibility = View.GONE
        songList.let {
            if (it.songs.isEmpty()) {
                mRecyclerView?.visibility = View.GONE
                exitImageButton?.post { exitImageButton?.requestFocus() }
            } else {
                mRecyclerView?.visibility = View.VISIBLE
                mRecyclerView?.post { mRecyclerView?.requestFocus() }
            }
        }
    }

    private fun firstPage() {
        pageNo = 1
        retrieveSongList()
    }

    private fun previousPage() {
        pageNo--
        if (pageNo < 1) {
            pageNo = 1
        }
        retrieveSongList()
    }

    private fun nextPage() {
        pageNo++
        if (pageNo > totalPages) {
            pageNo = totalPages
        }
        retrieveSongList()
    }

    private fun lastPage() {
        pageNo = -1 // represent last page
        retrieveSongList()
    }

    private fun dataSongToSongInfo(vSong: Song): SongInfo {
        val logStr = "dataSongToSongInfo"
        LogUtil.d(TAG, logStr)
        return SongInfo().apply {
            included = "1"
            filePath = vSong.vodNo
            songName = vSong.songNa
            bitmapUrl = vSong.pathname
        }
    }

    private fun dataSongsToSongs(): ArrayList<SongInfo> {
        val logStr = "dataSongsToSongs"
        LogUtil.d(TAG, logStr)
        val songs = ArrayList<SongInfo>()
        songList.let { list ->
            val listSongs = list.songs
            var index = 0
            for (i in 0 until listSongs.size) {
                val lSong = listSongs[i]
                if (lSong.vodYn.uppercase() != "Y") continue
                songs.add(dataSongToSongInfo(lSong))
                index++
                if (index >= MySingleton.MAX_SONGS) {
                    // excess the max
                    LogUtil.i(TAG, "$logStr.excess the max")
                    ScreenUtil.showToast(
                        activity, getString(R.string.excess_max) +
                                " ${MySingleton.MAX_SONGS}", textFontSize,
                        Toast.LENGTH_SHORT)
                    break
                }
            }
        }
        return songs
    }

    private inner class MyRestApi(private val isSearch: Boolean = false)
        : RestApiAsync<SongList>() {
        @SuppressLint("SetTextI18n")
        override fun onResponse(call: Call<SongList?>, response: Response<SongList?>) {
            val act = activity ?: return
            LogUtil.d(TAG, "MyRestApi.onResponse.response.isSuccessful = {response.isSuccessful}")
            var tempList = response.body()
            if (response.isSuccessful) {
                tempList?.let { sList ->
                    if (sList.songs.isEmpty()) {
                        songListEmptyTextView?.text = act.getString(R.string.noResultString)
                        songListEmptyTextView?.visibility = View.VISIBLE
                    } else {
                        songListEmptyTextView?.visibility = View.GONE
                    }
                } ?: { tempList = SongList() }
            } else {
                tempList = SongList()
                songListEmptyTextView?.text = act.getString(R.string.failedMessage)
                songListEmptyTextView?.visibility = View.VISIBLE
            }
            selectedSongs.clear()
            songList.songs.clear()
            tempList?.let { tempList ->
                songList.pageNo = tempList.pageNo
                songList.pageSize = tempList.pageSize
                songList.totalRecords = tempList.totalRecords
                songList.totalPages = tempList.totalPages
                songList.songs.addAll(tempList.songs)
            }
            myViewAdapter?.myNotifyDataSetChanged()
            updateRecyclerView()
            pageNo = songList.pageNo
            pageSize = songList.pageSize
            totalPages = songList.totalPages
            pageNoTextView?.text = "$pageNo/$totalPages"
            if (isSearch) searchEditText?.post { searchEditText?.requestFocus() }
        }

        override fun onFailure(call: Call<SongList>, t: Throwable) {
            LogUtil.e(TAG, "MyRestApi.onFailure.", t)
            songList = SongList()
            songListEmptyTextView?.text = activity?.getString(R.string.failedMessage)
            songListEmptyTextView?.visibility = View.VISIBLE
        }
    }
}
