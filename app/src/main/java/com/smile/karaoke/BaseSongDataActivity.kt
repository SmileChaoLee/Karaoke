package com.smile.karaoke

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.smile.karaoke.adapters.SpinnerAdapter
import com.smile.karaoke.constants.CommonConstants
import com.smile.karaoke.constants.MyPlayerConstants
import com.smile.karaoke.models.SongInfo
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil

class BaseSongDataActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "BaseSongDataActivity"
    }

    private var textFontSize = 0f
    private var toastTextSize = 0f
    private var titleNameEditText: EditText? = null
    private var filePathEditText: EditText? = null
    private var musicTrackSpinner: Spinner? = null
    private var musicChannelSpinner: Spinner? = null
    private var vocalTrackSpinner: Spinner? = null
    private var vocalChannelSpinner: Spinner? = null
    private var includedPlaylistCheckBox: CheckBox? = null
    private var karaokeSettingLayout: LinearLayout? = null
    private lateinit var mSongInfo: SongInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogUtil.d(TAG, "onCreate")
        setContentView(R.layout.activity_song_data)

        textFontSize = ScreenUtil.getPxTextFontSizeNeeded(this)
        textFontSize *= 0.8f
        toastTextSize = 0.9f * textFontSize
        val extras: Bundle?
        if (savedInstanceState == null) {
            extras = intent.extras
            LogUtil.d(TAG, "savedInstanceState is null.")
        } else {
            // not null, has savedInstanceState
            extras = savedInstanceState
            LogUtil.d(TAG, "savedInstanceState is not null.")
        }
        var song: SongInfo? = null
        extras?.let {
            song = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                it.getParcelable(MyPlayerConstants.SINGLE_SONG_INFO_STATE,
                SongInfo::class.java)
            else it.getParcelable(MyPlayerConstants.SINGLE_SONG_INFO_STATE)
        }

        if (song == null) returnToPreviousWithResult(RESULT_CANCELED)
        mSongInfo = song!!
        // ArrayAdapters for spinners
        val numList = ArrayList<String?>()
        numList.add("1")
        numList.add("2")
        numList.add("3")
        numList.add("4")
        numList.add("5")
        numList.add("6")
        numList.add("7")
        numList.add("8")
        val audioMusicTrackAdapter = SpinnerAdapter(
            this, R.layout.spinner_item_layout,
            R.id.spinnerTextView, numList, textFontSize
        )
        val audioVocalTrackAdapter = SpinnerAdapter(
            this, R.layout.spinner_item_layout,
            R.id.spinnerTextView, numList, textFontSize
        )
        val aList = ArrayList<String?>(SmileAppBase.audioChannelMap.values)
        val audioMusicChannelAdapter = SpinnerAdapter(
            this, R.layout.spinner_item_layout,
            R.id.spinnerTextView, aList, textFontSize
        )
        val audioVocalChannelAdapter = SpinnerAdapter(
            this, R.layout.spinner_item_layout,
            R.id.spinnerTextView, aList, textFontSize
        )

        val titleStringTextView = findViewById<TextView>(R.id.edit_titleStringTextView)
        ScreenUtil.resizeTextSize(titleStringTextView, textFontSize)
        titleNameEditText = findViewById(R.id.edit_titleNameEditText)
        ScreenUtil.resizeTextSize(titleNameEditText, textFontSize)
        titleNameEditText?.setText(mSongInfo.songName)

        val filePathStringTextView = findViewById<TextView>(R.id.edit_filePathStringTextView)
        ScreenUtil.resizeTextSize(filePathStringTextView, textFontSize)
        filePathEditText = findViewById(R.id.edit_filePathEditText)
        filePathEditText?.setEnabled(false)
        ScreenUtil.resizeTextSize(filePathEditText, textFontSize)
        filePathEditText?.setText(mSongInfo.filePath)
        karaokeSettingLayout = findViewById(R.id.karaokeSettingLayout)

        val musicTrackStringTextView =
            findViewById<TextView>(R.id.edit_musicTrackStringTextView)
        ScreenUtil.resizeTextSize(musicTrackStringTextView, textFontSize)
        musicTrackSpinner = findViewById(R.id.edit_musicTrackSpinner)
        musicTrackSpinner?.setAdapter(audioMusicTrackAdapter)
        mSongInfo.musicTrackNo?.let {
            musicTrackSpinner?.setSelection(it - 1)
        } ?: musicTrackSpinner?.setSelection(0)
        val musicChannelStringTextView =
            findViewById<TextView>(R.id.edit_musicChannelStringTextView)
        ScreenUtil.resizeTextSize(musicChannelStringTextView, textFontSize)
        musicChannelSpinner = findViewById(R.id.edit_musicChannelSpinner)
        musicChannelSpinner?.setAdapter(audioMusicChannelAdapter)
        musicChannelSpinner?.setSelection(mSongInfo.musicChannel?: CommonConstants.STEREO)
        val vocalTrackStringTextView =
            findViewById<TextView>(R.id.edit_vocalTrackStringTextView)
        ScreenUtil.resizeTextSize(vocalTrackStringTextView, textFontSize)
        vocalTrackSpinner = findViewById(R.id.edit_vocalTrackSpinner)
        vocalTrackSpinner?.setAdapter(audioVocalTrackAdapter)
        mSongInfo.vocalTrackNo?.let {
            vocalTrackSpinner?.setSelection(it - 1)
        } ?: vocalTrackSpinner?.setSelection(0)

        val vocalChannelStringTextView =
            findViewById<TextView>(R.id.edit_vocalChannelStringTextView)
        ScreenUtil.resizeTextSize(vocalChannelStringTextView, textFontSize)
        vocalChannelSpinner = findViewById(R.id.edit_vocalChannelSpinner)
        vocalChannelSpinner?.setAdapter(audioVocalChannelAdapter)
        vocalChannelSpinner?.setSelection(mSongInfo.vocalChannel?: CommonConstants.STEREO)

        karaokeSettingLayout?.visibility = View.VISIBLE

        val includedPlaylistTextView = findViewById<TextView>(R.id.editIncludedPlayListTextView)
        ScreenUtil.resizeTextSize(includedPlaylistTextView, textFontSize)
        includedPlaylistCheckBox = findViewById(R.id.editIncludedPlaylistCheckBox)
        ScreenUtil.resizeTextSize(includedPlaylistCheckBox, textFontSize)
        val isChecked = mSongInfo.included == "1"
        includedPlaylistCheckBox?.setChecked(isChecked)
        includedPlaylistCheckBox?.setOnCheckedChangeListener { buttonView: CompoundButton?,
                                                                isChecked1: Boolean ->
            includedPlaylistCheckBox!!.setChecked(isChecked1)
            includedPlaylistCheckBox!!.jumpDrawablesToCurrentState()
        }
        includedPlaylistCheckBox?.setOnFocusChangeListener { v: View?, hasFocus: Boolean ->
            if (hasFocus) {
                includedPlaylistTextView.setTextColor(Color.RED)
            } else {
                includedPlaylistTextView.setTextColor(Color.BLACK)
            }
        }

        val saveOneSongButton = findViewById<Button>(R.id.saveOneSongButton)
        ScreenUtil.resizeTextSize(saveOneSongButton, textFontSize)
        saveOneSongButton.setOnClickListener { view: View? ->
            val isValid = setSongInfoFromInput(true)
            if (isValid) {
                returnToPreviousWithResult(RESULT_OK, CommonConstants.SAVE_ACTION)
            }
        }

        val deleteOneSongButton = findViewById<Button>(R.id.deleteOneSongButton)
        ScreenUtil.resizeTextSize(deleteOneSongButton, textFontSize)
        deleteOneSongButton.setOnClickListener { view: View? ->
            returnToPreviousWithResult(RESULT_OK, CommonConstants.DELETE_ACTION)
        }

        val exitEditSongButton = findViewById<Button>(R.id.exitEditSongButton)
        ScreenUtil.resizeTextSize(exitEditSongButton, textFontSize)
        exitEditSongButton.setOnClickListener { view: View? ->
            returnToPreviousWithResult(RESULT_CANCELED)
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                LogUtil.d(TAG, "getOnBackPressedDispatcher.handleOnBackPressed")
                returnToPreviousWithResult(RESULT_CANCELED)
            }
        })

        // Find the LinearLayout by its ID
        val songDataLayout = findViewById<ConstraintLayout>(R.id.songDataLayout)
        // Get the ViewTreeObserver for the LinearLayout
        songDataLayout.getViewTreeObserver().addOnGlobalLayoutListener(
            object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    // Layout has been finished.
                    // Remove the listener to avoid it being called repeatedly.
                    // The removeOnGlobalLayoutListener() method is used for API 16 and above.
                    songDataLayout.getViewTreeObserver()
                        .removeOnGlobalLayoutListener(this)
                    // Now it's safe to get the view's dimensions or perform other actions
                    // that depend on the layout being complete.
                    // do something after layout finished
                }
            }
        )
    }

    override fun onResume() {
        super.onResume()
        LogUtil.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        LogUtil.d(TAG, "onPause")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        LogUtil.d(TAG, "onSaveInstanceState")
        setSongInfoFromInput(false)
        outState.putParcelable(MyPlayerConstants.SINGLE_SONG_INFO_STATE, mSongInfo)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.d(TAG, "onDestroy")
    }

    private fun returnToPreviousWithResult(isOK: Int, actionCode: String = "") {
        LogUtil.d(TAG, "returnToPreviousWithResult")
        val returnIntent = Intent()
        Bundle().apply {
            putString(CommonConstants.CRUD_ACTION, actionCode)
            putParcelable(MyPlayerConstants.SINGLE_SONG_INFO_STATE, mSongInfo)
            returnIntent.putExtras(this@apply)
            // can bundle some data to previous activity
            setResult(isOK, returnIntent)
            finish()
        }
    }

    private fun setSongInfoFromInput(hasMessage: Boolean): Boolean {
        LogUtil.d(TAG, "setSongInfoFromInput")
        var isValid = true
        var title = ""
        var text = titleNameEditText?.getText()
        if (text != null) {
            title = text.toString().trim { it <= ' ' }
        }
        text = filePathEditText?.getText()
        var filePath = ""
        if (text != null) {
            filePath = text.toString().trim { it <= ' ' }
        }

        val musicTrack = musicTrackSpinner?.getSelectedItem().toString()
        val musicChannel = musicChannelSpinner?.getSelectedItem().toString()
        val vocalTrack = vocalTrackSpinner?.getSelectedItem().toString()
        val vocalChannel = vocalChannelSpinner?.getSelectedItem().toString()
        val included = if (includedPlaylistCheckBox?.isChecked == true) "1" else "0"

        mSongInfo.songName = title
        mSongInfo.filePath = filePath
        mSongInfo.musicTrackNo = musicTrack.toInt()
        var channel = SmileAppBase.audioChannelReverseMap[musicChannel]
        mSongInfo.musicChannel = channel
        mSongInfo.vocalTrackNo = vocalTrack.toInt()
        channel = SmileAppBase.audioChannelReverseMap[vocalChannel]
        mSongInfo.vocalChannel = channel
        mSongInfo.included = included

        if (filePath.isEmpty()) {
            isValid = false
            if (hasMessage) {
                ScreenUtil.showToast(
                    this, getString(R.string.filepathEmptyString),
                    toastTextSize, ScreenUtil.FontSize_Pixel_Type,
                    Toast.LENGTH_SHORT
                )
            }
        }

        return isValid
    }
}
