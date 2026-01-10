package com.smile.u2bkaraoke

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smile.karaoke.R
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.alertdialogfragment.AlertDialogFragment
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.u2bkaraoke.U2bKaraokeApp.Companion.appCompBuilder
import com.smile.u2bkaraoke.model.Constants
import com.smile.u2bkaraoke.model.Language
import com.smile.u2bkaraoke.model.Singer
import com.smile.u2bkaraoke.model.SongList
import com.smile.u2bkaraoke.retrofit.RestApiAsync
import com.smile.u2bkaraoke.adapters.SongListAdapter
import retrofit2.Call
import retrofit2.Response
import javax.inject.Inject

class SongListActivity : AppCompatActivity() {
    private var textFontSize = 0f
    private var searchEditText: EditText? = null
    private var isSearchEditTextChanged = false
    private var filterString: String? = null
    private var songsListEmptyTextView: TextView? = null
    private var mRecyclerView: RecyclerView? = null

    @JvmField
    @Inject
    var myViewAdapter: SongListAdapter? = null
    private var songList: SongList? = null
    private var singer: Singer? = null
    private var language: Language? = null
    private var objectPassed: Any? = null

    private var orderedFrom = 0
    private var numOfWords = 0
    private var pageNo = 1
    private var pageSize = 7
    private var totalPages = 0
    private var noResultString: String? = null
    private var failedMessage: String? = null
    private var loadingString: String? = null
    private var loadingDialog: AlertDialogFragment? = null

    private var restApi: MyRestApi? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.d(TAG, "onCreate")
        noResultString = getString(R.string.noResultString)
        failedMessage = getString(R.string.failedMessage)
        loadingString = getString(R.string.loadingString)
        textFontSize = ScreenUtil.getPxTextFontSizeNeeded(this)

