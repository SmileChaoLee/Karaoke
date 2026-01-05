package com.smile.androidsong.view_adapter

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.smile.androidsong.R
import com.smile.androidsong.SongListActivity
import com.smile.androidsong.model.Constants
import com.smile.androidsong.model.Singer
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.androidsong.view_adapter.SingerListAdapter.MyViewHolder

class SingerListAdapter(
    private val mActivity: Activity,
    private val mSingers: ArrayList<Singer>,
    private val mTextFontSize: Float
) : RecyclerView.Adapter<MyViewHolder>() {
    inner class MyViewHolder(itemView: View) : ViewHolder(itemView) {
        val positionNoTextView: TextView
        val singerNoTextView: TextView
        val singerNaTextView: TextView

        init {
            positionNoTextView =
                itemView.findViewById(R.id.singerItem_Layout_positionNoTextView)
            ScreenUtil.resizeTextSize(
                positionNoTextView,
                mTextFontSize,
                Constants.FontSize_Scale_Type
            )
            singerNoTextView = itemView.findViewById(R.id.singerNoTextView)
            ScreenUtil.resizeTextSize(
                singerNoTextView,
                mTextFontSize,
                Constants.FontSize_Scale_Type
            )
            singerNaTextView = itemView.findViewById(R.id.singerNaTextView)
            ScreenUtil.resizeTextSize(
                singerNaTextView,
                mTextFontSize,
                Constants.FontSize_Scale_Type
            )
        }
    }

    // Usually involves inflating a layout from XML and returning the holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        // inflate the singer item view
        return MyViewHolder(layoutInflater.inflate(R.layout.singer_list_item, parent, false))
    }

    // Involves populating data into the item through holder
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val singer = mSingers[position]
        holder.apply {
            positionNoTextView.text = position.toString()
            singerNoTextView.text = singer.singNo
            singerNaTextView.text = singer.singNa
            itemView.setOnClickListener {
                Log.d(TAG, "itemView.setOnClickListener.${singer.singNa}")
                ScreenUtil.showToast(
                    mActivity, singer.singNa,
                    mTextFontSize, Constants.FontSize_Scale_Type, Toast.LENGTH_SHORT
                )
                Intent(mActivity, SongListActivity::class.java).let {
                    it.putExtra(Constants.OrderedFrom, Constants.SingerOrdered)
                    it.putExtra(Constants.SongListActivityTitle, singer.singNa)
                    it.putExtra(Constants.SingerParcelable, singer)
                    mActivity.startActivity(it)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return mSingers.size
    }

    companion object {
        private const val TAG = "SingerAdapter"
    }
}
