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
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil
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

class SingerTyListFragment : U2bKKBaseFragment(),
    SingerTypeListAdapter.SingerTypeItemListener {

    companion object {
        private const val TAG = "SingerTyListFragment"
    }

    @Inject
    lateinit var myViewAdapter: SingerTypeListAdapter
    private var mRecyclerView: RecyclerView? = null
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
        val view = inflater.inflate(R.layout.fragment_singer_type_list,
            container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        LogUtil.d(TAG, "onViewCreated")
        textFontSize = ScreenUtil.getPxTextFontSizeNeeded(activity)

        val act = activity ?: return
        view.apply {
            val singerTypesListMenuTextView = findViewById<TextView>(R.id.singerTypesListMenuTextView)
            ScreenUtil.resizeTextSize(singerTypesListMenuTextView, textFontSize)
            singerTypeListEmptyTextView = findViewById(R.id.singerTypeListEmptyTextView)
            ScreenUtil.resizeTextSize(singerTypeListEmptyTextView, textFontSize)
            singerTypeListEmptyTextView?.visibility = View.GONE
            mRecyclerView = findViewById(R.id.singerTypeListRecyclerView)
        }

        super.onViewCreated(view, savedInstanceState)
        exitImageButton?.nextFocusUpId = R.id.singerTypeListRecyclerView
        showVideoButton?.nextFocusUpId = R.id.singerTypeListRecyclerView

        // MyRestApi().getAllSingerTypes()
        act.lifecycleScope.launch(Dispatchers.Main) {
            mRecyclerView?.visibility = View.GONE
            singerTypeListEmptyTextView?.visibility = View.VISIBLE
            singerTypeListEmptyTextView?.text = act.getString(R.string.loadingString)
            withContext(Dispatchers.IO) {
                singerTypeList = RestApiSync.getApiSync().getAllSingerTypes()
            }
            // update the UI
            withContext(Dispatchers.Main) {
                singerTypeList?.let {
                    if (it.singerTypes.isEmpty()) {
                        singerTypeListEmptyTextView?.text = act.getString(R.string.noResultString)
                        singerTypeListEmptyTextView?.visibility = View.VISIBLE
                    } else {
                        singerTypeListEmptyTextView?.visibility = View.GONE
                    }
                } ?: run {
                    singerTypeList = SingerTypeList()
                    singerTypeListEmptyTextView?.text = act.getString(R.string.failedMessage)
                    singerTypeListEmptyTextView?.visibility = View.VISIBLE
                }
                LogUtil.d(TAG, "inject().myViewAdapter")
                appCompBuilder
                    .singerTypeItemListenerModule(this@SingerTyListFragment)
                    .singerTypeArrayListModule(singerTypeList!!.singerTypes)
                    .floatModule(textFontSize).build()
                    .inject(this@SingerTyListFragment)
                mRecyclerView?.setAdapter(myViewAdapter)
                mRecyclerView?.setLayoutManager(LinearLayoutManager(act.applicationContext))
                updateRecyclerView()
            }
        }
    }

    private fun updateRecyclerView() {
        singerTypeListEmptyTextView?.visibility = View.GONE
        singerTypeList?.let {
            if (it.singerTypes.isEmpty()) {
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
        singerTypeList?.let { list ->
            val singerType = list.singerTypes[position]
            LogUtil.i(TAG, "onItemClick.singerType.areaNa = ${singerType.areaNa}")
            ScreenUtil.showToast(act, singerType.areaNa,
                textFontSize,  Toast.LENGTH_SHORT)
            /*
            Intent(act, SingerListActivity::class.java).let { int ->
                int.putExtra(Constants.SingerListActivityTitle, singerType.areaNa)
                int.putExtra(Constants.SingerTypeParcelable, singerType)
                act.startActivity(int)
            }
            */
            val nFragment = SingerListFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(Constants.SingerTypeParcelable, singerType)
                }
            }
            U2bKaOkUtil.beginTransaction(fragManager, fragContainerId, nFragment)
        }
    }

    override fun getSexString(sex: String): String {
        LogUtil.d(TAG, "getSexString.sex = $sex")
        return when (sex) {
            "1" -> activity?.getString(R.string.male) ?: ""
            "2" -> activity?.getString(R.string.female) ?: ""
            else ->                 // "0"
                ""
        }
    }

    private inner class MyRestApi : RestApiAsync<SingerTypeList>() {
        @SuppressLint("SetTextI18n")
        override fun onResponse(call: Call<SingerTypeList?>, response: Response<SingerTypeList?>) {
            LogUtil.d(TAG, "MyRestApi.onResponse.response.isSuccessful = ${response.isSuccessful}")
            val act = activity ?: return
            if (response.isSuccessful) {
                singerTypeList = response.body()
                singerTypeList?.let {
                    if (it.singerTypes.isEmpty()) {
                        singerTypeListEmptyTextView?.text = act.getString(R.string.noResultString)
                        singerTypeListEmptyTextView?.visibility = View.VISIBLE
                    } else {
                        singerTypeListEmptyTextView?.visibility = View.GONE
                    }
                } ?: { singerTypeList = SingerTypeList() }
            } else {
                singerTypeList = SingerTypeList()
                singerTypeListEmptyTextView?.text = act.getString(R.string.failedMessage)
                singerTypeListEmptyTextView?.visibility = View.VISIBLE
            }
            LogUtil.d(TAG, "MyRestApi.onResponse.inject().myViewAdapter")
            appCompBuilder
                .singerTypeItemListenerModule(this@SingerTyListFragment)
                .singerTypeArrayListModule(singerTypeList!!.singerTypes)
                .floatModule(textFontSize).build()
                .inject(this@SingerTyListFragment)
            mRecyclerView?.setAdapter(myViewAdapter)
            mRecyclerView?.setLayoutManager(LinearLayoutManager(act.applicationContext))
        }

        override fun onFailure(call: Call<SingerTypeList>, t: Throwable) {
            LogUtil.e(TAG, "MyRestApi.onFailure.", t)
            singerTypeList = SingerTypeList()
            singerTypeListEmptyTextView?.text = activity?.getString(R.string.failedMessage)
            singerTypeListEmptyTextView?.visibility = View.VISIBLE
        }
    }
}
