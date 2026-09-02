package com.smile.u2bkaraoke.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smile.karaoke.R
import com.smile.karaoke.interfaces.RecyclerItemListener
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.u2bkaraoke.U2bKaraokeApp.Companion.appCompBuilder
import com.smile.u2bkaraoke.retrofit.U2bKkRestApiAsync
import com.smile.u2bkaraoke.adapters.SingerAreaListAdapter
import com.smile.u2bkaraoke.model.SingerArea
import com.smile.u2bkaraoke.model.SingerAreaList
import com.smile.u2bkaraoke.model.SingerType
import com.smile.u2bkaraoke.u2bkaok_constants.U2bKKConstants
import com.smile.u2bkaraoke.retrofit.U2bKkRestApiSync
import com.smile.u2bkaraoke.utilities.U2bKaOkUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Response
import javax.inject.Inject

class SingerAreaListFragment : U2bKKBaseFragment(), RecyclerItemListener {

    companion object {
        private const val TAG = "SingAreaLstFragment"
    }

    @Inject
    lateinit var myViewAdapter: SingerAreaListAdapter
    private var mRecyclerView: RecyclerView? = null
    private var singerAreaListEmptyTextView: TextView? = null
    private var singerAreaList: SingerAreaList? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        LogUtil.i(TAG, "onCreateView")
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_singer_area_list,
            container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        LogUtil.d(TAG, "onViewCreated")
        textFontSize = ScreenUtil.getPxTextFontSizeNeeded(activity)

        val act = activity ?: return
        view.apply {
            val singerAreasListMenuTextView = findViewById<TextView>(R.id.singerAreasListMenuTextView)
            ScreenUtil.resizeTextSize(singerAreasListMenuTextView, textFontSize)
            singerAreaListEmptyTextView = findViewById(R.id.singerAreaListEmptyTextView)
            ScreenUtil.resizeTextSize(singerAreaListEmptyTextView, textFontSize)
            singerAreaListEmptyTextView?.visibility = View.GONE
            mRecyclerView = findViewById(R.id.singerAreaListRecyclerView)
        }

        super.onViewCreated(view, savedInstanceState)
        exitImageButton?.nextFocusUpId = R.id.singerAreaListRecyclerView
        showVideoButton?.nextFocusUpId = R.id.singerAreaListRecyclerView

