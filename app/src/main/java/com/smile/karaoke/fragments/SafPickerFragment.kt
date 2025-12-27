package com.smile.karaoke.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.scale
import androidx.lifecycle.lifecycleScope
import com.smile.karaoke.R
import com.smile.karaoke.interfaces.PlayMyFavorites
import com.smile.karaoke.models.FileDescription
import com.smile.karaoke.models.MySingleton
import com.smile.karaoke.utilities.ContentUriUtil
import com.smile.karaoke.utilities.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SafPickerFragment: ComOpenFragment() {

    companion object {
        private const val TAG = "SafPickerFragment"
    }

    private var playMyFavorites: PlayMyFavorites? = null
    private lateinit var openDocumentLauncher: ActivityResultLauncher<Intent>
    private var pickerButton: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        activity?.let {
            if (it is PlayMyFavorites) playMyFavorites = it
            LogUtil.d(TAG, "onCreate.playMyFavorites = $playMyFavorites")
        }
        openDocumentLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            playMyFavorites?.restorePlayingState()
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let {
                    val uriList = ContentUriUtil.getUrisList(requireActivity(),it)
                    MySingleton.fileList.clear()
                    // convert uriList to MySingleton.fileList
                    val dirBm = BitmapFactory.decodeResource(resources, R.drawable.folder_open_icon)
                    for (uri in uriList) {
                        LogUtil.d(TAG, "openDocumentLauncher.result.uri = $uri")
                        val file = ContentUriUtil.getFileFromContentUri(activity, uri)
                        LogUtil.d(TAG, "openDocumentLauncher.result.file = $file")
                        if (file == null) continue
                        var bm: Bitmap? = null
                        LogUtil.d(TAG, "openDocumentLauncher.result.file.path = ${file.path}")
                        try {
                            mediaRetriever.setDataSource(file.path)
                            bm = mediaRetriever.getFrameAtTime(0,
                                MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                        } catch (ex: Exception) {
                            LogUtil.e(TAG, "openDocumentLauncher.setDataSource.Exception:",ex)
                        }
                        if (bm == null) bm = dirBm
                        bm = bm?.scale(videoThumbnailsWidth, videoThumbnailsHeight)
                        MySingleton.fileList.add(FileDescription(file, bm, true))
                    }
                    LogUtil.d(TAG, "openDocumentLauncher.size = ${MySingleton.fileList.size}")
                    // play the selected songs later because the activity life cycle
                    // BaseActivity will be coming back from invisible and similar to
                    // coming back from background
                    lifecycleScope.launch(Dispatchers.Main) {
                        startPlaySelectedSong(activity)
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        LogUtil.i(TAG, "onCreateView")
        return inflater.inflate(R.layout.fragment_saf_picker,
            container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "onViewCreated")

        view.let {
            pickerButton = it.findViewById(R.id.safPickerImageView)
            showVideoButton = it.findViewById(R.id.showVideoImageButton)
            exitImageButton = it.findViewById(R.id.exitImageButton)
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        LogUtil.i(TAG, "onResume")
        pickerButton?.post {  pickerButton?.requestFocus() }
    }

    override fun setClickListeners() {
        pickerButton?.setOnClickListener {
            val intent = ContentUriUtil.intentForSelectFile(false)
            playMyFavorites?.onSavePlayingState(intent.component)
            openDocumentLauncher.launch(intent)
        }
        super.setClickListeners()
    }

    override fun setButtonsSize() {
        LogUtil.i(TAG, "setButtonsSize")
        // do nothing, just follow the xml view file
    }
}