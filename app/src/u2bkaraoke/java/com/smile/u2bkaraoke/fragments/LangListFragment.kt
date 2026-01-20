package com.smile.u2bkaraoke.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.smile.u2bkaraoke.u2bkaok_constants.U2bKKConstants
import com.smile.u2bkaraoke.model.LanguageList
import com.smile.u2bkaraoke.retrofit.RestApiAsync
import com.smile.u2bkaraoke.adapters.LangListAdapter
import com.smile.u2bkaraoke.retrofit.RestApiSync
import com.smile.u2bkaraoke.utilities.U2bKaOkUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Response
import javax.inject.Inject

class LangListFragment : U2bKKBaseFragment(), RecyclerItemListener {

    companion object {
        private const val TAG = "LangListFragment"
    }

    @Inject
    lateinit var myViewAdapter: LangListAdapter
    private var mRecyclerView: RecyclerView? = null
    private var langListEmptyTextView: TextView? = null
    private var languageList: LanguageList? = null
    private var orderedFrom = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        orderedFrom = U2bKKConstants.WordsOrdered
        arguments?.let { args ->
            orderedFrom = args.getInt(U2bKKConstants.OrderedFrom, U2bKKConstants.WordsOrdered)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        LogUtil.i(TAG, "onCreateView")
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_language_list,
            container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        LogUtil.d(TAG, "onViewCreated")
        textFontSize = ScreenUtil.getPxTextFontSizeNeeded(activity)
        val act = activity ?: return
        view.apply {
            val menuTextView = findViewById<TextView>(R.id.languagesListMenuTextView)
            ScreenUtil.resizeTextSize(menuTextView, textFontSize)
            when (orderedFrom) {
                U2bKKConstants.WordsOrdered ->  // from main activity (U2bKkActivity)
                    menuTextView.text = act.getString(R.string.languagesListString)
                U2bKKConstants.NewSongOrdered ->
                    menuTextView.text = act.getString(R.string.newSongLanguagesListString)
                U2bKKConstants.HotSongOrdered ->
                    menuTextView.text = act.getString(R.string.hotSongLanguagesListString)
            }
            langListEmptyTextView = findViewById(R.id.languagesListEmptyTextView)
            ScreenUtil.resizeTextSize(langListEmptyTextView, textFontSize)
            langListEmptyTextView?.visibility = View.GONE
            mRecyclerView = findViewById(R.id.languageListRecyclerView)
        }

        super.onViewCreated(view, savedInstanceState)
        exitImageButton?.nextFocusUpId = R.id.languageListRecyclerView
        showVideoButton?.nextFocusUpId = R.id.languageListRecyclerView

        // MyRestApi().getAllLanguages()
        act.lifecycleScope.launch(Dispatchers.Main) {
            mRecyclerView?.visibility = View.GONE
            langListEmptyTextView?.visibility = View.VISIBLE
            langListEmptyTextView?.text = act.getString(R.string.loadingString)
            withContext(Dispatchers.IO) {
                languageList = RestApiSync.getApiSync().getAllLanguages()
            }
            // update the UI
            withContext(Dispatchers.Main) {
                languageList?.let {
                    if (it.languages.isEmpty()) {
                        langListEmptyTextView?.text = act.getString(R.string.noResultString)
                        langListEmptyTextView?.visibility = View.VISIBLE
                    } else {
                        langListEmptyTextView?.visibility = View.GONE
                    }
                } ?: run {
                    languageList = LanguageList()
                    langListEmptyTextView?.text = act.getString(R.string.failedMessage)
                    langListEmptyTextView?.visibility = View.VISIBLE
                }
                LogUtil.d(TAG, "onViewCreated.inject().myViewAdapter")
                appCompBuilder
                    .recyclerItemListenerModule(this@LangListFragment)
                    .languageArrayListModule(languageList!!.languages)
                    .floatModule(textFontSize).build()
                    .inject(this@LangListFragment)
                mRecyclerView?.setAdapter(myViewAdapter)
                mRecyclerView?.setLayoutManager(LinearLayoutManager(act.applicationContext))
                updateRecyclerView()
            }
        }
    }

    private fun updateRecyclerView() {
        langListEmptyTextView?.visibility = View.GONE
        languageList?.let {
            if (it.languages.isEmpty()) {
                mRecyclerView?.visibility = View.GONE
                exitImageButton?.post { exitImageButton?.requestFocus() }
            } else {
                mRecyclerView?.visibility = View.VISIBLE
                mRecyclerView?.post { mRecyclerView?.requestFocus() }
            }
        }
    }

