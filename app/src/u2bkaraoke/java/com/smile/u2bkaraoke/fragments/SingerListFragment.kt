package com.smile.u2bkaraoke.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
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
import com.smile.u2bkaraoke.U2bKaraokeApp.Companion.appCompBuilder
import com.smile.u2bkaraoke.model.Constants
import com.smile.u2bkaraoke.model.SingerList
import com.smile.u2bkaraoke.model.SingerType
import com.smile.u2bkaraoke.retrofit.RestApiAsync
import com.smile.u2bkaraoke.adapters.SingerListAdapter
import com.smile.u2bkaraoke.retrofit.RestApiSync
import com.smile.u2bkaraoke.utilities.U2bKaOkUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Response
import javax.inject.Inject

class SingerListFragment : Fragment(), RecyclerItemListener {


    companion object {
        private const val TAG = "SingerListFragment"
    }

    @JvmField
    @Inject
    var myViewAdapter: SingerListAdapter? = null
    private var textFontSize = 0f
    private var searchEditText: EditText? = null
    private var isSearchEditTextChanged = false
    private var filterString: String? = null
    private var singerListEmptyTextView: TextView? = null
    private var mRecyclerView: RecyclerView? = null
    private var singerList: SingerList? = null
    private var singerType: SingerType? = null
    private var activityTitle = ""
    private var pageNo = 1
    private var pageSize = 10
    private var totalPages = 0
    // private var restApi: MyRestApi? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            activityTitle = args.getString(Constants.SingerListTitle, "")
            singerType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                args.getParcelable(Constants.SingerTypeParcelable, SingerType::class.java)
            } else args.getParcelable(Constants.SingerTypeParcelable)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        LogUtil.i(TAG, "onCreateView")
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.activity_singer_list,
            container, false)
        return view
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        LogUtil.d(TAG, "onViewCreated")
        textFontSize = ScreenUtil.getPxTextFontSizeNeeded(activity)
        super.onViewCreated(view, savedInstanceState)

        val act = activity ?: return
        val singerListTitle = act.getString(R.string.singersListString)
        view.apply {
            val singerListMenuTextView = findViewById<TextView>(R.id.singerListMenuTextView)
            ScreenUtil.resizeTextSize(singerListMenuTextView, textFontSize)
            singerListMenuTextView.text = "$activityTitle $singerListTitle"
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
            singersListReturnButton.setOnClickListener { U2bKaOkUtil.returnToPrevious(act) }
        }

        // restApi = MyRestApi()
        retrieveSingerList()
    }

    override fun onItemClick(v: View?, position: Int) {
        LogUtil.i(TAG, "onItemClick.position = $position")
        if (position < 0) return
        val act = activity ?: return
        val fragContainerId = this.id   // container id of the fragment
        val fragManager = act.supportFragmentManager
        singerList?.let { list ->
            val singer = list.singers[position]
            LogUtil.i(TAG, "onItemClick.singer.singNa = ${singer.singNa}")
            ScreenUtil.showToast(act, singer.singNa,
                textFontSize,  Toast.LENGTH_SHORT)
            /*
            Intent(mActivity, SongListActivity::class.java).let {
                it.putExtra(Constants.OrderedFrom, Constants.SingerOrdered)
                it.putExtra(Constants.SongListTitle, singer.singNa)
                it.putExtra(Constants.SingerParcelable, singer)
                mActivity.startActivity(it)
            }
            */
            val nFragment = SongListFragment().apply {
                arguments = Bundle().apply {
                    putInt(Constants.OrderedFrom, Constants.SingerOrdered)
                    putString(Constants.SongListTitle, singer.singNa)
                    putParcelable(Constants.SingerParcelable, singer)
                }
            }
            U2bKaOkUtil.beginTransaction(fragManager, fragContainerId, nFragment)
        }
    }

    private fun retrieveSingerList() {
        val logStr = "retrieveSingerList"
        LogUtil.d(TAG, "$logStr.filterString = $filterString")
        val act = activity ?: return
        act.lifecycleScope.launch(Dispatchers.IO) {
            RestApiSync.getApiSync().let {rApi ->
                val sType = singerType ?: SingerType()
                singerList = if (filterString.isNullOrEmpty()) {
                    rApi.getSingersBySingerType(sType, pageSize, pageNo)
                } else {
                    rApi.getSingersBySingerType(sType, pageSize, pageNo,
                        filterString!!)
                }
                singerType = sType
                withContext(Dispatchers.Main) {
                    singerList?.let { sList ->
                        pageNo = sList.pageNo // get the back value from called function
                        pageSize = sList.pageSize // get the back value from called function
                        totalPages = sList.totalPages // get the back value from called function
                        if (sList.singers.isEmpty()) {
                            singerListEmptyTextView?.text = act.getString(R.string.noResultString)
                            singerListEmptyTextView?.visibility = View.VISIBLE
                        } else {
                            singerListEmptyTextView?.visibility = View.GONE
                        }
                    } ?: run {
                        singerList = SingerList()
                        singerListEmptyTextView?.text = act.getString(R.string.failedMessage)
                        singerListEmptyTextView?.visibility = View.VISIBLE
                    }
                    LogUtil.d(TAG, "$logStr.inject().myViewAdapter")
                    appCompBuilder
                        .recyclerItemListenerModule(this@SingerListFragment)
                        .singerArrayListModule(singerList!!.singers)
                        .floatModule(textFontSize).build()
                        .inject(this@SingerListFragment)
                    mRecyclerView?.setAdapter(myViewAdapter)
                    mRecyclerView?.setLayoutManager(LinearLayoutManager(act.applicationContext))
                    LogUtil.d(TAG, "$logStr.isSearchEditTextChanged = $isSearchEditTextChanged")
                    if (isSearchEditTextChanged) {
                        // searchEditText.setFocusable(true);              // needed for requestFocus()
                        // searchEditText.setFocusableInTouchMode(true);   // needed for requestFocus()
                        // searchEditText.requestFocus();  // needed for the next two statements
                        val imm =
                            act.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        // imm.showSoftInput(null, InputMethodManager.SHOW_IMPLICIT);
                        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
                        isSearchEditTextChanged = false
                    }
                }
            }
        }
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
            LogUtil.d(TAG, "MyRestApi.onResponse.response.isSuccessful = ${response.isSuccessful}")
            val act = activity ?: return
            singerList = response.body()
            if (!response.isSuccessful || singerList == null) {
                singerList = SingerList()
                singerListEmptyTextView?.text = act.getString(R.string.failedMessage)
                singerListEmptyTextView?.visibility = View.VISIBLE
            } else {
                singerList?.let { sList ->
                    pageNo = sList.pageNo // get the back value from called function
                    pageSize = sList.pageSize // get the back value from called function
                    totalPages = sList.totalPages // get the back value from called function
                    if (sList.singers.isEmpty()) {
                        singerListEmptyTextView?.text = act.getString(R.string.noResultString)
                        singerListEmptyTextView?.visibility = View.VISIBLE
                    } else {
                        singerListEmptyTextView?.visibility = View.GONE
                    }
                } ?: { singerList = SingerList() }
            }
            LogUtil.d(TAG, "MyRestApi.onResponse.inject()")
            appCompBuilder
                .recyclerItemListenerModule(this@SingerListFragment)
                .singerArrayListModule(singerList!!.singers)
                .floatModule(textFontSize).build()
                .inject(this@SingerListFragment)
            mRecyclerView?.setAdapter(myViewAdapter)
            mRecyclerView?.setLayoutManager(LinearLayoutManager(act.applicationContext))
            LogUtil.d(TAG, "MyRestApi.onResponse.isSearchEditTextChanged = $isSearchEditTextChanged")
            if (isSearchEditTextChanged) {
                // searchEditText.setFocusable(true);              // needed for requestFocus()
                // searchEditText.setFocusableInTouchMode(true);   // needed for requestFocus()
                // searchEditText.requestFocus();  // needed for the next two statements
                val imm = act.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                // imm.showSoftInput(null, InputMethodManager.SHOW_IMPLICIT);
                imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
                isSearchEditTextChanged = false
            }
        }

        override fun onFailure(call: Call<SingerList>, t: Throwable) {
            LogUtil.e(TAG, "MyRestApi.onFailure.", t)
            singerList = SingerList()
            singerListEmptyTextView?.text = activity?.getString(R.string.failedMessage)
            singerListEmptyTextView?.visibility = View.VISIBLE
        }
    }
}
