package com.smile.u2bkaraoke.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.smile.karaoke.R
import com.smile.karaoke.interfaces.RecyclerItemListener
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.u2bkaraoke.adapters.WordListAdapter.MyViewHolder

class WordListAdapter(
    private val itemListener : RecyclerItemListener,
    private val mWordList: ArrayList<String>,
    private val mTextFontSize: Float
) : RecyclerView.Adapter<MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : ViewHolder(itemView) {
        val wordNoTextView: TextView
        val wordNaTextView: TextView

        init {
            wordNoTextView = itemView.findViewById(R.id.wordsOrderNoTextView)
            ScreenUtil.resizeTextSize(wordNoTextView, mTextFontSize)
            wordNaTextView = itemView.findViewById(R.id.wordsOrderNaTextView)
            ScreenUtil.resizeTextSize(wordNaTextView, mTextFontSize)

            itemView.setOnClickListener {
                itemListener.onItemClick(it, bindingAdapterPosition)
            }

            itemView.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                itemListener.onItemViewFocusChanged(
                    v, bindingAdapterPosition, hasFocus
                )
            }

            itemListener.nextFocusUpId(itemView)
        }
    }

    // Usually involves inflating a layout from XML and returning the holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        // inflate the singerType item view
        return MyViewHolder(layoutInflater.inflate(R.layout.word_list_item,
            parent, false))
    }

    // Involves populating data into the item through holder
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val word = mWordList[position]
        holder.apply {
            wordNoTextView.text = "${position + 1}"
            wordNaTextView.text = word
            if (position == 0) {
                itemView.post { itemView.requestFocus() }
            }
        }
    }

    override fun getItemCount(): Int {
        return mWordList.size
    }
}
