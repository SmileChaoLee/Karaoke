package com.smile.karaokeplayer

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BundleCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media3.common.util.UnstableApi
import com.smile.karaokeplayer.constants.PlayerConstants
import com.smile.karaokeplayer.fragments.PlayerBaseFragment
import com.smile.karaokeplayer.fragments.TablayoutFragment
import com.smile.karaokeplayer.interfaces.PlayMyFavorites
import com.smile.karaokeplayer.interfaces.PlaySongs
import com.smile.karaokeplayer.models.FileDesList
import com.smile.karaokeplayer.models.MySingleTon
import com.smile.karaokeplayer.models.PlayingParameters
import com.smile.karaokeplayer.models.SongInfo

private const val TAG : String = "BaseActivity"
private const val PLAYER_FRAGMENT = "PlayerFragment"
private const val TAB_LAYOUT_FRAGMENT = "TablayoutFragment"
private const val IS_PLAY_TO_PAUSE = "IsPlayToPause"
private const val PLAY_DATA = "PlayData"
private const val CALLING_COMPONENT = "CallingComponent"

@UnstableApi
abstract class BaseActivity : AppCompatActivity(),
    PlayerBaseFragment.PlayBaseFragmentFunc,
    PlaySongs, PlayMyFavorites {

    private var playerFragment: PlayerBaseFragment? = null
    private lateinit var basePlayViewLayout : LinearLayout
    private var tablayoutFragment : TablayoutFragment? = null
    private lateinit var tablayoutViewLayout : LinearLayout
    private lateinit var baseTabLayout : LinearLayout
    private var weightSum : Float = 0f
    private lateinit var baseReceiver: BroadcastReceiver
    private lateinit var callingIntent : Intent
    private var isPlayToPause : Boolean = false
    private var hasPlayedSingle : Boolean = false
    private var callingComponentName : ComponentName? = null
    private var playData = Bundle()

    @OptIn(UnstableApi::class)
    abstract fun getFragment() : PlayerBaseFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG,"onCreate")
        window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        if (savedInstanceState == null) {
            // the orientation is always portrait when created
            Log.d(TAG,"onCreate.new created")
            requestedOrientation = when (resources.configuration.orientation) {
                Configuration.ORIENTATION_PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                Configuration.ORIENTATION_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                else -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        } else {
            // the orientation keep the one before recreated
            Log.d(TAG,"onCreate.recreated")
            requestedOrientation = when (resources.configuration.orientation) {
                Configuration.ORIENTATION_PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                Configuration.ORIENTATION_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)

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

        basePlayViewLayout = findViewById(R.id.basePlayViewLayout)
        baseTabLayout = findViewById(R.id.baseTabLayout)
        weightSum = baseTabLayout.weightSum
        tablayoutViewLayout = findViewById(R.id.tablayoutViewLayout)
        setTabLayoutViewWeight(resources.configuration.orientation)

        callingIntent = intent
        Log.d(TAG,"onCreate.callingIntent = $callingIntent")
        Log.d(TAG,"onCreate.savedInstanceState = $savedInstanceState")

        if (callingIntent.extras == null) {
            Log.d(TAG, "callingIntent.extras is null")
        } else {
            Log.d(TAG, "callingIntent.extras is not null")
        }

        if (savedInstanceState != null) {
            isPlayToPause = savedInstanceState.getBoolean(IS_PLAY_TO_PAUSE, false)

            callingComponentName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                BundleCompat.getParcelable(savedInstanceState, CALLING_COMPONENT, ComponentName::class.java)
            else savedInstanceState.getParcelable(CALLING_COMPONENT)

            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                BundleCompat.getParcelable(savedInstanceState, PLAY_DATA, Bundle::class.java)
            else savedInstanceState.getParcelable(PLAY_DATA))?.also {
                playData = it
            }
            playerFragment = supportFragmentManager.findFragmentByTag(PLAYER_FRAGMENT) as PlayerBaseFragment
            Log.d(TAG, "playerFragment = $playerFragment")
            tablayoutFragment = supportFragmentManager.findFragmentByTag(TAB_LAYOUT_FRAGMENT) as TablayoutFragment?
            Log.d(TAG, "tablayoutFragment = $tablayoutFragment")
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
                    Log.d(TAG, "tablayoutFragment.isInLayout() = false")
                    replace(R.id.tablayoutViewLayout, it, TAB_LAYOUT_FRAGMENT)
                    tablayoutViewLayout.visibility = View.VISIBLE
                    isReplaced = true
                }
            }
            playerFragment?.let {
                if (!it.isInLayout) {
                    Log.d(TAG, "playerFragment.isInLayout() = false")
                    replace(R.id.basePlayViewLayout, it, PLAYER_FRAGMENT)
                    isReplaced = true
                }
            }
            if (isReplaced) commit()
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d(TAG, "onBackPressedDispatcher.handleOnBackPressed")
                playerFragment?.onBackPressed()
            }
        })

        findViewById<FrameLayout>(R.id.activity_base_layout).apply {
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

    override fun onResume() {
        Log.d(TAG, "onResume()")
        super.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        Log.d(TAG, "onSaveInstanceState()")
        outState.putBoolean(IS_PLAY_TO_PAUSE, isPlayToPause)
        outState.putParcelable(CALLING_COMPONENT, callingComponentName)
        outState.putParcelable(PLAY_DATA, playData)
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        Log.d(TAG, "onConfigurationChanged()")
        super.onConfigurationChanged(newConfig)
        setTabLayoutViewWeight(newConfig.orientation)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
        LocalBroadcastManager.getInstance(this).apply {
            unregisterReceiver(baseReceiver)
        }
        super.onDestroy()
    }

    fun onReceiveFunc(isSingleSong: Boolean, needPlay: Boolean,
                      intent : Intent?, pData : Bundle?) {
        Log.d(TAG, "onReceiveFunc.needPlay = $needPlay")
        playerFragment?.run {
            mPresenter.let{
                it.initializeVariables(pData, intent,
                    it.playingParam.isAutoPlay)
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
                callingComponentName?.let { callIt ->
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
        MySingleTon.favorites.clear()
        MySingleTon.selectedFavorites.clear()
        MySingleTon.orderedSongs.clear()
        FileDesList.fileList.clear()
        // exit application
        finish()
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
                // pIt.preparedStatus = 10   // going to BaseFavoriteListActivity
                pIt.wentToFavorite = true   // going to BaseFavoriteListActivity
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
            Log.d(TAG, "restorePlayingState.wentToFavorite = $wentToFavorite")
            if (isPlayToPause) currentPlaybackState = PlaybackStateCompat.STATE_PLAYING // restore to playing
            preparedStatus = 4  // come Back From Favorite, simulate onStart() of PlayerBaseViewFragment
            Log.d(TAG, "restorePlayingState.preparedStatus changed to $preparedStatus")
            wentToFavorite = false  // set back to default
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
    override fun playSelectedSongList(songs: ArrayList<SongInfo>) {
        Log.d(TAG, "playSelectedSongList.songs.size" +
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
    // Finish implementing interface PlaySongs

    private fun createViewDependingOnOrientation() {
        Log.d(TAG, "createViewDependingOnOrientation")
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