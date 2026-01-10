package com.smile.u2bkaraoke

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
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
import com.smile.u2bkaraoke.model.SingerList
import com.smile.u2bkaraoke.model.SingerType
import com.smile.u2bkaraoke.retrofit.RestApiAsync
import com.smile.u2bkaraoke.adapters.SingerListAdapter
import retrofit2.Call
import retrofit2.Response
import javax.inject.Inject

class SingerListActivity : AppCompatActivity() {


    companion object {
        private const val TAG = "SingerListActivity"
    }

    private var textFontSize = 0f
    private var searchEditText: EditText? = null
    private var isSearchEditTextChanged = false
    private var filterString: String? = null
    private var singerListEmptyTextView: TextView? = null
    private var mRecyclerView: RecyclerView? = null

    @JvmField
    @Inject
    var myViewAdapter: SingerListAdapter? = null
    private var singerList: SingerList? = null
    private var singerType: SingerType? = null

    private var pageNo = 1
    private var pageSize = 10
    private var totalPages = 0
    private var noResultString: String? = null
    private var failedMessage: String? = null
    private var loadingString: String? = null
    private var loadingDialog: AlertDialogFragment? = null

    private var restApi: MyRestApi? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.d(TAG, "onCreate")

        noResultString = getString(R.string.noResultString)
        failedMessage = getString(R.string.failedMessage)
        loadingString = getString(R.string.loadingString)
        textFontSize = ScreenUtil.getPxTextFontSizeNeeded(this)

