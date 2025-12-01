package com.smile.karaoke.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.scale
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.smile.karaoke.R
import com.smile.karaoke.adapters.MyLinearLayoutManager
import com.smile.karaoke.adapters.OpenFilesRecyclerViewAdapter
import com.smile.karaoke.interfaces.RecyclerItemListener
import com.smile.karaoke.models.FileDescription
import com.smile.karaoke.models.MySingleton
import com.smile.karaoke.models.SongListSQLite
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class OpenFileFragment : ComOpenFragment(), RecyclerItemListener {

    companion object {
        private const val TAG : String = "OpenFileFragment"
        private const val SEARCH_FOLDER_COMPLETED = "SearchCurrentFolder"
    }

    private var pathTextView: TextView? = null
    private var filesRecyclerView : RecyclerView? = null
    private var myRecyclerViewAdapter : OpenFilesRecyclerViewAdapter? = null
    private lateinit var broadcastReceiver: BroadcastReceiver
    private var backKeyButton: ImageButton? = null
    private var selectAllButton: ImageButton? = null
    private var unselectButton: ImageButton? = null
    private var switchDecoderButton: ImageButton? = null
    private var playSelectedButton: ImageButton? = null
    private var addToFavoriteButton: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)

        // FileDesList.currentPath = Environment.getExternalStorageDirectory().toString()
        LogUtil.d(TAG, "onCreate.FileDesList.currentPath = ${MySingleton.currentPath}")

        activity?.applicationContext?.externalCacheDirs?.let {
            LogUtil.d(TAG, "externalCacheDirs = $it, externalCacheDirs.size = ${it.size}")
            MySingleton.rootPathSet.clear()
            for (element in it) {
                LogUtil.d(TAG, "externalCacheDirs.element = $element")
                element?.absolutePath?.let { pathIt ->
                    pathIt.indexOf("/Android/data").let {indexIt ->
                        if (indexIt >= 0) {
                            pathIt.substring(0, indexIt).let {subIt ->
                                LogUtil.d(TAG, "element.substring(0, indexIt) = $subIt")
                                MySingleton.rootPathSet.add(subIt)
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
                        pathTextView?.text = MySingleton.currentPath
                        myRecyclerViewAdapter?.myNotifyDataSetChanged()
                        LogUtil.d(TAG, "BroadcastReceiver.onReceive.focusView = $focusView")
                        if (MySingleton.fileList.isEmpty()) {
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

        LogUtil.i(TAG, "onCreate.FileDesList.fileList.size = ${MySingleton.fileList.size}")
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
        LogUtil.i(TAG, "onViewCreated")
        view.let {
            filesRecyclerView = it.findViewById(R.id.openFilesRecyclerView)
            filesRecyclerView?.setHasFixedSize(true)
            pathTextView = it.findViewById(R.id.pathTextView)
            ScreenUtil.resizeTextSize(pathTextView, textFontSize)
            backKeyButton = it.findViewById(R.id.openFileBackKeyButton)
            selectAllButton = it.findViewById(R.id.openFileSelectAllButton)
            unselectButton = it.findViewById(R.id.openFileUnselectButton)
            switchDecoderButton = it.findViewById(R.id.openFileSwitchDecoderButton)
            setupSwitchDecoderButton()
            playSelectedButton = it.findViewById(R.id.openFilePlaySelectedButton)
            addToFavoriteButton = it.findViewById(R.id.addToFavoriteButton)
            showVideoButton = it.findViewById(R.id.showVideoImageButton)
            showVideoButton?.visibility = View.VISIBLE
            exitImageButton = it.findViewById(R.id.exitImageButton)
            exitImageButton?.visibility = View.VISIBLE
        }
        initFilesRecyclerView()

        super.onViewCreated(view, savedInstanceState)
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

    override fun onItemClick(v: View?, position: Int) {
        LogUtil.i(TAG, "onItemClick.position = $position")
        if (position < 0) return
        v?.requestFocus()
        if (MySingleton.fileList[position].file.isFile) {
            MySingleton.fileList[position].selected = !MySingleton.fileList[position].selected
            myRecyclerViewAdapter?.myNotifyItemChanged(position)
            return
        }
        MySingleton.currentPath = MySingleton.fileList[position].file.path
        searchCurrentFolder()
    }

    fun clearFileList() {
        MySingleton.fileList.clear()
        myRecyclerViewAdapter?.myNotifyDataSetChanged()
    }

    fun searchCurrentFolder() {
        LogUtil.i(TAG, "searchCurrentFolder")
        searchCompleted = false
        lifecycleScope.launch(Dispatchers.IO) {
            val tempList: ArrayList<FileDescription> = ArrayList(MySingleton.MAX_FILES)
            MySingleton.currentPath.let {
                if (it == "/") {
                    for (element in MySingleton.rootPathSet) {
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
            MySingleton.fileList.clear()
            MySingleton.fileList.addAll(tempList)
            LogUtil.d(TAG, "searchCurrentFolder.FileDesList.fileList.size = ${MySingleton.fileList.size}")

            for (fileDes in MySingleton.fileList) {
                LogUtil.d(TAG, "searchCurrentFolder.file = ${fileDes.file}")
            }

            activity?.let {
                LocalBroadcastManager.getInstance(it).apply {
                    sendBroadcast(Intent().apply {
                        action = SEARCH_FOLDER_COMPLETED
                    })
                }
            }
        }
    }

    override fun setClickListeners() {
        backKeyButton?.setOnClickListener {
            if (!searchCompleted) return@setOnClickListener // searching
            if (MySingleton.currentPath == "/") return@setOnClickListener
            MySingleton.currentPath =
                if (MySingleton.rootPathSet.contains(MySingleton.currentPath)) "/"
                else {
                    val index = MySingleton.currentPath.lastIndexOf('/')
                    if (index >= 0 ) MySingleton.currentPath.substring(0, index) else "/"
                }
            if (MySingleton.currentPath.isEmpty()) MySingleton.currentPath = "/"
            searchCurrentFolder()
        }
        selectAllButton?.setOnClickListener {
            if (!searchCompleted) return@setOnClickListener // searching
            for (i in 0 until MySingleton.fileList.size) {
                MySingleton.fileList[i].run {
                    if (!file.isDirectory && !selected) {
                        selected = true
                        myRecyclerViewAdapter?.notifyItemChanged(i)
                    }
                }
            }
        }
        unselectButton?.setOnClickListener {
            if (!searchCompleted) return@setOnClickListener // searching
            for (i in 0 until MySingleton.fileList.size) {
                MySingleton.fileList[i].run {
                    if (!file.isDirectory && selected) {
                        selected = false
                        myRecyclerViewAdapter?.notifyItemChanged(i)
                    }
                }
            }
        }
        switchDecoderButton?.let {switchIt ->
            LogUtil.d(TAG, "setClickListeners.switchDecoderButton.searchCompleted = $searchCompleted")
            switchIt.setOnClickListener {
                if (!searchCompleted) return@setOnClickListener // searching
                playSongs?.switchBetweenSoftAndHardDecoder()
                setupSwitchDecoderButton()
            }
        }
        playSelectedButton?.setOnClickListener {
            if (!searchCompleted) return@setOnClickListener // searching
            // open the files to play
            startPlaySelectedSong(activity, "playSelectedButton")
        }
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
                            if (numRecords < MySingleton.MAX_SONGS) {
                                songListSQLite.addSongToSongList(song)
                            } else {
                                // excess max number of favorites
                                ScreenUtil.showToast(activity,
                                    getString(R.string.excess_max) +
                                            " ${MySingleton.MAX_SONGS}", textFontSize,
                                    Toast.LENGTH_SHORT)
                                break
                            }
                        }
                        toastMsg = getString(R.string.add_to_favorites)
                    }
                    ScreenUtil.showToast(activity, toastMsg, textFontSize,
                        Toast.LENGTH_SHORT)
                }
                songListSQLite.closeDatabase()
            }
        }

        super.setClickListeners()
    }

    override fun setButtonsSize() {
        buttonLayout = fragmentView?.findViewById(R.id.openFileButtonLayout)
        super.setButtonsSize()
        backKeyButton?.layoutParams = buttonParam
        selectAllButton?.layoutParams = buttonParam
        unselectButton?.layoutParams = buttonParam
        switchDecoderButton?.layoutParams = buttonParam
        playSelectedButton?.layoutParams = buttonParam
        addToFavoriteButton?.layoutParams = buttonParam
    }

    private fun initFilesRecyclerView() {
        LogUtil.i(TAG, "initFilesRecyclerView() is called")
        activity?.let {
            myRecyclerViewAdapter = OpenFilesRecyclerViewAdapter(
                this, MySingleton.fileList,
                textFontSize,
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