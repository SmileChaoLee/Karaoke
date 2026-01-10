package com.smile.u2bkaraoke.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.smile.karaoke.R
import com.smile.karaoke.interfaces.RecyclerItemListener
import com.smile.u2bkaraoke.model.Language
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.u2bkaraoke.adapters.LangListAdapter.MyViewHolder

class LangListAdapter (
    private val itemListener : RecyclerItemListener,
    private val mLanguages: ArrayList<Language>,
    private val mTextFontSize: Float
) : RecyclerView.Adapter<MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : ViewHolder(itemView) {
        val positionNoTextView: TextView
        val languageNaTextView: TextView

        init {
            // LogUtil.d(TAG, "MyViewHolder init")
            positionNoTextView =
                itemView.findViewById(R.id.languageItem_Layout_positionNoTextView)
            ScreenUtil.resizeTextSize(positionNoTextView, mTextFontSize)
            languageNaTextView = itemView.findViewById(R.id.languageNaTextView)
            ScreenUtil.resizeTextSize(languageNaTextView, mTextFontSize)

            itemView.setOnClickListener {
                itemListener.onItemClick(it, bindingAdapterPosition)
            }
        }
    }

    // Usually involves inflating a layout from XML and returning the holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // LogUtil.d(TAG, "onCreateViewHolder")
        val layoutInflater = LayoutInflater.from(parent.context)
        // inflate the singerType item view
        return MyViewHolder(layoutInflater.inflate(R.layout.language_list_item,
            parent, false))
    }

    // Involves populating data into the item through holder
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // LogUtil.d(TAG, "onBindViewHolder")
        val language = mLanguages[position]
        holder.apply {
            positionNoTextView.text = position.toString()
            languageNaTextView.text = language.langNa
        }
    }

    override fun getItemCount(): Int {
        return mLanguages.size
    }
}
