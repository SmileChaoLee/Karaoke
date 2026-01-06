package com.smile.u2bkaraoke

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.smile.karaoke.R
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.u2bkaraoke.model.Constants

class U2bKkActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "U2bKkActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.d(TAG, "onCreate")
        val textFontSize = ScreenUtil.getPxTextFontSizeNeeded(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_my)

        // getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        val mainMenuTextView = findViewById<TextView>(R.id.mainMenuTextView)
        ScreenUtil.resizeTextSize(mainMenuTextView, textFontSize)

        val singerOrderButton = findViewById<Button>(R.id.singerOrderButton)
        ScreenUtil.resizeTextSize(singerOrderButton, textFontSize)
        singerOrderButton.setOnClickListener {
            val singerTypesIntent = Intent(
                this@U2bKkActivity,
                SingerTypeListActivity::class.java
            )
            startActivity(singerTypesIntent)
        }

        val newSongOrderButton = findViewById<Button>(R.id.newSongOrderButton)
        ScreenUtil.resizeTextSize(newSongOrderButton, textFontSize)
        newSongOrderButton.setOnClickListener {
            val languagesIntent = Intent(
                this@U2bKkActivity,
                LanguageListActivity::class.java
            )
            languagesIntent.putExtra(Constants.OrderedFrom, Constants.NewSongOrdered)
            startActivity(languagesIntent)
        }

        val hotSongOrderButton = findViewById<Button>(R.id.hotSongOrderButton)
        ScreenUtil.resizeTextSize(hotSongOrderButton, textFontSize)
        hotSongOrderButton.setOnClickListener {
            val languagesIntent = Intent(
                this@U2bKkActivity,
                LanguageListActivity::class.java
            )
            languagesIntent.putExtra(Constants.OrderedFrom, Constants.HotSongOrdered)
            startActivity(languagesIntent)
        }

        val languageOrderButton = findViewById<Button>(R.id.languageOrderButton)
        ScreenUtil.resizeTextSize(languageOrderButton, textFontSize)
        languageOrderButton.setOnClickListener {
            val intentLanguageOrder = Intent(
                this@U2bKkActivity,
                LanguageListActivity::class.java
            )
            startActivity(intentLanguageOrder)
        }

        val exitProgramButton = findViewById<Button>(R.id.exitProgramButton)
        ScreenUtil.resizeTextSize(exitProgramButton, textFontSize)
        exitProgramButton.setOnClickListener { quitApplication() }

        onBackPressedDispatcher.addCallback(
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    LogUtil.d(TAG, "onBackPressedDispatcher.handleOnBackPressed")
                    quitApplication()
                }
            })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (item.itemId == R.id.action_settings) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    public override fun onDestroy() {
        super.onDestroy()
    }

    private fun quitApplication() {
        val handlerClose = Handler(mainLooper)
        val timeDelay = 200
        handlerClose.postDelayed({ // quit game
            finish()
        }, timeDelay.toLong())
    }
}
