package com.smile.youtube.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smile.karaoke.R
import com.smile.karaoke.interfaces.RecyclerItemListener
import com.smile.karaoke.models.SongDescription
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import java.util.ArrayList

class YouTubeRecyclerAdapter (
    private val itemListener : RecyclerItemListener,
    private val mList:  ArrayList<SongDescription>,
    private val textFontSize: Float,
    private val videoThumbnailsWidth: Int,
    private val videoThumbnailsHeight: Int)

    : RecyclerView.Adapter<YouTubeRecyclerAdapter.MyViewHolder>() {
    companion object {
        private const val TAG = "YouTubeRecAdapter"
    }

    private var positionUpdated: Int = -1
    private var isDataSetChanged = true

    class MyViewHolder(itemView: View,
                       textFontSize: Float,
                       videoThumbnailsWidth: Int,
                       videoThumbnailsHeight: Int,
                       itemListener : RecyclerItemListener)
        : RecyclerView.ViewHolder(itemView) {

        val songVideoImageView: ImageView
        val songNameTextView: TextView
        init {
            LogUtil.d(TAG, "MyViewHolder")

            itemView.setOnClickListener {view ->
                LogUtil.d(TAG, "MyViewHolder.infoLayout.setOnClickListener")
                itemListener.onItemClick(
                    view, bindingAdapterPosition)
            }
            itemView.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                LogUtil.d(TAG, "MyViewHolder.infoLayout.onFocusChangeListener.hasFocus $hasFocus")
                itemListener.onItemViewFocusChanged(
                    v, bindingAdapterPosition, hasFocus)
            }

            songVideoImageView = itemView.findViewById(R.id.myListVideoImageView)
            val nLayoutParams = songVideoImageView.layoutParams as LinearLayout.LayoutParams
            nLayoutParams.width = videoThumbnailsWidth
            nLayoutParams.height = videoThumbnailsHeight
            songNameTextView = itemView.findViewById(R.id.myListNameTextView)
            ScreenUtil.resizeTextSize(songNameTextView,
                textFontSize * 0.5f)
        }
    }

    // Involves populating data into the item through holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        LogUtil.d(TAG, "onCreateViewHolder().mList.size = ${mList.size}")
        val layoutInflater = LayoutInflater.from(parent.context)
        val fileView = layoutInflater.inflate(R.layout.fragment_youtube_video_item,
            parent, false)
        return MyViewHolder(fileView, textFontSize,
            videoThumbnailsWidth, videoThumbnailsHeight, itemListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        LogUtil.d(TAG, "onBindViewHolder.position = $position")
        val item = mList[position]
        holder.apply {
            songVideoImageView.setImageBitmap(item.bm)
            LogUtil.d(TAG, "onBindViewHolder.item.song.songName = ${item.song.songName}")
            val songName = item.song.songName?.trim()?: ""
            LogUtil.d(TAG, "onBindViewHolder.songName = $songName")
            songNameTextView.apply {
                text = songName.ifEmpty { "No Name" }
                if (item.song.included == "1") setTextColor(Color.GREEN)
                else setTextColor(Color.WHITE)
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