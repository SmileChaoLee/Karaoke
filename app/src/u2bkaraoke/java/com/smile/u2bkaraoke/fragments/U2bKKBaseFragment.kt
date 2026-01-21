package com.smile.u2bkaraoke.fragments

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Point
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.smile.karaoke.R
import com.smile.karaoke.interfaces.PlaySongs
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.u2bkaraoke.utilities.U2bKaOkUtil

abstract class U2bKKBaseFragment : Fragment() {

    companion object {
        private const val TAG : String = "U2bKKBaseFragment"
    }

    var textFontSize = 0.0f
    var screen = Point()
    var fragmentView : View? = null
    var fragContainerId = 0
    var mFragManager: FragmentManager? = null
    var showVideoButton: ImageButton? = null
    var exitImageButton: ImageButton? = null
    var playSongs: PlaySongs? = null
    var buttonParam: LinearLayout.LayoutParams? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        activity?.let {
            textFontSize = ScreenUtil.getPxTextFontSizeNeeded(it)
            screen = ScreenUtil.getScreenSize(it)
            if (it is PlaySongs) playSongs = it
            LogUtil.d(TAG, "onCreate.playSongs = $playSongs")
            fragContainerId = this.id   // container id of the fragment
            mFragManager = it.supportFragmentManager
        }
        LogUtil.d(TAG, "onCreate.finished")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "onViewCreated")
        super.onViewCreated(view, savedInstanceState)

        fragmentView = view
        view.apply {
            showVideoButton = findViewById(R.id.u2bKShowVideoButton)
            exitImageButton = findViewById(R.id.u2bKExitButton)
            isFocusable = true
            isFocusableInTouchMode = true
            requestFocus()
            setOnKeyListener {_, keyCode, event ->
                exitImageButton?.post { exitImageButton?.requestFocus() }
                return@setOnKeyListener false
            }
        }
        setClickListeners()
        setButtonsSize()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        LogUtil.i(TAG, "onConfigurationChanged")
        setButtonsSize()
        super.onConfigurationChanged(newConfig)
    }

    override fun onResume() {
        LogUtil.i(TAG, "onResume")
        super.onResume()
        exitImageButton?.post { exitImageButton?.requestFocus() }
    }

    override fun onPause() {
        LogUtil.i(TAG, "onPause")
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.i(TAG, "onDestroy")
    }

    open fun setClickListeners() {
        showVideoButton?.apply {
            setShowVideoButtonVisibility()
            setOnClickListener {
                playSongs?.switchToPlayerView()
            }
        }
        exitImageButton?.setOnClickListener {
            U2bKaOkUtil.returnToPrevious(activity)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    open fun setButtonsSize() {
        val buttonWidth = (textFontSize*1.5f).toInt()
        // val margin = ScreenUtil.dpToPixel(50f).toInt()
        showVideoButton?.let {
            buttonParam = it.layoutParams as LinearLayout.LayoutParams
            buttonParam?.apply {
                width = buttonWidth
                height = buttonWidth
                gravity = Gravity.CENTER
                setMargins(0, 0, 0, 0)
            }
            it.layoutParams = buttonParam
            exitImageButton?.layoutParams = buttonParam
        }
    }

    fun setShowVideoButtonVisibility() {
        val isVisible = playSongs?.isThereAnySongPlaying() ?: false
        showVideoButton?.visibility = if (isVisible) View.VISIBLE else View.GONE
    }
}