package com.smile.karaokeplayer

import android.Manifest
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.os.BundleCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media3.common.util.UnstableApi
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.dynamite.DynamiteModule.LoadingException
import com.smile.karaokeplayer.constants.PlayerConstants
import com.smile.karaokeplayer.exoplayer.fragments.ExoPlayerFragment
import com.smile.karaokeplayer.fragments.PlayerBaseViewFragment
import com.smile.karaokeplayer.fragments.TablayoutFragment
import com.smile.karaokeplayer.interfaces.PlayMyFavorites
import com.smile.karaokeplayer.interfaces.PlaySongs
import com.smile.karaokeplayer.models.FileDesList
import com.smile.karaokeplayer.models.MySingleTon
import com.smile.karaokeplayer.models.PlayingParameters
import com.smile.karaokeplayer.models.SongInfo
import com.smile.karaokeplayer.vlcplayer.fragments.VlcPlayerFragment
import com.smile.smilelibraries.models.ExitAppTimer
import com.smile.smilelibraries.utilities.ScreenUtil


@UnstableApi
class MainActivity : AppCompatActivity(), PlayerBaseViewFragment.PlayBaseFragmentFunc,
        PlaySongs, PlayMyFavorites {

    companion object {
        private const val TAG : String = "MainActivity"
        private const val PERMISSION_WRITE_EXTERNAL_CODE = 0x11
        private const val PLAYER_FRAGMENT_TAG = "PlayerFragment"
        private const val TAB_LAYOUT_FRAGMENT_TAG = "TablayoutFragment"
        private const val IS_PLAY_TO_PAUSE_STATE = "IsPlayToPause"
        private const val PLAY_DATA_STATE = "PlayData"
        private const val CALLING_COMPONENT_STATE = "CallingComponentName"
        private const val WHICH_PLAYER_STATE = "WhichPlayer"
    }

    private var textFontSize = 0f
    private var toastTextSize = 0f
    private var playerFragment: PlayerBaseViewFragment? = null
    private var permissionExternalStorage = false
    // private var permissionManageExternalStorage = false
    private lateinit var basePlayViewLayout : LinearLayout
    private lateinit var baseTabLayout : LinearLayout
    private lateinit var tablayoutViewLayout : LinearLayout
    private lateinit var choosePlayerLayout : ConstraintLayout
    private lateinit var chooseButtonConstraintLayout : ConstraintLayout
    private lateinit var vlcPlayerLinearLayout : LinearLayout
    private lateinit var exoPlayerLinearLayout : LinearLayout
    private lateinit var vlcPlayerButton : Button
    private lateinit var isVlcCurrent : TextView
    private lateinit var exoPlayerButton : Button
    private lateinit var isExoCurrent : TextView
    private lateinit var cancelButton : Button
    private var tablayoutFragment : TablayoutFragment? = null
    private var weightSum : Float = 0f
    // the declaration of baseReceiver must be lateinit var.
    // Not var and BroadcastReceiver? = null
    private lateinit var baseReceiver: BroadcastReceiver
    private lateinit var callingIntent : Intent
    private var isPlayToPause : Boolean = false
    private var hasPlayedSingle : Boolean = false
    private var callingComponentName : ComponentName? = null
    private var playData = Bundle()
    private var whichPlayer : Int = 0   // 0--> no selection, 1-->vlcPlayer, 2-->exoPlayer
    private var mOrderedSongs : ArrayList<SongInfo>? = null
    var castContext: CastContext? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG,"onCreate")
        window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        // the orientation is always the current one right now before creating or recreating after destroying
        requestedOrientation = when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            Configuration.ORIENTATION_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }

        val defaultTextFontSize = ScreenUtil.getDefaultTextSizeFromTheme(this@MainActivity,
            SmileApplication.FontSize_Scale_Type, null)
        textFontSize = ScreenUtil.suitableFontSize(this@MainActivity, defaultTextFontSize,
            SmileApplication.FontSize_Scale_Type,0.0f)
        toastTextSize = textFontSize * 0.7f

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d(TAG, "onCreate.BroadcastReceiver.onReceive")
                intent?.action?.let {
                    if (it == PlayerConstants.PlaySingleSongAction) {
                        Log.d(TAG, "onReceive.PlaySingleSongAction")
                        intent.putExtra(PlayerConstants.SingleSongVolume,
                                playerFragment?.mPresenter?.playingParam?.currentVolume)
                        onReceiveFunc(isSingleSong = true, needPlay = true, intent = intent, pData = null)
                        hasPlayedSingle = true
                    }
                }
            }
        }.also { baseReceiver = it }

        LocalBroadcastManager.getInstance(this).apply {
            Log.d(TAG, "onCreate.LocalBroadcastManager.registerReceiver")
            registerReceiver(baseReceiver, IntentFilter().apply {
                addAction(PlayerConstants.PlaySingleSongAction)
                addAction(PlayerConstants.BackToBaseActivity)
            })
        }

        permissionExternalStorage =
                (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED)
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!permissionExternalStorage) {
                val permissions : Array<String> =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.READ_MEDIA_AUDIO)
                } else {
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_WRITE_EXTERNAL_CODE)
            }

        askIgnoreOptimizationsBattery()

        basePlayViewLayout = findViewById(R.id.basePlayViewLayout)
        baseTabLayout = findViewById(R.id.baseTabLayout)
        weightSum = baseTabLayout.weightSum
        tablayoutViewLayout = findViewById(R.id.tablayoutViewLayout)
        setTabLayoutViewWeight(resources.configuration.orientation)

        choosePlayerLayout = findViewById(R.id.choosePlayerLayout)
        choosePlayerLayout.visibility = View.GONE
        chooseButtonConstraintLayout = findViewById(R.id.chooseButtonConstraintLayout)
        vlcPlayerLinearLayout = findViewById(R.id.vlcPlayerLinearLayout)
        exoPlayerLinearLayout = findViewById(R.id.exoPlayerLinearLayout)

        val descriptionRatio = 0.7f
        vlcPlayerButton = findViewById(R.id.vlcPlayerButton)
        vlcPlayerButton.let {
            it.setOnClickListener {
                Log.d(TAG, "vlcPlayerButton.setOnClickListener")
                whichPlayer = 1
                isEnableView(tablayoutViewLayout, true)
                playerFragment?.enableSomeButtonsDueToPopupGone()
                choosePlayerLayout.visibility = View.GONE
                startPlaySelectedSongList()
            }
        }
        isVlcCurrent = findViewById(R.id.isVlcCurrent)
        val vlcDescription : TextView = findViewById(R.id.vlcDescription)
        ScreenUtil.resizeTextSize(vlcDescription, textFontSize*descriptionRatio
            , ScreenUtil.FontSize_Pixel_Type)
        exoPlayerButton = findViewById(R.id.exoPlayerButton)
        exoPlayerButton.let {
            it.setOnClickListener {
                Log.d(TAG, "exoPlayerButton.setOnClickListener")
                whichPlayer = 2
                isEnableView(tablayoutViewLayout, true)
                playerFragment?.enableSomeButtonsDueToPopupGone()
                choosePlayerLayout.visibility = View.GONE
                startPlaySelectedSongList()
            }
        }
        isExoCurrent = findViewById(R.id.isExoCurrent)
        val exoDescription : TextView = findViewById(R.id.exoDescription)
        ScreenUtil.resizeTextSize(exoDescription, textFontSize*descriptionRatio
            , ScreenUtil.FontSize_Pixel_Type)
        cancelButton = findViewById(R.id.cancelButton)
        cancelButton.let {
            it.setOnClickListener {
                Log.d(TAG, "cancelButton.setOnClickListener")
                isEnableView(tablayoutViewLayout, true)
                playerFragment?.enableSomeButtonsDueToPopupGone()
                choosePlayerLayout.visibility = View.GONE
            }
        }
        setLayoutAndTextSize(resources.configuration.orientation)

        tablayoutFragment = null
        callingIntent = intent
        Log.d(TAG, "savedInstanceState = null")
        Log.d(TAG, "callingIntent.extras = ${callingIntent.extras}")
        if (savedInstanceState != null) {
            isPlayToPause = savedInstanceState.getBoolean(IS_PLAY_TO_PAUSE_STATE, false)
            whichPlayer = savedInstanceState.getInt(WHICH_PLAYER_STATE, 1)

            callingComponentName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                BundleCompat.getParcelable(savedInstanceState, CALLING_COMPONENT_STATE, ComponentName::class.java)
            else savedInstanceState.getParcelable(CALLING_COMPONENT_STATE)

            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                BundleCompat.getParcelable(savedInstanceState, PLAY_DATA_STATE, Bundle::class.java)
            else savedInstanceState.getParcelable(PLAY_DATA_STATE))?.also {
                playData = it
            }

            supportFragmentManager.findFragmentByTag(TAB_LAYOUT_FRAGMENT_TAG)?.let {
                tablayoutFragment = it as TablayoutFragment
            }
            Log.d(TAG, "savedInstanceState is not null.tablayoutFragment = $tablayoutFragment")
        }

        if (tablayoutFragment == null) tablayoutFragment = TablayoutFragment()
        supportFragmentManager.beginTransaction().apply {
            tablayoutFragment?.let {
                if (!it.isInLayout) {
                    Log.d(TAG, "tablayoutFragment.isInLayout() = false")
                    replace(R.id.tablayoutViewLayout, it, TAB_LAYOUT_FRAGMENT_TAG)
                    tablayoutViewLayout.visibility = View.VISIBLE
                    commit()
                }
            }
        }

        // for the chrome cast
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "com.smile.karaokeplayer.BuildConfig.DEBUG")
            try {
                castContext = CastContext.getSharedInstance(this)
                Log.d(TAG, "castContext = $castContext")
            } catch (e: RuntimeException) {
                castContext = null
                var cause = e.cause
                while (cause != null) {
                    if (cause is LoadingException) {
                        Log.d(TAG,"onCreate.Failed to get CastContext." +
                                "Try updating Google Play Services and restart the app.")
                    }
                    cause = cause.cause
                }
                // Unknown error. We propagate it.
                Log.d(TAG, "onCreate.Failed to get CastContext. Unknown error.")
            }
        }
        //

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d(TAG, "onBackPressedDispatcher.handleOnBackPressed")
                /*
                playerFragment?.apply {
                    onBackPressed()
                } ?: run {
                    // tablayoutFragment?.onBackPressed()
                    onBackPressedActivity()
                }
                */
                onBackPressedActivity()
            }
        })

        findViewById<FrameLayout?>(R.id.activity_main_layout).apply {
            viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    // Layout has been finished
                    // hove to use removeGlobalOnLayoutListener() method after API 16 or is API 16
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    createViewDependingOnOrientation()
                }
            })
        }
    }

    // @RequiresApi(api = Build.VERSION_CODES.M)
    private fun askIgnoreOptimizationsBattery() {
        val pm = getSystemService(Context.POWER_SERVICE) as? PowerManager
        if (pm != null && !pm.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent()
            val pName = packageName
            intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            intent.data = Uri.parse("package:$pName")
            startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (str : String? in permissions) {
            Log.d(TAG, "onRequestPermissionsResult.permissions = $str")
        }
        if (requestCode == PERMISSION_WRITE_EXTERNAL_CODE) {
            val rLen = grantResults.size
            permissionExternalStorage = rLen > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
            Log.d(TAG, "onRequestPermissionsResult.permissionExternalStorage = $permissionExternalStorage")
        }
        if (!permissionExternalStorage) {
            ScreenUtil.showToast(this, "Permission Denied", 60f, ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_LONG)
            Log.d(TAG, "onRequestPermissionsResult.Permission Denied")
            returnToPrevious(false) // exit the activity immediately
        }
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        Log.d(TAG, "onSaveInstanceState()")
        outState.putBoolean(IS_PLAY_TO_PAUSE_STATE, isPlayToPause)
        outState.putParcelable(CALLING_COMPONENT_STATE, callingComponentName)
        outState.putParcelable(PLAY_DATA_STATE, playData)
        outState.putInt(WHICH_PLAYER_STATE, whichPlayer)
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        Log.d(TAG, "onConfigurationChanged()")
        super.onConfigurationChanged(newConfig)
        setTabLayoutViewWeight(newConfig.orientation)
        setLayoutAndTextSize(newConfig.orientation)
    }

    private fun setLayoutAndTextSize(orientation: Int) {
        Log.d(TAG, "setLayoutAndTextSize")

        val textSize = if (orientation == Configuration.ORIENTATION_PORTRAIT)
            textFontSize * 1.0f else textFontSize * 0.8f
        ScreenUtil.resizeTextSize(vlcPlayerButton, textSize, ScreenUtil.FontSize_Pixel_Type)
        ScreenUtil.resizeTextSize(exoPlayerButton, textSize, ScreenUtil.FontSize_Pixel_Type)
        ScreenUtil.resizeTextSize(cancelButton, textSize, ScreenUtil.FontSize_Pixel_Type)
        ScreenUtil.resizeTextSize(isVlcCurrent, textSize, ScreenUtil.FontSize_Pixel_Type)
        ScreenUtil.resizeTextSize(isExoCurrent, textSize, ScreenUtil.FontSize_Pixel_Type)

        var percentHeightRatio = 0.6f
        var percentWidthRatio = 0.85f
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            percentHeightRatio = 0.72f
            percentWidthRatio = 0.6f
        }
        var lp = chooseButtonConstraintLayout.layoutParams as ConstraintLayout.LayoutParams
        lp.matchConstraintPercentHeight = percentHeightRatio
        lp.matchConstraintPercentWidth = percentWidthRatio

        percentHeightRatio = if (orientation == Configuration.ORIENTATION_PORTRAIT)
            0.3f else 0.45f
        lp = vlcPlayerLinearLayout.layoutParams as ConstraintLayout.LayoutParams
        lp.matchConstraintPercentHeight = percentHeightRatio
        lp = exoPlayerLinearLayout.layoutParams as ConstraintLayout.LayoutParams
        lp.matchConstraintPercentHeight = percentHeightRatio
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        LocalBroadcastManager.getInstance(this).apply {
            unregisterReceiver(baseReceiver)
        }
        super.onDestroy()
    }

    private fun onBackPressedActivity() {
        Log.d(TAG, "onBackPressedActivity() is called")
        val exitAppTimer = ExitAppTimer.getInstance(1000) // singleton class
        if (exitAppTimer.canExit()) {
            returnToPrevious(false)
        } else {
            exitAppTimer.start()
            ScreenUtil.showToast(this, getString(R.string.backKeyToExitApp), toastTextSize,
                ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_SHORT)
        }
    }

    fun onReceiveFunc(isSingleSong: Boolean, needPlay: Boolean, intent : Intent?, pData : Bundle?) {
        Log.d(TAG, "onReceiveFunc.needPlay = $needPlay")
        playerFragment?.run {
            mPresenter.let{
                it.initializeVariables(pData, intent)
                if (needPlay) it.playSongPlayedBeforeActivityCreated()
                setMainMenu()
                Log.d(TAG, "onReceiveFunc.isSingleSong = $isSingleSong")
                if (isSingleSong) {
                    it.playingParam.singleSongPlayingStatus = 1  // start playing single song
                    showPlayerView()
                } else {
                    // PlayerConstants.BackToBaseActivity
                    if (it.playingParam.isPlayerViewVisible) showPlayerView() else hidePlayerView()
                    Log.d(TAG, "onReceiveFunc.currentPlaybackState = ${it.playingParam.currentPlaybackState}")
                }
            }
            showSupportToolbarAudioControlSetTimer()
        }
        Intent().apply {
            Log.d(TAG, "onReceiveFunc.componentName = $componentName")
            component = componentName
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(this)
        }
    }

    // implementing interface PlayerBaseViewFragment.PlayBaseFragmentFunc
    override fun baseHidePlayerView() {
        Log.d(TAG, "baseHidePlayerView()")
        tablayoutViewLayout.visibility = View.VISIBLE
        tablayoutFragment?.becomeVisible()
    }
    override fun baseShowPlayerView() {
        Log.d(TAG, "baseShowPlayerView()")
        tablayoutViewLayout.visibility = View.GONE
        tablayoutFragment?.becomeInVisible()
    }

    // Implement interface PlayerBaseViewFragment.PlayBaseFragmentFunc
    override fun returnToPrevious(isSingleSong : Boolean) {
        Log.d(TAG, "returnToPrevious.isSingleSong = $isSingleSong")
        if (isSingleSong) {
            playerFragment?.mPresenter?.let {
                it.pausePlay()
                it.playingParam.singleSongPlayingStatus = 0  // exit playing single song
                callingComponentName?.let { callIt->
                    Intent().apply {
                        component = callIt
                        addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        startActivity(this)
                    }
                }
                Log.d(TAG, "returnToPrevious.preparedStatus = ${it.playingParam.preparedStatus}")
            }
            return
        }
        // exit application
        // finish()
        Log.d(TAG, "returnToPrevious.finish()")
        finishAndRemoveTask()
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) finishAndRemoveTask()
        // else finishAffinity()

        Log.d(TAG, "returnToPrevious().onDestroy()")
        onDestroy()

        MySingleTon.favorites.clear()
        MySingleTon.selectedFavorites.clear()
        MySingleTon.orderedSongs.clear()
        FileDesList.fileList.clear()

        Log.d(TAG, "returnToPrevious().Process.killProcess()")
        Process.killProcess(Process.myPid())
        // exitProcess(0);
    }
    // Finishes interface PlayerBaseViewFragment.PlayBaseFragmentFunc

    // implementing interface PlayMyFavorites
    override fun onSavePlayingState(compName : ComponentName?) {
        Log.d(TAG, "onSavePlayingState.compName = $compName")
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
                pIt.preparedStatus = 10;    // going to BaseFavoriteListActivity
            }
        }
    }

    override fun restorePlayingState() {
        Log.d(TAG, "restorePlayingState.return from BaseFavoriteListActivity")
        // come Back From Favorite
        (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            BundleCompat.getParcelable(playData, PlayerConstants.PlayingParamState, PlayingParameters::class.java)
        else playData.getParcelable(PlayerConstants.PlayingParamState))?.apply {
            Log.d(TAG, "restorePlayingState.currentPlaybackState = $currentPlaybackState")
            Log.d(TAG, "restorePlayingState.currentAudioPosition = $currentAudioPosition")
            Log.d(TAG, "restorePlayingState.preparedStatus = $preparedStatus")
            if (isPlayToPause) currentPlaybackState = PlaybackStateCompat.STATE_PLAYING // restore to playing
            preparedStatus = 4  // come Back From Favorite, simulate onStart() of PlayerBaseViewFragment
            Log.d(TAG, "restorePlayingState.preparedStatus changed to $preparedStatus")
        }
        onReceiveFunc(isSingleSong = false, needPlay = true, intent = null, pData = playData)
        callingComponentName = null
        isPlayToPause = false
    }

    override fun switchToOpenFileFragment() {
        tablayoutFragment?.switchToOpenFileFragment()
    }
    // Finishes implementing interface PlayMyFavorites

    // implementing interface PlaySongs
    @OptIn(UnstableApi::class)
    override fun playSelectedSongList(songs : ArrayList<SongInfo>) {
        Log.d(TAG, "playSelectedSongList.whichPlayer = $whichPlayer")
        Log.d(TAG, "playSelectedSongList.songs.size = ${songs.size}")
        if (songs.isEmpty()) return
        mOrderedSongs = ArrayList(songs)    // temporary memory
        isEnableView(tablayoutViewLayout, false)
        playerFragment?.disableSomeButtonsDueToBecausePopup()
        choosePlayerLayout.visibility = View.VISIBLE
        when (whichPlayer) {
            1-> {
                isVlcCurrent.visibility = View.VISIBLE
                isExoCurrent.visibility = View.GONE
            }
            2-> {
                isExoCurrent.visibility = View.VISIBLE
                isVlcCurrent.visibility = View.GONE
            }
            else->{
                isVlcCurrent.visibility = View.GONE
                isExoCurrent.visibility = View.GONE
            }
        }
    }

    private fun isEnableView(view : View, isEnable : Boolean) {
        view.let {
            it.isEnabled = isEnable
            if (it is ViewGroup) {
                for (i in 0 until it.childCount) {
                    isEnableView(it.getChildAt(i), isEnable)
                }
            }
        }
    }

    private fun startPlaySelectedSongList() {
        Log.d(TAG, "startPlaySelectedSongList.whichPlayer = $whichPlayer")
        if (whichPlayer != 1 && whichPlayer != 2) return
        mOrderedSongs?.let {
            Log.d(TAG, "startPlaySelectedSongList.mOrderedSongs.size = ${it.size}")
            if (it.isEmpty()) return
            MySingleTon.orderedSongs.clear()
            MySingleTon.orderedSongs.addAll(it)
        } ?: run {
            return
        }
        var needPlace = true
        supportFragmentManager.findFragmentByTag(PLAYER_FRAGMENT_TAG)?.let {
            playerFragment = it as PlayerBaseViewFragment
            if (playerFragment is VlcPlayerFragment) {
                Log.d(TAG, "playSelectedSongList.VlcPlayerFragment found")
                if (whichPlayer != 1) playerFragment = ExoPlayerFragment() else needPlace = false
            } else {
                // ExoPlayerFragment
                Log.d(TAG, "playSelectedSongList.ExoPlayerFragment found")
                if (whichPlayer == 1) playerFragment = VlcPlayerFragment() else needPlace = false
            }
        } ?: run {
            Log.d(TAG, "playSelectedSongList.No playerFragment found!")
            playerFragment = if (whichPlayer == 1) VlcPlayerFragment() else ExoPlayerFragment()
        }
        Log.d(TAG, "playSelectedSongList.playerFragment = $playerFragment")
        Log.d(TAG, "playSelectedSongList.needPlace = $needPlace")
        playerFragment?.let {
            if (needPlace) {
                supportFragmentManager.beginTransaction().apply {
                    if (!it.isInLayout) {
                        Log.d(TAG, "playSelectedSongList.playerFragment.isInLayout() = false")
                        replace(R.id.basePlayViewLayout, it, PLAYER_FRAGMENT_TAG)
                        commit()
                        /*  moved to PlayerBaseViewFragment under onServiceConnected()
                        it.mPresenter.playingParam.isAutoPlay = false
                        it.mPresenter.autoPlaySongList()
                        it.showPlayerView()
                        */
                    }
                }
            } else {
                it.mPresenter.playingParam.isAutoPlay = false
                it.mPresenter.autoPlaySongList()
                it.showPlayerView()
            }
        }
    }
    // Finish implementing interface PlaySongs

    private fun createViewDependingOnOrientation() {
        if (callingIntent.extras == null) {
            playerFragment?.hidePlayerView()
        }
    }

    private fun setTabLayoutViewWeight(orientation : Int) {
        Log.d(TAG, "weightSum = $weightSum")
        val layoutP = tablayoutViewLayout.layoutParams as LinearLayout.LayoutParams
        layoutP.weight = if (orientation == Configuration.ORIENTATION_LANDSCAPE) weightSum * 0.7f
        else weightSum * 0.8f
    }
}