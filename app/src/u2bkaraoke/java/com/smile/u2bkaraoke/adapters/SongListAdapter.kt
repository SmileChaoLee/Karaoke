package com.smile.u2bkaraoke.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.smile.karaoke.R
import com.smile.karaoke.interfaces.RecyclerItemListener
import com.smile.karaoke.utilities.LogUtil
import com.smile.u2bkaraoke.model.Song
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.u2bkaraoke.adapters.SongListAdapter.MyViewHolder

class SongListAdapter(
    private val itemListener : RecyclerItemListener,
    private val mSongs: ArrayList<Song>,
    private val mTextFontSize: Float
) : RecyclerView.Adapter<MyViewHolder>() {

    companion object {
        private const val TAG = "SongListAdapter"
    }

    private var positionUpdated: Int = -1
    private var isDataSetChanged = true

    inner class MyViewHolder(itemView: View) : ViewHolder(itemView) {
        val positionNoTextView: TextView
        val songNaTextView: TextView
        var languageNameTextView: TextView
        var singer1NameTextView: TextView
        var singer2NameTextView: TextView

        init {
            val songNaFontSize: Float = mTextFontSize * 0.8f
            val smallFontSize: Float = mTextFontSize * 0.6f
            positionNoTextView =
                itemView.findViewById(R.id.songItem_Layout_positionNoTextView)
            ScreenUtil.resizeTextSize(positionNoTextView, songNaFontSize)
            songNaTextView = itemView.findViewById(R.id.songNaTextView)
            ScreenUtil.resizeTextSize(songNaTextView, songNaFontSize)
            languageNameTextView = itemView.findViewById(R.id.languageNameTextView)
            ScreenUtil.resizeTextSize(languageNameTextView, smallFontSize)
            singer1NameTextView = itemView.findViewById(R.id.singer1NameTextView)
            ScreenUtil.resizeTextSize(singer1NameTextView, smallFontSize)
            singer2NameTextView = itemView.findViewById(R.id.singer2NameTextView)
            ScreenUtil.resizeTextSize(singer2NameTextView, smallFontSize)

            itemView.setOnClickListener {
                itemListener.onItemClick(it, bindingAdapterPosition)
            }

            itemView.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                itemListener.onItemViewFocusChanged(
                    v, bindingAdapterPosition, hasFocus
                )
            }
        }
    }

    // Usually involves inflating a layout from XML and returning the holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        // inflate the singerType item view
        return MyViewHolder(layoutInflater.inflate(R.layout.song_list_item,
            parent, false))
    }

    // Involves populating data into the item through holder
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val song = mSongs[position]
        holder.apply {
            positionNoTextView.text = position.toString()
            songNaTextView.text = song.songNa
            languageNameTextView.text = song.languageNa
            singer1NameTextView.text = song.singer1Na
            singer2NameTextView.text = song.singer2Na

            itemView.setBackgroundColor(itemListener.myBackgroundColor(position))

            if (isDataSetChanged && position == 0) {
                itemView.post { itemView.requestFocus() }
                isDataSetChanged = false
            }
            if (position == positionUpdated) {
                itemView.post { itemView.requestFocus() }
                positionUpdated = -1
            }
        }
    }

    override fun getItemCount(): Int {
        return mSongs.size
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
