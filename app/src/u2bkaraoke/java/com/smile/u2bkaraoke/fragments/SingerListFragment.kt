package com.smile.u2bkaraoke.fragments

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
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
import com.smile.u2bkaraoke.u2bkaok_constants.U2bKKConstants
import com.smile.u2bkaraoke.model.SingerList
import com.smile.u2bkaraoke.model.SingerType
import com.smile.u2bkaraoke.adapters.SingerListAdapter
import com.smile.u2bkaraoke.retrofit.RestApiSync
import com.smile.u2bkaraoke.utilities.U2bKaOkUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SingerListFragment : U2bKKBaseFragment(), RecyclerItemListener {


    companion object {
        private const val TAG = "SingerListFragment"
    }

    @JvmField
    @Inject
    var myViewAdapter: SingerListAdapter? = null
    private var mRecyclerView: RecyclerView? = null
    private var searchEditText: EditText? = null
    private var filterString: String? = null
    private var singerListEmptyTextView: TextView? = null
    private var firstPageButton: Button? = null
    private var previousPageButton: Button? = null
    private var nextPageButton: Button? = null
    private var lastPageButton: Button? = null
    private var pageNoTextView: TextView? = null
    private lateinit var singerList: SingerList
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
            singerType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                args.getParcelable(U2bKKConstants.SingerTypeParcelable, SingerType::class.java)
            } else args.getParcelable(U2bKKConstants.SingerTypeParcelable)
            singerType?.let {sType ->
                val act = activity ?: return
                val sexString = when (sType.sex) {
                    "1" -> " - ${act.getString(R.string.male)}"
                    "2" -> " - ${act.getString(R.string.female)}"
                    else ->                 // "0"
                        ""
                }
                activityTitle = "${sType.areaNa}$sexString"
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        LogUtil.i(TAG, "onCreateView")
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_singer_list,
            container, false)
        return view
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        LogUtil.d(TAG, "onViewCreated")
        textFontSize = ScreenUtil.getPxTextFontSizeNeeded(activity)
        singerList = SingerList()

        view.apply {
            val singerListMenuTextView = findViewById<TextView>(R.id.singerListMenuTextView)
            ScreenUtil.resizeTextSize(singerListMenuTextView, textFontSize)
            singerListMenuTextView.text = activityTitle
            filterString = ""
            searchEditText = findViewById(R.id.singerSearchEditText)
            searchEditText?.let { sEt ->
                ScreenUtil.resizeTextSize(sEt, textFontSize)
                sEt.setText(filterString)
                sEt.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {}
                    override fun onTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {}
                    override fun afterTextChanged(editable: Editable) {
                        LogUtil.d(TAG, "addTextChangedListener.afterTextChanged")
                        val content = editable.toString().trim()
                        filterString = if (content.isEmpty()) "" else "SingNa+$content"
                        LogUtil.d(TAG, "addTextChangedListener.afterTextChanged.filterString = $filterString")
                        pageNo = 1
                        retrieveSingerList(true)
                    }
                })
            }
            singerListEmptyTextView = findViewById(R.id.singerListEmptyTextView)
            ScreenUtil.resizeTextSize(singerListEmptyTextView, textFontSize)
            singerListEmptyTextView?.visibility = View.GONE
            mRecyclerView = findViewById(R.id.singerListRecyclerView)
            val smallButtonFontSize = textFontSize * 0.7f
            firstPageButton = findViewById(R.id.firstPageButton)
            ScreenUtil.resizeTextSize(firstPageButton, smallButtonFontSize)
            previousPageButton = findViewById(R.id.previousPageButton)
            ScreenUtil.resizeTextSize(previousPageButton, smallButtonFontSize)
            nextPageButton = findViewById(R.id.nextPageButton)
            ScreenUtil.resizeTextSize(nextPageButton, smallButtonFontSize)
            lastPageButton = findViewById(R.id.lastPageButton)
            ScreenUtil.resizeTextSize(lastPageButton, smallButtonFontSize)
            pageNoTextView = findViewById(R.id.pageNoTotal)
            ScreenUtil.resizeTextSize(pageNoTextView, smallButtonFontSize)
        }

        super.onViewCreated(view, savedInstanceState)

        firstPageButton?.nextFocusUpId = R.id.singerListRecyclerView
        previousPageButton?.nextFocusUpId = R.id.singerListRecyclerView
        nextPageButton?.nextFocusUpId = R.id.singerListRecyclerView
        lastPageButton?.nextFocusUpId = R.id.singerListRecyclerView
        exitImageButton?.nextFocusUpId = R.id.nextPageButton
        showVideoButton?.nextFocusUpId = R.id.nextPageButton

        appCompBuilder
            .recyclerItemListenerModule(this@SingerListFragment)
            .singerArrayListModule(singerList.singers)
            .floatModule(textFontSize).build()
            .inject(this@SingerListFragment)
        mRecyclerView?.setAdapter(myViewAdapter)
        mRecyclerView?.setLayoutManager(LinearLayoutManager(requireContext()))

        // restApi = MyRestApi()
        retrieveSingerList()
    }

    override fun setClickListeners() {
        super.setClickListeners()
        firstPageButton?.setOnClickListener { firstPage() }
        previousPageButton?.setOnClickListener { previousPage() }
        nextPageButton?.setOnClickListener { nextPage() }
        lastPageButton?.setOnClickListener { lastPage() }
    }

    override fun onItemClick(v: View?, position: Int) {
        LogUtil.i(TAG, "onItemClick.position = $position")
        if (position < 0) return
        val act = activity ?: return
        val fragContainerId = this.id   // container id of the fragment
        val fragManager = act.supportFragmentManager
        singerList.let { list ->
            val singer = list.singers[position]
            LogUtil.i(TAG, "onItemClick.singer.singNa = ${singer.singNa}")
            ScreenUtil.showToast(act, singer.singNa,
                textFontSize,  Toast.LENGTH_SHORT)
            val nFragment = SongListFragment().apply {
                arguments = Bundle().apply {
                    putInt(U2bKKConstants.OrderedFrom, U2bKKConstants.SingerOrdered)
                    putString(U2bKKConstants.SongListTitle, singer.singNa)
                    putParcelable(U2bKKConstants.SingerParcelable, singer)
                }
            }
            U2bKaOkUtil.beginTransaction(fragManager, fragContainerId, nFragment)
        }
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    private fun retrieveSingerList(isSearch: Boolean = false) {
        val logStr = "retrieveSingerList"
        LogUtil.d(TAG, "$logStr.filterString = $filterString")
        val act = activity ?: return
        act.lifecycleScope.launch(Dispatchers.Main) {
            mRecyclerView?.visibility = View.GONE
            singerListEmptyTextView?.visibility = View.VISIBLE
            singerListEmptyTextView?.text = act.getString(R.string.loadingString)
            var tempList: SingerList? = null
            withContext(Dispatchers.IO) {
                RestApiSync.getApiSync().let { rApi ->
                    val sType = singerType ?: SingerType()
                    tempList = if (filterString.isNullOrEmpty()) {
                        rApi.getSingersBySingerType(sType, pageSize, pageNo)
                    } else {
                        rApi.getSingersBySingerType(
                            sType, pageSize, pageNo,
                            filterString!!
                        )
                    }
                    singerType = sType
                }
            }
            withContext(Dispatchers.Main) {
                tempList?.let { sList ->
                    if (sList.singers.isEmpty()) {
                        singerListEmptyTextView?.text = act.getString(R.string.noResultString)
                        singerListEmptyTextView?.visibility = View.VISIBLE
                    } else {
                        singerListEmptyTextView?.visibility = View.GONE
                    }
                } ?: run {
                    tempList = SingerList()
                    singerListEmptyTextView?.text = act.getString(R.string.failedMessage)
                    singerListEmptyTextView?.visibility = View.VISIBLE
                }
                LogUtil.d(TAG, "$logStr.tempList.singers.size = ${tempList?.singers?.size}")
                singerList.singers.clear()
                tempList?.let { tempList ->
                    singerList.pageNo = tempList.pageNo
                    singerList.pageSize = tempList.pageSize
                    singerList.totalRecords = tempList.totalRecords
                    singerList.totalPages = tempList.totalPages
                    singerList.singers.addAll(tempList.singers)
                }
                myViewAdapter?.notifyDataSetChanged()
                updateRecyclerView()
                pageNo = singerList.pageNo
                pageSize = singerList.pageSize
                totalPages = singerList.totalPages
                pageNoTextView?.text = "$pageNo/$totalPages"
                if (isSearch) searchEditText?.post { searchEditText?.requestFocus() }
            }
        }
    }

    private fun updateRecyclerView() {
        singerListEmptyTextView?.visibility = View.GONE
        singerList.let {
            if (it.singers.isEmpty()) {
                mRecyclerView?.visibility = View.GONE
                exitImageButton?.post { exitImageButton?.requestFocus() }
            } else {
                mRecyclerView?.visibility = View.VISIBLE
                mRecyclerView?.post { mRecyclerView?.requestFocus() }
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
}
