package com.smile.karaoke.viewmodels

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import androidx.core.graphics.scale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smile.karaoke.R
import com.smile.karaoke.constants.CommonConstants
import com.smile.karaoke.interfaces.PlaySongs
import com.smile.karaoke.models.FileDescription
import com.smile.karaoke.models.MySingleton
import com.smile.karaoke.models.SongInfo
import com.smile.karaoke.ui_intents.OpenFileUiIntent
import com.smile.karaoke.ui_states.OpenFileUiState
import com.smile.karaoke.utilities.CommonUtil
import com.smile.karaoke.utilities.DatabaseUtil
import com.smile.karaoke.utilities.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.LinkedList
import java.util.Queue

class OpenFileViewModel: ViewModel() {
    companion object {
        private const val TAG = "OpenFileViewModel"
    }

    private val _uiState = MutableStateFlow<OpenFileUiState>(OpenFileUiState.Initial)
    val uiState = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private val mediaRetriever = MediaMetadataRetriever()
    private val selectedSongs : ArrayList<SongInfo> = ArrayList()

    fun handleIntent(intent: OpenFileUiIntent) {
        when (intent) {
            is OpenFileUiIntent.SearchFiles -> searchFiles(intent.activity, intent.content)
            is OpenFileUiIntent.SearchCurrentFolder -> searchCurrentFolder(
                intent.activity,
                intent.videoThumbNailsWidth,
                intent.videoThumbNailsHeight
            )
            is OpenFileUiIntent.SongOnClicked -> songOnClicked(
                intent.position,
                intent.activity,
                intent.videoThumbNailsWidth,
                intent.videoThumbNailsHeight
            )
            is OpenFileUiIntent.AddToFavorites -> addToFavorites(intent.activity)
            is OpenFileUiIntent.ClearSelectedSongs -> {
                selectedSongs.clear()
                _uiState.update {
                    OpenFileUiState.FinishLoading
                }
            }
            is OpenFileUiIntent.StartPlaySelectedSong -> startPlaySelectedSong(
                intent.activity,
                intent.playSongs,
            )
        }
    }

