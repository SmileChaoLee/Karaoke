package com.smile.u2bkaraoke.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smile.karaoke.R
import com.smile.karaoke.interfaces.RecyclerItemListener
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.u2bkaraoke.SingerListActivity
import com.smile.u2bkaraoke.U2bKaraokeApp.Companion.appCompBuilder
import com.smile.u2bkaraoke.model.SingerTypeList
import com.smile.u2bkaraoke.retrofit.RestApiAsync
import com.smile.u2bkaraoke.adapters.SingerTypeListAdapter
import com.smile.u2bkaraoke.model.Constants
import com.smile.u2bkaraoke.retrofit.RestApiSync
import com.smile.u2bkaraoke.utilities.U2bKaOkUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Response
import javax.inject.Inject

class SingerTyListFragment : Fragment(), RecyclerItemListener {


    companion object {
        private const val TAG = "SingerTyListFragment"
    }

    @Inject
    lateinit var myViewAdapter: SingerTypeListAdapter
    private var mRecyclerView: RecyclerView? = null
    private var textFontSize = 0f
    private var singerTypeListEmptyTextView: TextView? = null
    private var singerTypeList: SingerTypeList? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        LogUtil.i(TAG, "onCreateView")
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.activity_singer_type_list,
            container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        LogUtil.d(TAG, "onCreate")
        textFontSize = ScreenUtil.getPxTextFontSizeNeeded(activity)
        super.onViewCreated(view, savedInstanceState)

        view.apply {
            val singerTypesListMenuTextView = findViewById<TextView>(R.id.singerTypesListMenuTextView)
            ScreenUtil.resizeTextSize(singerTypesListMenuTextView, textFontSize)
            singerTypeListEmptyTextView = findViewById(R.id.singerTypeListEmptyTextView)
            ScreenUtil.resizeTextSize(singerTypeListEmptyTextView, textFontSize)
            singerTypeListEmptyTextView?.visibility = View.GONE
            mRecyclerView = findViewById(R.id.singerTypeListRecyclerView)
            val singerTypesListReturnButton = findViewById<Button>(R.id.singerTypesListReturnButton)
            ScreenUtil.resizeTextSize(singerTypesListReturnButton, textFontSize)
            singerTypesListReturnButton.setOnClickListener { U2bKaOkUtil.returnToPrevious(activity) }
        }

        // MyRestApi().getAllSingerTypes()
        val act = activity ?: return
        act.lifecycleScope.launch(Dispatchers.IO) {
            singerTypeList = RestApiSync.getApiSync().getAllSingerTypes()
            // update the UI
            withContext(Dispatchers.Main) {
                singerTypeList?.let {
                    if (it.singerTypes.isEmpty()) {
                        singerTypeListEmptyTextView?.text = getString(R.string.noResultString)
                        singerTypeListEmptyTextView?.visibility = View.VISIBLE
                    } else {
                        singerTypeListEmptyTextView?.visibility = View.GONE
                    }
                } ?: run {
                    singerTypeList = SingerTypeList()
                    singerTypeListEmptyTextView?.text = getString(R.string.failedMessage)
                    singerTypeListEmptyTextView?.visibility = View.VISIBLE
                }
                LogUtil.d(TAG, "inject().myViewAdapter")
                appCompBuilder
                    .recyclerItemListenerModule(this@SingerTyListFragment)
                    .singerTypeArrayListModule(singerTypeList!!.singerTypes)
                    .floatModule(textFontSize).build()
                    .inject(this@SingerTyListFragment)
                mRecyclerView?.setAdapter(myViewAdapter)
                mRecyclerView?.setLayoutManager(LinearLayoutManager(act.applicationContext))
            }
        }
    }

    override fun onItemClick(v: View?, position: Int) {
        LogUtil.i(TAG, "onItemClick.position = $position")
        if (position < 0) return
        val act = activity ?: return
        singerTypeList?.let { list ->
            val singerType = list.singerTypes[position]
            ScreenUtil.showToast(
                act, singerType.areaNa,
                textFontSize,  Toast.LENGTH_SHORT
            )
            Intent(act, SingerListActivity::class.java).let { int ->
                int.putExtra(Constants.SingerListActivityTitle, singerType.areaNa)
                int.putExtra(Constants.SingerTypeParcelable, singerType)
                act.startActivity(int)
            }
        }
    }

    private inner class MyRestApi : RestApiAsync<SingerTypeList>() {
        @SuppressLint("SetTextI18n")
        override fun onResponse(call: Call<SingerTypeList?>, response: Response<SingerTypeList?>) {
            LogUtil.d(TAG, "MyRestApi.onResponse.response.isSuccessful = ${response.isSuccessful}")
            if (response.isSuccessful) {
                singerTypeList = response.body()
                singerTypeList?.let {
                    if (it.singerTypes.isEmpty()) {
                        singerTypeListEmptyTextView?.text = getString(R.string.noResultString)
                        singerTypeListEmptyTextView?.visibility = View.VISIBLE
                    } else {
                        singerTypeListEmptyTextView?.visibility = View.GONE
                    }
                } ?: { singerTypeList = SingerTypeList() }
            } else {
                singerTypeList = SingerTypeList()
                singerTypeListEmptyTextView?.text = getString(R.string.failedMessage)
                singerTypeListEmptyTextView?.visibility = View.VISIBLE
            }
            val act = activity ?: return
            LogUtil.d(TAG, "MyRestApi.onResponse.inject().myViewAdapter")
            appCompBuilder
                .recyclerItemListenerModule(this@SingerTyListFragment)
                .singerTypeArrayListModule(singerTypeList!!.singerTypes)
                .floatModule(textFontSize).build()
                .inject(this@SingerTyListFragment)
            mRecyclerView?.setAdapter(myViewAdapter)
            mRecyclerView?.setLayoutManager(LinearLayoutManager(act.applicationContext))
        }

        override fun onFailure(call: Call<SingerTypeList>, t: Throwable) {
            LogUtil.e(TAG, "MyRestApi.onFailure.", t)
            singerTypeList = SingerTypeList()
            singerTypeListEmptyTextView?.text = getString(R.string.failedMessage)
            singerTypeListEmptyTextView?.visibility = View.VISIBLE
        }
    }
}
