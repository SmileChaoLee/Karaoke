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

private const val TAG = "FilesRecyclerVAdapter"

class OpenFilesRecyclerViewAdapter private constructor(
    private var recyclerItemClickListener : OnRecyclerItemClickListener,
    private var mList : java.util.ArrayList<FileDescription>,
    private var textColor : Int, private var transparentLightGray : Int)

    : RecyclerView.Adapter<OpenFilesRecyclerViewAdapter.MyViewHolder>() {

    private var positionUpdated: Int = -1

    interface OnRecyclerItemClickListener {
        fun onRecyclerItemClick(v: View?, position: Int)
    }

    companion object {
        private var viewAdapter : OpenFilesRecyclerViewAdapter? = null
        @JvmStatic
        fun getInstance(recyclerItemClickListener: OnRecyclerItemClickListener,
                        mList : java.util.ArrayList<FileDescription>,
                        textColor : Int, transparentLightGray : Int)
        : OpenFilesRecyclerViewAdapter {

            LogUtil.d(TAG, "getInstance.viewAdapter = $viewAdapter")
            if (viewAdapter == null) {
                viewAdapter = OpenFilesRecyclerViewAdapter(recyclerItemClickListener,
                    mList, textColor, transparentLightGray)
            } else {
                viewAdapter?.let {
                    it.recyclerItemClickListener = recyclerItemClickListener
                    it.mList = mList
                    it.textColor = textColor
                    it.transparentLightGray = transparentLightGray
                }
            }

            return viewAdapter!!
        }
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
            var layoutParams: ViewGroup.MarginLayoutParams = folderImageView.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.width = (SmileAppBase.textFontSize * 2.0f).toInt()
            layoutParams.height = layoutParams.width
            folderImageView.layoutParams = layoutParams
            fileNameTextView = itemView.findViewById(R.id.openFileNameTextView)
            fileNameTextView.visibility = View.VISIBLE
            videoImageView = itemView.findViewById(R.id.videoImageView)
            videoImageView.visibility = View.VISIBLE
            layoutParams = videoImageView.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.width = SmileAppBase.videoThumbnailsWidth
            layoutParams.height = SmileAppBase.videoThumbnailsHeight

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
                SmileAppBase.textFontSize * 0.8f,
                ScreenUtil.FontSize_Pixel_Type)
            holder.videoImageView.visibility = View.GONE
        } else {
            holder.folderImageView.visibility = View.GONE
            ScreenUtil.resizeTextSize(holder.fileNameTextView,
                SmileAppBase.textFontSize * 0.5f,
                ScreenUtil.FontSize_Pixel_Type)
            holder.videoImageView.visibility = View.VISIBLE
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
        LogUtil.d(TAG, "getItemCount().favoriteList.size = ${mList.size}")
        return mList.size
    }

    fun myNotifyItemChanged(position:Int) {
        LogUtil.d(TAG, "myNotifyItemChanged.position = $position")
        positionUpdated = position
        notifyItemChanged(position)
    }
}