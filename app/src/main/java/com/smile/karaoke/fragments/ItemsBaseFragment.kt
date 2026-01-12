package com.smile.karaoke.fragments

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.smile.karaoke.interfaces.PlaySongs
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil

abstract class ItemsBaseFragment : Fragment() {

    companion object {
        private const val TAG : String = "ItemsBaseFragment"
    }

    abstract fun gridSpanCount(): Int

    var searchCompleted = true
    var fragmentView : View? = null
    var textFontSize = 0.0f
    var videoThumbnailsWidth = 0
    var videoThumbnailsHeight = 0
    var buttonLayout: LinearLayout? = null
    var buttonLayoutWidthPercent = 1.0f
    var showVideoButton: ImageButton? = null
    var exitImageButton: ImageButton? = null
    var buttonWidth = 0
    lateinit var buttonParam: LinearLayout.LayoutParams
    lateinit var mediaRetriever: MediaMetadataRetriever
    var playSongs: PlaySongs? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        arguments?.let { }
        activity?.let {
            textFontSize = ScreenUtil.getPxTextFontSizeNeeded(it)
            val screen = ScreenUtil.getScreenSize(it)
            val aWidth = screen.x.toFloat() / gridSpanCount().toFloat()
            videoThumbnailsWidth = aWidth.toInt()
            videoThumbnailsHeight = videoThumbnailsWidth
            if (it is PlaySongs) playSongs = it
            LogUtil.d(TAG, "onCreate.playSongs = $playSongs")
        }
        mediaRetriever = MediaMetadataRetriever()

        LogUtil.d(TAG, "onCreate.finished")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "onViewCreated")
        super.onViewCreated(view, savedInstanceState)
        fragmentView = view

        view.isFocusable = true
        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener {
                _, keyCode, event ->
            showVideoButton?.post { showVideoButton?.requestFocus() }
            return@setOnKeyListener false
        }

        setClickListeners()
        setButtonsSize()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        LogUtil.i(TAG, "onConfigurationChanged")
        setButtonsSize()
        super.onConfigurationChanged(newConfig)
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.i(TAG, "onDestroy")
        mediaRetriever.release()
    }

    open fun setClickListeners() {
        showVideoButton?.setOnClickListener {
            if (!searchCompleted) return@setOnClickListener // searching
            playSongs?.switchToPlayerView()
        }

        exitImageButton?.setOnClickListener {
            playSongs?.returnToPrevious()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    open fun setButtonsSize() {
        buttonWidth = (textFontSize*1.5f).toInt()
        buttonLayoutWidthPercent = 1.0f
        if (resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
            buttonLayoutWidthPercent = 0.6f
        }
        buttonLayout?.let {
            val constrainParam = it.layoutParams as ConstraintLayout.LayoutParams
            constrainParam.constrainedWidth = true
            constrainParam.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            constrainParam.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            constrainParam.matchConstraintPercentWidth = buttonLayoutWidthPercent
        }
        buttonParam = LinearLayout.LayoutParams(buttonWidth, buttonWidth)
        buttonParam.setMargins(0, 0, 0, 0)
        buttonParam.weight = 1.0f
        buttonParam.gravity = Gravity.CENTER
        showVideoButton?.let {
            buttonParam = it.layoutParams as LinearLayout.LayoutParams
            buttonParam.width = buttonWidth
            buttonParam.height = buttonWidth
            buttonParam.setMargins(0, 0, 0, 0)
            exitImageButton?.layoutParams = buttonParam
        }
    }
}