        orderedFrom = 0 // default value
        val songListTitle = getString(R.string.songsListString)
        var activityTitle = ""
        numOfWords = 0
        val extras = intent.extras
        if (extras != null) {
            orderedFrom = extras.getInt(Constants.OrderedFrom, 0)
            activityTitle = extras.getString(Constants.SongListActivityTitle, "").trim { it <= ' ' }
            when (orderedFrom) {
                Constants.SingerOrdered -> {
                    singer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        extras.getParcelable(Constants.SingerParcelable, Singer::class.java)
                    } else extras.getParcelable(Constants.SingerParcelable)
                    objectPassed = singer
                }
                Constants.NewSongOrdered -> objectPassed = language
                Constants.NewSongLanguageOrdered, Constants.HotSongLanguageOrdered -> {
                    language = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        extras.getParcelable(Constants.LanguageParcelable, Language::class.java)
                    } else extras.getParcelable(Constants.LanguageParcelable)
                    objectPassed = language
                }
                Constants.HotSongOrdered -> objectPassed = null
                Constants.LanguageOrdered, Constants.LanguageWordsOrdered -> {
                    language = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        extras.getParcelable(Constants.LanguageParcelable, Language::class.java)
                    } else extras.getParcelable(Constants.LanguageParcelable)
                    objectPassed = language
                    numOfWords = extras.getInt(Constants.NumOfWords)
                }
            }
        }
        LogUtil.d(TAG, "onCreate.orderedFrom = $orderedFrom")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_list)

        val songsListMenuTextView = findViewById<TextView>(R.id.songsListMenuTextView)
        ScreenUtil.resizeTextSize(songsListMenuTextView, textFontSize)
        songsListMenuTextView.text = "$activityTitle $songListTitle"

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
        songsListEmptyTextView = findViewById(R.id.songsListEmptyTextView)
        ScreenUtil.resizeTextSize(songsListEmptyTextView, textFontSize)
        songsListEmptyTextView?.visibility = View.GONE

        val smallButtonFontSize = textFontSize * 0.7f
        val firstPageButton = findViewById<Button>(R.id.firstPageButton)
        ScreenUtil.resizeTextSize(firstPageButton, smallButtonFontSize)
        firstPageButton.setOnClickListener { firstPage() }

        val previousPageButton = findViewById<Button>(R.id.previousPageButton)
        ScreenUtil.resizeTextSize(previousPageButton, smallButtonFontSize)
        previousPageButton.setOnClickListener { previousPage() }

        val nextPageButton = findViewById<Button>(R.id.nextPageButton)
        ScreenUtil.resizeTextSize(nextPageButton, smallButtonFontSize)
        nextPageButton.setOnClickListener { nextPage() }

        val lastPageButton = findViewById<Button>(R.id.lastPageButton)
        ScreenUtil.resizeTextSize(lastPageButton, smallButtonFontSize)
        lastPageButton.setOnClickListener { lastPage() }

        val songsListReturnButton = findViewById<Button>(R.id.songsListReturnButton)
        ScreenUtil.resizeTextSize(songsListReturnButton, textFontSize)
        songsListReturnButton.setOnClickListener { returnToPrevious() }

        restApi = MyRestApi()
        retrieveSongList()

        onBackPressedDispatcher.addCallback(
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    returnToPrevious()
                }
            })
    }

    private fun retrieveSongList() {
        LogUtil.d(TAG, "retrieveSongList.orderedFrom = $orderedFrom")
        if (loadingDialog == null) {
            loadingDialog = AlertDialogFragment.newInstance(
                loadingString,
                Constants.FontSize_Scale_Type,
                textFontSize, Color.RED, 0, 0, true
            )
            loadingDialog!!.show(supportFragmentManager, "LoadingDialogTag")
        }
        when (orderedFrom) {
            Constants.SingerOrdered -> {
                LogUtil.d(TAG, "retrieveSongList.SingerOrdered")
                restApi?.let { rApi ->
                    val singer = objectPassed as? Singer ?: Singer()
                    if (filterString.isNullOrEmpty()) {
                        rApi.getSongsBySinger(singer, pageSize, pageNo)
                    } else {
                        rApi.getSongsBySinger(singer, pageSize, pageNo,filterString!!)
                    }
                    objectPassed = singer
                }
            }
            Constants.NewSongOrdered -> LogUtil.d(TAG, "retrieveSongList.NewSongOrdered")
            Constants.HotSongOrdered -> LogUtil.d(TAG, "retrieveSongList.HotSongOrdered")
            Constants.NewSongLanguageOrdered -> {
                LogUtil.d(TAG, "retrieveSongList.NewSongLanguageOrdered")
                restApi?.let { rApi ->
                    val language = objectPassed as? Language ?: Language()
                    if (filterString.isNullOrEmpty()) {
                        rApi.getNewSongsByLanguage(language, pageSize, pageNo)
                    } else {
                        rApi.getNewSongsByLanguage(language, pageSize, pageNo, filterString!!)
                    }
                    objectPassed = language
                }
            }
            Constants.HotSongLanguageOrdered -> {
                LogUtil.d(TAG, "retrieveSongList.HotSongLanguageOrdered")
                restApi?.let { rApi ->
                    val language = objectPassed as? Language ?: Language()
                    if (filterString.isNullOrEmpty()) {
                        rApi.getHotSongsByLanguage(language, pageSize, pageNo)
                    } else {
                        rApi.getHotSongsByLanguage(language, pageSize, pageNo, filterString!!)
                    }
                    objectPassed = language
                }
            }
            Constants.LanguageOrdered -> {
                LogUtil.d(TAG, "retrieveSongList.LanguageOrdered")
                restApi?.let { rApi ->
                    val language = objectPassed as? Language ?: Language()
                    if (filterString.isNullOrEmpty()) {
                        rApi.getSongsByLanguage(language, pageSize, pageNo)
                    } else {
                        rApi.getSongsByLanguage(language, pageSize, pageNo, filterString!!)
                    }
                    objectPassed = language
                }
            }
            Constants.LanguageWordsOrdered -> {
                LogUtil.d(TAG, "retrieveSongList.LanguageWordsOrdered")
                restApi?.let { rApi ->
                    val language = objectPassed as? Language ?: Language()
                    if (filterString.isNullOrEmpty()) {
                        rApi.getSongsByLanguageNumOfWords(language, numOfWords, pageSize, pageNo)
                    } else {
                        rApi.getSongsByLanguageNumOfWords(language, numOfWords, pageSize, pageNo, filterString!!)
                    }
                    objectPassed = language
                }
            }
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
    }

    private fun returnToPrevious() {
        LogUtil.d(TAG, "returnToPrevious")
        finish()
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
            loadingDialog?.dismissAllowingStateLoss()
            loadingDialog = null
            LogUtil.d(TAG, "MyRestApi.onResponse.response.isSuccessful = {response.isSuccessful}")
            songList = response.body()
            if (response.isSuccessful) {
                songList?.let { sList ->
                    pageNo = sList.pageNo
                    pageSize = sList.pageSize
                    totalPages = sList.totalPages
                    if (sList.songs.isEmpty()) {
                        songsListEmptyTextView?.text = noResultString
                        songsListEmptyTextView?.visibility = View.VISIBLE
                    } else {
                        songsListEmptyTextView?.visibility = View.GONE
                    }
                } ?: { songList = SongList() }
            } else {
                songList = SongList()
                songsListEmptyTextView?.text = failedMessage
                songsListEmptyTextView?.visibility = View.VISIBLE
            }
            LogUtil.d(TAG, "MyRestApi.onResponse.inject()")
            appCompBuilder
                .activityModule(this@SongListActivity)
                .songArrayListModule(songList!!.songs)
                .floatModule(textFontSize).build()
                .inject(this@SongListActivity)
            mRecyclerView?.setAdapter(myViewAdapter)
            mRecyclerView?.setLayoutManager(LinearLayoutManager(applicationContext))

            LogUtil.d(TAG, "MyRestApi.onResponse.isSearchEditTextChanged = $isSearchEditTextChanged")
            if (isSearchEditTextChanged) {
                // searchEditText.setFocusable(true);              // needed for requestFocus()
                // searchEditText.setFocusableInTouchMode(true);   // needed for requestFocus()
                // searchEditText.requestFocus();  // needed for the next two statements
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                // imm.showSoftInput(null, InputMethodManager.SHOW_IMPLICIT);
                imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
                isSearchEditTextChanged = false
            }
        }

        override fun onFailure(call: Call<SongList>, t: Throwable) {
            LogUtil.e(TAG, "MyRestApi.onFailure.", t)
            if (loadingDialog != null) loadingDialog!!.dismissAllowingStateLoss()
            loadingDialog = null
            songList = SongList()
            songsListEmptyTextView?.text = failedMessage
            songsListEmptyTextView?.visibility = View.VISIBLE
        }
    }

    companion object {
        private const val TAG = "SongListActivity"
    }
}
