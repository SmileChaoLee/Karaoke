package com.smile.karaoke.viewmodels

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smile.karaoke.R
import com.smile.karaoke.models.FileDescription
import com.smile.karaoke.models.MySingleton
import com.smile.karaoke.ui_states.OpenFileUiState
import com.smile.karaoke.utilities.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.LinkedList
import java.util.Queue

class OpenFileViewModel: ViewModel() {
    companion object {
        private const val TAG = "OpenFileViewModel"
    }

    val _uiState = MutableStateFlow<OpenFileUiState>(OpenFileUiState.Initial)
    val uiState = _uiState.asStateFlow()
    var searchJob: Job? = null

    val mediaRetriever = MediaMetadataRetriever()
    val fileList = ArrayList<FileDescription>()

    fun searchFiles(activity: Activity?, content: String) {
        val logStr = "searchFiles"
        val act = activity ?: run {
            LogUtil.d(TAG, "$logStr.activity is null")
            return
        }
        viewModelScope.launch {
            searchJob?.cancelAndJoin()
            LogUtil.w(TAG, "$logStr.searchJob cancelled and joined")
            searchJob = launch(Dispatchers.IO) {
                fileList.clear()
                _uiState.update {
                    OpenFileUiState.StartLoading
                }
                val pathQueue: Queue<String> = LinkedList(MySingleton.rootPathSet)
                while (pathQueue.isNotEmpty()) {
                    val path = pathQueue.poll() ?: continue
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
                }
                LogUtil.w(TAG, "$logStr.fileList.size = ${fileList.size}")
                // _uiState.emit(OpenFileUiState.FinishLoading(fileList))
                //or
                // _uiState.value = OpenFileUiState.FinishLoading(fileList)
                // or
                _uiState.update {
                    OpenFileUiState.FinishLoading(fileList)
                }
            }
        }
    }
}