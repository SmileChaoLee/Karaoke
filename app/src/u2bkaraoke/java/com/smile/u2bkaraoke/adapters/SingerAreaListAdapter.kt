package com.smile.u2bkaraoke.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.smile.karaoke.R
import com.smile.karaoke.interfaces.RecyclerItemListener
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.u2bkaraoke.adapters.SingerAreaListAdapter.MyViewHolder
import com.smile.u2bkaraoke.model.SingerArea

class SingerAreaListAdapter(
    private val itemListener : RecyclerItemListener,
    private val mSingerAreas: ArrayList<SingerArea>,
    private val mTextFontSize: Float
) : RecyclerView.Adapter<MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : ViewHolder(itemView) {
        val positionNoTextView: TextView
        val singerAreaNaTextView: TextView

        init {
            positionNoTextView =
                itemView.findViewById(R.id.singerAreaItem_Layout_positionNoTextView)
            ScreenUtil.resizeTextSize(positionNoTextView, mTextFontSize)
            singerAreaNaTextView = itemView.findViewById(R.id.singerAreaNaTextView)
            ScreenUtil.resizeTextSize(singerAreaNaTextView, mTextFontSize)

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

        return MyViewHolder(layoutInflater.inflate(R.layout.singer_area_list_item,
            parent, false))
    }

    // Involves populating data into the item through holder
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val singerArea = mSingerAreas[position]
        holder.apply {
            positionNoTextView.text = position.toString()
            singerAreaNaTextView.text = singerArea.areaNa
            if (position == 0) {
                itemView.post { itemView.requestFocus() }
            }
        }
    }

    override fun getItemCount(): Int {
        return mSingerAreas.size
    }
}