    private fun searchFiles(activity: Activity?, content: String) {
        val logStr = "searchFiles"
        val act = activity ?: run {
            LogUtil.d(TAG, "$logStr.activity is null")
            return
        }
        viewModelScope.launch {
            searchJob?.cancelAndJoin()
            LogUtil.w(TAG, "$logStr.searchJob cancelled and joined")
            searchJob = launch(Dispatchers.IO) {
                val fileList = ArrayList<FileDescription>()
                _uiState.update {
                    OpenFileUiState.StartLoading
                }
                val pathQueue: Queue<String> = LinkedList(MySingleton.rootPathSet)
                while (pathQueue.isNotEmpty()) {
                    val path = pathQueue.poll() ?: continue
                    try {
                        val fList = File(path).listFiles()
                        fList?.let { fIt ->
                            for (f in fIt) {
                                if (f.isDirectory) {
                                    // LogUtil.d(TAG, "$logStr.isDirectory")
                                    pathQueue.add(f.path)
                                } else {
                                    var bm: Bitmap? = null
                                    val fileBm = BitmapFactory.decodeResource(act.resources, R.drawable.video_image)
                                    // LogUtil.d(TAG, "$logStr.content = $content")
                                    // LogUtil.w(TAG, "$logStr.f.name = ${f.name}")
                                    if (f.name.contains(content, ignoreCase = true)) {
                                        LogUtil.d(TAG, "$logStr.found file: ${f.path}")
                                        try {
                                            mediaRetriever.setDataSource(f.path)
                                            bm = mediaRetriever.getFrameAtTime(0,
                                                MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                                        } catch (ex: Exception) {
                                            LogUtil.w(TAG, "$logStr.setDataSource.Exception:", ex)
                                        }
                                        if (bm == null) bm = fileBm
                                        // add to fileList
                                        fileList.add(FileDescription(f, bm, false))
                                        if (fileList.size >= MySingleton.MAX_FILES) {
                                            _uiState.update {
                                                OpenFileUiState.ShowToast(OpenFileUiState.EXCESS_MAX)
                                            }
                                            delay(200)
                                            break
                                        }
                                    }
                                }
                            }
                        }
                    } catch (ex: Exception) {
                        LogUtil.w(TAG, "$logStr.Exception", ex)
                    }
                }
                LogUtil.w(TAG, "$logStr.fileList.size = ${fileList.size}")
                MySingleton.fileList.clear()
                MySingleton.fileList.addAll(fileList)
                // _uiState.emit(OpenFileUiState.FinishLoading(fileList))
                //or
                // _uiState.value = OpenFileUiState.FinishLoading(fileList)
                // or
                _uiState.update {
                    OpenFileUiState.FinishLoading
                }
                selectedSongs.clear()
            }
        }
    }

    private fun searchCurrentFolder(
        activity: Activity?,
        videoThumbNailsWidth: Int,
        videoThumbNailsHeight: Int
    ) {
        val logStr = "searchCurrentFolder"
        LogUtil.d(TAG, logStr)
        val act = activity ?: run {
            LogUtil.d(TAG, "$logStr.activity is null")
            return
        }
        // LogUtil.d(TAG, "$logStr.calling viewModelScope.launch")
        viewModelScope.launch {
            // LogUtil.d(TAG, "$logStr.viewModelScope.cancelAndJoin()")
            searchJob?.cancelAndJoin()
            // LogUtil.d(TAG, "$logStr.viewModelScope.cancelled and joined")
            searchJob = launch(Dispatchers.IO) {
                val fileList = ArrayList<FileDescription>()
                // LogUtil.d(TAG, "$logStr.viewModelScope.OpenFileUiState.StartLoading")
                _uiState.update {
                    OpenFileUiState.StartLoading
                }
                LogUtil.d(TAG, "$logStr.viewModelScope.MySingleton.currentPath = ${MySingleton.currentPath}")
                MySingleton.currentPath.let {
                    var index = 0
                    val dirBm = BitmapFactory.decodeResource(act.resources, R.drawable.folder_open_icon)
                    if (it == "/") {
                        val bm = dirBm?.scale(videoThumbNailsWidth, videoThumbNailsHeight)
                        for (element in MySingleton.rootPathSet) {
                            LogUtil.d(TAG, "$logStr.element = $element")
                            fileList.add(FileDescription(File(element), bm, false))
                            index++
                            if (index >= MySingleton.MAX_FILES) {
                                // excess the max
                                _uiState.update {
                                    OpenFileUiState.ShowToast(OpenFileUiState.EXCESS_MAX)
                                }
                                delay(200)
                                break
                            }
                        }
                    } else {
                        try {
                            val fileBm = BitmapFactory.decodeResource(act.resources, R.drawable.video_image)
                            File(it).listFiles()?.also { fIt ->
                                // LogUtil.d(TAG, "$logStr.file.list().size() = ${fIt.size}")
                                for (f in fIt) {
                                    // LogUtil.d(TAG, "$logStr.isDirectory = ${f.isDirectory}, f.path = ${f.path}")
                                    var bm: Bitmap? = null
                                    if (!f.isDirectory) {
                                        try {
                                            mediaRetriever.setDataSource(f.path)
                                            bm = mediaRetriever.getFrameAtTime(0,
                                                MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                                        } catch (ex: Exception) {
                                            LogUtil.w(TAG, "$logStr.setDataSource.Exception:", ex)
                                        }
                                        if (bm == null) bm = fileBm
                                    } else {
                                        bm = dirBm
                                    }
                                    bm = bm?.scale(videoThumbNailsWidth, videoThumbNailsHeight)
                                    fileList.add(FileDescription(f, bm, false))
                                    index++
                                    if (index >= MySingleton.MAX_FILES) {
                                        // excess the max
                                        _uiState.update {
                                            OpenFileUiState.ShowToast(OpenFileUiState.EXCESS_MAX)
                                        }
                                        delay(200)
                                        break
                                    }
                                }
                            }
                        } catch (ex: Exception) {
                            LogUtil.w(TAG, "$logStr.Exception", ex )
                        }
                    }
                }
                ensureActive() // 👈 Final sanity check before modifying global singleton state
                MySingleton.fileList.clear()
                MySingleton.fileList.addAll(fileList)
                _uiState.update {
                    OpenFileUiState.FinishLoading
                }
            }
        }
    }

    private fun songOnClicked(
        position: Int,
        activity: Activity?,
        videoThumbNailsWidth: Int,
        videoThumbNailsHeight: Int) {
        val logStr = "songOnClicked"
        LogUtil.d(TAG, logStr)
        val fileDes = MySingleton.fileList[position]
        if (fileDes.file.isFile) {
            var isUpdated = false
            if (fileDes.selected) {
                selectedSongs.remove(CommonUtil.fileDescriptionToSongInfo(fileDes))
                fileDes.selected = false
                isUpdated = true
            } else {
                if (selectedSongs.size >= MySingleton.MAX_SONGS) {
                    _uiState.update {
                        OpenFileUiState.ShowToast(OpenFileUiState.EXCESS_MAX)
                    }
                } else {
                    selectedSongs.add(CommonUtil.fileDescriptionToSongInfo(fileDes))
                    fileDes.selected = true
                    isUpdated = true
                }
            }
            if (isUpdated) {
                _uiState.update {
                    OpenFileUiState.UpdateSelectedSong(position = position)
                }
            }
        } else {
            LogUtil.d(TAG, "$logStr.fileDes.file is not file")
            MySingleton.currentPath = fileDes.file.path
            searchCurrentFolder(
                activity, videoThumbNailsWidth, videoThumbNailsHeight
            )
        }
    }

    private fun addToFavorites(activity: Activity?) {
        val logStr = "addToFavorites"
        LogUtil.d(TAG, logStr)
        val act = activity ?: run {
            LogUtil.d(TAG, "$logStr.activity is null")
            return
        }
        if (selectedSongs.isEmpty()) {
            _uiState.update {
                OpenFileUiState.ShowToast(OpenFileUiState.NO_FILES_SELECTED)
            }
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                LogUtil.d(TAG, "$logStr.viewModelScope.launch")
                if (DatabaseUtil.addSongsToFavorites(act,
                        CommonConstants.FAVORITE_DB_NAME, selectedSongs)) {
                    LogUtil.d(TAG, "$logStr.viewModelScope.launch.addSongsToFavorites success")
                    _uiState.update {
                        OpenFileUiState.ShowToast(OpenFileUiState.ADD_TO_FAVORITES)
                    }
                }
            }
        }
    }

    private fun startPlaySelectedSong(
        activity: Activity?,
        playSongs: PlaySongs?,
    ) {
        val logStr = "startPlaySelectedSong"
        LogUtil.d(TAG, logStr)
        val act = activity ?: run {
            LogUtil.d(TAG, "$logStr.activity is null")
            return
        }
        if (selectedSongs.isEmpty()) {
            _uiState.update {
                OpenFileUiState.ShowToast(OpenFileUiState.NO_FILES_SELECTED)
            }
        } else {
            // Check if song is in database
            viewModelScope.launch(Dispatchers.IO) {
                val vSongs = ArrayList(selectedSongs.take(MySingleton.MAX_SONGS))
                DatabaseUtil.getSongsToPlay(
                    act,
                    CommonConstants.FAVORITE_DB_NAME, vSongs
                )
                withContext(Dispatchers.Main) {
                    playSongs?.playSelectedSongList(vSongs)
                }
            }
        }
    }
}