        val singerListTitle = getString(R.string.singersListString)
        var activityTitle = ""
        val extras = intent.extras
        if (extras != null) {
            activityTitle = extras.getString(Constants.SingerListActivityTitle, "")
            singerType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                extras.getParcelable(Constants.SingerTypeParcelable, SingerType::class.java)
            } else extras.getParcelable(Constants.SingerTypeParcelable)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_singer_list)

        val singersListMenuTextView = findViewById<TextView>(R.id.singersListMenuTextView)
        ScreenUtil.resizeTextSize(singersListMenuTextView, textFontSize)
        singersListMenuTextView.text = "$activityTitle $singerListTitle"
        filterString = ""
        searchEditText = findViewById(R.id.singerSearchEditText)
        searchEditText?.let { sEt ->
            ScreenUtil.resizeTextSize(sEt, textFontSize)
            val searchEditLp = sEt.layoutParams as LinearLayout.LayoutParams
            searchEditLp.leftMargin = (textFontSize * 2.0f).toInt()
            searchEditLp.rightMargin = (textFontSize * 5.0f).toInt()
            sEt.setText(filterString)
            isSearchEditTextChanged = false
            sEt.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {}
                override fun onTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {}
                override fun afterTextChanged(editable: Editable) {
                    LogUtil.d(TAG, "addTextChangedListener.afterTextChanged")
                    val content = editable.toString().trim()
                    filterString = if (content.isEmpty()) "" else "SingNa+$content"
                    LogUtil.d(TAG, "addTextChangedListener.afterTextChanged.filterString = $filterString")
                    pageNo = 1
                    isSearchEditTextChanged = true
                    retrieveSingerList()
                }
            })
        }
        singerListEmptyTextView = findViewById(R.id.singerListEmptyTextView)
        ScreenUtil.resizeTextSize(singerListEmptyTextView, textFontSize)
        singerListEmptyTextView?.visibility = View.GONE
        mRecyclerView = findViewById(R.id.singerListRecyclerView)
        val smallButtonFontSize = textFontSize * 0.7f
        val firstPageButton = findViewById<Button>(R.id.firstPageButton)
        ScreenUtil.resizeTextSize(firstPageButton, smallButtonFontSize)
        firstPageButton.setOnClickListener { firstPage() }
        val previousPageButton = findViewById<Button>(R.id.previousPageButton)
        ScreenUtil.resizeTextSize(previousPageButton, smallButtonFontSize)
        previousPageButton.setOnClickListener { previousPage() }
        val nextPageButton = findViewById<Button>(R.id.nextPageButton)
        ScreenUtil.resizeTextSize(nextPageButton, smallButtonFontSize)
        nextPageButton.setOnClickListener { nextPage() }
        val lastPageButton = findViewById<Button>(R.id.lastPageButton)
        ScreenUtil.resizeTextSize(lastPageButton, smallButtonFontSize)
        lastPageButton.setOnClickListener { lastPage() }
        val singersListReturnButton = findViewById<Button>(R.id.singersListReturnButton)
        ScreenUtil.resizeTextSize(singersListReturnButton, textFontSize)
        singersListReturnButton.setOnClickListener { returnToPrevious() }

        restApi = MyRestApi()
        retrieveSingerList()

        onBackPressedDispatcher.addCallback(
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    returnToPrevious()
                }
            })
    }

    private fun retrieveSingerList() {
        LogUtil.d(TAG, "retrieveSingerList.filterString = $filterString")
        if (loadingDialog == null) {
            loadingDialog = AlertDialogFragment.newInstance(
                loadingString,
                Constants.FontSize_Scale_Type,
                textFontSize, Color.RED, 0, 0, true
            )
            loadingDialog!!.show(supportFragmentManager, "LoadingDialogTag")
        }
        restApi?.let {rApi ->
            val sType = singerType ?: SingerType()
            if (filterString.isNullOrEmpty()) {
                rApi.getSingersBySingerType(sType, pageSize, pageNo)
            } else {
                rApi.getSingersBySingerType(sType, pageSize, pageNo, filterString!!)
            }
            singerType = sType
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
    }

    private fun returnToPrevious() {
        LogUtil.d(TAG, "returnToPrevious")
        finish()
    }

    private fun firstPage() {
        pageNo = 1
        retrieveSingerList()
    }

    private fun previousPage() {
        pageNo--
        if (pageNo < 1) {
            pageNo = 1
        }
        retrieveSingerList()
    }

    private fun nextPage() {
        pageNo++
        if (pageNo > totalPages) {
            pageNo = totalPages
        }
        retrieveSingerList()
    }

    private fun lastPage() {
        pageNo = -1 // represent last page
        retrieveSingerList()
    }

    private inner class MyRestApi : RestApiAsync<SingerList>() {
        override fun onResponse(call: Call<SingerList?>, response: Response<SingerList?>) {
            if (loadingDialog != null) loadingDialog!!.dismissAllowingStateLoss()
            loadingDialog = null
            LogUtil.d(TAG, "MyRestApi.onResponse.response.isSuccessful = ${response.isSuccessful}")
            singerList = response.body()
            if (!response.isSuccessful || singerList == null) {
                singerList = SingerList()
                singerListEmptyTextView?.text = failedMessage
                singerListEmptyTextView?.visibility = View.VISIBLE
            } else {
                singerList?.let { sList ->
                    pageNo = sList.pageNo // get the back value from called function
                    pageSize = sList.pageSize // get the back value from called function
                    totalPages = sList.totalPages // get the back value from called function
                    if (sList.singers.isEmpty()) {
                        singerListEmptyTextView?.text = noResultString
                        singerListEmptyTextView?.visibility = View.VISIBLE
                    } else {
                        singerListEmptyTextView?.visibility = View.GONE
                    }
                } ?: { singerList = SingerList() }
            }
            LogUtil.d(TAG, "MyRestApi.onResponse.inject()")
            appCompBuilder
                .activityModule(this@SingerListActivity)
                .singerArrayListModule(singerList!!.singers)
                .floatModule(textFontSize).build()
                .inject(this@SingerListActivity)
            mRecyclerView?.setAdapter(myViewAdapter)
            mRecyclerView?.setLayoutManager(LinearLayoutManager(applicationContext))
            LogUtil.d(TAG, "MyRestApi.onResponse.isSearchEditTextChanged = $isSearchEditTextChanged")
            if (isSearchEditTextChanged) {
                // searchEditText.setFocusable(true);              // needed for requestFocus()
                // searchEditText.setFocusableInTouchMode(true);   // needed for requestFocus()
                // searchEditText.requestFocus();  // needed for the next two statements
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                // imm.showSoftInput(null, InputMethodManager.SHOW_IMPLICIT);
                imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
                isSearchEditTextChanged = false
            }
        }

        override fun onFailure(call: Call<SingerList>, t: Throwable) {
            LogUtil.e(TAG, "MyRestApi.onFailure.", t)
            if (loadingDialog != null) loadingDialog!!.dismissAllowingStateLoss()
            loadingDialog = null
            singerList = SingerList()
            singerListEmptyTextView?.text = failedMessage
            singerListEmptyTextView?.visibility = View.VISIBLE
        }
    }
}
