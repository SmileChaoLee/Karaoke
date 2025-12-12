package com.smile.karaoke.fragments

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
import androidx.recyclerview.widget.RecyclerView
import com.smile.karaoke.R
import com.smile.karaoke.adapters.MyLinearLayoutManager
import com.smile.karaoke.adapters.OpenFilesRecyclerViewAdapter
import com.smile.karaoke.constants.CommonConstants
import com.smile.karaoke.interfaces.RecyclerItemListener
import com.smile.karaoke.models.FileDescription
import com.smile.karaoke.models.MySingleton
import com.smile.karaoke.roomdatabase.FavSongDatabase
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class OpenFileFragment : ComOpenFragment(), RecyclerItemListener {

    companion object {
        private const val TAG : String = "OpenFileFragment"
    }

    private var pathTextView: TextView? = null
    private var filesRecyclerView : RecyclerView? = null
    private var loadingMsgTextView: TextView? = null
    private var myRecyclerViewAdapter : OpenFilesRecyclerViewAdapter? = null
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
        LogUtil.i(TAG, "onCreate.FileDesList.fileList.size = ${MySingleton.fileList.size}")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        LogUtil.i(TAG, "onCreateView")
        return inflater.inflate(R.layout.fragment_open_file,
            container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "onViewCreated")
        view.let {
            filesRecyclerView = it.findViewById(R.id.openFilesRecyclerView)
            filesRecyclerView?.setHasFixedSize(true)
            filesRecyclerView?.visibility = View.GONE
            loadingMsgTextView = it.findViewById(R.id.loadingMsgTextView)
            ScreenUtil.resizeTextSize(loadingMsgTextView, textFontSize * 2f)
            loadingMsgTextView?.visibility = View.GONE
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
        setupSwitchDecoderButton()
        searchCurrentFolder()   // has to be in onResume()
    }

    override fun onResume() {
        super.onResume()
        LogUtil.i(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        LogUtil.i(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        LogUtil.i(TAG, "onStop")
        clearFileList()
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
        filesRecyclerView?.visibility = View.GONE
    }

    fun searchCurrentFolder() {
        val logStr = "searchCurrentFolder"
        LogUtil.i(TAG, logStr)
        searchCompleted = false
        filesRecyclerView?.visibility = View.GONE
        loadingMsgTextView?.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            val tempList: ArrayList<FileDescription> = ArrayList(MySingleton.MAX_FILES)
            MySingleton.currentPath.let {
                if (it == "/") {
                    for (element in MySingleton.rootPathSet) {
                        LogUtil.d(TAG, "$logStr.element = $element")
                        tempList.add(FileDescription(File(element),
                            null, false))
                    }
                } else {
                    try {
                        File(it).listFiles()?.also { fIt ->
                            LogUtil.d(TAG, "$logStr.file.list().size() = ${fIt.size}")
                            for (f in fIt) {
                                LogUtil.d(TAG, "$logStr.isDirectory = ${f.isDirectory}, f.path = ${f.path}")
                                var bm: Bitmap? = null
                                if (!f.isDirectory) {
                                    try {
                                        mediaRetriever.setDataSource(f.path)
                                        bm = mediaRetriever.getFrameAtTime(0,
                                            MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                                            ?.scale(videoThumbnailsWidth, videoThumbnailsHeight)
                                    } catch (ex: Exception) {
                                        LogUtil.e(TAG, "$logStr.setDataSource.Exception:",
                                                ex)
                                    }
                                }
                                tempList.add(FileDescription(f, bm, false))
                            }
                        }
                    } catch (ex: Exception) {
                        LogUtil.e(TAG, "$logStr.Exception", ex )
                    }
                }
            }
            MySingleton.fileList.clear()
            MySingleton.fileList.addAll(tempList)
            LogUtil.d(TAG, "$logStr.FileDesList.fileList.size = ${MySingleton.fileList.size}")

            // Update the UI
            withContext(Dispatchers.Main) {
                pathTextView?.text = MySingleton.currentPath
                myRecyclerViewAdapter?.myNotifyDataSetChanged()
                filesRecyclerView?.visibility = View.VISIBLE
                loadingMsgTextView?.visibility = View.GONE
                if (MySingleton.fileList.isEmpty()) {
                    LogUtil.d(TAG, "$logStr.MySingleTon.fileList is empty")
                    filesRecyclerView?.visibility = View.GONE
                    val keyEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)
                    val isKeyDown: Boolean? = fragmentView?.dispatchKeyEvent(keyEvent)
                    LogUtil.d(TAG, "$logStr.isKeyDown = $isKeyDown")
                    backKeyButton?.requestFocus()
                }
                searchCompleted = true  // searching thread finished
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
            lifecycleScope.launch(Dispatchers.Main) {
                // open the files to play
                startPlaySelectedSong(activity, "playSelectedButton")
            }
        }
        addToFavoriteButton?.setOnClickListener {
            if (!searchCompleted) return@setOnClickListener // searching
            val act = activity ?: return@setOnClickListener
            lifecycleScope.launch(Dispatchers.IO) {
                val db = FavSongDatabase.getDatabase(act,
                    CommonConstants.FAVORITE_DB_NAME)
                getSongs(db, "addToFavoriteButton").also { songsIt ->
                    var toastMsg = getString(R.string.noFilesSelectedString)
                    if (songsIt.isNotEmpty()) {
                        for (song in songsIt) {
                            song.included = "1"
                            val numRecords = db.recordsOfPlayList()
                            LogUtil.d(TAG, "addToFavoriteButton.recordsOfPlayList() = $numRecords")
                            if (numRecords < MySingleton.MAX_SONGS) {
                                db.addSongToSongList(song)
                            } else {
                                // excess max number of favorites
                                withContext(Dispatchers.Main) {
                                    ScreenUtil.showToast(
                                        activity,
                                        getString(R.string.excess_max) +
                                                " ${MySingleton.MAX_SONGS}", textFontSize,
                                        Toast.LENGTH_SHORT
                                    )
                                }
                                break
                            }
                        }
                        toastMsg = getString(R.string.add_to_favorites)
                    }
                    withContext(Dispatchers.Main) {
                        ScreenUtil.showToast(
                            activity, toastMsg, textFontSize,
                            Toast.LENGTH_SHORT
                        )
                    }
                }
                db.close()
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