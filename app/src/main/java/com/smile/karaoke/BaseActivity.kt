package com.smile.karaoke

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import com.google.android.ump.ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA
import com.smile.karaoke.constants.MyPlayerConstants
import com.smile.karaoke.fragments.PlayerBaseFragment
import com.smile.karaoke.fragments.TablayoutFragment
import com.smile.karaoke.interfaces.PlayMyFavorites
import com.smile.karaoke.interfaces.PlaySongs
import com.smile.karaoke.models.MySingleton
import com.smile.karaoke.models.PlayingParameters
import com.smile.karaoke.models.SongInfo
import com.smile.karaoke.smileapps.SmileAppsActivity
import com.smile.karaoke.utilities.LogUtil
import com.smile.karaoke.utilities.PermissionUtil
import com.smile.smilelibraries.show_interstitial_ads.ShowInterstitial
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.smilelibraries.utilities.UmpUtil

private const val TAG : String = "BaseActivity"
private const val PLAYER_FRAGMENT = "PlayerFragment"
private const val TAB_LAYOUT_FRAGMENT = "TablayoutFragment"
private const val IS_PLAY_TO_PAUSE = "IsPlayToPause"
private const val PLAY_DATA = "PlayData"
private const val CALLING_COMPONENT = "CallingComponent"

