package youtube.fragments

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.smile.karaoke.BuildConfig
import com.smile.karaoke.R
import com.smile.karaoke.adapters.MyLinearLayoutManager
import com.smile.karaoke.adapters.OpenFilesRecyclerViewAdapter
import com.smile.karaoke.fragments.CommonFragment
import com.smile.karaoke.interfaces.RecyclerItemListener
import com.smile.karaoke.models.MySingleTon
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import youtube.retrofit.RestApiSync

class SearchVideosFragment : CommonFragment(), RecyclerItemListener {

    companion object {
        private const val TAG : String = "SearchVideosFragment"
        private const val SEARCH_FOLDER_COMPLETED = "SearchCurrentFolder"
    }

    private var fragmentView : View? = null
    private var pathTextView: TextView? = null
    private var filesRecyclerView : RecyclerView? = null
    private var myRecyclerViewAdapter : OpenFilesRecyclerViewAdapter? = null
    private var searchCompleted = true
    private var backKeyButton: ImageButton? = null
    private var selectAllButton: ImageButton? = null
    private var unselectButton: ImageButton? = null
    private var switchDecoderButton: ImageButton? = null
    private var playSelectedButton: ImageButton? = null
    private var addToFavoriteButton: ImageButton? = null
    private var showVideoButton: ImageButton? = null
    private var appsImageButton: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)

        LogUtil.i(TAG, "onCreate.finished")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        LogUtil.i(TAG, "onCreateView")
        return inflater.inflate(R.layout.fragment_open_file, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LogUtil.i(TAG, "onViewCreated")
        fragmentView = view
        fragmentView?.let {
            filesRecyclerView = it.findViewById(R.id.openFilesRecyclerView)
            filesRecyclerView?.setHasFixedSize(true)
            pathTextView = it.findViewById(R.id.pathTextView)
            ScreenUtil.resizeTextSize(pathTextView, textFontSize)
            backKeyButton = it.findViewById(R.id.openFileBackKeyButton)
            backKeyButton?.setOnClickListener {
                if (!searchCompleted) return@setOnClickListener // searching
                if (MySingleTon.currentPath == "/") return@setOnClickListener
                MySingleTon.currentPath =
                    if (MySingleTon.rootPathSet.contains(MySingleTon.currentPath)) "/"
                else {
                    val index = MySingleTon.currentPath.lastIndexOf('/')
                    if (index >= 0 ) MySingleTon.currentPath.substring(0, index) else "/"
                }
                if (MySingleTon.currentPath.isEmpty()) MySingleTon.currentPath = "/"
                searchYouTubeVideos()
            }
            selectAllButton = it.findViewById(R.id.openFileSelectAllButton)
            selectAllButton?.setOnClickListener {
                if (!searchCompleted) return@setOnClickListener // searching
                for (i in 0 until MySingleTon.fileList.size) {
                    MySingleTon.fileList[i].run {
                        if (!file.isDirectory && !selected) {
                            selected = true
                            myRecyclerViewAdapter?.notifyItemChanged(i)
                        }
                    }
                }
            }
            unselectButton = it.findViewById(R.id.openFileUnselectButton)
            unselectButton?.setOnClickListener {
                if (!searchCompleted) return@setOnClickListener // searching
                for (i in 0 until MySingleTon.fileList.size) {
                    MySingleTon.fileList[i].run {
                        if (!file.isDirectory && selected) {
                            selected = false
                            myRecyclerViewAdapter?.notifyItemChanged(i)
                        }
                    }
                }
            }
            switchDecoderButton = it.findViewById(R.id.openFileSwitchDecoderButton)
            setupSwitchDecoderButton()
            switchDecoderButton?.let {switchIt ->
                switchIt.setOnClickListener {
                    if (!searchCompleted) return@setOnClickListener // searching
                    playSongs?.switchBetweenSoftAndHardDecoder()
                    setupSwitchDecoderButton()
                }
            }
            playSelectedButton = it.findViewById(R.id.openFilePlaySelectedButton)
            playSelectedButton?.setImageResource(
                    if (isPlayButton) R.drawable.play_media_button_image
                    else R.drawable.open_files)
            playSelectedButton?.setOnClickListener {
                if (!searchCompleted) return@setOnClickListener // searching
                // open the files to play
                // startPlaySelectedSong(activity, "playSelectedButton")
            }
            addToFavoriteButton = it.findViewById(R.id.addToFavoriteButton)
            addToFavoriteButton?.setOnClickListener {
                if (!searchCompleted) return@setOnClickListener // searching
            }
            showVideoButton = it.findViewById(R.id.showVideoImageButton)
            showVideoButton?.visibility = View.VISIBLE
            showVideoButton?.setOnClickListener {
                if (!searchCompleted) return@setOnClickListener // searching
                playSongs?.switchToPlayerView()
            }

            appsImageButton = it.findViewById(R.id.appsImageButton)
            appsImageButton?.visibility = View.VISIBLE
            appsImageButton?.setOnClickListener {
                playSongs?.showSmileAppsActivity()
            }

            it.isFocusable = true
            it.isFocusableInTouchMode = true
            it.requestFocus()
            it.setOnKeyListener {
                    _, keyCode, event ->
                showVideoButton?.requestFocus()
                return@setOnKeyListener false
            }
        }

        setButtonsSize()
        initFilesRecyclerView()
    }

    override fun onStart() {
        super.onStart()
        LogUtil.i(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        LogUtil.i(TAG, "onResume")
        setupSwitchDecoderButton()
        searchYouTubeVideos()   // has to be in onResume()
    }

    override fun onPause() {
        super.onPause()
        LogUtil.i(TAG, "onPause")
        clearFileList()
    }

    override fun onStop() {
        super.onStop()
        LogUtil.i(TAG, "onStop")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        LogUtil.i(TAG, "onConfigurationChanged")
        setButtonsSize()
        super.onConfigurationChanged(newConfig)
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.i(TAG, "onDestroy")
        clearFileList()
        mediaRetriever.release()
    }

    override fun onItemClick(v: View?, position: Int) {
        LogUtil.i(TAG, "onItemClick.position = $position")
        if (position < 0) return
        v?.requestFocus()
        if (MySingleTon.fileList[position].file.isFile) {
            MySingleTon.fileList[position].selected = !MySingleTon.fileList[position].selected
            myRecyclerViewAdapter?.myNotifyItemChanged(position)
            return
        }
        MySingleTon.currentPath = MySingleTon.fileList[position].file.path
        searchYouTubeVideos()
    }

    fun clearFileList() {
        MySingleTon.fileList.clear()
        myRecyclerViewAdapter?.myNotifyDataSetChanged()
    }

    fun searchYouTubeVideos() {
        LogUtil.i(TAG, "searchCurrentFolder")
        searchCompleted = false
        LogUtil.i(TAG, "searchCurrentFolder.APPLICATION_ID = ${BuildConfig.APPLICATION_ID}")
        lifecycleScope.launch(Dispatchers.IO) {
            val videoList = RestApiSync.getVideoList(BuildConfig.APPLICATION_ID,
                "Android")
            LogUtil.d(TAG, "videoList.items.size = ${videoList.items.size}")
            for (item in videoList.items) {
                LogUtil.d(TAG, "kind = ${item.id.kind}")
                LogUtil.d(TAG, "videoId = ${item.id.videoId}")
            }
            searchCompleted = true
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setButtonsSize() {
        val buttonWidth = (textFontSize*1.5f).toInt()
        var percentWidth = 1.0f
        if (resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
            percentWidth = 0.6f
        }
        val buttonLayout = fragmentView?.findViewById<LinearLayout>(R.id.openFileButtonLayout)
        val constrainParam = buttonLayout?.layoutParams as ConstraintLayout.LayoutParams
        constrainParam.constrainedWidth = true
        constrainParam.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        constrainParam.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        constrainParam.matchConstraintPercentWidth = percentWidth
        buttonLayout.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            LogUtil.d(TAG, "setButtonsSize.setOnTouchListener.hasFocus() = $hasFocus")
        }

        var linearParam = backKeyButton?.layoutParams as LinearLayout.LayoutParams
        linearParam.width = buttonWidth
        linearParam.height = buttonWidth
        linearParam.setMargins(0, 0, 0, 0)
        selectAllButton?.layoutParams = linearParam
        unselectButton?.layoutParams = linearParam
        switchDecoderButton?.layoutParams = linearParam
        playSelectedButton?.layoutParams = linearParam
        addToFavoriteButton?.layoutParams = linearParam
        showVideoButton?.layoutParams = linearParam

        linearParam = appsImageButton?.layoutParams as LinearLayout.LayoutParams
        linearParam.width = buttonWidth
        linearParam.height = buttonWidth
        linearParam.setMargins(0, 0, 0, 0)
    }

    private fun initFilesRecyclerView() {
        LogUtil.i(TAG, "initFilesRecyclerView() is called")
        activity?.let {
            myRecyclerViewAdapter = OpenFilesRecyclerViewAdapter(
                this, MySingleTon.fileList,
                textFontSize,
                videoThumbnailsWidth, videoThumbnailsHeight)
            filesRecyclerView?.adapter = myRecyclerViewAdapter
            filesRecyclerView?.layoutManager = MyLinearLayoutManager(context)
        }
    }

    fun setupSwitchDecoderButton() {
        LogUtil.i(TAG, "setupSwitchDecoderButton")
    }
}