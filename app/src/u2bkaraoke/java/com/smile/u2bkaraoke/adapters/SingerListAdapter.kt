package com.smile.u2bkaraoke.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.smile.karaoke.R
import com.smile.karaoke.interfaces.RecyclerItemListener
import com.smile.u2bkaraoke.model.Singer
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.u2bkaraoke.adapters.SingerListAdapter.MyViewHolder

class SingerListAdapter(
    private val itemListener : RecyclerItemListener,
    private val mSingers: ArrayList<Singer>,
    private val mTextFontSize: Float
) : RecyclerView.Adapter<MyViewHolder>() {
    inner class MyViewHolder(itemView: View) : ViewHolder(itemView) {
        val positionNoTextView: TextView
        val singerNoTextView: TextView
        val singerNaTextView: TextView

        init {
            positionNoTextView =
                itemView.findViewById(R.id.singerItem_Layout_positionNoTextView)
            ScreenUtil.resizeTextSize(positionNoTextView, mTextFontSize)
            singerNoTextView = itemView.findViewById(R.id.singerNoTextView)
            ScreenUtil.resizeTextSize(singerNoTextView, mTextFontSize)
            singerNaTextView = itemView.findViewById(R.id.singerNaTextView)
            ScreenUtil.resizeTextSize(singerNaTextView, mTextFontSize)

            itemView.setOnClickListener {
                itemListener.onItemClick(it, bindingAdapterPosition)
            }

            itemView.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                itemListener.onItemViewFocusChanged(
                    v, bindingAdapterPosition, hasFocus
                )
            }
        }
    }

    // Usually involves inflating a layout from XML and returning the holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        // inflate the singer item view
        return MyViewHolder(layoutInflater.inflate(R.layout.singer_list_item,
            parent, false))
    }

    // Involves populating data into the item through holder
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val singer = mSingers[position]
        holder.apply {
            positionNoTextView.text = position.toString()
            singerNoTextView.text = singer.singNo
            singerNaTextView.text = singer.singNa
        }
    }

    override fun getItemCount(): Int {
        return mSingers.size
    }
}
