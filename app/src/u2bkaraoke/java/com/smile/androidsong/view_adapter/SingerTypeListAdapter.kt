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
import com.smile.androidsong.SingerListActivity
import com.smile.androidsong.model.Constants
import com.smile.androidsong.model.SingerType
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.androidsong.view_adapter.SingerTypeListAdapter.MyViewHolder

class SingerTypeListAdapter(
    private val mActivity: Activity,
    private val mSingerTypes: ArrayList<SingerType>,
    private val mTextFontSize: Float
) : RecyclerView.Adapter<MyViewHolder>() {
    inner class MyViewHolder(itemView: View) : ViewHolder(itemView) {
        val positionNoTextView: TextView
        val singerAreaNaTextView: TextView
        val singerSexTextView: TextView

        init {
            positionNoTextView =
                itemView.findViewById(R.id.singerTypeItem_Layout_positionNoTextView)
            ScreenUtil.resizeTextSize(
                positionNoTextView,
                mTextFontSize,
                Constants.FontSize_Scale_Type
            )
            singerAreaNaTextView = itemView.findViewById(R.id.singerAreaNaTextView)
            ScreenUtil.resizeTextSize(
                singerAreaNaTextView,
                mTextFontSize,
                Constants.FontSize_Scale_Type
            )
            singerSexTextView = itemView.findViewById(R.id.singerSexTextView)
            ScreenUtil.resizeTextSize(
                singerSexTextView,
                mTextFontSize,
                Constants.FontSize_Scale_Type
            )
        }
    }

    // Usually involves inflating a layout from XML and returning the holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        // inflate the singerType item view
        return MyViewHolder(layoutInflater.inflate(R.layout.singer_type_list_item, parent, false))
    }

    // Involves populating data into the item through holder
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val singerType = mSingerTypes[position]
        holder.apply {
            positionNoTextView.text = position.toString()
            singerAreaNaTextView.text = singerType.areaNa
            singerSexTextView.text = when (singerType.sex) {
                "1" -> "Male"
                "2" -> "Female"
                else ->                 // "0"
                    ""
            }
            itemView.setOnClickListener {
                Log.d(TAG, "itemView.setOnClickListener.${singerType.areaNa}")
                ScreenUtil.showToast(
                    mActivity, singerType.areaNa,
                    mTextFontSize, Constants.FontSize_Scale_Type, Toast.LENGTH_SHORT
                )
                Intent(mActivity, SingerListActivity::class.java).let {
                    it.putExtra(Constants.SingerListActivityTitle, singerType.areaNa)
                    it.putExtra(Constants.SingerTypeParcelable, singerType)
                    mActivity.startActivity(it)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return mSingerTypes.size
    }

    companion object {
        private const val TAG = "SingerTypeAdapter"
    }
}
