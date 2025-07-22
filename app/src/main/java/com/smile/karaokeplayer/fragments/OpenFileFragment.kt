package com.smile.karaokeplayer.fragments

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smile.karaokeplayer.R
import com.smile.karaokeplayer.adapters.OpenFilesRecyclerViewAdapter
import com.smile.karaokeplayer.constants.CommonConstants
import com.smile.karaokeplayer.interfaces.PlaySongs
import com.smile.karaokeplayer.models.FileDescription
import com.smile.karaokeplayer.models.MySingleTon
import com.smile.karaokeplayer.models.SongInfo
import com.smile.karaokeplayer.models.SongListSQLite
import com.smile.smilelibraries.utilities.ScreenUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import androidx.core.graphics.scale

class OpenFileFragment : Fragment(),
    OpenFilesRecyclerViewAdapter.OnRecyclerItemClickListener {

    companion object {
        private const val TAG : String = "OpenFileFragment"
        private const val SEARCH_FOLDER_COMPLETED = "SearchCurrentFolder"
    }

    private var textFontSize = 0f
    private var playSongs: PlaySongs? = null
    private var pathTextView: TextView? = null
    private var filesRecyclerView : RecyclerView? = null
    private var myRecyclerViewAdapter : OpenFilesRecyclerViewAdapter? = null
    private var isPlayButton: Boolean = true
    private lateinit var broadcastReceiver: BroadcastReceiver
    private var searchCompleted = true
    private lateinit var mediaRetriever: MediaMetadataRetriever

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate() is called")
        super.onCreate(savedInstanceState)
        arguments?.let {
            isPlayButton = it.getBoolean(CommonConstants.IS_BUTTON_PLAY, true)
            Log.d(TAG, "onCreate.isPlayButton = $isPlayButton")
        }

        mediaRetriever = MediaMetadataRetriever()

        val defaultTextFontSize = ScreenUtil.getDefaultTextSizeFromTheme(activity,
            ScreenUtil.FontSize_Pixel_Type, null)
        textFontSize = ScreenUtil.suitableFontSize(activity,
            defaultTextFontSize,
            ScreenUtil.FontSize_Pixel_Type,0.0f)

        playSongs = (activity as PlaySongs)
        Log.d(TAG, "onCreate.playSongs = $playSongs")

        // FileDesList.currentPath = Environment.getExternalStorageDirectory().toString()
        Log.d(TAG, "onCreate.FileDesList.currentPath = ${MySingleTon.currentPath}")

        activity?.applicationContext?.externalCacheDirs?.let {
            Log.d(TAG, "externalCacheDirs = $it, externalCacheDirs.size = ${it.size}")
            MySingleTon.rootPathSet.clear()
            for (element in it) {
                Log.d(TAG, "externalCacheDirs.element = $element")
                element?.absolutePath?.let { pathIt ->
                    pathIt.indexOf("/Android/data").let {indexIt ->
                        if (indexIt >= 0) {
                            pathIt.substring(0, indexIt).let {subIt ->
                                Log.d(TAG, "element.substring(0, indexIt) = $subIt")
                                MySingleTon.rootPathSet.add(subIt)
                            }
                        }
                    }
                }
            }
        }

        object : BroadcastReceiver() {
            @SuppressLint("NotifyDataSetChanged")
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d(TAG, "BroadcastReceiver.onReceive")
                intent?.action?.let {
                    if (it == SEARCH_FOLDER_COMPLETED) {
                        Log.d(TAG, "BroadcastReceiver.onReceive.SearchFolder")
                        pathTextView?.text = MySingleTon.currentPath
                        myRecyclerViewAdapter?.notifyDataSetChanged()
                        searchCompleted = true  // searching thread finished
                    }
                }
            }
        }.also { broadcastReceiver = it }
        activity?.let {
            LocalBroadcastManager.getInstance(it).apply {
                Log.d(TAG, "LocalBroadcastManager.registerReceiver")
                registerReceiver(broadcastReceiver, IntentFilter().apply {
                    addAction(SEARCH_FOLDER_COMPLETED)
                })
            }
        }

        Log.d(TAG, "onCreate.FileDesList.fileList.size = ${MySingleTon.fileList.size}")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView() is called")
        return inflater.inflate(R.layout.fragment_open_file, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonWidth = (textFontSize*1.5f).toInt()
        view.let {
            filesRecyclerView = it.findViewById(R.id.openFilesRecyclerView)
            filesRecyclerView?.setHasFixedSize(true)
            pathTextView = it.findViewById(R.id.pathTextView)
            ScreenUtil.resizeTextSize(pathTextView, textFontSize,
                ScreenUtil.FontSize_Pixel_Type)
            val backKeyButton: ImageButton = it.findViewById(R.id.openFileBackKeyButton)
            var layoutParams: ViewGroup.MarginLayoutParams = backKeyButton.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.width = buttonWidth
            layoutParams.height = buttonWidth
            backKeyButton.setOnClickListener {
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
            val selectAllButton: ImageButton = it.findViewById(R.id.openFileSelectAllButton)
            layoutParams = selectAllButton.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.width = buttonWidth
            layoutParams.height = buttonWidth
            selectAllButton.setOnClickListener {
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
            val unselectButton: ImageButton = it.findViewById(R.id.openFileUnselectButton)
            layoutParams = unselectButton.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.width = buttonWidth
            layoutParams.height = buttonWidth
            unselectButton.setOnClickListener {
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
            val refreshButton: ImageButton = it.findViewById(R.id.openFileRefreshButton)
            layoutParams = refreshButton.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.width = buttonWidth
            layoutParams.height = buttonWidth
            refreshButton.setOnClickListener {
                if (!searchCompleted) return@setOnClickListener // searching
                searchCurrentFolder()
            }
            val playSelectedButton: ImageButton = it.findViewById(R.id.openFilePlaySelectedButton)
            layoutParams = playSelectedButton.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.width = buttonWidth
            layoutParams.height = buttonWidth
            playSelectedButton.setImageResource(
                    if (isPlayButton) R.drawable.play_media_button_image else R.drawable.open_files)
            playSelectedButton.setOnClickListener {
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
            val addToFavoriteButton: ImageButton = it.findViewById(R.id.addToFavoriteButton)
            layoutParams = addToFavoriteButton.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.width = buttonWidth
            layoutParams.height = buttonWidth
            addToFavoriteButton.setOnClickListener {
                if (!searchCompleted) return@setOnClickListener // searching
                activity?.let {activityIt ->
                    val songListSQLite = SongListSQLite(activityIt)
                    getSongs(songListSQLite, "addToFavoriteButton").also { songsIt ->
                        var toastMsg = getString(R.string.noFilesSelectedString)
                        if (songsIt.isNotEmpty()) {
                            for (song in songsIt) {
                                song.included = "1"
                                val numRecords = songListSQLite.recordsOfPlayList()
                                Log.d(TAG, "addToFavoriteButton.recordsOfPlayList() = $numRecords")
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
            val showVideoButton: ImageButton = it.findViewById(R.id.showVideoImageButton)
            layoutParams = showVideoButton.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.width = buttonWidth
            layoutParams.height = buttonWidth
            showVideoButton.visibility = View.VISIBLE
            showVideoButton.setOnClickListener {
                if (!searchCompleted) return@setOnClickListener // searching
                playSongs?.switchToPlayerView()
            }
        }

        initFilesRecyclerView()
    }

    override fun onStart() {
        Log.d(TAG, "onStart()")
        super.onStart()
    }

    override fun onResume() {
        Log.d(TAG, "onResume()")
        super.onResume()
        searchCurrentFolder()   // has to be in onResume()
    }

    override fun onPause() {
        Log.d(TAG, "onPause()")
        super.onPause()
        clearFileList()
    }

    override fun onStop() {
        Log.d(TAG, "onStop()")
        super.onStop()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
        super.onDestroy()
        clearFileList()
        activity?.let {
            LocalBroadcastManager.getInstance(it).apply {
                unregisterReceiver(broadcastReceiver)
            }
        }
        mediaRetriever.release()
    }

    override fun onRecyclerItemClick(v: View?, position: Int) {
        Log.d(TAG, "onRecyclerItemClick.position = $position")
        if (position < 0) return
        if (MySingleTon.fileList[position].file.isFile) {
            MySingleTon.fileList[position].selected = !MySingleTon.fileList[position].selected
            myRecyclerViewAdapter?.notifyItemChanged(position)
            return
        }
        MySingleTon.currentPath = MySingleTon.fileList[position].file.path
        searchCurrentFolder()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearFileList() {
        MySingleTon.fileList.clear()
        myRecyclerViewAdapter?.notifyDataSetChanged()
    }

    fun searchCurrentFolder() {
        Log.d(TAG, "searchCurrentFolder")
        searchCompleted = false
        lifecycleScope.launch(Dispatchers.IO) {
            val tempList: ArrayList<FileDescription> = ArrayList(MySingleTon.maxFiles)
            MySingleTon.currentPath.let {
                if (it == "/") {
                    for (element in MySingleTon.rootPathSet) {
                        Log.d(TAG, "searchCurrentFolder.element = $element")
                        tempList.add(FileDescription(File(element),
                            null, false))
                    }
                } else {
                    val imageWidth = (textFontSize * 3.0f).toInt()
                    val imageHeight = (textFontSize * 3.0f).toInt()
                    try {
                        File(it).listFiles()?.also { fIt ->
                            Log.d(TAG, "searchCurrentFolder.file.list().size() = ${fIt.size}")
                            for (f in fIt) {
                                Log.d(TAG, "searchCurrentFolder.isDirectory = ${f.isDirectory}, f.path = ${f.path}")
                                var bm: Bitmap? = null
                                if (!f.isDirectory) {
                                    try {
                                        mediaRetriever.setDataSource(f.path)
                                        bm = mediaRetriever.getFrameAtTime(0,
                                            MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                                            ?.scale(imageWidth, imageHeight)
                                    } catch (ex: Exception) {
                                        Log.e(TAG, "searchCurrentFolder.setDataSource.Exception:",
                                                ex)
                                    }
                                }
                                tempList.add(FileDescription(f, bm, false))
                            }
                        }
                    } catch (ex: Exception) {
                        Log.e(TAG, "searchCurrentFolder.Exception", ex )
                    }
                }
            }
            MySingleTon.fileList.clear()
            MySingleTon.fileList.addAll(tempList)
            Log.d(TAG, "searchCurrentFolder.FileDesList.fileList.size = ${MySingleTon.fileList.size}")

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
                        Log.d(TAG, "searchCurrentFolder.element = $element")
                        tempList.add(FileDescription(File(element), false))
                    }
                } else {
                    try {
                        File(it).listFiles()?.also { fIt ->
                            Log.d(TAG, "file.list().size() = ${fIt.size}")
                            for (f in fIt) {
                                Log.d(TAG, "isDirectory = ${f.isDirectory}, f.path = ${f.path}")
                                // if (f.canRead()) {
                                tempList.add(FileDescription(f, false))
                                // }
                            }
                        }
                    } catch (ex: Exception) {
                        Log.d(TAG, "${ex.message}")
                    }
                }
            }
            MySingleTon.fileList.clear()
            MySingleTon.fileList.addAll(tempList)
            Log.d(TAG, "searchCurrentFolder.FileDesList.fileList.size = ${MySingleTon.fileList.size}")

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

    private fun getSongs(songListSQLite : SongListSQLite, msg : String) : ArrayList<SongInfo> {
        val songs = ArrayList<SongInfo>().also {songIt ->
            var index = 0
            for (i in 0 until MySingleTon.fileList.size) {
                if (MySingleTon.fileList[i].selected) {
                    Log.d(TAG, "$msg.file.path = ${MySingleTon.fileList[i].file.path}")
                    Log.d(TAG, "$msg.file.toUri() = ${MySingleTon.fileList[i].file.toUri()}")
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
                        Log.d(TAG, "$msg.found")
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
        Log.d(TAG, "initFilesRecyclerView() is called")
        activity?.let {
            val tColor = ContextCompat.getColor(it, R.color.gnt_green)
            val transparentLightGray = ContextCompat.getColor(it,
                R.color.transparentLightGray)
            myRecyclerViewAdapter = OpenFilesRecyclerViewAdapter.getInstance(
                this, textFontSize, MySingleTon.fileList,
                tColor, transparentLightGray)
            filesRecyclerView?.adapter = myRecyclerViewAdapter
            filesRecyclerView?.layoutManager = object : LinearLayoutManager(context) {
                override fun isAutoMeasureEnabled(): Boolean {
                    return false
                }
            }
        }
    }
}