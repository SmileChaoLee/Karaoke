package com.smile.u2bkaraoke.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smile.karaoke.R
import com.smile.karaoke.interfaces.RecyclerItemListener
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.u2bkaraoke.SongListActivity
import com.smile.u2bkaraoke.U2bKaraokeApp.Companion.appCompBuilder
import com.smile.u2bkaraoke.model.Constants
import com.smile.u2bkaraoke.model.Language
import com.smile.u2bkaraoke.adapters.WordListAdapter
import com.smile.u2bkaraoke.utilities.U2bKaOkUtil
import javax.inject.Inject

class WordListFragment : Fragment(), RecyclerItemListener {

    companion object {
        private const val TAG = "WordListFragment"
    }

    // @JvmField
    @Inject
    lateinit var myViewAdapter: WordListAdapter
    private var textFontSize = 0f
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
        LogUtil.d(TAG, "onViewCreated.inject()")
        textFontSize = ScreenUtil.getPxTextFontSizeNeeded(activity)
        super.onViewCreated(view, savedInstanceState)

        val wordsListTitle = getString(R.string.wordsListString)
        view.apply {
            val wordsListMenuTextView = findViewById<TextView>(R.id.wordsListMenuTextView)
            ScreenUtil.resizeTextSize(wordsListMenuTextView, textFontSize)
            wordsListMenuTextView.text = "$languageTitle $wordsListTitle"

            mWordList = ArrayList()
            mWordList.add( getString(R.string.oneWordOrderString))
            mWordList.add(getString(R.string.twoWordsOrderString))
            mWordList.add(getString(R.string.threeWordsOrderString))
            mWordList.add( getString(R.string.fourWordsOrderString))
            mWordList.add(getString(R.string.fiveWordsOrderString))
            mWordList.add(getString(R.string.sixWordsOrderString))
            mWordList.add(getString(R.string.sevenWordsOrderString))
            mWordList.add(getString(R.string.eightWordsOrderString))
            mWordList.add(getString(R.string.nineWordsOrderString))
            mWordList.add(getString(R.string.tenWordsOrderString))

            val mRecyclerView = findViewById<RecyclerView>(R.id.wordListRecyclerView)
            val wordsListReturnButton = findViewById<Button>(R.id.wordsListReturnButton)
            ScreenUtil.resizeTextSize(wordsListReturnButton, textFontSize)
            wordsListReturnButton.setOnClickListener { U2bKaOkUtil.returnToPrevious(activity) }

            appCompBuilder
                .recyclerItemListenerModule(this@WordListFragment)
                .arraylistModule(mWordList)
                .floatModule(textFontSize).build()
                .inject(this@WordListFragment)

            mRecyclerView.setAdapter(myViewAdapter)
            mRecyclerView.setLayoutManager(LinearLayoutManager(activity?.applicationContext))
        }
    }

    override fun onItemClick(v: View?, position: Int) {
        LogUtil.d(TAG, "itemView.position = $position")
        if (position < 0) return
        val act = activity ?: return
        val fragContainerId = this.id   // container id of the fragment
        mWordList.let { list ->
            val word = list[position]
            ScreenUtil.showToast(act, word,
                textFontSize,Toast.LENGTH_SHORT)
            Intent(act, SongListActivity::class.java).let {
                it.putExtra(Constants.OrderedFrom, Constants.LanguageWordsOrdered)
                it.putExtra(Constants.SongListActivityTitle,
                    "$languageTitle $word")
                it.putExtra(Constants.LanguageParcelable, mLanguage)
                it.putExtra(Constants.NumOfWords, position + 1)
                act.startActivity(it)
            }
        }
    }

    private fun returnToPrevious() {
        LogUtil.d(TAG, "returnToPrevious")
        val act = activity ?: return
        LogUtil.d(TAG, "returnToPrevious.popBackStack()")
        act.supportFragmentManager.popBackStack()
    }
}