    override fun onItemClick(v: View?, position: Int) {
        LogUtil.i(TAG, "onItemClick.position = $position")
        if (position < 0) return
        val act = activity ?: return
        val fragContainerId = this.id   // container id of the fragment
        val fragManager = act.supportFragmentManager
        languageList?.let { list ->
            val language = list.languages[position]
            LogUtil.d(TAG, "onItemClick.language.langNa = ${language.langNa}")
            ScreenUtil.showToast(act, language.langNa,
                textFontSize, Toast.LENGTH_SHORT)
            val languageTitle = language.langNa
            ScreenUtil.showToast(act, languageTitle,
                textFontSize, Toast.LENGTH_SHORT)
            when (orderedFrom) {
                U2bKKConstants.WordsOrdered -> {
                    val nFragment = WordListFragment().apply {
                        arguments = Bundle().apply {
                            putInt(U2bKKConstants.OrderedFrom, U2bKKConstants.LanguageOrdered)
                            putString(U2bKKConstants.LanguageTitle, languageTitle)
                            putParcelable(U2bKKConstants.LanguageParcelable, language)
                        }
                    }
                    U2bKaOkUtil.beginTransaction(fragManager, fragContainerId, nFragment)
                }
                U2bKKConstants.NewSongOrdered -> {
                    LogUtil.d(TAG, "onItemClick.NewSongOrdered")
                    /*
                    Intent(act, SongListActivity::class.java).let {
                        it.putExtra(Constants.OrderedFrom, Constants.NewSongLanguageOrdered)
                        it.putExtra(Constants.SongListTitle,
                            languageTitle + " " + getString(R.string.newString))
                        it.putExtra(Constants.LanguageParcelable, language)
                        act.startActivity(it)
                    }
                    */
                    val nFragment = SongListFragment().apply {
                        arguments = Bundle().apply {
                            putInt(U2bKKConstants.OrderedFrom, U2bKKConstants.NewSongLanguageOrdered)
                            val listTitle = languageTitle + " " + act.getString(R.string.newString)
                            putString(U2bKKConstants.SongListTitle, listTitle)
                            putParcelable(U2bKKConstants.LanguageParcelable, language)
                        }
                    }
                    U2bKaOkUtil.beginTransaction(fragManager, fragContainerId, nFragment)
                }
                U2bKKConstants.HotSongOrdered -> {
                    LogUtil.d(TAG, "onItemClick.HotSongOrdered")
                    /*
                    Intent(act, SongListActivity::class.java).let {
                        it.putExtra(Constants.OrderedFrom, Constants.HotSongLanguageOrdered)
                        it.putExtra(Constants.SongListTitle,
                            languageTitle + " " + act.getString(R.string.hotString))
                        it.putExtra(Constants.LanguageParcelable, language)
                        act.startActivity(it)
                    }
                    */
                    val nFragment = SongListFragment().apply {
                        arguments = Bundle().apply {
                            putInt(U2bKKConstants.OrderedFrom, U2bKKConstants.HotSongLanguageOrdered)
                            val listTitle = languageTitle + " " + act.getString(R.string.hotString)
                            putString(U2bKKConstants.SongListTitle, listTitle)
                            putParcelable(U2bKKConstants.LanguageParcelable, language)
                        }
                    }
                    U2bKaOkUtil.beginTransaction(fragManager, fragContainerId, nFragment)
                }
            }
        }
    }

    private inner class MyRestApi : RestApiAsync<LanguageList>() {
        override fun onResponse(call: Call<LanguageList?>, response: Response<LanguageList?>) {
            LogUtil.d(TAG, "MyRestApi.onResponse")
            LogUtil.d(TAG, "MyRestApi.onResponse.response.isSuccessful() = ${response.isSuccessful}")
            val act = activity ?: return
            if (response.isSuccessful) {
                languageList = response.body()
                languageList?.let {
                    if (it.languages.isEmpty()) {
                        langListEmptyTextView?.text = act.getString(R.string.noResultString)
                        langListEmptyTextView?.visibility = View.VISIBLE
                    } else {
                        langListEmptyTextView?.visibility = View.GONE
                    }
                } ?: { languageList = LanguageList() }
            } else {
                languageList = LanguageList()
                langListEmptyTextView?.text = act.getString(R.string.failedMessage)
                langListEmptyTextView?.visibility = View.VISIBLE
            }
            LogUtil.d(TAG, "MyRestApi.onResponse.inject().myViewAdapter")
            appCompBuilder
                .recyclerItemListenerModule(this@LangListFragment)
                .languageArrayListModule(languageList!!.languages)
                .floatModule(textFontSize).build()
                .inject(this@LangListFragment)
            mRecyclerView?.setAdapter(myViewAdapter)
            mRecyclerView?.setLayoutManager(LinearLayoutManager(act.applicationContext))
        }

        override fun onFailure(call: Call<LanguageList>, t: Throwable) {
            LogUtil.e(TAG, "MyRestApi.onFailure.", t)
            languageList = LanguageList()
            langListEmptyTextView?.text = activity?.getString(R.string.failedMessage)
            langListEmptyTextView?.visibility = View.VISIBLE
        }
    }
}
