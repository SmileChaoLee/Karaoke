package com.smile.karaoke.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.smile.karaoke.SmileAppBase
import com.smile.karaoke.R
import com.smile.karaoke.models.SongInfo
import com.smile.karaoke.models.SongListSQLite
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil

class SelectedFavoriteAdapter (
        private var itemClickListener : OnRecyclerItemClickListener,
        private var songListSQLite : SongListSQLite,
        private var mList : ArrayList<SongInfo>,
        private var textFontSize: Float,
        private var yellow2Color: Int, private var yellow3Color: Int)

    : RecyclerView.Adapter<SelectedFavoriteAdapter.MyViewHolder>() {

    companion object {
        private const val TAG = "SelectedFavAdapter"
    }

    private var positionUpdated: Int = -1
    private var isDataSetChanged = true

    interface OnRecyclerItemClickListener {
        fun onRecyclerItemClick(v: View?, position: Int)
        fun editSongButtonFunc(position : Int)
        fun deleteSongButtonFunc(position : Int)
        fun playSongButtonFunc(position : Int)
    }

    init {
        setHasStableIds(true) // 1. Enable stable IDs
    }

    class MyViewHolder(itemView: View, itemClickListener : OnRecyclerItemClickListener, textFontSize: Float)
        : RecyclerView.ViewHolder(itemView) {

        val titleNameTextView: TextView
        val filePathTextView: TextView
        val musicTrackTextView: TextView
        val musicChannelTextView: TextView
        val vocalTrackTextView: TextView
        val vocalChannelTextView: TextView
        val includedPlaylistCheckBox: CheckBox
        val editSongButton : Button
        val deleteSongButton : Button
        val playSongButton : Button
        var inPlaylist: Boolean = true

        init {
            LogUtil.d(TAG, "MyViewHolder() is called")
            val itemTextSize = textFontSize * 0.6f
            val buttonTextSize = textFontSize * 0.8f

            val titleStringTextView : TextView = itemView.findViewById(R.id.titleStringTextView)
            ScreenUtil.resizeTextSize(titleStringTextView, itemTextSize)
            titleNameTextView = itemView.findViewById(R.id.titleNameTextView)
            ScreenUtil.resizeTextSize(titleNameTextView, itemTextSize)

            val filePathStringTextView : TextView = itemView.findViewById(R.id.filePathStringTextView)
            ScreenUtil.resizeTextSize(filePathStringTextView, itemTextSize, ScreenUtil.FontSize_Pixel_Type)
            filePathTextView = itemView.findViewById(R.id.filePathTextView)
            ScreenUtil.resizeTextSize(filePathTextView, itemTextSize)
            val audioMusicLinearLayout : LinearLayout = itemView.findViewById(R.id.audioMusicLinearLayout)
            val musicTrackStringTextView : TextView = itemView.findViewById(R.id.musicTrackStringTextView)
            ScreenUtil.resizeTextSize(musicTrackStringTextView, itemTextSize)
            musicTrackTextView = itemView.findViewById(R.id.musicTrackTextView)
            ScreenUtil.resizeTextSize(musicTrackTextView, itemTextSize)

            val musicChannelStringTextView : TextView = itemView.findViewById(R.id.musicChannelStringTextView)
            ScreenUtil.resizeTextSize(musicChannelStringTextView, itemTextSize)
            musicChannelTextView = itemView.findViewById(R.id.musicChannelTextView)
            ScreenUtil.resizeTextSize(musicChannelTextView, itemTextSize)
            val audioVocalLinearLayout : LinearLayout = itemView.findViewById(R.id.audioVocalLinearLayout)
            val vocalTrackStringTextView : TextView = itemView.findViewById(R.id.vocalTrackStringTextView)
            ScreenUtil.resizeTextSize(vocalTrackStringTextView, itemTextSize)
            vocalTrackTextView = itemView.findViewById(R.id.vocalTrackTextView)
            ScreenUtil.resizeTextSize(vocalTrackTextView, itemTextSize)

            val vocalChannelStringTextView : TextView = itemView.findViewById(R.id.vocalChannelStringTextView)
            ScreenUtil.resizeTextSize(vocalChannelStringTextView, itemTextSize)
            vocalChannelTextView = itemView.findViewById(R.id.vocalChannelTextView)
            ScreenUtil.resizeTextSize(vocalChannelTextView, itemTextSize)

            // audioVocalLinearLayout.setVisibility(View.GONE);
            audioVocalLinearLayout.visibility = View.VISIBLE

            val includedPlaylistTextView : TextView = itemView.findViewById(R.id.includedPlaylistTextView)
            ScreenUtil.resizeTextSize(includedPlaylistTextView, itemTextSize)
            includedPlaylistCheckBox = itemView.findViewById(R.id.includedPlaylistCheckBox)
            ScreenUtil.resizeTextSize(includedPlaylistCheckBox, itemTextSize)
            includedPlaylistCheckBox.onFocusChangeListener
            includedPlaylistCheckBox.onFocusChangeListener = View.OnFocusChangeListener{ _, hasFocus ->
                if (hasFocus) {
                    includedPlaylistTextView.setBackgroundColor(SmileAppBase.accentColor)
                    includedPlaylistCheckBox.setBackgroundColor(Color.BLUE)
                } else {
                    includedPlaylistTextView.setBackgroundColor(Color.TRANSPARENT)
                    includedPlaylistCheckBox.setBackgroundColor(Color.TRANSPARENT)
                }
            }

            editSongButton = itemView.findViewById(R.id.editSongButton)
            ScreenUtil.resizeTextSize(editSongButton, buttonTextSize)
            deleteSongButton = itemView.findViewById(R.id.deleteSongButton)
            ScreenUtil.resizeTextSize(deleteSongButton, buttonTextSize)
            playSongButton = itemView.findViewById(R.id.playSongButton)
            ScreenUtil.resizeTextSize(playSongButton, buttonTextSize)

            // the following is still needed to be test (have to reduce the usage of memory)
            // if (!com.smile.karaokeplayer.BuildConfig.DEBUG) playSongButton.setVisibility(View.GONE);
            editSongButton.setOnClickListener {
                LogUtil.d(TAG, "MyViewHolder.editSongButton.bindingAdapterPosition = $bindingAdapterPosition")
                itemClickListener.editSongButtonFunc(bindingAdapterPosition)
            }
            deleteSongButton.setOnClickListener {
                LogUtil.d(TAG, "MyViewHolder.deleteSongButton.bindingAdapterPosition = $bindingAdapterPosition")
                itemClickListener.deleteSongButtonFunc(bindingAdapterPosition)
            }
            playSongButton.setOnClickListener {
                LogUtil.d(TAG, "MyViewHolder.playSongButton.bindingAdapterPosition = $bindingAdapterPosition")
                itemClickListener.playSongButtonFunc(bindingAdapterPosition)
            }
            itemView.setOnClickListener {
                LogUtil.d(TAG, "MyViewHolder.itemView.bindingAdapterPosition = $bindingAdapterPosition")
                itemClickListener.onRecyclerItemClick(itemView, bindingAdapterPosition)
            }

            itemView.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                LogUtil.d(TAG, "MyViewHolder.itemView.onFocusChangeListener.hasFocus = $hasFocus")
                if (hasFocus) {
                    editSongButton.post { editSongButton.requestFocus() }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        LogUtil.d(TAG, "onCreateViewHolder().mList.size = ${mList.size}")
        val layoutInflater = LayoutInflater.from(parent.context)
        val fileView = layoutInflater.inflate(R.layout.activity_favorite_list_item, parent, false)
        return MyViewHolder(fileView, itemClickListener, textFontSize)
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        LogUtil.d(TAG, "onBindViewHolder().position = $position")
        val singleSongInfo = mList[position]
        holder.apply {
            titleNameTextView.text = singleSongInfo.songName
            filePathTextView.text = singleSongInfo.filePath
            musicTrackTextView.text = singleSongInfo.musicTrackNo.toString()
            musicChannelTextView.text = SmileAppBase.audioChannelMap[singleSongInfo.musicChannel]
            vocalTrackTextView.text = singleSongInfo.vocalTrackNo.toString()
            vocalChannelTextView.text = SmileAppBase.audioChannelMap[singleSongInfo.vocalChannel]
            inPlaylist = singleSongInfo.included == "1"
            includedPlaylistCheckBox.isChecked = holder.inPlaylist
            includedPlaylistCheckBox.setOnCheckedChangeListener {
                _: CompoundButton?, isChecked: Boolean ->
                includedPlaylistCheckBox.isChecked = isChecked
                includedPlaylistCheckBox.jumpDrawablesToCurrentState()
                val included = if (isChecked) "1" else "0"
                singleSongInfo.included = included
                songListSQLite.updateOneSongFromSongList(singleSongInfo)
            }

            itemView.setBackgroundColor(if (position % 2 == 0) yellow2Color
            else yellow3Color)

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

    override fun onViewAttachedToWindow(holder: MyViewHolder) {
        super.onViewAttachedToWindow(holder)
        val position = holder.absoluteAdapterPosition
        LogUtil.d(TAG, "onViewAttachedToWindow.position = $position")
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    // Return a unique ID for each item based on the file path or name
    override fun getItemId(position: Int): Long {
        return mList[position].hashCode().toLong()
    }

    fun myNotifyItemChanged(position:Int) {
        LogUtil.d(TAG, "myNotifyItemChanged.position = $position")
        positionUpdated = position
        isDataSetChanged = true
        notifyItemChanged(position)
    }
}