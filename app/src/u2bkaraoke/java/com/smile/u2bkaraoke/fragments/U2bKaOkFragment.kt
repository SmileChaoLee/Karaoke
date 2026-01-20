package com.smile.u2bkaraoke.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.smile.karaoke.R
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.u2bkaraoke.u2bkaok_constants.U2bKKConstants
import com.smile.u2bkaraoke.utilities.U2bKaOkUtil

class U2bKaOkFragment: U2bKKBaseFragment() {

    companion object {
        private const val TAG = "U2bKaOkFragment"
    }

    var singerOrderButton: Button? = null
    var newSongOrderButton: Button? = null
    var hotSongOrderButton: Button? = null
    var languageOrderButton: Button? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        LogUtil.i(TAG, "onCreateView")
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_u2bkaok,
            container, false)

        // commented out because moving to onViewCreated()
        // Make the root view focusable
        // Allows it to receive focus when touched
        // view.isFocusableInTouchMode = true
        // view.isFocusable = true
        // view.requestFocus()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "onViewCreated")
        // val fragContainerId = this.id   // container id of the fragment
        // val fragManager = activity?.supportFragmentManager
        view.apply {
            singerOrderButton = findViewById(R.id.singerOrderButton)
            ScreenUtil.resizeTextSize(singerOrderButton, textFontSize)
            newSongOrderButton = findViewById(R.id.newSongOrderButton)
            ScreenUtil.resizeTextSize(newSongOrderButton, textFontSize)
            hotSongOrderButton = findViewById(R.id.hotSongOrderButton)
            ScreenUtil.resizeTextSize(hotSongOrderButton, textFontSize)
            languageOrderButton = findViewById(R.id.languageOrderButton)
            ScreenUtil.resizeTextSize(languageOrderButton, textFontSize)
        }

        super.onViewCreated(view, savedInstanceState)
        exitImageButton?.nextFocusUpId = R.id.languageOrderButton
        showVideoButton?.nextFocusUpId = R.id.languageOrderButton
    }

    override fun onResume() {
        LogUtil.i(TAG, "onResume")
        super.onResume()
    }

    override fun onPause() {
        LogUtil.i(TAG, "onPause")
        super.onPause()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        LogUtil.i(TAG, "onConfigurationChanged")
        super.onConfigurationChanged(newConfig)
    }

    override fun onDestroy() {
        LogUtil.i(TAG, "onDestroy")
        super.onDestroy()
    }

    override fun setClickListeners() {
        super.setClickListeners()

        exitImageButton?.setOnClickListener {
            activity?.finish()
        }
        singerOrderButton?.setOnClickListener {
            mFragManager?.let { fm ->
                val nFragment = SingerTyListFragment()
                U2bKaOkUtil.beginTransaction(fm, fragContainerId, nFragment)
            }
        }
        newSongOrderButton?.setOnClickListener {
            mFragManager?.let { fm ->
                val nFragment = LangListFragment().apply {
                    arguments = Bundle().apply {
                        putInt(U2bKKConstants.OrderedFrom, U2bKKConstants.NewSongOrdered)
                    }
                }
                U2bKaOkUtil.beginTransaction(fm, fragContainerId, nFragment)
            }
        }
        hotSongOrderButton?.setOnClickListener {
            mFragManager?.let { fm ->
                val nFragment = LangListFragment().apply {
                    arguments = Bundle().apply {
                        putInt(U2bKKConstants.OrderedFrom, U2bKKConstants.HotSongOrdered)
                    }
                }
                U2bKaOkUtil.beginTransaction(fm, fragContainerId, nFragment)
            }
        }
        languageOrderButton?.setOnClickListener {
            mFragManager?.let { fm ->
                val nFragment = LangListFragment()
                U2bKaOkUtil.beginTransaction(fm, fragContainerId, nFragment)
            }
        }
    }
}