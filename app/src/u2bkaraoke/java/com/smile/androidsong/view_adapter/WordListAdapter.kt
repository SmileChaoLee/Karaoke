package com.smile.androidsong.view_adapter

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.smile.androidsong.R
import com.smile.androidsong.SongListActivity
import com.smile.androidsong.model.Constants
import com.smile.androidsong.model.Language
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.androidsong.view_adapter.WordListAdapter.MyViewHolder
import javax.inject.Inject

class WordListAdapter @Inject constructor(
) : RecyclerView.Adapter<MyViewHolder>() {
    private lateinit var mActivity: Activity
    private lateinit var mLanguage: Language
    private lateinit var mLanguageTitle: String
    private lateinit var mWordList: ArrayList<Pair<Int, String>>
    private var mTextFontSize: Float = 0f

    inner class MyViewHolder(itemView: View) : ViewHolder(itemView) {
        val wordNoTextView : TextView = itemView.findViewById(R.id.wordsOrderNoTextView)
        val wordNaTextView: TextView

        init {
            ScreenUtil.resizeTextSize(
                wordNoTextView,
                mTextFontSize,
                Constants.FontSize_Scale_Type
            )
            wordNaTextView = itemView.findViewById(R.id.wordsOrderNaTextView)
            ScreenUtil.resizeTextSize(
                wordNaTextView,
                mTextFontSize,
                Constants.FontSize_Scale_Type
            )
        }
    }

    fun setParameters(activity: Activity, language: Language,
                      languageTitle: String, wordList: ArrayList<Pair<Int, String>>,
                      textFontSize: Float) {
        mActivity = activity
        mLanguage = language
        mLanguageTitle = languageTitle
        mWordList = wordList
        mTextFontSize = textFontSize
    }

    // Usually involves inflating a layout from XML and returning the holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        // inflate the singerType item view
        return MyViewHolder(layoutInflater.inflate(R.layout.word_list_item, parent, false))
    }

    // Involves populating data into the item through holder
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val word = mWordList[position]
        holder.apply {
            wordNoTextView.text = word.first.toString()
            wordNaTextView.text = word.second
            itemView.setOnClickListener {
                Log.d(TAG, "itemView.setOnClickListener.$word")
                ScreenUtil.showToast(
                    mActivity, word.second,
                    mTextFontSize, Constants.FontSize_Scale_Type, Toast.LENGTH_SHORT
                )
                Intent(mActivity, SongListActivity::class.java).let {
                    it.putExtra(Constants.OrderedFrom, Constants.LanguageWordsOrdered)
                    it.putExtra(Constants.SongListActivityTitle,
                        mLanguageTitle + " " + word.second
                    )
                    it.putExtra(Constants.LanguageParcelable, mLanguage)
                    it.putExtra(Constants.NumOfWords, word.first)
                    mActivity.startActivity(it)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return mWordList.size
    }

    companion object {
        private const val TAG = "WordListAdapter"
    }
}
