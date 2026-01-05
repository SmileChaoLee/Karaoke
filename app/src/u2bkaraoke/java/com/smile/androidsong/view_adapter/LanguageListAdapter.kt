package com.smile.androidsong.view_adapter

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.smile.androidsong.R
import com.smile.androidsong.SongListActivity
import com.smile.androidsong.WordListActivity
import com.smile.androidsong.model.Constants
import com.smile.androidsong.model.Language
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.androidsong.view_adapter.LanguageListAdapter.MyViewHolder

class LanguageListAdapter (
    private val mActivity: Activity,
    private val mLanguages: ArrayList<Language>,
    private val orderedFrom: Int,
    private val mTextFontSize: Float
) : RecyclerView.Adapter<MyViewHolder>() {
    inner class MyViewHolder(itemView: View) : ViewHolder(itemView) {
        val positionNoTextView: TextView
        val languageNaTextView: TextView

        init {
            Log.d(TAG, "MyViewHolder created")
            positionNoTextView =
                itemView.findViewById(R.id.languageItem_Layout_positionNoTextView)
            ScreenUtil.resizeTextSize(
                positionNoTextView,
                mTextFontSize,
                Constants.FontSize_Scale_Type
            )
            languageNaTextView = itemView.findViewById(R.id.languageNaTextView)
            ScreenUtil.resizeTextSize(
                languageNaTextView,
                mTextFontSize,
                Constants.FontSize_Scale_Type
            )
        }
    }

    // Usually involves inflating a layout from XML and returning the holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        Log.d(TAG, "onCreateViewHolder")
        val layoutInflater = LayoutInflater.from(parent.context)
        // inflate the singerType item view
        return MyViewHolder(layoutInflater.inflate(R.layout.language_list_item,
            parent, false))
    }

    // Involves populating data into the item through holder
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder")
        val language = mLanguages[position]
        holder.apply {
            positionNoTextView.text = position.toString()
            languageNaTextView.text = language.langNa
            itemView.setOnClickListener {
                Log.d(TAG, "onBindViewHolder.itemView.setOnClickListener.${language.langNa}")
                ScreenUtil.showToast(
                    mActivity, language.langNa,
                    mTextFontSize, Constants.FontSize_Scale_Type, Toast.LENGTH_SHORT
                )
                val languageTitle = language.langNa
                ScreenUtil.showToast(
                    mActivity, languageTitle,
                    mTextFontSize, Constants.FontSize_Scale_Type, Toast.LENGTH_SHORT
                )
                when (orderedFrom) {
                    Constants.WordsOrdered -> {
                        Intent(mActivity, WordListActivity::class.java).let {
                            it.putExtra(Constants.OrderedFrom, Constants.LanguageOrdered)
                            it.putExtra(Constants.LanguageTitle, languageTitle)
                            it.putExtra(Constants.LanguageParcelable, language)
                            mActivity.startActivity(it)
                        }
                    }
                    Constants.NewSongOrdered -> {
                        Log.d(TAG, "onBindViewHolder.itemView.setOnClickListener.NewSongOrdered")
                        Intent(mActivity, SongListActivity::class.java).let {
                            it.putExtra(Constants.OrderedFrom, Constants.NewSongLanguageOrdered)
                            it.putExtra(
                                Constants.SongListActivityTitle,
                                languageTitle + " " + mActivity.getString(R.string.newString)
                            )
                            it.putExtra(Constants.LanguageParcelable, language)
                            mActivity.startActivity(it)
                        }
                    }
                    Constants.HotSongOrdered -> {
                        Log.d(TAG, "onBindViewHolder.itemView.setOnClickListener.HotSongOrdered")
                        Intent(mActivity, SongListActivity::class.java).let {
                            it.putExtra(Constants.OrderedFrom, Constants.HotSongLanguageOrdered)
                            it.putExtra(
                                Constants.SongListActivityTitle,
                                languageTitle + " " + mActivity.getString(R.string.hotString)
                            )
                            it.putExtra(Constants.LanguageParcelable, language)
                            mActivity.startActivity(it)
                        }
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        Log.d(TAG, "getItemCount")
        return mLanguages.size
    }

    companion object {
        private const val TAG = "LanguageListAdapter"
    }
}
