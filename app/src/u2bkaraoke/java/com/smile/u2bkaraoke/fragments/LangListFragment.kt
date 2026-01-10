package com.smile.u2bkaraoke.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smile.karaoke.R
import com.smile.karaoke.interfaces.RecyclerItemListener
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.u2bkaraoke.SongListActivity
import com.smile.u2bkaraoke.U2bKaraokeApp.Companion.appCompBuilder
import com.smile.u2bkaraoke.model.Constants
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

class LangListFragment : Fragment(), RecyclerItemListener {

    companion object {
        private const val TAG = "LangListFragment"
    }

    @Inject
    lateinit var myViewAdapter: LangListAdapter
    private var mRecyclerView: RecyclerView? = null
    private var textFontSize = 0f
    private var langListEmptyTextView: TextView? = null
    private var languageList: LanguageList? = null
    private var orderedFrom = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        orderedFrom = Constants.WordsOrdered
        arguments?.let { args ->
            orderedFrom = args.getInt(Constants.OrderedFrom, Constants.WordsOrdered)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        LogUtil.i(TAG, "onCreateView")
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.activity_language_list,
            container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        LogUtil.d(TAG, "onCreate")
        textFontSize = ScreenUtil.getPxTextFontSizeNeeded(activity)
        super.onViewCreated(view, savedInstanceState)

        view.apply {
            val menuTextView = findViewById<TextView>(R.id.languagesListMenuTextView)
            ScreenUtil.resizeTextSize(menuTextView, textFontSize)
            when (orderedFrom) {
                Constants.WordsOrdered ->  // from main activity (U2bKkActivity)
                    menuTextView.text = getString(R.string.languagesListString)
                Constants.NewSongOrdered -> menuTextView.text = getString(R.string.newSongLanguagesListString)
                Constants.HotSongOrdered -> menuTextView.text = getString(R.string.hotSongLanguagesListString)
            }

            langListEmptyTextView = findViewById(R.id.languagesListEmptyTextView)
            ScreenUtil.resizeTextSize(langListEmptyTextView, textFontSize)
            langListEmptyTextView?.visibility = View.GONE

            mRecyclerView = findViewById(R.id.languageListRecyclerView)

            val languagesListReturnButton = findViewById<Button>(R.id.languagesListReturnButton)
            ScreenUtil.resizeTextSize(languagesListReturnButton, textFontSize)
            languagesListReturnButton.setOnClickListener { U2bKaOkUtil.returnToPrevious(activity) }
        }

        // MyRestApi().getAllLanguages()
        val act = activity ?: return
        act.lifecycleScope.launch(Dispatchers.IO) {
            languageList = RestApiSync.getApiSync().getAllLanguages()
            // update the UI
            withContext(Dispatchers.Main) {
                languageList?.let {
                    if (it.languages.isEmpty()) {
                        langListEmptyTextView?.text = getString(R.string.noResultString)
                        langListEmptyTextView?.visibility = View.VISIBLE
                    } else {
                        langListEmptyTextView?.visibility = View.GONE
                    }
                } ?: run {
                    languageList = LanguageList()
                    langListEmptyTextView?.text = getString(R.string.failedMessage)
                    langListEmptyTextView?.visibility = View.VISIBLE
                }
                appCompBuilder
                    .recyclerItemListenerModule(this@LangListFragment)
                    .languageArrayListModule(languageList!!.languages)
                    .floatModule(textFontSize).build()
                    .inject(this@LangListFragment)
                mRecyclerView?.setAdapter(myViewAdapter)
                mRecyclerView?.setLayoutManager(LinearLayoutManager(act.applicationContext))
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
            LogUtil.d(TAG, "onItemClick.${language.langNa}")
            ScreenUtil.showToast(act, language.langNa,
                textFontSize, Toast.LENGTH_SHORT)
            val languageTitle = language.langNa
            ScreenUtil.showToast(act, languageTitle,
                textFontSize, Toast.LENGTH_SHORT)
            when (orderedFrom) {
                Constants.WordsOrdered -> {
                    val nFragment = WordListFragment().apply {
                        arguments = Bundle().apply {
                            putInt(Constants.OrderedFrom, Constants.LanguageOrdered)
                            putString(Constants.LanguageTitle, languageTitle)
                            putParcelable(Constants.LanguageParcelable, language)
                        }
                    }
                    U2bKaOkUtil.beginTransaction(fragManager, fragContainerId, nFragment)
                }
                Constants.NewSongOrdered -> {
                    LogUtil.d(TAG, "onItemClick.NewSongOrdered")
                    Intent(act, SongListActivity::class.java).let {
                        it.putExtra(Constants.OrderedFrom, Constants.NewSongLanguageOrdered)
                        it.putExtra(Constants.SongListActivityTitle,
                            languageTitle + " " + getString(R.string.newString))
                        it.putExtra(Constants.LanguageParcelable, language)
                        act.startActivity(it)
                    }
                }
                Constants.HotSongOrdered -> {
                    LogUtil.d(TAG, "onItemClick.HotSongOrdered")
                    Intent(act, SongListActivity::class.java).let {
                        it.putExtra(Constants.OrderedFrom, Constants.HotSongLanguageOrdered)
                        it.putExtra(Constants.SongListActivityTitle,
                            languageTitle + " " + act.getString(R.string.hotString))
                        it.putExtra(Constants.LanguageParcelable, language)
                        act.startActivity(it)
                    }
                }
            }
        }
    }

    private inner class MyRestApi : RestApiAsync<LanguageList>() {
        override fun onResponse(call: Call<LanguageList?>, response: Response<LanguageList?>) {
            LogUtil.d(TAG, "MyRestApi.onResponse")
            LogUtil.d(TAG, "MyRestApi.onResponse.response.isSuccessful() = ${response.isSuccessful}")
            if (response.isSuccessful) {
                languageList = response.body()
                languageList?.let {
                    if (it.languages.isEmpty()) {
                        langListEmptyTextView?.text = getString(R.string.noResultString)
                        langListEmptyTextView?.visibility = View.VISIBLE
                    } else {
                        langListEmptyTextView?.visibility = View.GONE
                    }
                } ?: { languageList = LanguageList() }
            } else {
                languageList = LanguageList()
                langListEmptyTextView?.text = getString(R.string.failedMessage)
                langListEmptyTextView?.visibility = View.VISIBLE
            }
            val act = activity ?: return
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
            langListEmptyTextView?.text = getString(R.string.failedMessage)
            langListEmptyTextView?.visibility = View.VISIBLE
        }
    }
}
