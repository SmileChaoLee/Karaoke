package com.smile.u2bkaraoke

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
import com.smile.u2bkaraoke.model.LanguageList
import com.smile.u2bkaraoke.retrofit.RestApiAsync
import com.smile.u2bkaraoke.view_adapter.LanguageListAdapter
import retrofit2.Call
import retrofit2.Response
import javax.inject.Inject

class LanguageListActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LanguageListActivity"
    }

    private var textFontSize = 0f
    private var languagesListEmptyTextView: TextView? = null
    private var mRecyclerView: RecyclerView? = null

    @JvmField
    @Inject
    var myViewAdapter: LanguageListAdapter? = null
    private var languageList: LanguageList? = null
    private var noResultString: String? = null
    private var failedMessage: String? = null
    private var loadingDialog: AlertDialogFragment? = null
    private var orderedFrom = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.d(TAG, "onCreate")
        noResultString = getString(R.string.noResultString)
        failedMessage = getString(R.string.failedMessage)
        val loadingString = getString(R.string.loadingString)

        textFontSize = ScreenUtil.getPxTextFontSizeNeeded(this)

        orderedFrom = Constants.WordsOrdered
        val extras = intent.extras
        if (extras != null) {
            orderedFrom = extras.getInt(Constants.OrderedFrom, Constants.WordsOrdered)
        }

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_language_list)

        val menuTextView = findViewById<TextView>(R.id.languagesListMenuTextView)
        ScreenUtil.resizeTextSize(menuTextView, textFontSize)
        when (orderedFrom) {
            Constants.WordsOrdered ->  // from main activity (U2bKkActivity)
                menuTextView.text = getString(R.string.languagesListString)
            Constants.NewSongOrdered -> menuTextView.text = getString(R.string.newSOngLanguagesListString)
            Constants.HotSongOrdered -> menuTextView.text = getString(R.string.hotSongLanguagesListString)
        }

        languagesListEmptyTextView = findViewById<TextView>(R.id.languagesListEmptyTextView)
        ScreenUtil.resizeTextSize(languagesListEmptyTextView, textFontSize)
        languagesListEmptyTextView?.visibility = View.GONE

        mRecyclerView = findViewById<RecyclerView>(R.id.languageListRecyclerView)

        val languagesListReturnButton = findViewById<Button>(R.id.languagesListReturnButton)
        ScreenUtil.resizeTextSize(languagesListReturnButton, textFontSize)
        languagesListReturnButton.setOnClickListener { returnToPrevious() }

        loadingDialog = AlertDialogFragment.newInstance(
            loadingString,
            Constants.FontSize_Scale_Type,
            textFontSize, Color.RED, 0, 0, true
        )
        loadingDialog?.show(supportFragmentManager, "LoadingDialogTag")
        MyRestApi().getAllLanguages()

        onBackPressedDispatcher.addCallback(
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    LogUtil.d(TAG, "onBackPressedDispatcher.handleOnBackPressed")
                    returnToPrevious()
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun returnToPrevious() {
        finish()
    }

    private inner class MyRestApi : RestApiAsync<LanguageList>() {
        override fun onResponse(call: Call<LanguageList?>, response: Response<LanguageList?>) {
            LogUtil.d(TAG, "MyRestApi.onResponse")
            loadingDialog?.dismissAllowingStateLoss()
            LogUtil.d(TAG, "MyRestApi.onResponse.response.isSuccessful() = ${response.isSuccessful}")
            if (response.isSuccessful) {
                languageList = response.body()
                languageList?.let {
                    if (it.languages.isEmpty()) {
                        languagesListEmptyTextView?.text = noResultString
                        languagesListEmptyTextView?.visibility = View.VISIBLE
                    } else {
                        languagesListEmptyTextView?.visibility = View.GONE
                    }
                } ?: run { languageList = LanguageList() }
            } else {
                languageList = LanguageList()
                languagesListEmptyTextView?.text = failedMessage
                languagesListEmptyTextView?.visibility = View.VISIBLE
            }
            LogUtil.d(TAG, "MyRestApi.onResponse.inject()")
            appCompBuilder
                .activityModule(this@LanguageListActivity)
                .languageArrayListModule(languageList!!.languages)
                .intModule(orderedFrom)
                .floatModule(textFontSize).build()
                .inject(this@LanguageListActivity)
            mRecyclerView?.setAdapter(myViewAdapter)
            mRecyclerView?.setLayoutManager(LinearLayoutManager(applicationContext))
        }

        override fun onFailure(call: Call<LanguageList?>, t: Throwable) {
            LogUtil.e(TAG, "MyRestApi.onFailure.", t)
            loadingDialog?.dismissAllowingStateLoss()
            languageList = LanguageList()
            languagesListEmptyTextView?.text = failedMessage
            languagesListEmptyTextView?.visibility = View.VISIBLE
        }
    }
}
