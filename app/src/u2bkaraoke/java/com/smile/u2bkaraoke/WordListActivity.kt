package com.smile.u2bkaraoke

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Pair
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smile.karaoke.R
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.u2bkaraoke.U2bKaraokeApp.Companion.appComponent
import com.smile.u2bkaraoke.model.Constants
import com.smile.u2bkaraoke.model.Language
import com.smile.u2bkaraoke.adapters.WordListAdapter
import javax.inject.Inject

class WordListActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "WordListActivity"
    }

    // @JvmField
    @Inject
    lateinit var myViewAdapter: WordListAdapter

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.d(TAG, "onCreate.inject()")
        appComponent.inject(this)

        val textFontSize = ScreenUtil.getPxTextFontSizeNeeded(this)

        var orderedFrom = Constants.WordsOrdered
        val wordsListTitle = getString(R.string.wordsListString)
        var languageTitle = ""
        val extras = intent.extras
        var lang: Language? = null
        if (extras != null) {
            orderedFrom = extras.getInt(Constants.OrderedFrom, Constants.WordsOrdered)
            languageTitle = extras.getString(Constants.LanguageTitle, "")
            lang = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                extras.getParcelable(Constants.LanguageParcelable, Language::class.java)
            } else extras.getParcelable(Constants.LanguageParcelable)
        }
        val vLanguage = lang ?: Language()

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_word_list)

        val wordsListMenuTextView = findViewById<TextView>(R.id.wordsListMenuTextView)
        ScreenUtil.resizeTextSize(wordsListMenuTextView, textFontSize)
        wordsListMenuTextView.text = "$languageTitle $wordsListTitle"

        val mWordList = ArrayList<Pair<Int, String>>()
        mWordList.add(Pair(1, getString(R.string.oneWordOrderString)))
        mWordList.add(Pair(2, getString(R.string.twoWordsOrderString)))
        mWordList.add(Pair(3, getString(R.string.threeWordsOrderString)))
        mWordList.add(Pair(4, getString(R.string.fourWordsOrderString)))
        mWordList.add(Pair(5, getString(R.string.fiveWordsOrderString)))
        mWordList.add(Pair(6, getString(R.string.sixWordsOrderString)))
        mWordList.add(Pair(7, getString(R.string.sevenWordsOrderString)))
        mWordList.add(Pair(8, getString(R.string.eightWordsOrderString)))
        mWordList.add(Pair(9, getString(R.string.nineWordsOrderString)))
        mWordList.add(Pair(10, getString(R.string.tenWordsOrderString)))

        val mRecyclerView = findViewById<RecyclerView>(R.id.wordListRecyclerView)
        val wordsListReturnButton = findViewById<Button>(R.id.wordsListReturnButton)
        ScreenUtil.resizeTextSize(wordsListReturnButton, textFontSize)
        wordsListReturnButton.setOnClickListener { returnToPrevious() }
        myViewAdapter.setParameters(
            this@WordListActivity,
            vLanguage, languageTitle, mWordList, textFontSize
        )
        mRecyclerView.setAdapter(myViewAdapter)
        mRecyclerView.setLayoutManager(LinearLayoutManager(applicationContext))

        onBackPressedDispatcher.addCallback(
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    LogUtil.d(TAG, "onBackPressedDispatcher.handleOnBackPressed")
                    returnToPrevious()
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun returnToPrevious() {
        finish()
    }
}
