package com.smile.u2bkaraoke.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smile.karaoke.R
import com.smile.karaoke.interfaces.RecyclerItemListener
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.u2bkaraoke.U2bKaraokeApp.Companion.appCompBuilder
import com.smile.u2bkaraoke.model.Constants
import com.smile.u2bkaraoke.model.Language
import com.smile.u2bkaraoke.model.Singer
import com.smile.u2bkaraoke.model.SongList
import com.smile.u2bkaraoke.retrofit.RestApiAsync
import com.smile.u2bkaraoke.adapters.SongListAdapter
import com.smile.u2bkaraoke.retrofit.RestApiSync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Response
import javax.inject.Inject

class SongListFragment : U2bKKBaseFragment(), RecyclerItemListener {

    companion object {
        private const val TAG = "SongListFragment"
    }

    @Inject
    lateinit var myViewAdapter: SongListAdapter
    private var searchEditText: EditText? = null
    private var isSearchEditTextChanged = false
    private var filterString: String? = null
    private var songListEmptyTextView: TextView? = null
    private var mRecyclerView: RecyclerView? = null
    private var firstPageButton: Button? = null
    private var previousPageButton: Button? = null
    private var nextPageButton: Button? = null
    private var lastPageButton: Button? = null
    private var songList: SongList? = null
    private var singer: Singer? = null
    private var language: Language? = null
    private var objectPassed: Any? = null
    private var orderedFrom = 0
    private var activityTitle = ""
    private var numOfWords = 0
    private var pageNo = 1
    private var pageSize = 7
    private var totalPages = 0
    // private var restApi: MyRestApi? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        orderedFrom = 0 // default value
        numOfWords = 0
        arguments?.let { args ->
            orderedFrom = args.getInt(Constants.OrderedFrom, 0)
            activityTitle = args.getString(Constants.SongListTitle, "").trim()
            when (orderedFrom) {
                Constants.SingerOrdered -> {
                    singer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        args.getParcelable(Constants.SingerParcelable, Singer::class.java)
                    } else args.getParcelable(Constants.SingerParcelable)
                    objectPassed = singer
                }
                // Constants.NewSongOrdered -> objectPassed = language
                Constants.NewSongLanguageOrdered, Constants.HotSongLanguageOrdered -> {
                    language = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        args.getParcelable(Constants.LanguageParcelable, Language::class.java)
                    } else args.getParcelable(Constants.LanguageParcelable)
                    objectPassed = language
                    LogUtil.i(TAG, "onCreate.NewSongLanguageOrdered.language = $language")
                }
                // Constants.HotSongOrdered -> objectPassed = null
                Constants.LanguageOrdered, Constants.LanguageWordsOrdered -> {
                    language = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        args.getParcelable(Constants.LanguageParcelable, Language::class.java)
                    } else args.getParcelable(Constants.LanguageParcelable)
                    objectPassed = language
                    numOfWords = args.getInt(Constants.NumOfWords)
                }
            }
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

        view.apply {
            val songsListMenuTextView = findViewById<TextView>(R.id.songsListMenuTextView)
            ScreenUtil.resizeTextSize(songsListMenuTextView, textFontSize)
            songsListMenuTextView.text = activityTitle

            filterString = ""
            searchEditText = findViewById(R.id.songSearchEditText)
            searchEditText?.let { sEt ->
                ScreenUtil.resizeTextSize(sEt, textFontSize)
                val searchEditLp = sEt.layoutParams as LinearLayout.LayoutParams
                searchEditLp.leftMargin = (textFontSize * 2.0f).toInt()
                searchEditLp.rightMargin = (textFontSize * 5.0f).toInt()
                sEt.setText(filterString)
                isSearchEditTextChanged = false
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
                        isSearchEditTextChanged = true
                        retrieveSongList()
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
        }

        super.onViewCreated(view, savedInstanceState)
        exitImageButton?.nextFocusUpId = R.id.nextPageButton
        showVideoButton?.nextFocusUpId = R.id.nextPageButton

        // restApi = MyRestApi()
        retrieveSongList()
    }

    override fun setClickListeners() {
        super.setClickListeners()
        firstPageButton?.setOnClickListener { firstPage() }
        previousPageButton?.setOnClickListener { previousPage() }
        nextPageButton?.setOnClickListener { nextPage() }
        lastPageButton?.setOnClickListener { lastPage() }
    }

    override fun onItemClick(v: View?, position: Int) {
        LogUtil.i(TAG, "onItemClick.position = $position")
        if (position < 0) return
        val act = activity ?: return
        val fragContainerId = this.id   // container id of the fragment
        val fragManager = act.supportFragmentManager
        songList?.let { list ->
            val song = list.songs[position]
            ScreenUtil.showToast(act, song.songNa,
                textFontSize, Toast.LENGTH_SHORT)
        }
    }

    private fun retrieveSongList() {
        val logStr = "retrieveSingerList"
        LogUtil.d(TAG, "$logStr.orderedFrom = $orderedFrom")
        LogUtil.d(TAG, "$logStr.filterString = $filterString")
        val act = activity ?: return
        act.lifecycleScope.launch(Dispatchers.Main) {
            mRecyclerView?.visibility = View.GONE
            songListEmptyTextView?.visibility = View.VISIBLE
            songListEmptyTextView?.text = act.getString(R.string.loadingString)
            withContext(Dispatchers.IO) {
                val restApi = RestApiSync.getApiSync()
                when (orderedFrom) {
                    Constants.SingerOrdered -> {
                        LogUtil.d(TAG, "$logStr.SingerOrdered")
                        restApi.let { rApi ->
                            val singer = objectPassed as? Singer ?: Singer()
                            songList = if (filterString.isNullOrEmpty()) {
                                rApi.getSongsBySinger(singer, pageSize, pageNo)
                            } else {
                                rApi.getSongsBySinger(singer, pageSize, pageNo, filterString!!)
                            }
                            objectPassed = singer
                        }
                    }
                    Constants.NewSongLanguageOrdered -> {
                        LogUtil.d(TAG, "$logStr.NewSongLanguageOrdered")
                        restApi.let { rApi ->
                            val language = objectPassed as? Language ?: Language()
                            songList = if (filterString.isNullOrEmpty()) {
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
                    Constants.HotSongLanguageOrdered -> {
                        LogUtil.d(TAG, "$logStr.HotSongLanguageOrdered")
                        restApi.let { rApi ->
                            val language = objectPassed as? Language ?: Language()
                            songList = if (filterString.isNullOrEmpty()) {
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
                    Constants.LanguageOrdered -> {
                        LogUtil.d(TAG, "$logStr.LanguageOrdered")
                        restApi.let { rApi ->
                            val language = objectPassed as? Language ?: Language()
                            songList = if (filterString.isNullOrEmpty()) {
                                rApi.getSongsByLanguage(language, pageSize, pageNo)
                            } else {
                                rApi.getSongsByLanguage(language, pageSize, pageNo, filterString!!)
                            }
                            objectPassed = language
                        }
                    }
                    Constants.LanguageWordsOrdered -> {
                        LogUtil.d(TAG, "$logStr.LanguageWordsOrdered")
                        restApi.let { rApi ->
                            val language = objectPassed as? Language ?: Language()
                            songList = if (filterString.isNullOrEmpty()) {
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
                songList?.let { sList ->
                    pageNo = sList.pageNo
                    pageSize = sList.pageSize
                    totalPages = sList.totalPages
                    if (sList.songs.isEmpty()) {
                        songListEmptyTextView?.text = act.getString(R.string.noResultString)
                        songListEmptyTextView?.visibility = View.VISIBLE
                    } else {
                        songListEmptyTextView?.visibility = View.GONE
                    }
                } ?: run{
                    songList = SongList()
                    songListEmptyTextView?.text = act.getString(R.string.failedMessage)
                    songListEmptyTextView?.visibility = View.VISIBLE
                }
                LogUtil.d(TAG, "$logStr.inject().myViewAdapter")
                appCompBuilder
                    .recyclerItemListenerModule(this@SongListFragment)
                    .songArrayListModule(songList!!.songs)
                    .floatModule(textFontSize).build()
                    .inject(this@SongListFragment)
                mRecyclerView?.setAdapter(myViewAdapter)
                mRecyclerView?.setLayoutManager(LinearLayoutManager(act.applicationContext))
                LogUtil.d(TAG, "$logStr.isSearchEditTextChanged = $isSearchEditTextChanged")
                if (isSearchEditTextChanged) {
                    // searchEditText.setFocusable(true);              // needed for requestFocus()
                    // searchEditText.setFocusableInTouchMode(true);   // needed for requestFocus()
                    // searchEditText.requestFocus();  // needed for the next two statements
                    val imm = act.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    // imm.showSoftInput(null, InputMethodManager.SHOW_IMPLICIT);
                    imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
                    isSearchEditTextChanged = false
                }
                updateRecyclerView()
            }
        }
    }

    private fun updateRecyclerView() {
        songListEmptyTextView?.visibility = View.GONE
        songList?.let {
            if (it.songs.isEmpty()) {
                mRecyclerView?.visibility = View.GONE
                showVideoButton?.post { showVideoButton?.requestFocus() }
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

    private inner class MyRestApi : RestApiAsync<SongList>() {
        override fun onResponse(call: Call<SongList?>, response: Response<SongList?>) {
            val act = activity ?: return
            LogUtil.d(TAG, "MyRestApi.onResponse.response.isSuccessful = {response.isSuccessful}")
            songList = response.body()
            if (response.isSuccessful) {
                songList?.let { sList ->
                    pageNo = sList.pageNo
                    pageSize = sList.pageSize
                    totalPages = sList.totalPages
                    if (sList.songs.isEmpty()) {
                        songListEmptyTextView?.text = act.getString(R.string.noResultString)
                        songListEmptyTextView?.visibility = View.VISIBLE
                    } else {
                        songListEmptyTextView?.visibility = View.GONE
                    }
                } ?: { songList = SongList() }
            } else {
                songList = SongList()
                songListEmptyTextView?.text = act.getString(R.string.failedMessage)
                songListEmptyTextView?.visibility = View.VISIBLE
            }
            LogUtil.d(TAG, "MyRestApi.onResponse.inject()")
            appCompBuilder
                .recyclerItemListenerModule(this@SongListFragment)
                .songArrayListModule(songList!!.songs)
                .floatModule(textFontSize).build()
                .inject(this@SongListFragment)
            mRecyclerView?.setAdapter(myViewAdapter)
            mRecyclerView?.setLayoutManager(LinearLayoutManager(act.applicationContext))

            LogUtil.d(TAG, "MyRestApi.onResponse.isSearchEditTextChanged = $isSearchEditTextChanged")
            if (isSearchEditTextChanged) {
                // searchEditText.setFocusable(true);              // needed for requestFocus()
                // searchEditText.setFocusableInTouchMode(true);   // needed for requestFocus()
                // searchEditText.requestFocus();  // needed for the next two statements
                val imm = act.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                // imm.showSoftInput(null, InputMethodManager.SHOW_IMPLICIT);
                imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
                isSearchEditTextChanged = false
            }
        }

        override fun onFailure(call: Call<SongList>, t: Throwable) {
            LogUtil.e(TAG, "MyRestApi.onFailure.", t)
            songList = SongList()
            songListEmptyTextView?.text = activity?.getString(R.string.failedMessage)
            songListEmptyTextView?.visibility = View.VISIBLE
        }
    }
}
