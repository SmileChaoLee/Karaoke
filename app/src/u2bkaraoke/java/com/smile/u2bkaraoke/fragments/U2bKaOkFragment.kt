package com.smile.u2bkaraoke.fragments

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.smile.karaoke.R
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.u2bkaraoke.LanguageListActivity
import com.smile.u2bkaraoke.model.Constants
import com.smile.u2bkaraoke.u2bkaok_constants.U2bKaOkConstants

class U2bKaOkFragment: Fragment() {

    companion object {
        private const val TAG = "U2bKaOkFragment"
    }

    var singerOrderButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        LogUtil.i(TAG, "onCreateView")
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.activity_u2bkaok,
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
        val textFontSize = ScreenUtil.getPxTextFontSizeNeeded(activity)
        super.onViewCreated(view, savedInstanceState)

        val fragContainerId = this.id   // container id of the fragment
        val fragManager = activity?.supportFragmentManager
        view.apply {
            val mainMenuTextView = findViewById<TextView>(R.id.mainMenuTextView)
            ScreenUtil.resizeTextSize(mainMenuTextView, textFontSize)

            singerOrderButton = findViewById(R.id.singerOrderButton)
            ScreenUtil.resizeTextSize(singerOrderButton, textFontSize)
            singerOrderButton?.setOnClickListener {
                // val singerTypesIntent = Intent(activity,SingerTypeListActivity::class.java)
                // startActivity(singerTypesIntent)
                fragManager?.let { fm ->
                    val nFragment = SingerTyListFragment()
                    fm.beginTransaction().apply {
                        replace(fragContainerId, nFragment)
                        addToBackStack(null)
                        commit()
                    }
                }
            }

            val newSongOrderButton = findViewById<Button>(R.id.newSongOrderButton)
            ScreenUtil.resizeTextSize(newSongOrderButton, textFontSize)
            newSongOrderButton.setOnClickListener {
                val languagesIntent = Intent(activity,LanguageListActivity::class.java)
                languagesIntent.putExtra(Constants.OrderedFrom, Constants.NewSongOrdered)
                startActivity(languagesIntent)
            }

            val hotSongOrderButton = findViewById<Button>(R.id.hotSongOrderButton)
            ScreenUtil.resizeTextSize(hotSongOrderButton, textFontSize)
            hotSongOrderButton.setOnClickListener {
                val languagesIntent = Intent(activity,LanguageListActivity::class.java)
                languagesIntent.putExtra(Constants.OrderedFrom, Constants.HotSongOrdered)
                startActivity(languagesIntent)
            }

            val languageOrderButton = findViewById<Button>(R.id.languageOrderButton)
            ScreenUtil.resizeTextSize(languageOrderButton, textFontSize)
            languageOrderButton.setOnClickListener {
                val intentLanguageOrder = Intent(activity,LanguageListActivity::class.java)
                startActivity(intentLanguageOrder)
            }

            val exitProgramButton = findViewById<Button>(R.id.exitProgramButton)
            ScreenUtil.resizeTextSize(exitProgramButton, textFontSize)
            exitProgramButton.setOnClickListener { activity?.finish() }
        }
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
}