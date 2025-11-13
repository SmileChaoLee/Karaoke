package com.smile.karaoke.fragments

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.scale
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.smile.karaoke.R
import com.smile.karaoke.adapters.MyLinearLayoutManager
import com.smile.karaoke.adapters.OpenFilesRecyclerViewAdapter
import com.smile.karaoke.constants.CommonConstants
import com.smile.karaoke.interfaces.PlaySongs
import com.smile.karaoke.models.FileDescription
import com.smile.karaoke.models.MySingleTon
import com.smile.karaoke.models.SongInfo
import com.smile.karaoke.models.SongListSQLite
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class OpenFileFragment : Fragment(),
    OpenFilesRecyclerViewAdapter.OnRecyclerItemClickListener {

    companion object {
        private const val TAG : String = "OpenFileFragment"
        private const val SEARCH_FOLDER_COMPLETED = "SearchCurrentFolder"
    }

    private var textFontSize = 0.0f
    private var videoThumbnailsWidth = 0
    private var videoThumbnailsHeight = 0
    private var fragmentView : View? = null
    private var playSongs: PlaySongs? = null
    private var pathTextView: TextView? = null
    private var filesRecyclerView : RecyclerView? = null
    private var myRecyclerViewAdapter : OpenFilesRecyclerViewAdapter? = null
    private var isPlayButton: Boolean = true
    private lateinit var broadcastReceiver: BroadcastReceiver
    private var searchCompleted = true
    private lateinit var mediaRetriever: MediaMetadataRetriever
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

        arguments?.let {
            isPlayButton = it.getBoolean(CommonConstants.IS_BUTTON_PLAY, true)
            LogUtil.d(TAG, "onCreate.isPlayButton = $isPlayButton")
        }
        activity?.let {
            textFontSize = ScreenUtil.getPxTextFontSizeNeeded(it)
            videoThumbnailsWidth = (textFontSize * 3.0f).toInt()
            videoThumbnailsHeight = (textFontSize * 2.0f).toInt()
            if (it is PlaySongs) playSongs = it
            LogUtil.d(TAG, "onCreate.playSongs = $playSongs")
        }

        mediaRetriever = MediaMetadataRetriever()

        // FileDesList.currentPath = Environment.getExternalStorageDirectory().toString()
        LogUtil.d(TAG, "onCreate.FileDesList.currentPath = ${MySingleTon.currentPath}")

        activity?.applicationContext?.externalCacheDirs?.let {
            LogUtil.d(TAG, "externalCacheDirs = $it, externalCacheDirs.size = ${it.size}")
            MySingleTon.rootPathSet.clear()
            for (element in it) {
                LogUtil.d(TAG, "externalCacheDirs.element = $element")
                element?.absolutePath?.let { pathIt ->
                    pathIt.indexOf("/Android/data").let {indexIt ->
                        if (indexIt >= 0) {
                            pathIt.substring(0, indexIt).let {subIt ->
                                LogUtil.d(TAG, "element.substring(0, indexIt) = $subIt")
                                MySingleTon.rootPathSet.add(subIt)
                            }
                        }
                    }
                }
            }
        }

        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                LogUtil.i(TAG, "BroadcastReceiver.onReceive")
                val focusView = activity?.currentFocus
                intent?.action?.let {
                    if (it == SEARCH_FOLDER_COMPLETED) {
                        LogUtil.d(TAG, "BroadcastReceiver.onReceive.SEARCH_FOLDER_COMPLETED")
                        pathTextView?.text = MySingleTon.currentPath
                        myRecyclerViewAdapter?.myNotifyDataSetChanged()
                        LogUtil.d(TAG, "BroadcastReceiver.onReceive.focusView = $focusView")
                        if (MySingleTon.fileList.isEmpty()) {
                            LogUtil.d(TAG, "BroadcastReceiver.onReceive.MySingleTon.fileList is empty")
                            val keyEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)
                            val isKeyDown: Boolean? = fragmentView?.dispatchKeyEvent(keyEvent)
                            LogUtil.d(TAG, "BroadcastReceiver.onReceive.isKeyDown = $isKeyDown")
                            backKeyButton?.requestFocus()
                        }
                        searchCompleted = true  // searching thread finished
                    }
                }
            }
        }.also { broadcastReceiver = it }
        activity?.let {
            LocalBroadcastManager.getInstance(it).apply {
                LogUtil.d(TAG, "LocalBroadcastManager.registerReceiver")
                registerReceiver(broadcastReceiver, IntentFilter().apply {
                    addAction(SEARCH_FOLDER_COMPLETED)
                })
            }
        }

        LogUtil.i(TAG, "onCreate.FileDesList.fileList.size = ${MySingleTon.fileList.size}")
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
            ScreenUtil.resizeTextSize(pathTextView, textFontSize,
                ScreenUtil.FontSize_Pixel_Type)
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
                searchCurrentFolder()
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
                    if (isPlayButton) R.drawable.play_media_button_image else R.drawable.open_files)
            playSelectedButton?.setOnClickListener {
                if (!searchCompleted) return@setOnClickListener // searching
                // open the files to play
                activity?.let {activityIt ->
                    val songListSQLite = SongListSQLite(activityIt)
                    getSongs(songListSQLite, "playSelectedButton").let { songsIt ->
                        if (songsIt.isEmpty()) {
                            ScreenUtil.showToast(
                                activityIt, getString(R.string.noFilesSelectedString), textFontSize,
                                ScreenUtil.FontSize_Pixel_Type,
                                Toast.LENGTH_SHORT)
                        } else {
                            playSongs?.playSelectedSongList(ArrayList(songsIt))
                        }
                    }
                    songListSQLite.closeDatabase()
                }
            }
            addToFavoriteButton = it.findViewById(R.id.addToFavoriteButton)
            addToFavoriteButton?.setOnClickListener {
                if (!searchCompleted) return@setOnClickListener // searching
                activity?.let {activityIt ->
                    val songListSQLite = SongListSQLite(activityIt)
                    getSongs(songListSQLite, "addToFavoriteButton").also { songsIt ->
                        var toastMsg = getString(R.string.noFilesSelectedString)
                        if (songsIt.isNotEmpty()) {
                            for (song in songsIt) {
                                song.included = "1"
                                val numRecords = songListSQLite.recordsOfPlayList()
                                LogUtil.d(TAG, "addToFavoriteButton.recordsOfPlayList() = $numRecords")
                                if (numRecords < MySingleTon.MAX_SONGS) {
                                    songListSQLite.addSongToSongList(song)
                                } else {
                                    // excess max number of favorites
                                    ScreenUtil.showToast(activity,getString(R.string.excess_max) +
                                            " ${MySingleTon.MAX_SONGS}", textFontSize,
                                        ScreenUtil.FontSize_Pixel_Type,
                                        Toast.LENGTH_SHORT)
                                    break
                                }
                            }
                            toastMsg = getString(R.string.add_to_favorites)
                        }
                        ScreenUtil.showToast(activity, toastMsg, textFontSize,
                            ScreenUtil.FontSize_Pixel_Type,
                            Toast.LENGTH_SHORT)
                    }
                    songListSQLite.closeDatabase()
                }
            }
            showVideoButton = it.findViewById(R.id.showVideoImageButton)
            showVideoButton?.visibility = View.VISIBLE
            showVideoButton?.setOnClickListener {
                if (!searchCompleted) return@setOnClickListener // searching
                playSongs?.switchToPlayerView()
            }
            it.isFocusable = true
            it.isFocusableInTouchMode = true
            it.requestFocus()

            appsImageButton = it.findViewById(R.id.appsImageButton)
            appsImageButton?.visibility = View.VISIBLE
            appsImageButton?.setOnClickListener {
                playSongs?.showSmileAppsActivity()
            }
            it.isFocusable = true
            it.isFocusableInTouchMode = true
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
        searchCurrentFolder()   // has to be in onResume()
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
        activity?.let {
            LocalBroadcastManager.getInstance(it).apply {
                unregisterReceiver(broadcastReceiver)
            }
        }
        mediaRetriever.release()
    }

    override fun onRecyclerItemClick(v: View?, position: Int) {
        LogUtil.i(TAG, "onRecyclerItemClick.position = $position")
        if (position < 0) return
        v?.requestFocus()
        if (MySingleTon.fileList[position].file.isFile) {
            MySingleTon.fileList[position].selected = !MySingleTon.fileList[position].selected
            myRecyclerViewAdapter?.myNotifyItemChanged(position)
            return
        }
        MySingleTon.currentPath = MySingleTon.fileList[position].file.path
        searchCurrentFolder()
    }

    fun clearFileList() {
        MySingleTon.fileList.clear()
        myRecyclerViewAdapter?.myNotifyDataSetChanged()
    }

    fun searchCurrentFolder() {
        LogUtil.i(TAG, "searchCurrentFolder")
        searchCompleted = false
        lifecycleScope.launch(Dispatchers.IO) {
            val tempList: ArrayList<FileDescription> = ArrayList(MySingleTon.maxFiles)
            MySingleTon.currentPath.let {
                if (it == "/") {
                    for (element in MySingleTon.rootPathSet) {
                        LogUtil.d(TAG, "searchCurrentFolder.element = $element")
                        tempList.add(FileDescription(File(element),
                            null, false))
                    }
                } else {
                    try {
                        File(it).listFiles()?.also { fIt ->
                            LogUtil.d(TAG, "searchCurrentFolder.file.list().size() = ${fIt.size}")
                            for (f in fIt) {
                                LogUtil.d(TAG, "searchCurrentFolder.isDirectory = ${f.isDirectory}, f.path = ${f.path}")
                                var bm: Bitmap? = null
                                if (!f.isDirectory) {
                                    try {
                                        mediaRetriever.setDataSource(f.path)
                                        bm = mediaRetriever.getFrameAtTime(0,
                                            MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                                            ?.scale(videoThumbnailsWidth, videoThumbnailsHeight)
                                    } catch (ex: Exception) {
                                        LogUtil.e(TAG, "searchCurrentFolder.setDataSource.Exception:",
                                                ex)
                                    }
                                }
                                tempList.add(FileDescription(f, bm, false))
                            }
                        }
                    } catch (ex: Exception) {
                        LogUtil.e(TAG, "searchCurrentFolder.Exception", ex )
                    }
                }
            }
            MySingleTon.fileList.clear()
            MySingleTon.fileList.addAll(tempList)
            LogUtil.d(TAG, "searchCurrentFolder.FileDesList.fileList.size = ${MySingleTon.fileList.size}")

            activity?.let {
                LocalBroadcastManager.getInstance(it).apply {
                    sendBroadcast(Intent().apply {
                        action = SEARCH_FOLDER_COMPLETED
                    })
                }
            }
        }
        /*
        Thread {
            val tempList: ArrayList<FileDescription> = ArrayList(MySingleTon.maxFiles)
            MySingleTon.currentPath.let {
                if (it == "/") {
                    for (element in MySingleTon.rootPathSet) {
                        LogUtil.d(TAG, "searchCurrentFolder.element = $element")
                        tempList.add(FileDescription(File(element), false))
                    }
                } else {
                    try {
                        File(it).listFiles()?.also { fIt ->
                            LogUtil.d(TAG, "file.list().size() = ${fIt.size}")
                            for (f in fIt) {
                                LogUtil.d(TAG, "isDirectory = ${f.isDirectory}, f.path = ${f.path}")
                                // if (f.canRead()) {
                                tempList.add(FileDescription(f, false))
                                // }
                            }
                        }
                    } catch (ex: Exception) {
                        LogUtil.d(TAG, "${ex.message}")
                    }
                }
            }
            MySingleTon.fileList.clear()
            MySingleTon.fileList.addAll(tempList)
            LogUtil.d(TAG, "searchCurrentFolder.FileDesList.fileList.size = ${MySingleTon.fileList.size}")

            activity?.let {
                LocalBroadcastManager.getInstance(it).apply {
                    sendBroadcast(Intent().apply {
                        action = SEARCH_FOLDER_COMPLETED
                    })
                }
            }

        }.start()
        */
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setButtonsSize() {
        val buttonWidth = (textFontSize*1.5f).toInt()
        var percentWidth = 1.0f
        var rightMargin = 5
        if (resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
            percentWidth = 0.6f
            rightMargin = 15
        }
        val buttonLayout = fragmentView?.findViewById<LinearLayout>(R.id.openFileButtonLayout)
        val constrainParam = buttonLayout?.layoutParams as ConstraintLayout.LayoutParams
        constrainParam.constrainedWidth = true
        constrainParam.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        constrainParam.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        constrainParam.matchConstraintPercentWidth = percentWidth
        buttonLayout.setOnTouchListener { view, _ ->
            // issue requestFocus() will get focus immediately
            // but it still be able to get focus a little bit later
            // if do not issue requestFocus()
            val hasFocus = view.requestFocus()
            LogUtil.d(TAG, "setButtonsSize.setOnTouchListener.hasFocus() = $hasFocus")
            false
        }

        var linearParam = LinearLayout.LayoutParams(buttonWidth, buttonWidth)
        linearParam.setMargins(0, 0, rightMargin, 0)
        linearParam.gravity = Gravity.CENTER
        backKeyButton?.layoutParams = linearParam
        selectAllButton?.layoutParams = linearParam
        unselectButton?.layoutParams = linearParam
        switchDecoderButton?.layoutParams = linearParam
        playSelectedButton?.layoutParams = linearParam
        addToFavoriteButton?.layoutParams = linearParam
        showVideoButton?.layoutParams = linearParam

        linearParam = LinearLayout.LayoutParams(buttonWidth, buttonWidth)
        linearParam.setMargins(0, 0, 0, 0)
        linearParam.gravity = Gravity.CENTER
        appsImageButton?.layoutParams = linearParam
    }

    private fun getSongs(songListSQLite : SongListSQLite, msg : String) : ArrayList<SongInfo> {
        val songs = ArrayList<SongInfo>().also {songIt ->
            var index = 0
            for (i in 0 until MySingleTon.fileList.size) {
                if (MySingleTon.fileList[i].selected) {
                    LogUtil.d(TAG, "$msg.file.path = ${MySingleTon.fileList[i].file.path}")
                    LogUtil.d(TAG, "$msg.file.toUri() = ${MySingleTon.fileList[i].file.toUri()}")
                    var song = SongInfo().apply {
                        songName = MySingleTon.fileList[i].file.name
                        filePath = MySingleTon.fileList[i].file.toUri().toString()
                        musicTrackNo = 1    // guess
                        musicChannel = CommonConstants.STEREO
                        vocalTrackNo = 2    // guess
                        vocalChannel = CommonConstants.STEREO
                        included = "0"
                    }
                    songListSQLite.findOneSongByUriString(song.filePath)?.apply {
                        LogUtil.d(TAG, "$msg.found")
                        included = "1"
                        song = this
                    }
                    songIt.add(song)
                    index++
                    if (index >= MySingleTon.MAX_SONGS) {
                        // excess the max
                        ScreenUtil.showToast(
                                activity, getString(R.string.excess_max) +
                                " ${MySingleTon.MAX_SONGS}", textFontSize,
                            ScreenUtil.FontSize_Pixel_Type,
                            Toast.LENGTH_SHORT)
                        break
                    }
                }
            }
        }
        return songs
    }

    private fun initFilesRecyclerView() {
        LogUtil.i(TAG, "initFilesRecyclerView() is called")
        activity?.let {
            val tColor = ContextCompat.getColor(it, R.color.gnt_green)
            val transparentLightGray = ContextCompat.getColor(it,
                R.color.transparentLightGray)
            myRecyclerViewAdapter = OpenFilesRecyclerViewAdapter(
                this, MySingleTon.fileList,
                tColor, transparentLightGray, textFontSize,
                videoThumbnailsWidth, videoThumbnailsHeight)
            filesRecyclerView?.adapter = myRecyclerViewAdapter
            filesRecyclerView?.layoutManager = MyLinearLayoutManager(context)
        }
    }

    fun setupSwitchDecoderButton() {
        LogUtil.i(TAG, "setupSwitchDecoderButton")
        switchDecoderButton?.apply {
            playSongs?.let {
                setImageResource(
                    if (it.isSoftDecoderFirst()) R.drawable.soft_decoder
                    else R.drawable.hard_decoder
                )
            }
        }
    }
}