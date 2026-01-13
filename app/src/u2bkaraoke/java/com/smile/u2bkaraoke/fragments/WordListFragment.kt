package com.smile.u2bkaraoke.fragments

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smile.karaoke.R
import com.smile.karaoke.interfaces.RecyclerItemListener
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.u2bkaraoke.U2bKaraokeApp.Companion.appCompBuilder
import com.smile.u2bkaraoke.model.Constants
import com.smile.u2bkaraoke.model.Language
import com.smile.u2bkaraoke.adapters.WordListAdapter
import com.smile.u2bkaraoke.utilities.U2bKaOkUtil
import javax.inject.Inject

class WordListFragment : U2bKKBaseFragment(), RecyclerItemListener {

    companion object {
        private const val TAG = "WordListFragment"
    }

    @Inject
    lateinit var myViewAdapter: WordListAdapter
    private var mRecyclerView: RecyclerView? = null
    private var languageTitle = ""
    private lateinit var mWordList: ArrayList<String>
    private lateinit var mLanguage: Language
    private var orderedFrom = Constants.WordsOrdered

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        orderedFrom = Constants.WordsOrdered
        languageTitle = ""
        var lang: Language? = null
        arguments?.let { args ->
            orderedFrom = args.getInt(Constants.OrderedFrom, Constants.WordsOrdered)
            languageTitle = args.getString(Constants.LanguageTitle, "")
            lang = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                args.getParcelable(Constants.LanguageParcelable, Language::class.java)
            } else args.getParcelable(Constants.LanguageParcelable)
        }
        mLanguage = lang ?: Language()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        LogUtil.i(TAG, "onCreateView")
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.activity_word_list,
            container, false)
        return view
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        LogUtil.d(TAG, "onViewCreated")
        textFontSize = ScreenUtil.getPxTextFontSizeNeeded(activity)

        val act = activity ?: return
        val wordsListTitle = act.getString(R.string.wordsListString)
        view.apply {
            val wordsListMenuTextView = findViewById<TextView>(R.id.wordsListMenuTextView)
            ScreenUtil.resizeTextSize(wordsListMenuTextView, textFontSize)
            wordsListMenuTextView.text = "$languageTitle $wordsListTitle"
            mWordList = ArrayList()
            mWordList.add(act.getString(R.string.oneWordOrderString))
            mWordList.add(act.getString(R.string.twoWordsOrderString))
            mWordList.add(act.getString(R.string.threeWordsOrderString))
            mWordList.add(act.getString(R.string.fourWordsOrderString))
            mWordList.add(act.getString(R.string.fiveWordsOrderString))
            mWordList.add(act.getString(R.string.sixWordsOrderString))
            mWordList.add(act.getString(R.string.sevenWordsOrderString))
            mWordList.add(act.getString(R.string.eightWordsOrderString))
            mWordList.add(act.getString(R.string.nineWordsOrderString))
            mWordList.add(act.getString(R.string.tenWordsOrderString))
            mRecyclerView = findViewById<RecyclerView>(R.id.wordListRecyclerView)
            appCompBuilder
                .recyclerItemListenerModule(this@WordListFragment)
                .arraylistModule(mWordList)
                .floatModule(textFontSize).build()
                .inject(this@WordListFragment)
            mRecyclerView?.setAdapter(myViewAdapter)
            mRecyclerView?.setLayoutManager(LinearLayoutManager(act.applicationContext))
            updateRecyclerView()
        }

        super.onViewCreated(view, savedInstanceState)
        exitImageButton?.nextFocusUpId = R.id.wordListRecyclerView
        showVideoButton?.nextFocusUpId = R.id.wordListRecyclerView
    }

    private fun updateRecyclerView() {
        if (mWordList.isEmpty()) {
            mRecyclerView?.visibility = View.GONE
            showVideoButton?.post { showVideoButton?.requestFocus() }
        } else {
            mRecyclerView?.visibility = View.VISIBLE
            mRecyclerView?.post { mRecyclerView?.requestFocus() }
        }
    }

    override fun onItemClick(v: View?, position: Int) {
        LogUtil.d(TAG, "itemView.position = $position")
        if (position < 0) return
        val act = activity ?: return
        val fragContainerId = this.id   // container id of the fragment
        val fragManager = act.supportFragmentManager
        mWordList.let { list ->
            val word = list[position]
            ScreenUtil.showToast(act, word,
                textFontSize,Toast.LENGTH_SHORT)
            /*
            Intent(act, SongListActivity::class.java).let {
                it.putExtra(Constants.OrderedFrom, Constants.LanguageWordsOrdered)
                it.putExtra(Constants.SongListTitle,
                    "$languageTitle $word")
                it.putExtra(Constants.LanguageParcelable, mLanguage)
                it.putExtra(Constants.NumOfWords, position + 1)
                act.startActivity(it)
            }
            */
            val nFragment = SongListFragment().apply {
                arguments = Bundle().apply {
                    putInt(Constants.OrderedFrom, Constants.LanguageWordsOrdered)
                    putString(Constants.SongListTitle, "$languageTitle $word")
                    putParcelable(Constants.LanguageParcelable, mLanguage)
                    putInt(Constants.NumOfWords, position + 1)
                }
            }
            U2bKaOkUtil.beginTransaction(fragManager, fragContainerId, nFragment)
        }
    }
}
