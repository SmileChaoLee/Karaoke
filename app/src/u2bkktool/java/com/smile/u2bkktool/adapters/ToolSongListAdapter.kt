package com.smile.u2bkktool.adapters

import android.view.View
import com.smile.karaoke.interfaces.RecyclerItemListener
import com.smile.karaoke.utilities.LogUtil
import com.smile.u2bkaraoke.model.Song
import com.smile.u2bkaraoke.adapters.SongListAdapter

class ToolSongListAdapter(
    private val itemListener: RecyclerItemListener,
    songs: ArrayList<Song>,
    textFontSize: Float
) : SongListAdapter(itemListener, songs, textFontSize) {

    companion object {
        private const val TAG = "ToolSongListAdapt"
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        LogUtil.d(TAG, "onBindViewHolder")
        holder.itemView.setOnLongClickListener { view: View ->
            itemListener.onItemLongClick(view, holder.bindingAdapterPosition)
        }
    }
}