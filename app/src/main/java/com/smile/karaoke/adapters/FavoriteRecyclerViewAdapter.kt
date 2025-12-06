package com.smile.karaoke.adapters

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smile.karaoke.R
import com.smile.karaoke.interfaces.RecyclerItemListener
import com.smile.karaoke.models.SongDescription
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil

class FavoriteRecyclerViewAdapter (
    private val itemListener : FavItemListener,
    private val mList:  java.util.ArrayList<SongDescription>,
    private val orientation: Int,
    private val textFontSize: Float,
    private val videoThumbnailsWidth: Int,
    private val videoThumbnailsHeight: Int)

    : RecyclerView.Adapter<FavoriteRecyclerViewAdapter.MyViewHolder>() {
    companion object {
        private const val TAG = "FaRecyclerVAdapter"
    }

    interface FavItemListener: RecyclerItemListener {
        fun startEditSongInfo(position: Int)
    }

    private var positionUpdated: Int = -1
    private var isDataSetChanged = true

    class MyViewHolder(itemView: View,
                       orientation: Int,
                       textFontSize: Float,
                       videoThumbnailsWidth: Int,
                       videoThumbnailsHeight: Int,
                       itemListener : FavItemListener)
        : RecyclerView.ViewHolder(itemView) {

        val infoLayout: LinearLayout
        val songVideoImageView: ImageView
        val songNameTextView: TextView
        val editButton: ImageButton
        init {
            LogUtil.d(TAG, "MyViewHolder")

            var infoLayoutWeight = 8f
            if (orientation != Configuration.ORIENTATION_PORTRAIT) {
                infoLayoutWeight = 9f
            }
            infoLayout = itemView.findViewById(R.id.myListInfoLayout)
            var nLayoutParams = infoLayout.layoutParams as LinearLayout.LayoutParams
            nLayoutParams.weight = infoLayoutWeight
            val myListEditLayout = itemView.findViewById<LinearLayout>(R.id.myListEditLayout)
            nLayoutParams = myListEditLayout.layoutParams as LinearLayout.LayoutParams
            nLayoutParams.weight = 10f - infoLayoutWeight

            infoLayout.setOnClickListener {view ->
                LogUtil.d(TAG, "MyViewHolder.infoLayout.setOnClickListener")
                itemListener.onItemClick(
                    view, bindingAdapterPosition)
            }
            infoLayout.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                LogUtil.d(TAG, "MyViewHolder.infoLayout.onFocusChangeListener.hasFocus $hasFocus")
                itemListener.onItemViewFocusChanged(
                    v, bindingAdapterPosition, hasFocus)
            }

            songVideoImageView = itemView.findViewById(R.id.myListVideoImageView)
            nLayoutParams = songVideoImageView.layoutParams as LinearLayout.LayoutParams
            nLayoutParams.width = videoThumbnailsWidth
            nLayoutParams.height = videoThumbnailsHeight
            songNameTextView = itemView.findViewById(R.id.myListNameTextView)
            ScreenUtil.resizeTextSize(songNameTextView,
                textFontSize * 0.5f)

            val buttonWidth = (textFontSize*1.5f).toInt()
            editButton = itemView.findViewById(R.id.myListEditButton)
            nLayoutParams = editButton.layoutParams as LinearLayout.LayoutParams
            nLayoutParams.width = buttonWidth
            nLayoutParams.height = buttonWidth


            editButton.setOnClickListener {
                LogUtil.d(TAG, "MyViewHolder.editButton.setOnClickListener")
                itemListener.startEditSongInfo(bindingAdapterPosition)
            }
            editButton.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                LogUtil.d(TAG, "MyViewHolder.editButton.onFocusChangeListener")
                itemListener.onItemViewFocusChanged(
                    v, bindingAdapterPosition, hasFocus)
            }

            itemView.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                infoLayout.requestFocus()
            }
        }
    }

    // Involves populating data into the item through holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        LogUtil.d(TAG, "onCreateViewHolder().mList.size = ${mList.size}")
        val layoutInflater = LayoutInflater.from(parent.context)
        val fileView = layoutInflater.inflate(R.layout.fragment_my_favorites_item,
            parent, false)
        return MyViewHolder(fileView, orientation, textFontSize,
            videoThumbnailsWidth, videoThumbnailsHeight, itemListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        LogUtil.d(TAG, "onBindViewHolder.position = $position")
        val item = mList[position]
        holder.apply {
            songVideoImageView.setImageBitmap(item.bm)
            val songName = item.song.songName?.trim()?: ""
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