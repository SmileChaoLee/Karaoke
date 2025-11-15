package com.smile.karaoke.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smile.karaoke.R
import com.smile.karaoke.interfaces.RecyclerItemListener
import com.smile.karaoke.models.SongDescription
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil

class FavoriteRecyclerViewAdapter (
    private var itemListener : RecyclerItemListener,
    private var mList:  java.util.ArrayList<SongDescription>,
    private val textFontSize: Float,
    private val videoThumbnailsWidth: Int,
    private val videoThumbnailsHeight: Int)

    : RecyclerView.Adapter<FavoriteRecyclerViewAdapter.MyViewHolder>() {

    private val transparentLightGray =
        Color.argb(0x33, 0xd5, 0xd5, 0xd5) //Color(0x33D5D5D5)
    private var positionUpdated: Int = -1
    private var isDataSetChanged = true

    companion object {
        private const val TAG = "FaRecyclerVAdapter"
    }

    class MyViewHolder(itemView: View,
                       itemListener : RecyclerItemListener)
        : RecyclerView.ViewHolder(itemView) {
        val songVideoImageView: ImageView
        val songNameTextView: TextView
        init {
            LogUtil.d(TAG, "MyViewHolder")
            songVideoImageView = itemView.findViewById(R.id.myListVideoImageView)
            songNameTextView = itemView.findViewById(R.id.myListNameTextView)
            itemView.setOnClickListener {view ->
                itemListener.onItemClick(
                    view, bindingAdapterPosition
                )
            }
            itemView.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                itemListener.onItemViewFocusChanged(
                    v, bindingAdapterPosition, hasFocus
                )
            }
        }
    }

    // Involves populating data into the item through holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        LogUtil.d(TAG, "onCreateViewHolder().mList.size = ${mList.size}")
        val layoutInflater = LayoutInflater.from(parent.context)
        val fileView = layoutInflater.inflate(R.layout.fragment_my_favorites_item, parent, false)
        return MyViewHolder(fileView, itemListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        LogUtil.d(TAG, "onBindViewHolder.position = $position")
        val item = mList[position]
        holder.apply {
            songVideoImageView.setImageBitmap(item.bm)
            val layoutParams = songVideoImageView.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.width = videoThumbnailsWidth
            layoutParams.height = videoThumbnailsHeight
            val songName = item.song.songName?.trim()?: ""
            songNameTextView.apply {
                text = songName.ifEmpty { "No Name" }
                if (item.song.included == "1") setTextColor(Color.GREEN)
                else setTextColor(Color.WHITE)
                ScreenUtil.resizeTextSize(this,
                    textFontSize * 0.5f,
                    ScreenUtil.FontSize_Pixel_Type)
            }

            itemView.setBackgroundColor(itemListener.myBackgroundColor(position))

            if (isDataSetChanged) {
                if (position == 0) {
                    itemView.requestFocus()
                }
                isDataSetChanged = false
            }
            if (position == positionUpdated) {
                itemView.requestFocus()
                positionUpdated = -1
            }
        }
    }

    override fun onViewAttachedToWindow(holder: MyViewHolder) {
        super.onViewAttachedToWindow(holder)
        val position = holder.absoluteAdapterPosition
        LogUtil.d(TAG, "onViewAttachedToWindow.position = $position")
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

    @SuppressLint("NotifyDataSetChanged")
    fun myNotifyDataSetChanged() {
        LogUtil.d(TAG, "myNotifyDataSetChanged")
        isDataSetChanged = true
        notifyDataSetChanged()
    }
}