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
import com.smile.karaoke.models.FileDescription
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil

class OpenFilesRecyclerViewAdapter(
    private var recyclerItemClickListener : OnRecyclerItemClickListener,
    private var mList : java.util.ArrayList<FileDescription>,
    private var textColor : Int, private var transparentLightGray : Int,
    private val textFontSize: Float,
    private val videoThumbnailsWidth: Int,
    private val videoThumbnailsHeight: Int)

    : RecyclerView.Adapter<OpenFilesRecyclerViewAdapter.MyViewHolder>() {

    private var positionUpdated: Int = -1

    interface OnRecyclerItemClickListener {
        fun onRecyclerItemClick(v: View?, position: Int)
    }

    companion object {
        private const val TAG = "FilesRecyclerVAdapter"
    }

    class MyViewHolder(itemView: View,
                       recyclerItemClickListener : OnRecyclerItemClickListener)
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
                recyclerItemClickListener.onRecyclerItemClick(
                    view, bindingAdapterPosition)
            }
        }
    }

    // Involves populating data into the item through holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        LogUtil.d(TAG, "onCreateViewHolder.mList.size = ${mList.size}")
        val layoutInflater = LayoutInflater.from(parent.context)
        val fileView = layoutInflater.inflate(R.layout.fragment_open_file_item, parent, false)
        return MyViewHolder(fileView, recyclerItemClickListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        LogUtil.d(TAG, "onBindViewHolder.position = $position")
        val item = mList[position]
        holder.videoImageView.setImageBitmap(item.bm)
        holder.fileNameTextView.apply {
            text = item.file.name
            setTextColor(Color.WHITE)
            if (item.selected) setTextColor(textColor)
        }
        if (item.file.isDirectory) {
            holder.folderImageView.visibility = View.VISIBLE
            ScreenUtil.resizeTextSize(holder.fileNameTextView,
                textFontSize * 0.8f,
                ScreenUtil.FontSize_Pixel_Type)
            holder.videoImageView.visibility = View.GONE
        } else {
            holder.folderImageView.visibility = View.GONE
            ScreenUtil.resizeTextSize(holder.fileNameTextView,
                textFontSize * 0.5f,
                ScreenUtil.FontSize_Pixel_Type)
            holder.videoImageView.visibility = View.VISIBLE
        }

        var layoutParams: ViewGroup.MarginLayoutParams = holder.folderImageView.layoutParams
                as ViewGroup.MarginLayoutParams
        layoutParams.width = (textFontSize * 2.0f).toInt()
        layoutParams.height = layoutParams.width
        holder.folderImageView.layoutParams = layoutParams

        layoutParams = holder.videoImageView.layoutParams
                as ViewGroup.MarginLayoutParams
        layoutParams.width = videoThumbnailsWidth
        layoutParams.height = videoThumbnailsHeight

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
        LogUtil.d(TAG, "getItemCount().favoriteList.size = ${mList.size}")
        return mList.size
    }

    fun myNotifyItemChanged(position:Int) {
        LogUtil.d(TAG, "myNotifyItemChanged.position = $position")
        positionUpdated = position
        notifyItemChanged(position)
    }
}