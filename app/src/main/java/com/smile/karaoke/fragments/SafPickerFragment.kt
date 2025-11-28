package com.smile.karaoke.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.scale
import com.smile.karaoke.R
import com.smile.karaoke.models.FileDescription
import com.smile.karaoke.models.MySingleTon
import com.smile.karaoke.utilities.ContentUriUtil
import com.smile.karaoke.utilities.LogUtil

class SafPickerFragment: CommonFragment() {

    companion object {
        private const val TAG = "SafPickerFragment"
    }

    private lateinit var openDocumentLauncher: ActivityResultLauncher<Intent>
    var fragmentView : View? = null
    var showVideoButton: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        openDocumentLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let {
                    val uriList = ContentUriUtil.getUrisList(
                        requireActivity(),
                        it
                    )
                    MySingleTon.fileList.clear()
                    for (uri in uriList) {
                        LogUtil.d(TAG, "openDocumentLauncher.result.uri = $uri")
                        val file = ContentUriUtil.getFileFromContentUri(activity, uri)
                        LogUtil.d(TAG, "openDocumentLauncher.result.file = $file")
                        if (file == null) continue
                        var bm: Bitmap? = null
                        LogUtil.d(TAG, "openDocumentLauncher.result.file.path = ${file.path}")
                        try {
                            mediaRetriever.setDataSource(file.path)
                            bm = mediaRetriever.getFrameAtTime(
                                0,
                                MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                            )
                                ?.scale(videoThumbnailsWidth, videoThumbnailsHeight)
                        } catch (ex: Exception) {
                            LogUtil.e(
                                TAG, "openDocumentLauncher.setDataSource.Exception:",
                                ex
                            )
                        }
                        MySingleTon.fileList.add(FileDescription(file, bm, true))
                    }
                    LogUtil.d(TAG, "openDocumentLauncher.size = ${MySingleTon.fileList.size}")
                    startPlaySelectedSong(activity, "openDocumentLauncher")
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
        super.onViewCreated(view, savedInstanceState)
        LogUtil.i(TAG, "onViewCreated")
        fragmentView = view

        val pickerButton: ImageView = view.findViewById(R.id.safPickerImageView)
        pickerButton.isClickable = true
        pickerButton.isFocusable = true
        pickerButton.setOnClickListener {
            val intent = ContentUriUtil.intentForSelectFile(false)
            openDocumentLauncher.launch(intent)
        }

        showVideoButton = view.findViewById(R.id.showVideoImageButton)
        showVideoButton?.isClickable = true
        showVideoButton?.isFocusable = true
        showVideoButton?.setOnClickListener {
            playSongs?.switchToPlayerView()
        }
        view.isFocusable = true
        view.isFocusableInTouchMode = true
        view.requestFocus()

        view.setOnKeyListener {
                _, keyCode, event ->
            /*
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP,
                KeyEvent.KEYCODE_DPAD_DOWN,
                KeyEvent.KEYCODE_DPAD_LEFT,
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    if (event?.action == KeyEvent.ACTION_DOWN) {
                        // D-pad move started
                        // Handle your logic here
                        // pickerButton.requestFocus()
                        return@setOnKeyListener true
                    }
                }
            }
            */
            pickerButton.requestFocus()
            return@setOnKeyListener false
        }
    }
}