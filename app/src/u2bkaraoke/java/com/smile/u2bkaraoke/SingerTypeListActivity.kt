package com.smile.u2bkaraoke

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smile.karaoke.R
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.alertdialogfragment.AlertDialogFragment
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.u2bkaraoke.U2bKaraokeApp.Companion.appCompBuilder
import com.smile.u2bkaraoke.model.Constants
import com.smile.u2bkaraoke.model.SingerTypeList
import com.smile.u2bkaraoke.retrofit.RestApiAsync
import com.smile.u2bkaraoke.view_adapter.SingerTypeListAdapter
import retrofit2.Call
import retrofit2.Response
import javax.inject.Inject

class SingerTypeListActivity : AppCompatActivity() {


    companion object {
        private const val TAG = "SingerTyLstActivity"
    }

    private var textFontSize = 0f
    private var singerTypeListEmptyTextView: TextView? = null
    private var mRecyclerView: RecyclerView? = null

    @JvmField
    @Inject
    var myViewAdapter: SingerTypeListAdapter? = null
    private var singerTypeList: SingerTypeList? = null
    private var noResultString: String? = null
    private var failedMessage: String? = null
    private var loadingDialog: AlertDialogFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.d(TAG, "onCreate")

        noResultString = getString(R.string.noResultString)
        failedMessage = getString(R.string.failedMessage)
        val loadingString = getString(R.string.loadingString)

        textFontSize = ScreenUtil.getPxTextFontSizeNeeded(this)

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_singer_type_list)

        val singerTypesListMenuTextView = findViewById<TextView>(R.id.singerTypesListMenuTextView)
        ScreenUtil.resizeTextSize(singerTypesListMenuTextView, textFontSize)
        singerTypeListEmptyTextView = findViewById(R.id.singerTypeListEmptyTextView)
        ScreenUtil.resizeTextSize(singerTypeListEmptyTextView, textFontSize)
        singerTypeListEmptyTextView?.visibility = View.GONE
        mRecyclerView = findViewById(R.id.singerTypeListRecyclerView)
        val singerTypesListReturnButton = findViewById<Button>(R.id.singerTypesListReturnButton)
        ScreenUtil.resizeTextSize(singerTypesListReturnButton, textFontSize)
        singerTypesListReturnButton.setOnClickListener { returnToPrevious() }
        loadingDialog = AlertDialogFragment.newInstance(
            loadingString,
            Constants.FontSize_Scale_Type,
            textFontSize, Color.RED, 0, 0, true
        )

        loadingDialog?.show(supportFragmentManager, "LoadingDialogTag")
        MyRestApi().getAllSingerTypes()

        onBackPressedDispatcher.addCallback(
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    returnToPrevious()
                }
            })
    }

    private fun returnToPrevious() {
        LogUtil.d(TAG, "returnToPrevious")
        finish()
    }

    private inner class MyRestApi : RestApiAsync<SingerTypeList>() {
        @SuppressLint("SetTextI18n")
        override fun onResponse(call: Call<SingerTypeList?>, response: Response<SingerTypeList?>) {
            loadingDialog?.dismissAllowingStateLoss()
            LogUtil.d(TAG, "MyRestApi.onResponse.response.isSuccessful = ${response.isSuccessful}")
            if (response.isSuccessful) {
                singerTypeList = response.body()
                singerTypeList?.let {
                    if (it.singerTypes.isEmpty()) {
                        singerTypeListEmptyTextView?.text = noResultString
                        singerTypeListEmptyTextView?.visibility = View.VISIBLE
                    } else {
                        singerTypeListEmptyTextView?.visibility = View.GONE
                    }
                } ?: { singerTypeList = SingerTypeList() }
            } else {
                singerTypeList = SingerTypeList()
                singerTypeListEmptyTextView?.text = "MyRestApi.response.isSuccessful = false."
                singerTypeListEmptyTextView?.visibility = View.VISIBLE
            }
            LogUtil.d(TAG, "MyRestApi.onResponse.inject()")
            appCompBuilder
                .activityModule(this@SingerTypeListActivity)
                .singerTypeArrayListModule(singerTypeList!!.singerTypes)
                .floatModule(textFontSize).build()
                .inject(this@SingerTypeListActivity)
            mRecyclerView?.setAdapter(myViewAdapter)
            mRecyclerView?.setLayoutManager(LinearLayoutManager(applicationContext))
        }

        override fun onFailure(call: Call<SingerTypeList>, t: Throwable) {
            LogUtil.e(TAG, "MyRestApi.onFailure.", t)
            loadingDialog?.dismissAllowingStateLoss()
            singerTypeList = SingerTypeList()
            singerTypeListEmptyTextView?.text = failedMessage
            singerTypeListEmptyTextView?.visibility = View.VISIBLE
        }
    }
}
