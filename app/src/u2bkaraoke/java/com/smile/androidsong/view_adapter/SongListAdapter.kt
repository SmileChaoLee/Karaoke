package com.smile.androidsong.view_adapter

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.smile.androidsong.R
import com.smile.androidsong.model.Constants
import com.smile.androidsong.model.Song
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.androidsong.view_adapter.SongListAdapter.MyViewHolder

class SongListAdapter(
    private val mActivity: Activity,
    private val mSongs: ArrayList<Song>,
    private val mTextFontSize: Float
) : RecyclerView.Adapter<MyViewHolder>() {
    inner class MyViewHolder(itemView: View) : ViewHolder(itemView) {
        val positionNoTextView: TextView
        var songNoTextView: TextView
        val songNaTextView: TextView
        var languageNameTextView: TextView
        var singer1NameTextView: TextView
        var singer2NameTextView: TextView

        init {
            val songNaFontSize: Float = mTextFontSize * 0.8f
            val smallFontSize: Float = mTextFontSize * 0.6f
            positionNoTextView =
                itemView.findViewById(R.id.songItem_Layout_positionNoTextView)
            ScreenUtil.resizeTextSize(
                positionNoTextView,
                songNaFontSize,
                Constants.FontSize_Scale_Type
            )
            songNaTextView = itemView.findViewById(R.id.songNaTextView)
            ScreenUtil.resizeTextSize(
                songNaTextView,
                songNaFontSize,
                Constants.FontSize_Scale_Type
            )
            songNoTextView = itemView.findViewById(R.id.songNoTextView)
            ScreenUtil.resizeTextSize(
                songNoTextView,
                smallFontSize,
                Constants.FontSize_Scale_Type)
            languageNameTextView = itemView.findViewById(R.id.languageNameTextView)
            ScreenUtil.resizeTextSize(
                languageNameTextView,
                smallFontSize,
                Constants.FontSize_Scale_Type
            )
            singer1NameTextView = itemView.findViewById(R.id.singer1NameTextView)
            ScreenUtil.resizeTextSize(
                singer1NameTextView,
                smallFontSize,
                Constants.FontSize_Scale_Type
            )
            singer2NameTextView = itemView.findViewById(R.id.singer2NameTextView)
            ScreenUtil.resizeTextSize(
                singer2NameTextView,
                smallFontSize,
                Constants.FontSize_Scale_Type
            )
        }
    }

    // Usually involves inflating a layout from XML and returning the holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        // inflate the singerType item view
        return MyViewHolder(layoutInflater.inflate(R.layout.song_list_item, parent, false))
    }

    // Involves populating data into the item through holder
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val song = mSongs[position]
        holder.apply {
            positionNoTextView.text = position.toString()
            songNaTextView.text = song.songNa
            songNoTextView.text = song.songNo
            languageNameTextView.text = song.languageNa
            singer1NameTextView.text = song.singer1Na
            singer2NameTextView.text = song.singer2Na
            itemView.setOnClickListener {
                Log.d(TAG, "itemView.setOnClickListener.${song.songNa}")
                ScreenUtil.showToast(
                    mActivity, song.songNa,
                    mTextFontSize, Constants.FontSize_Scale_Type, Toast.LENGTH_SHORT
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return mSongs.size
    }

    companion object {
        private const val TAG = "SongListAdapter"
    }
}
