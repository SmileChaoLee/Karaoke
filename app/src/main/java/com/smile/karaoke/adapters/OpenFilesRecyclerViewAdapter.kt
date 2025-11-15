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
import com.smile.karaoke.models.FileDescription
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil

class OpenFilesRecyclerViewAdapter(
    private val itemListener : RecyclerItemListener,
    private val mList : java.util.ArrayList<FileDescription>,
    private val textFontSize: Float,
    private val videoThumbnailsWidth: Int,
    private val videoThumbnailsHeight: Int)

    : RecyclerView.Adapter<OpenFilesRecyclerViewAdapter.MyViewHolder>() {

    private var positionUpdated: Int = -1
    private var isDataSetChanged = true

    companion object {
        private const val TAG = "FilesRecyclerVAdapter"
    }

    class MyViewHolder(itemView: View,
                       itemListener : RecyclerItemListener)
        : RecyclerView.ViewHolder(itemView) {
        val folderImageView: ImageView
        val fileNameTextView: TextView
        val videoImageView: ImageView
        init {
            LogUtil.d(TAG, "MyViewHolder")
            folderImageView = itemView.findViewById(R.id.folderImageView)
            fileNameTextView = itemView.findViewById(R.id.openFileNameTextView)
            fileNameTextView.visibility = View.VISIBLE
            videoImageView = itemView.findViewById(R.id.videoImageView)
            videoImageView.visibility = View.VISIBLE
            itemView.setOnClickListener { view ->
                LogUtil.d(TAG, "setOnClickListener.position = $bindingAdapterPosition")
                itemListener.onItemClick(
                    view, bindingAdapterPosition)
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
        LogUtil.d(TAG, "onCreateViewHolder.mList.size = ${mList.size}")
        val layoutInflater = LayoutInflater.from(parent.context)
        val fileView = layoutInflater.inflate(R.layout.fragment_open_file_item, parent, false)
        return MyViewHolder(fileView, itemListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        LogUtil.d(TAG, "onBindViewHolder.position = $position")
        val item = mList[position]
        LogUtil.d(TAG, "onBindViewHolder.item.filename = ${item.file.name}")
        holder.apply {
            videoImageView.setImageBitmap(item.bm)
            fileNameTextView.apply {
                text = item.file.name
                setTextColor(Color.WHITE)
                if (item.selected) setTextColor(Color.GREEN)
            }
            if (item.file.isDirectory) {
                folderImageView.visibility = View.VISIBLE
                ScreenUtil.resizeTextSize(fileNameTextView,
                    textFontSize * 0.8f,
                    ScreenUtil.FontSize_Pixel_Type)
                videoImageView.visibility = View.GONE
                LogUtil.d(TAG, "onBindViewHolder.item.file isDirectory")
            } else {
                folderImageView.visibility = View.GONE
                ScreenUtil.resizeTextSize(fileNameTextView,
                    textFontSize * 0.5f,
                    ScreenUtil.FontSize_Pixel_Type)
                videoImageView.visibility = View.VISIBLE
                LogUtil.d(TAG, "onBindViewHolder.item.file not isDirectory")
            }

            var layoutParams: ViewGroup.MarginLayoutParams = folderImageView.layoutParams
                    as ViewGroup.MarginLayoutParams
            layoutParams.width = (textFontSize * 2.0f).toInt()
            layoutParams.height = layoutParams.width
            folderImageView.layoutParams = layoutParams

            layoutParams = videoImageView.layoutParams
                    as ViewGroup.MarginLayoutParams
            layoutParams.width = videoThumbnailsWidth
            layoutParams.height = videoThumbnailsHeight

            itemView.setBackgroundColor(itemListener.myBackgroundColor(position))

            if (isDataSetChanged) {
                if (position == 0) {
                    holder.itemView.requestFocus()
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
        LogUtil.d(TAG, "getItemCount().size = ${mList.size}")
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