        // MyRestApi().getAllSingerAreas()
        singerAreaList = SingerAreaList()
        singerAreaList?.let {
            it.singerAreas.clear()
            it.singerAreas.add(SingerArea().apply {
                id = 0
                areaNo = U2bKKConstants.ALL_SINGERS_AREA_NO
                areaNa = act.getString(R.string.allSingersString)
                areaEn = "All Singers"
            })
        }
        act.lifecycleScope.launch(Dispatchers.Main) {
            mRecyclerView?.visibility = View.GONE
            singerAreaListEmptyTextView?.visibility = View.VISIBLE
            singerAreaListEmptyTextView?.text = act.getString(R.string.loadingString)
            withContext(Dispatchers.IO) {
                val saList = U2bKkRestApiSync.getApiSync().getAllSingerAreas()
                saList?.let { aList ->
                    singerAreaList?.singerAreas?.addAll(aList.singerAreas)
                }
            }
            // update the UI
            withContext(Dispatchers.Main) {
                singerAreaList?.let {
                    if (it.singerAreas.isEmpty()) {
                        singerAreaListEmptyTextView?.text = act.getString(R.string.noResultString)
                        singerAreaListEmptyTextView?.visibility = View.VISIBLE
                    } else {
                        singerAreaListEmptyTextView?.visibility = View.GONE
                    }
                } ?: run {
                    singerAreaList = SingerAreaList()
                    singerAreaListEmptyTextView?.text = act.getString(R.string.failedMessage)
                    singerAreaListEmptyTextView?.visibility = View.VISIBLE
                }
                LogUtil.d(TAG, "inject().myViewAdapter")
                appCompBuilder
                    .recyclerItemListenerModule(this@SingerAreaListFragment)
                    .singerAreaArrayListModule(singerAreaList!!.singerAreas)
                    .floatModule(textFontSize).build()
                    .inject(this@SingerAreaListFragment)
                mRecyclerView?.setAdapter(myViewAdapter)
                mRecyclerView?.setLayoutManager(LinearLayoutManager(act.applicationContext))
                updateRecyclerView()
            }
        }
    }

    private fun updateRecyclerView() {
        singerAreaListEmptyTextView?.visibility = View.GONE
        singerAreaList?.let {
            if (it.singerAreas.isEmpty()) {
                mRecyclerView?.visibility = View.GONE
                exitImageButton?.post { exitImageButton?.requestFocus() }
            } else {
                mRecyclerView?.visibility = View.VISIBLE
                mRecyclerView?.post { mRecyclerView?.requestFocus() }
            }
        }
    }

    override fun onItemClick(v: View?, position: Int) {
        LogUtil.i(TAG, "onItemClick.position = $position")
        if (position < 0) return
        val act = activity ?: return
        val fragContainerId = this.id   // container id of the fragment
        val fragManager = act.supportFragmentManager
        singerAreaList?.let { list ->
            val singerArea = list.singerAreas[position]
            val sType = SingerType().apply {
                id = singerArea.id
                areaNo = singerArea.areaNo
                areaNa = singerArea.areaNa
                areaEn = singerArea.areaEn
                sex = "0"
            }
            LogUtil.i(TAG, "onItemClick.sType.areaNa = ${sType.areaNa}")
            ScreenUtil.showToast(act, sType.areaNa,
                textFontSize, Toast.LENGTH_SHORT)
            val nFragment = SingerListFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(U2bKKConstants.SingerTypeParcelable, sType)
                }
            }
            U2bKaOkUtil.beginTransaction(fragManager, fragContainerId, nFragment)
        }
    }

    override fun nextFocusUpId(v: View) {
        selectTab?.view?.nextFocusDownId = R.id.singerAreaListRecyclerView
        favoriteTab?.view?.nextFocusDownId = R.id.singerAreaListRecyclerView
        // The following line does not work
        v.nextFocusUpId = selectTab?.view?.id ?: R.id.singerAreaListRecyclerView
    }

    private inner class MyRestApi : U2bKkRestApiAsync<SingerAreaList>() {
        @SuppressLint("SetTextI18n")
        override fun onResponse(call: Call<SingerAreaList?>, response: Response<SingerAreaList?>) {
            LogUtil.d(TAG, "MyRestApi.onResponse.response.isSuccessful = ${response.isSuccessful}")
            val act = activity ?: return
            if (response.isSuccessful) {
                singerAreaList = response.body()
                singerAreaList?.let {
                    if (it.singerAreas.isEmpty()) {
                        singerAreaListEmptyTextView?.text = act.getString(R.string.noResultString)
                        singerAreaListEmptyTextView?.visibility = View.VISIBLE
                    } else {
                        singerAreaListEmptyTextView?.visibility = View.GONE
                    }
                } ?: { singerAreaList = SingerAreaList() }
            } else {
                singerAreaList = SingerAreaList()
                singerAreaListEmptyTextView?.text = act.getString(R.string.failedMessage)
                singerAreaListEmptyTextView?.visibility = View.VISIBLE
            }
            LogUtil.d(TAG, "MyRestApi.onResponse.inject().myViewAdapter")
            appCompBuilder
                .recyclerItemListenerModule(this@SingerAreaListFragment)
                .singerAreaArrayListModule(singerAreaList!!.singerAreas)
                .floatModule(textFontSize).build()
                .inject(this@SingerAreaListFragment)
            mRecyclerView?.setAdapter(myViewAdapter)
            mRecyclerView?.setLayoutManager(LinearLayoutManager(act.applicationContext))
        }

        override fun onFailure(call: Call<SingerAreaList>, t: Throwable) {
            LogUtil.e(TAG, "MyRestApi.onFailure.", t)
            singerAreaList = SingerAreaList()
            singerAreaListEmptyTextView?.text = activity?.getString(R.string.failedMessage)
            singerAreaListEmptyTextView?.visibility = View.VISIBLE
        }
    }
}
