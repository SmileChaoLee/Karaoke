package com.smile.karaoke

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.os.PowerManager
import android.provider.Settings
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media3.common.util.UnstableApi
import com.smile.karaoke.constants.PlayerConstants
import com.smile.karaoke.fragments.PlayerBaseFragment
import com.smile.karaoke.fragments.TablayoutFragment
import com.smile.karaoke.interfaces.PlayMyFavorites
import com.smile.karaoke.interfaces.PlaySongs
import com.smile.karaoke.models.MySingleTon
import com.smile.karaoke.models.PlayingParameters
import com.smile.karaoke.models.SongInfo
import com.smile.karaoke.smileapps.SmileAppsActivity
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.interfaces.DismissFunction
import com.smile.smilelibraries.show_interstitial_ads.ShowInterstitial

private const val TAG : String = "BaseActivity"
private const val PLAYER_FRAGMENT = "PlayerFragment"
private const val TAB_LAYOUT_FRAGMENT = "TablayoutFragment"
private const val IS_PLAY_TO_PAUSE = "IsPlayToPause"
private const val PLAY_DATA = "PlayData"
private const val CALLING_COMPONENT = "CallingComponent"
private const val PERMISSION_WRITE_EXTERNAL_CODE = 0x11

@UnstableApi
abstract class BaseActivity : AppCompatActivity(),
    PlayerBaseFragment.PlayBaseFragmentFunc,
    PlaySongs, PlayMyFavorites {

    private var permissionExternalStorage = false
    private var playerFragment: PlayerBaseFragment? = null
    private lateinit var basePlayViewLayout : LinearLayout
    private var tablayoutFragment : TablayoutFragment? = null
    private lateinit var tablayoutViewLayout : LinearLayout
    private lateinit var baseReceiver: BroadcastReceiver
    private lateinit var callingIntent : Intent
    private var isPlayToPause : Boolean = false
    private var hasPlayedSingle : Boolean = false
    private var callingComponentName : ComponentName? = null
    private var playData = Bundle()
    private var interstitialAd: ShowInterstitial? = null

    @OptIn(UnstableApi::class)
    abstract fun getFragment() : PlayerBaseFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.i(TAG,"onCreate")
        settingBeforeCreate()
        MySingleTon.clearSingleton()
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_base)

        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                LogUtil.i(TAG, "onCreate.BroadcastReceiver.onReceive")
                intent?.action?.let {
                    if (it == PlayerConstants.PlaySingleSongAction) {
                        LogUtil.d(TAG, "onReceive.PlaySingleSongAction")
                        intent.putExtra(PlayerConstants.SingleSongVolume,
                                playerFragment?.mPresenter?.playingParam?.currentVolume)
                        onReceiveFunc(isSingleSong = true, needPlay = true, intent = intent, pData = null)
                        hasPlayedSingle = true
                    }
                }
            }
        }.also { baseReceiver = it }

        LocalBroadcastManager.getInstance(this).apply {
            LogUtil.d(TAG, "onCreate.LocalBroadcastManager.registerReceiver")
            registerReceiver(baseReceiver, IntentFilter().apply {
                addAction(PlayerConstants.PlaySingleSongAction)
                addAction(PlayerConstants.BackToBaseActivity)
            })
        }

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
        }

        onBackPressedDispatcher.addCallback(
            object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                LogUtil.d(TAG, "onBackPressedDispatcher.handleOnBackPressed")
                playerFragment?.onBackPressed()
            }
        })

        // Asking user's permissions
        permissionExternalStorage =
            (ActivityCompat.checkSelfPermission(applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED)
        if (!permissionExternalStorage) {
            val permissions : Array<String> =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.READ_MEDIA_AUDIO)
                } else {
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            ActivityCompat.requestPermissions(this,
                permissions,
                PERMISSION_WRITE_EXTERNAL_CODE
            )
        }
        askIgnoreOptimizationsBattery()

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
            // this in here represent FrameLayout (R.id.activity_base_layout)
            ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
                // Get the insets for the system bars (status bar on top, navigation bar at bottom)
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                // Apply these insets as padding to your FrameLayout
                view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
                // Return CONSUMED to signal that you've handled the insets
                WindowInsetsCompat.CONSUMED
            }
        }
    }

    @SuppressLint("BatteryLife")
    private fun askIgnoreOptimizationsBattery() {
        val pm = getSystemService(POWER_SERVICE) as? PowerManager
        if (pm != null && !pm.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent()
            val pName = packageName
            intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            intent.data = "package:$pName".toUri()
            startActivity(intent)
        }
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
        LocalBroadcastManager.getInstance(this).apply {
            unregisterReceiver(baseReceiver)
        }
        interstitialAd?.releaseInterstitial()
        MySingleTon.clearSingleton()
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
            mPresenter.let{
                it.initializeVariables(pData, intent,
                    it.playingParam.isAutoPlay)
                if (needPlay) it.playSongPlayedBeforeActivityCreated()
                setMainMenu()
                LogUtil.d(TAG, "onReceiveFunc.isSingleSong = $isSingleSong")
                if (isSingleSong) {
                    it.playingParam.singleSongPlayingStatus = 1  // start playing single song
                    showPlayerView()
                } else {
                    // PlayerConstants.BackToBaseActivity
                    if (it.playingParam.isPlayerViewVisible) showPlayerView()
                    else hidePlayerView()
                    LogUtil.d(TAG, "onReceiveFunc.currentPlaybackState = " +
                            "${it.playingParam.currentPlaybackState}")
                }
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
        basePlayViewLayout.visibility = View.GONE
        tablayoutViewLayout.visibility = View.VISIBLE
        tablayoutViewLayout.requestFocus()
        tablayoutFragment?.becomeVisible()
    }

    override fun baseShowPlayerView() {
        LogUtil.i(TAG, "baseShowPlayerView()")
        tablayoutViewLayout.clearFocus()
        tablayoutViewLayout.visibility = View.GONE
        basePlayViewLayout.visibility = View.VISIBLE
        basePlayViewLayout.requestFocus()
        tablayoutFragment?.becomeInVisible()
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
            stopPlay(PlayerConstants.STOPPED_BY_USER)
        }
        finishThisActivity()
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
            it.mPresenter.playingParam?.let { pIt->
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
            playData.getParcelable(PlayerConstants.PlayingParamState, PlayingParameters::class.java)
        else playData.getParcelable(PlayerConstants.PlayingParamState))?.apply {
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
        LogUtil.i(TAG, "${msgStr}.songs.size" +
                " = ${songs.size}")
        if (songs.isNotEmpty()) {
            MySingleTon.orderedSongs.clear()
            MySingleTon.orderedSongs.addAll(songs)
            playerFragment?.let {
                it.mPresenter.playingParam.isAutoPlay = false
                it.mPresenter.autoPlaySongList()
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
            it.playService?.switchDecoder()
        }
    }

    override fun showSmileAppsActivity() {
        Intent(this@BaseActivity,
            SmileAppsActivity::class.java
        ).also {
            startActivity(it)
        }
    }
    // Finish implementing interface PlaySongs

    private fun showInterstitialAd() {
        LogUtil.i(TAG, "showInterstitialAd = $interstitialAd")
        interstitialAd?.ShowAdThread()?.startShowAd(0) // AdMob first
    }

    private fun finishThisActivity() {
        LogUtil.i(TAG, "finishThisActivity = $interstitialAd")
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
    }

    private fun createViewDependingOnOrientation() {
        LogUtil.i(TAG, "createViewDependingOnOrientation")
        if (callingIntent.extras == null) {
            playerFragment?.hidePlayerView()
        }
    }
}