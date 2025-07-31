package com.smile.karaoke.adapters

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smile.karaoke.R
import com.smile.karaoke.SmileAppBase
import com.smile.karaoke.models.SongDescription
import com.smile.smilelibraries.utilities.ScreenUtil

private const val TAG = "FaRecyclerVAdapter"

class FavoriteRecyclerViewAdapter private constructor(
    private var recyclerItemClickListener : OnRecyclerItemClickListener,
    private var textFontSize : Float,
    private var mList:  java.util.ArrayList<SongDescription>,
    private var textColor : Int, private var transparentLightGray : Int)

    : RecyclerView.Adapter<FavoriteRecyclerViewAdapter.MyViewHolder>() {

    interface OnRecyclerItemClickListener {
        fun onRecyclerItemClick(v: View?, position: Int)
    }

    private var positionUpdated: Int = -1

    companion object {
        private var viewAdapter : FavoriteRecyclerViewAdapter? = null
        @JvmStatic
        fun getInstance(recyclerItemClickListener : OnRecyclerItemClickListener,
                        textFontSize : Float,
                        mList : java.util.ArrayList<SongDescription>,
                        textColor : Int, transparentLightGray : Int)
        : FavoriteRecyclerViewAdapter {

            Log.d(TAG, "getInstance.viewAdapter = $viewAdapter")
            if (viewAdapter == null) {
                viewAdapter = FavoriteRecyclerViewAdapter(recyclerItemClickListener,
                        textFontSize, mList, textColor, transparentLightGray)
            } else {
                viewAdapter?.let {
                    it.recyclerItemClickListener = recyclerItemClickListener
                    it.textFontSize = textFontSize
                    it.mList = mList
                    it.textColor = textColor
                    it.transparentLightGray = transparentLightGray
                }
            }

            return viewAdapter!!
        }
    }

    class MyViewHolder(itemView: View,
                       recyclerItemClickListener : OnRecyclerItemClickListener,
                       textFontSize: Float)
        : RecyclerView.ViewHolder(itemView) {
        val songVideoImageView: ImageView
        val songNameTextView: TextView
        init {
            Log.d(TAG, "MyViewHolder")
            songVideoImageView = itemView.findViewById(R.id.myListVideoImageView)
            songNameTextView = itemView.findViewById(R.id.myListNameTextView)
            ScreenUtil.resizeTextSize(songNameTextView, textFontSize, ScreenUtil.FontSize_Pixel_Type)

            itemView.setOnClickListener {view ->
                recyclerItemClickListener.onRecyclerItemClick(
                    view, bindingAdapterPosition
                )
            }
        }
    }

    // Involves populating data into the item through holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        Log.d(TAG, "onCreateViewHolder().mList.size = ${mList.size}")
        val layoutInflater = LayoutInflater.from(parent.context)
        val fileView = layoutInflater.inflate(R.layout.fragment_my_favorites_item, parent, false)
        return MyViewHolder(fileView, recyclerItemClickListener, textFontSize)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder.position = $position")
        val item = mList[position]
        holder.songVideoImageView.setImageBitmap(item.bm)
        val songName = item.song.songName?.trim()?: ""
        holder.songNameTextView.apply {
            text = songName.ifEmpty { "No Name" }
            if (item.song.included == "1") setTextColor(textColor)
            else setTextColor(Color.WHITE)
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
        Log.d(TAG, "getItemCount().mList.size = ${mList.size}")
        return mList.size
    }

    fun myNotifyItemChanged(position:Int) {
        Log.d(TAG, "myNotifyItemChanged.position = $position")
        positionUpdated = position
        notifyItemChanged(position)
    }
}