package com.smile.karaoke.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smile.karaoke.R
import com.smile.karaoke.SmileAppBase
import com.smile.karaoke.models.SongDescription
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import kotlin.times

class FavoriteRecyclerViewAdapter (
    private var recyclerItemClickListener : OnRecyclerItemClickListener,
    private var mList:  java.util.ArrayList<SongDescription>,
    private var textColor : Int, private var transparentLightGray : Int,
    private val textFontSize: Float,
    private val videoThumbnailsWidth: Int,
    private val videoThumbnailsHeight: Int)

    : RecyclerView.Adapter<FavoriteRecyclerViewAdapter.MyViewHolder>() {

    interface OnRecyclerItemClickListener {
        fun onRecyclerItemClick(v: View?, position: Int)
    }

    private var positionUpdated: Int = -1

    companion object {
        private const val TAG = "FaRecyclerVAdapter"
    }

    class MyViewHolder(itemView: View,
                       recyclerItemClickListener : OnRecyclerItemClickListener)
        : RecyclerView.ViewHolder(itemView) {
        val songVideoImageView: ImageView
        val songNameTextView: TextView
        init {
            LogUtil.d(TAG, "MyViewHolder")
            songVideoImageView = itemView.findViewById(R.id.myListVideoImageView)
            songNameTextView = itemView.findViewById(R.id.myListNameTextView)
            itemView.setOnClickListener {view ->
                recyclerItemClickListener.onRecyclerItemClick(
                    view, bindingAdapterPosition
                )
            }
        }
    }

    // Involves populating data into the item through holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        LogUtil.d(TAG, "onCreateViewHolder().mList.size = ${mList.size}")
        val layoutInflater = LayoutInflater.from(parent.context)
        val fileView = layoutInflater.inflate(R.layout.fragment_my_favorites_item, parent, false)
        return MyViewHolder(fileView, recyclerItemClickListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        LogUtil.d(TAG, "onBindViewHolder.position = $position")
        val item = mList[position]
        holder.songVideoImageView.setImageBitmap(item.bm)
        val layoutParams = holder.songVideoImageView.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.width = videoThumbnailsWidth
        layoutParams.height = videoThumbnailsHeight
        val songName = item.song.songName?.trim()?: ""
        holder.songNameTextView.apply {
            text = songName.ifEmpty { "No Name" }
            if (item.song.included == "1") setTextColor(textColor)
            else setTextColor(Color.WHITE)
            ScreenUtil.resizeTextSize(this,
                textFontSize * 0.5f,
                ScreenUtil.FontSize_Pixel_Type)
        }
        holder.itemView.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                v.setBackgroundColor(SmileAppBase.accentColor) // Example
            } else {
                v.setBackgroundColor(if (position % 2 == 0) Color.BLACK
                else transparentLightGray)
            }
        }
        if (position == 0) {
            holder.itemView.requestFocus()
        }
        if(position == positionUpdated) {
            holder.itemView.requestFocus()
            positionUpdated = -1
        }
    }

    override fun getItemCount(): Int {
        LogUtil.d(TAG, "getItemCount().mList.size = ${mList.size}")
        return mList.size
    }

    fun myNotifyItemChanged(position:Int) {
        LogUtil.d(TAG, "myNotifyItemChanged.position = $position")
        positionUpdated = position
        notifyItemChanged(position)
    }
}