@UnstableApi
abstract class BaseActivity : AppCompatActivity(),
    PlayerBaseFragment.PlayBaseFragmentFunc,
    PlaySongs, PlayMyFavorites,
    TablayoutFragment.TabFragmentFunc {

    private var permissionExternalStorage = false
    private var playerFragment: PlayerBaseFragment? = null
    private lateinit var basePlayViewLayout : LinearLayout
    private var tablayoutFragment : TablayoutFragment? = null
    private lateinit var tablayoutViewLayout : LinearLayout
    private lateinit var callingIntent : Intent
    private var isPlayToPause : Boolean = false
    private var callingComponentName : ComponentName? = null
    private var playData = Bundle()
    private var interstitialAd: ShowInterstitial? = null
    private var touchDisabled = true

    @OptIn(UnstableApi::class)
    abstract fun getFragment() : PlayerBaseFragment
    abstract fun askPermissions(activity: Activity): Boolean
    open fun needInterstitialAd() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.i(TAG,"onCreate")
        settingBeforeCreate()
        MySingleton.clearSingleton()
        super.onCreate(savedInstanceState)
        // disabling the touch events
        touchDisabled = true

        setContentView(R.layout.activity_base)

        basePlayViewLayout = findViewById(R.id.basePlayViewLayout)
        basePlayViewLayout.visibility = View.VISIBLE
        basePlayViewLayout.isFocusable = true
        tablayoutViewLayout = findViewById(R.id.tablayoutViewLayout)
        tablayoutViewLayout.visibility = View.VISIBLE
        tablayoutViewLayout.isFocusable = true

        callingIntent = intent
        LogUtil.d(TAG,"onCreate.callingIntent = $callingIntent")
        LogUtil.d(TAG,"onCreate.savedInstanceState = $savedInstanceState")

        if (callingIntent.extras == null) {
            LogUtil.d(TAG, "callingIntent.extras is null")
        } else {
            LogUtil.d(TAG, "callingIntent.extras is not null")
        }

        if (savedInstanceState != null) {
            isPlayToPause = savedInstanceState.getBoolean(IS_PLAY_TO_PAUSE, false)

            callingComponentName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                savedInstanceState.getParcelable(CALLING_COMPONENT, ComponentName::class.java)
            else savedInstanceState.getParcelable(CALLING_COMPONENT)

            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                savedInstanceState.getParcelable(PLAY_DATA, Bundle::class.java)
            else savedInstanceState.getParcelable(PLAY_DATA))?.also {
                playData = it
            }
            playerFragment = supportFragmentManager.findFragmentByTag(PLAYER_FRAGMENT) as PlayerBaseFragment
            LogUtil.d(TAG, "playerFragment = $playerFragment")
            tablayoutFragment = supportFragmentManager.findFragmentByTag(TAB_LAYOUT_FRAGMENT) as TablayoutFragment?
            LogUtil.d(TAG, "tablayoutFragment = $tablayoutFragment")
        }

        if (playerFragment == null) {
            playerFragment = getFragment()
        }
        if (tablayoutFragment == null) {
            tablayoutFragment = TablayoutFragment()
        }

        supportFragmentManager.beginTransaction().apply {
            var isReplaced = false
            tablayoutFragment?.let {
                if (!it.isInLayout) {
                    LogUtil.d(TAG, "tablayoutFragment.isInLayout() = false")
                    replace(R.id.tablayoutViewLayout, it, TAB_LAYOUT_FRAGMENT)
                    isReplaced = true
                }
            }
            playerFragment?.let {
                if (!it.isInLayout) {
                    LogUtil.d(TAG, "playerFragment.isInLayout() = false")
                    replace(R.id.basePlayViewLayout, it, PLAYER_FRAGMENT)
                    isReplaced = true
                }
            }
            if (isReplaced) commit()
            LogUtil.d(TAG, "beginTransaction.commit()")
        }

        onBackPressedDispatcher.addCallback(
            object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                LogUtil.d(TAG, "onBackPressedDispatcher.handleOnBackPressed")
                playerFragment?.onBackPressed()
            }
        })

        // Asking user's permissions
        LogUtil.d(TAG, "askPermissions(this@BaseActivity)")
        permissionExternalStorage = askPermissions(this@BaseActivity)
        // permissionExternalStorage = PermissionUtil.askPermissions(this@BaseActivity)

        // user consent for personal data collection
        // val deviceHashedId = "8F6C5B0830E624E8D8BFFB5853B4EDDD" // for debug test
        val deviceHashedId = ""  // for release
        UmpUtil.initConsentInformation(this@BaseActivity,
            DEBUG_GEOGRAPHY_EEA, deviceHashedId,
            object : UmpUtil.UmpInterface {
                override fun callback() {
                    LogUtil.d(TAG, "onCreate.initConsentInformation.finished")
                    // enabling receiving touch events
                    touchDisabled = false
                }
            })

        findViewById<FrameLayout>(R.id.activity_base_layout).apply {
            viewTreeObserver.addOnGlobalLayoutListener(
                object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    // Layout has been finished
                    // hove to use removeGlobalOnLayoutListener() method after API 16 or is API 16
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    createViewDependingOnOrientation()
                }
            })
        }

        LogUtil.i(TAG,"onCreate.finished")
    }

    @Deprecated(
        "This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)} passing\n      in a {@link RequestMultiplePermissions} object for the {@link ActivityResultContract} and\n      handling the result in the {@link ActivityResultCallback#onActivityResult(Object) callback}."
    )
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionExternalStorage = PermissionUtil.onRequestPermResult(requestCode, grantResults)
        if (!permissionExternalStorage) {
            ScreenUtil.showToast(this, "Permission Denied", 60f, ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_LONG)
            LogUtil.i(TAG, "onRequestPermissionsResult.Permission Denied")
            finish()
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (touchDisabled) {
            // Consume the touch event, effectively disabling touch
            return true
        }
        // Allow touch events to proceed
        return super.dispatchTouchEvent(ev)
    }

    override fun onStart() {
        LogUtil.i(TAG, "onStart()")
        super.onStart()
    }

    override fun onResume() {
        LogUtil.i(TAG, "onResume()")
        super.onResume()
    }

    override fun onPause() {
        LogUtil.i(TAG, "onPause()")
        super.onPause()
    }

    override fun onStop() {
        LogUtil.i(TAG, "onStop()")
        super.onStop()
    }

    override fun onSaveInstanceState(
        outState: Bundle, outPersistentState: PersistableBundle) {
        LogUtil.i(TAG, "onSaveInstanceState()")
        outState.putBoolean(IS_PLAY_TO_PAUSE, isPlayToPause)
        outState.putParcelable(CALLING_COMPONENT, callingComponentName)
        outState.putParcelable(PLAY_DATA, playData)
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        LogUtil.i(TAG, "onConfigurationChanged()")
        settingBeforeCreate()
        super.onConfigurationChanged(newConfig)
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.i(TAG, "onDestroy()")
        interstitialAd?.releaseInterstitial()
        MySingleton.clearSingleton()
        // clear the screen on, added on 2021-02-18
        window?.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun settingBeforeCreate() {
        window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        val app = application as? SmileAppBase
        interstitialAd = ShowInterstitial(this, null,
            app?.getInterstitial())
    }

    fun onReceiveFunc(isSingleSong: Boolean, needPlay: Boolean,
                      intent : Intent?, pData : Bundle?) {
        LogUtil.i(TAG, "onReceiveFunc.needPlay = $needPlay")
        playerFragment?.run {
            mPresenter.let{ mpIt ->
                mpIt.initializeVariables(pData, intent,
                    mpIt.playingParam.isAutoPlay)
                if (needPlay) mpIt.playSongPlayedBeforeActivityCreated()
                setMainMenu()
                LogUtil.d(TAG, "onReceiveFunc.isSingleSong = $isSingleSong")
                if (mpIt.playingParam.isPlayerViewVisible) showPlayerView()
                else hidePlayerView()
                LogUtil.d(TAG, "onReceiveFunc.currentPlaybackState = " +
                        "${mpIt.playingParam.currentPlaybackState}")
            }
            showSupportToolbarAudioControlSetTimer()
        }
        Intent().apply {
            LogUtil.d(TAG, "onReceiveFunc.componentName = $componentName")
            component = componentName
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(this)
        }
    }

    // implementing interface PlayerBaseViewFragment.PlayBaseFragmentFunc
    override fun baseHidePlayerView() {
        LogUtil.i(TAG, "baseHidePlayerView()")
        basePlayViewLayout.clearFocus()
        basePlayViewLayout.visibility = View.INVISIBLE
        tablayoutViewLayout.visibility = View.VISIBLE
        tablayoutViewLayout.post { tablayoutViewLayout.requestFocus() }
        tablayoutFragment?.becomeVisible()
    }

    override fun baseShowPlayerView() {
        LogUtil.i(TAG, "baseShowPlayerView()")
        tablayoutViewLayout.clearFocus()
        tablayoutViewLayout.visibility = View.INVISIBLE
        basePlayViewLayout.visibility = View.VISIBLE
        basePlayViewLayout.post { basePlayViewLayout.requestFocus() }
        tablayoutFragment?.becomeInVisible()
    }
    // Finishes interface PlayerBaseViewFragment.PlayBaseFragmentFunc

    // implementing interface PlayMyFavorites
    override fun onSavePlayingState(compName : ComponentName?) {
        LogUtil.i(TAG, "onSavePlayingState.compName = $compName")
        callingComponentName = compName
        playerFragment?.let {
            playData.clear()
            it.onSaveInstanceState(playData)
            isPlayToPause = false
            it.mPresenter.playingParam.let { pIt->
                if (pIt.currentPlaybackState == PlaybackStateCompat.STATE_PLAYING) {
                    // playing then pause before going to my favorite activity
                    it.mPresenter.pausePlay()
                    isPlayToPause = true
                }
                pIt.wentToFavorite = true   // going to BaseFavoriteListActivity
            }
        }
    }

    override fun restorePlayingState() {
        val msgStr = "restorePlayingState"
        LogUtil.i(TAG, "${msgStr}.return from BaseFavoriteListActivity")
        // come Back From Favorite
        (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            playData.getParcelable(MyPlayerConstants.PlayingParamState, PlayingParameters::class.java)
        else playData.getParcelable(MyPlayerConstants.PlayingParamState))?.apply {
            LogUtil.d(TAG, "${msgStr}.currentPlaybackState = $currentPlaybackState")
            LogUtil.d(TAG, "${msgStr}.currentAudioPosition = $currentAudioPosition")
            LogUtil.d(TAG, "${msgStr}.preparedStatus = $preparedStatus")
            LogUtil.d(TAG, "${msgStr}.wentToFavorite = $wentToFavorite")
            if (isPlayToPause) currentPlaybackState = PlaybackStateCompat.STATE_PLAYING // restore to playing
            preparedStatus = 4  // come Back From Favorite, simulate onStart() of PlayerBaseViewFragment
            LogUtil.d(TAG, "${msgStr}.preparedStatus changed to $preparedStatus")
            wentToFavorite = false  // set back to default
        }
        onReceiveFunc(isSingleSong = false, needPlay = true, intent = null, pData = playData)
        callingComponentName = null
        isPlayToPause = false
    }
    // Finishes implementing interface PlayMyFavorites

    // implementing interface PlaySongs
    override fun playSelectedSongList(songs: ArrayList<SongInfo>) {
        val msgStr = "playSelectedSongList"
        LogUtil.i(TAG, "$msgStr.songs.size = ${songs.size}")
        if (songs.isNotEmpty()) {
            // MySingleton.orderedSongs.clear() // no more clear, using add instead
            val orderedSize = MySingleton.orderedSongs.size
            var found: Boolean
            for (songInfo in songs) {
                found = false
                LogUtil.d(TAG, "$msgStr.songInfo.filePath = ${songInfo.filePath}")
                for (i in 0 until orderedSize) {
                    val orderedSong = MySingleton.orderedSongs[i]
                    LogUtil.d(TAG, "$msgStr.orderedSong.filePath = ${orderedSong.filePath}")
                    if (orderedSong.filePath == songInfo.filePath) {
                        found = true
                        break
                    }
                }
                LogUtil.d(TAG, "$msgStr.found = $found")
                if (!found) MySingleton.orderedSongs.add(songInfo)
            }
            playerFragment?.let {
                it.mPresenter.playingParam.isAutoPlay = false
                if (it.mPresenter.mediaUri == null) it.mPresenter.autoPlaySongList()
                it.showPlayerView()
            }
        }
    }

    override fun switchToPlayerView() {
        LogUtil.i(TAG, "switchToPlayerView")
        playerFragment?.showPlayerView()
    }

    override fun isSoftDecoderFirst(): Boolean {
        playerFragment?.let {
            return it.mPresenter.playingParam.softDecoderFirst
        }
        return false
    }

    override fun switchBetweenSoftAndHardDecoder() {
        LogUtil.i(TAG, "switchBetweenSoftAndHardDecoder")
        playerFragment?.let {
            it.mPresenter.playingParam.softDecoderFirst = !it.mPresenter.playingParam.softDecoderFirst
            it.getPlayService()?.switchDecoder()
        }
    }

    override fun showSmileAppsActivity() {
        Intent(this@BaseActivity,
            SmileAppsActivity::class.java
        ).also {
            startActivity(it)
        }
    }

    override fun returnToPrevious(isSingleSong : Boolean) {
        val msgStr = "returnToPrevious"
        LogUtil.d(TAG, "${msgStr}.isSingleSong = $isSingleSong")
        if (isSingleSong) {
            playerFragment?.mPresenter?.let {
                it.pausePlay()
                it.playingParam.singleSongPlayingStatus = 0  // exit playing single song
                callingComponentName?.let { callIt ->
                    Intent().apply {
                        component = callIt
                        addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        startActivity(this)
                    }
                }
                LogUtil.d(TAG, "${msgStr}.preparedStatus = " +
                        "${it.playingParam.preparedStatus}")
            }
            return
        }
        // exit application
        playerFragment?.mPresenter?.apply {
            stopPlay(MyPlayerConstants.STOPPED_BY_USER)
        }
        finishThisActivity()
    }
    // Finish implementing interface PlaySongs

    private fun showInterstitialAd() {
        LogUtil.i(TAG, "showInterstitialAd = $interstitialAd")
        interstitialAd?.ShowAdThread()?.startShowAd(0) // AdMob first
    }

    private fun finishThisActivity() {
        LogUtil.i(TAG, "finishThisActivity = $interstitialAd")
        if (needInterstitialAd()) {
            showInterstitialAd()
        }
        finish()
        /*  // do not use this because it does not work sometimes
            because the executeDismiss does not happen
        interstitialAd?.ShowAdThread(object: DismissFunction {
            override fun backgroundWork() {
                // do nothing
            }

            override fun executeDismiss() {
                finish()    // finish() after dismissing ad
            }

            override fun afterFinished(isAdShown: Boolean) {
                if (!isAdShown) finish() // no ad, then finish
            }
        })?.startShowAd(0) ?: finish()
        */
    }

    private fun createViewDependingOnOrientation() {
        LogUtil.i(TAG, "createViewDependingOnOrientation")
        if (callingIntent.extras == null) {
            playerFragment?.hidePlayerView()
        }
    }
}