package com.smile.karaokeplayer.fragments

// import com.smile.karaokeplayer.models.VerticalSeekBar
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.ads.nativetemplates.TemplateView
import com.smile.karaokeplayer.BaseApplication
import com.smile.karaokeplayer.R
import com.smile.karaokeplayer.constants.CommonConstants
import com.smile.karaokeplayer.constants.PlayerConstants
import com.smile.karaokeplayer.models.MySingleTon
import com.smile.karaokeplayer.models.SongListSQLite
import com.smile.karaokeplayer.presenters.BasePlayerPresenter
import com.smile.karaokeplayer.presenters.BasePlayerPresenter.BasePresentView
import com.smile.karaokeplayer.utilities.BannerAdUtil
import com.smile.karaokeplayer.utilities.MyBannerAdView
import com.smile.nativetemplates_models.GoogleAdMobNativeTemplate
import com.smile.smilelibraries.interfaces.DismissFunction
import com.smile.smilelibraries.models.ExitAppTimer
import com.smile.smilelibraries.privacy_policy.PrivacyPolicyUtil
import com.smile.smilelibraries.show_banner_ads.SetBannerAdView
import com.smile.smilelibraries.show_interstitial_ads.ShowInterstitial
import com.smile.smilelibraries.utilities.ScreenUtil

private const val TAG: String = "PlayerBaseViewFragment"

abstract class PlayerBaseViewFragment : Fragment(), BasePresentView {

    interface PlayBaseFragmentFunc {
        fun baseHidePlayerView()
        fun baseShowPlayerView()
        fun returnToPrevious(isSingleSong : Boolean)
    }

    lateinit var mPresenter: BasePlayerPresenter
    private var screenSizeX = 0
    private var screenSizeY = 0
    private var playBaseFragmentFunc: PlayBaseFragmentFunc? = null
    protected var fragmentView: View? = null
    protected var textFontSize = 0f
    private var fontScale = 0f
    protected var toastTextSize = 0f
    protected var playerViewLinearLayout: LinearLayout? = null
    private var supportToolbar // use customized ToolBar
            : androidx.appcompat.widget.Toolbar? = null
    private var actionMenuView: ActionMenuView? = null
    private var audioControllerView: LinearLayout? = null
    // protected var volumeSeekBar: VerticalSeekBar? = null
    private var nonVolumeImageButton: ImageButton? = null
    private var volumeImageButton: ImageButton? = null
    private var previousMediaImageButton: ImageButton? = null
    private var playMediaImageButton: ImageButton? = null
    private var replayMediaImageButton: ImageButton? = null
    private var pauseMediaImageButton: ImageButton? = null
    private var stopMediaImageButton: ImageButton? = null
    private var nextMediaImageButton: ImageButton? = null
    private var heartImageButton: ImageButton? = null

    private var playingTimeTextView: TextView? = null
    private var player_duration_seekbar: AppCompatSeekBar? = null
    private var durationTimeTextView: TextView? = null

    private var orientationImageButton: ImageButton? = null
    private var repeatImageButton: ImageButton? = null
    private var switchToMusicImageButton: ImageButton? = null
    // protected var switchToVocalImageButton: ImageButton? = null
    private var switchToVocalImageButton: ImageButton? = null
    private var hideVideoImageButton: ImageButton? = null
    private var actionMenuImageButton: ImageButton? = null
    private var audioChannelImageButton: ImageButton? = null
    private var audioTrackImageButton: ImageButton? = null
    // private var volumeSeekBarHeightForLandscape = 0

    private var bannerLinearLayout: LinearLayout? = null
    private var myBannerAdView: SetBannerAdView? = null
    private var nativeTemplate: GoogleAdMobNativeTemplate? = null

    private var message_area_LinearLayout: LinearLayout? = null
    private var bufferingStringTextView: TextView? = null
    private var animationText: Animation? = null
    private var nativeAdsFrameLayout: FrameLayout? = null
    private var nativeAdViewVisibility = 0
    private var nativeAdTemplateView: TemplateView? = null
    protected var mainMenu: Menu? = null

    // submenu of file
    private var autoPlayMenuItem: MenuItem? = null
    private var audioMenuItem: MenuItem? = null
    // submenu of audio
    private var audioTrackMenuItem: MenuItem? = null
    // submenu of channel
    private var leftChannelMenuItem: MenuItem? = null
    private var rightChannelMenuItem: MenuItem? = null
    private var stereoChannelMenuItem: MenuItem? = null

    private var oldMotionEventX = 0.0f

    private val controllerTimerHandler = Handler(Looper.getMainLooper())
    private val controllerTimerRunnable = Runnable {
        Log.d(TAG, "controllerTimerRunnable() is called")
        controllerTimerHandler.removeCallbacksAndMessages(null)
        mPresenter.playingParam?.let {
            if (it.preparedStatus != 0) {
                Log.d(TAG, "controllerTimerRunnable.playingParam.preparedStatus != 0")
                if (supportToolbar?.visibility == View.VISIBLE) {
                    // hide supportToolbar
                    hideSupportToolbarAndAudioController()
                }
            } else {
                showSupportToolbarAudioControlSetTimer()
            }
        }
    }

    private var interstitialAd: ShowInterstitial? = null

    abstract fun getPlayerPresenter(): BasePlayerPresenter?
    abstract fun setMediaRouteButtonView(buttonMarginLeft: Int, imageButtonHeight: Int)
    // abstract fun setMediaRouteButtonVisible()
    abstract fun setMenuItemsVisibility()
    // abstract fun setSwitchToVocalImageButtonVisibility()
    abstract fun onPlayServiceConnected(service: IBinder)
    abstract fun onPlayServiceDisconnected()
    abstract fun startAndBindPlayService()
    abstract fun unbindAndStopPlayService()
    abstract fun audioChannelButtonListener()

    protected var isServiceBound: Boolean = false
    protected var isServiceDestroyed: Boolean = true
    protected val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.d(TAG, "onServiceConnected")
            onPlayServiceConnected(service)
            isServiceBound = true
            isServiceDestroyed = false
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.d(TAG, "onServiceDisconnected")
            isServiceBound = false
            isServiceDestroyed = true
            onPlayServiceDisconnected()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        arguments?.let {
            Log.d(TAG, "onCreate.arguments is not null")
        }
        // keep the screen on all the time, added on 2021-02-18
        activity?.window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        setHasOptionsMenu(true) // must have because it has menu

        activity?.let {
            interstitialAd = ShowInterstitial(it,
                    (it.application as BaseApplication).facebookInterstitial,
                    (it.application as BaseApplication).adMobInterstitial)
        }

        val presenter = getPlayerPresenter()
        if (presenter == null) {
            Log.d(TAG, "onCreate.presenter is null so exit activity.")
            playBaseFragmentFunc?.returnToPrevious(false)
            return
        }

        activity?.let {
            if (it is PlayBaseFragmentFunc) playBaseFragmentFunc = it
            Log.d(TAG, "onCreate.playBaseFragmentFunc = $playBaseFragmentFunc")
        }

        mPresenter = presenter
        textFontSize = mPresenter.textFontSize
        fontScale = mPresenter.fontScale
        toastTextSize = mPresenter.toastTextSize
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView() is called")
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_player_base_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated()")
        super.onViewCreated(view, savedInstanceState)

        fragmentView = view
        // Video player view
        fragmentView?.apply {
            playerViewLinearLayout = findViewById(R.id.playerViewLinearLayout)
            supportToolbar = findViewById(R.id.player_view_toolbar)
            supportToolbar?.visibility = View.VISIBLE
        }

        activity?.let {
            screenSizeX = ScreenUtil.getScreenSize(it).x
            screenSizeY = ScreenUtil.getScreenSize(it).y
            (it as AppCompatActivity).apply {
                setSupportActionBar(supportToolbar)
                supportActionBar?.setDisplayShowTitleEnabled(false)
            }
            // it.setActionBar(supportToolbar)
            // it.actionBar?.setDisplayShowTitleEnabled(false)
        }

        actionMenuView = supportToolbar?.findViewById(R.id.actionMenuViewLayout) // main menu
        fragmentView?.apply {
            audioControllerView = findViewById(R.id.audioControllerView)
            /*
            volumeSeekBar = findViewById(R.id.volumeSeekBar)
            volumeSeekBar?.layoutParams?.let {
                volumeSeekBarHeightForLandscape = it.height
            }
            */
            nonVolumeImageButton = findViewById(R.id.nonVolumeImageButton)
            volumeImageButton = findViewById(R.id.volumeImageButton)
            previousMediaImageButton = findViewById(R.id.previousMediaImageButton)
            playMediaImageButton = findViewById(R.id.playMediaImageButton)
            pauseMediaImageButton = findViewById(R.id.pauseMediaImageButton)
            mPresenter.playingParam.let {
                if (it.currentPlaybackState == PlaybackStateCompat.STATE_PLAYING) {
                    playButtonOffPauseButtonOn()
                } else {
                    playButtonOnPauseButtonOff()
                }
            }

            replayMediaImageButton = findViewById(R.id.replayMediaImageButton)
            stopMediaImageButton = findViewById(R.id.stopMediaImageButton)
            nextMediaImageButton = findViewById(R.id.nextMediaImageButton)
            heartImageButton = findViewById(R.id.heartImageButton)

            orientationImageButton = findViewById(R.id.orientationImageButton)
            repeatImageButton = findViewById(R.id.repeatImageButton)
            switchToMusicImageButton = findViewById(R.id.switchToMusicImageButton)
            switchToVocalImageButton = findViewById(R.id.switchToVocalImageButton)
            hideVideoImageButton = findViewById(R.id.hideVideoImageButton)
            actionMenuImageButton = findViewById(R.id.actionMenuImageButton)

            audioChannelImageButton = findViewById(R.id.audioChannelImageButton)
            audioTrackImageButton = findViewById(R.id.audioTrackImageButton)

            bannerLinearLayout = findViewById(R.id.bannerLinearLayout)
            activity?.let {actIt ->
                bannerLinearLayout?.also {layoutIt ->
                    layoutIt.visibility = View.VISIBLE // Show Banner Ad
                    myBannerAdView = BannerAdUtil.getBannerAdView(actIt as Activity, null,
                        layoutIt, actIt.resources.configuration.orientation)
                    myBannerAdView?.showBannerAdView(0) // Admob first
                }
            }

            // message area
            message_area_LinearLayout = findViewById(R.id.message_area_LinearLayout)
            message_area_LinearLayout?.visibility = View.GONE
            bufferingStringTextView = findViewById(R.id.bufferingStringTextView)
            ScreenUtil.resizeTextSize(bufferingStringTextView, textFontSize, ScreenUtil.FontSize_Pixel_Type)
        }

        fragmentView?.apply {

        }

        animationText = AlphaAnimation(0.0f, 1.0f)
        animationText?.duration = 500
        animationText?.startOffset = 0
        animationText?.repeatMode = Animation.REVERSE
        animationText?.repeatCount = Animation.INFINITE

        fragmentView?.apply {
            val durationTextSize = textFontSize * 0.6f
            playingTimeTextView = findViewById(R.id.playingTimeTextView)
            playingTimeTextView?.text = "000:00"
            ScreenUtil.resizeTextSize(playingTimeTextView, durationTextSize, ScreenUtil.FontSize_Pixel_Type)
            player_duration_seekbar = findViewById(R.id.player_duration_seekbar)
            durationTimeTextView = findViewById(R.id.durationTimeTextView)
            durationTimeTextView?.text = "000:00"
            ScreenUtil.resizeTextSize(durationTimeTextView, durationTextSize, ScreenUtil.FontSize_Pixel_Type)
            nativeAdsFrameLayout = findViewById(R.id.nativeAdsFrameLayout)
            nativeAdsFrameLayout?.let {
                nativeAdViewVisibility = it.visibility
            }
            nativeAdTemplateView = findViewById(R.id.nativeAdTemplateView)
            nativeTemplate = GoogleAdMobNativeTemplate(
                    activity, nativeAdsFrameLayout, BaseApplication.googleAdMobNativeID, nativeAdTemplateView
            )
        }

        // must before setImageButtonStatus() and showNativeAndBannerAd
        mPresenter.playingParam.let {
            if (it.isPlayerViewVisible) showPlayerView() else hidePlayerView()
        }

        setImageButtonStatus() // must before setButtonsPositionAndSize()
        setButtonsPositionAndSize(resources.configuration)
        setOnClickEvents()
        showNativeAndHideBannerAd()

        Log.d(TAG, "onViewCreated() is finished.")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        Log.d(TAG, "onCreateOptionsMenu() is called")
        // Inflate the menu; this adds items to the action bar if it is present.
        // mainMenu = menu;
        // menu.clear() does not work for the issue of onCreateOptionsMenu being called multiple times
        mainMenu = actionMenuView?.menu
        mainMenu?.clear()    // to avoid the issue of onCreateOptionsMenu being called multiple times
        inflater.inflate(R.menu.menu_main, mainMenu)
        // final Context wrapper = new ContextThemeWrapper(this, R.style.menu_text_style);
        // or
        supportToolbar?.popupTheme?.let {
            val wrapper: Context = ContextThemeWrapper(activity, it)
            // ScreenUtil.buildActionViewClassMenu(activity, wrapper, mainMenu, fontScale, BaseApplication.FontSize_Scale_Type);
            ScreenUtil.resizeMenuTextIconSize(wrapper, mainMenu, fontScale)
        }

        // submenu of file
        mainMenu?.let {
            autoPlayMenuItem = it.findItem(R.id.autoPlay)
            audioMenuItem = it.findItem(R.id.audio)
            // submenu of audio
            audioTrackMenuItem = it.findItem(R.id.audioTrack)
            // submenu of channel
            leftChannelMenuItem = it.findItem(R.id.leftChannel)
            rightChannelMenuItem = it.findItem(R.id.rightChannel)
            stereoChannelMenuItem = it.findItem(R.id.stereoChannel)
        }

        setMainMenu()

        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val playingParam = mPresenter.playingParam
        val currentChannelPlayed = playingParam.currentChannelPlayed
        if (item.hasSubMenu()) {
            item.subMenu?.clearHeader()
        }
        val id = item.itemId
        if (id == R.id.autoPlay) {
            // item.isChecked() return the previous value
            mPresenter.setAutoPlayStatusAndAction()
        } else if (id == R.id.privacyPolicy) {
            PrivacyPolicyUtil.startPrivacyPolicyActivity(
                activity,
                PlayerConstants.PrivacyPolicyActivityRequestCode
            )
        } else if (id == R.id.exit) {
            closeFragment()
        } else if (id == R.id.audioTrack) {
            // if there are audio tracks
            item.subMenu?.let {
                for (i in 0 until it.size()) {
                    val mItem = it.getItem(i)
                    // audio track index start from 1 for user interface
                    if (i + 1 == playingParam.currentAudioTrackIndexPlayed) {
                        mItem.isCheckable = true
                        mItem.isChecked = true
                    } else {
                        mItem.isCheckable = false
                    }
                }
            }
        } else if (id == R.id.audioTrack1) {
            mPresenter.setAudioTrackAndChannel(1, currentChannelPlayed)
        } else if (id == R.id.audioTrack2) {
            mPresenter.setAudioTrackAndChannel(2, currentChannelPlayed)
        } else if (id == R.id.audioTrack3) {
            mPresenter.setAudioTrackAndChannel(3, currentChannelPlayed)
        } else if (id == R.id.audioTrack4) {
            mPresenter.setAudioTrackAndChannel(4, currentChannelPlayed)
        } else if (id == R.id.audioTrack5) {
            mPresenter.setAudioTrackAndChannel(5, currentChannelPlayed)
        } else if (id == R.id.audioTrack6) {
            mPresenter.setAudioTrackAndChannel(6, currentChannelPlayed)
        } else if (id == R.id.audioTrack7) {
            mPresenter.setAudioTrackAndChannel(7, currentChannelPlayed)
        } else if (id == R.id.audioTrack8) {
            mPresenter.setAudioTrackAndChannel(8, currentChannelPlayed)
        } else if (id == R.id.channel) {
            val mediaUri = mPresenter.mediaUri
            val numberOfAudioTracks = mPresenter.numberOfAudioTracks
            if (mediaUri != null && Uri.EMPTY != mediaUri && numberOfAudioTracks > 0) {
                leftChannelMenuItem?.isEnabled = true
                rightChannelMenuItem?.isEnabled = true
                stereoChannelMenuItem?.isEnabled = true
                if (playingParam.preparedStatus != 0) {
                    if (currentChannelPlayed == CommonConstants.LeftChannel) {
                        leftChannelMenuItem?.isCheckable = true
                        leftChannelMenuItem?.isChecked = true
                    } else {
                        leftChannelMenuItem?.isCheckable = false
                        leftChannelMenuItem?.isChecked = false
                    }
                    if (currentChannelPlayed == CommonConstants.RightChannel) {
                        rightChannelMenuItem?.isCheckable = true
                        rightChannelMenuItem?.isChecked = true
                    } else {
                        rightChannelMenuItem?.isCheckable = false
                        rightChannelMenuItem?.isChecked = false
                    }
                    if (currentChannelPlayed == CommonConstants.StereoChannel) {
                        stereoChannelMenuItem?.isCheckable = true
                        stereoChannelMenuItem?.isChecked = true
                    } else {
                        stereoChannelMenuItem?.isCheckable = false
                        stereoChannelMenuItem?.isChecked = false
                    }
                } else {
                    leftChannelMenuItem?.isCheckable = false
                    leftChannelMenuItem?.isChecked = false
                    rightChannelMenuItem?.isCheckable = false
                    rightChannelMenuItem?.isChecked = false
                    stereoChannelMenuItem?.isCheckable = false
                    stereoChannelMenuItem?.isChecked = false
                }
            } else {
                leftChannelMenuItem?.isEnabled = false
                rightChannelMenuItem?.isEnabled = false
                stereoChannelMenuItem?.isEnabled = false
            }
        } else if (id == R.id.leftChannel) {
            mPresenter.playLeftChannel()
        } else if (id == R.id.rightChannel) {
            mPresenter.playRightChannel()
        } else if (id == R.id.stereoChannel) {
            mPresenter.playStereoChannel()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        Log.d(TAG, "onStart")
        super.onStart()
        mPresenter.playingParam?.let {
            Log.d(TAG, "onStart.preparedStatus = ${it.preparedStatus}")
            if (it.preparedStatus == 3) { // running in the background before
                // set to come back from background
                it.preparedStatus = 4
            }
        }
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()
        myBannerAdView?.resume()
        MyBannerAdView.setVisible(resources.configuration.orientation, bannerLinearLayout
            , nativeAdViewVisibility)
        startAndBindPlayService()
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
        myBannerAdView?.pause()
        bannerLinearLayout?.visibility = View.GONE
    }

    override fun onStop() {
        Log.d(TAG, "onStop")
        super.onStop()
        mPresenter.playingParam?.let {
            Log.d(TAG, "onStop.isPlaySingleSong = ${it.isPlaySingleSong}")
            it.preparedStatus = 3 // running in background
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        Log.d(TAG, "onConfigurationChanged()")
        super.onConfigurationChanged(newConfig)
        closeMenu(mainMenu)
        setOrientationImageButton(newConfig.orientation)
        setButtonsPositionAndSize(newConfig)
        activity?.let {actIt ->
            screenSizeX = ScreenUtil.getScreenSize(actIt).x
            screenSizeY = ScreenUtil.getScreenSize(actIt).y
            myBannerAdView?.destroy()
            bannerLinearLayout?.also {layoutIt ->
                layoutIt.visibility = View.VISIBLE // Show Banner Ad
                myBannerAdView = BannerAdUtil.getBannerAdView(actIt as Activity, null,
                        layoutIt, newConfig.orientation)
                myBannerAdView?.showBannerAdView(0) // AdMob first
            }
        }
        MyBannerAdView.setVisible(newConfig.orientation, bannerLinearLayout
            , nativeAdViewVisibility)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d(TAG, "onSaveInstanceState() is called.")
        mPresenter.saveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy() is called.")
        myBannerAdView?.destroy()
        nativeTemplate?.release()
        interstitialAd?.close()
        // clear the screen on, added on 2021-02-18
        activity?.window?.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        unbindAndStopPlayService()
        super.onDestroy()
    }

    fun onBackPressed() {
        Log.d(TAG, "onBackPressed() is called")
        val exitAppTimer = ExitAppTimer.getInstance(1000) // singleton class
        if (exitAppTimer.canExit()) {
            closeFragment()
        } else {
            exitAppTimer.start()
            ScreenUtil.showToast(activity, getString(R.string.backKeyToExitApp), toastTextSize,
                ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_SHORT)
        }
    }

    fun setMainMenu() {
        Log.d(TAG, "setMainMenu")
        mPresenter.playingParam.let {
            val isVisible = !it.isPlaySingleSong
            autoPlayMenuItem?.isVisible = isVisible
            audioMenuItem?.isVisible = isVisible
            audioTrackMenuItem?.isVisible = isVisible
            val channelMenuItem = mainMenu?.findItem(R.id.channel)
            channelMenuItem?.isVisible = isVisible
            val privacyPolicyMenuItem = mainMenu?.findItem(R.id.privacyPolicy)
            privacyPolicyMenuItem?.isVisible = isVisible
        }
        setMenuItemsVisibility() // abstract method
    }

    override fun hidePlayerView() {
        Log.d(TAG, "hidePlayerView() is called")
        playerViewLinearLayout?.visibility = View.INVISIBLE
        hideNativeAd()
        setImageButtonStatus()
        // must be after statement of playerViewLinearLayout.visibility = View.INVISIBLE
        controllerTimerHandler.removeCallbacksAndMessages(null) // cancel the timer
        playBaseFragmentFunc?.baseHidePlayerView()
        mPresenter.playingParam.isPlayerViewVisible = false
    }
    override fun showPlayerView() {
        Log.d(TAG, "showPlayerView() is called")
        playerViewLinearLayout?.visibility = View.VISIBLE
        mPresenter.run {
            if ( (playingParam.currentPlaybackState != PlaybackStateCompat.STATE_PLAYING) ||
                    (numberOfVideoTracks == 0) ) {
                // not playing then show ads or only music is being played, show native ads
                showNativeAndHideBannerAd()
            }
        }
        setImageButtonStatus()
        // must be after statement of playerViewLinearLayout.visibility = View.VISIBLE
        setTimerToHideSupportAudioControl()   // reset the timer
        playBaseFragmentFunc?.baseShowPlayerView()
        mPresenter.playingParam.isPlayerViewVisible = true
    }

    private fun setButtonsPositionAndSize(config: Configuration) {
        Log.d(TAG, "setButtonsPositionAndSize")
        var buttonMarginLeft = (60.0f * fontScale).toInt() // 60 pixels = 20dp on Nexus 5
        var buttonMarginLeft2 = buttonMarginLeft
        // val screenSize = ScreenUtil.getScreenSize(activity)
        // Log.d(TAG, "screenSize.x = ${screenSize.x}, screenSize.y = ${screenSize.y}, buttonMarginLeft = $buttonMarginLeft")
        Log.d(TAG, "screenSize.x = $screenSizeX, screenSize.y = $screenSizeX, buttonMarginLeft = $buttonMarginLeft")
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            buttonMarginLeft =
                (buttonMarginLeft.toFloat() * (screenSizeX.toFloat() / screenSizeY.toFloat())).toInt()
            Log.d(TAG, "buttonMarginLeft = $buttonMarginLeft")
        }
        val buttonNum = 8 // 8 buttons
        val imageButtonHeight = (textFontSize * 1.2f).toInt()
        val maxWidth = buttonNum * imageButtonHeight + (buttonNum - 1) * buttonMarginLeft
        if (maxWidth > screenSizeX) {
            Log.d(TAG, "maxWidth > screenSize.x")
            // greater than the width of screen
            buttonMarginLeft = (screenSizeX - 10 - buttonNum * imageButtonHeight) / (buttonNum-1)
        }
        Log.d(TAG, "buttonMarginLeft = $buttonMarginLeft")
        val buttonNum2 = 8
        val maxWidth2 = buttonNum2 * imageButtonHeight + (buttonNum2 - 1) * buttonMarginLeft2
        if (maxWidth2 > screenSizeX) {
            // greater than the width of screen
            buttonMarginLeft2 = (screenSizeX - 10 - buttonNum2 * imageButtonHeight) / (buttonNum2 - 1)
        }
        /*
        var layoutParams: MarginLayoutParams = volumeSeekBar?.layoutParams as MarginLayoutParams
        layoutParams.width = imageButtonHeight
        layoutParams.setMargins(0, 0, 0, 0)
        */

        val volumeButtonFrameLayout: FrameLayout? =
            fragmentView?.findViewById(R.id.volumeButtonFrameLayout)
        var layoutParams : MarginLayoutParams = volumeButtonFrameLayout?.layoutParams as MarginLayoutParams
        layoutParams.height = imageButtonHeight
        layoutParams.width = imageButtonHeight
        layoutParams.setMargins(0, 0, 0, 0)
        layoutParams = nonVolumeImageButton?.layoutParams as MarginLayoutParams
        layoutParams.height = imageButtonHeight
        layoutParams.width = imageButtonHeight
        layoutParams = volumeImageButton?.layoutParams as MarginLayoutParams
        layoutParams.height = imageButtonHeight
        layoutParams.width = imageButtonHeight
        layoutParams = previousMediaImageButton?.layoutParams as MarginLayoutParams
        layoutParams.height = imageButtonHeight
        layoutParams.width = imageButtonHeight
        layoutParams.setMargins(buttonMarginLeft, 0, 0, 0)
        val playPauseButtonFrameLayout: FrameLayout? =
            fragmentView?.findViewById(R.id.playPauseButtonFrameLayout)
        layoutParams = playPauseButtonFrameLayout?.layoutParams as MarginLayoutParams
        layoutParams.height = imageButtonHeight
        layoutParams.width = imageButtonHeight
        layoutParams.setMargins(buttonMarginLeft, 0, 0, 0)
        layoutParams = playMediaImageButton?.layoutParams as MarginLayoutParams
        layoutParams.height = imageButtonHeight
        layoutParams.width = imageButtonHeight
        layoutParams = pauseMediaImageButton?.layoutParams as MarginLayoutParams
        layoutParams.height = imageButtonHeight
        layoutParams.width = imageButtonHeight
        layoutParams = replayMediaImageButton?.layoutParams as MarginLayoutParams
        layoutParams.height = imageButtonHeight
        layoutParams.width = imageButtonHeight
        layoutParams.setMargins(buttonMarginLeft, 0, 0, 0)
        layoutParams = stopMediaImageButton?.layoutParams as MarginLayoutParams
        layoutParams.height = imageButtonHeight
        layoutParams.width = imageButtonHeight
        layoutParams.setMargins(buttonMarginLeft, 0, 0, 0)
        layoutParams = nextMediaImageButton?.layoutParams as MarginLayoutParams
        layoutParams.height = imageButtonHeight
        layoutParams.width = imageButtonHeight
        layoutParams.setMargins(buttonMarginLeft, 0, 0, 0)
        layoutParams = heartImageButton?.layoutParams as MarginLayoutParams
        layoutParams.height = imageButtonHeight
        layoutParams.width = imageButtonHeight
        layoutParams.setMargins(buttonMarginLeft, 0, 0, 0)
        layoutParams = actionMenuView?.layoutParams as MarginLayoutParams
        layoutParams.height = imageButtonHeight
        layoutParams.width = imageButtonHeight
        layoutParams.setMargins(buttonMarginLeft, 0, 0, 0)
        val tempBitmap = BitmapFactory.decodeResource(resources, R.drawable.circle_and_three_dots)
        val iconDrawable: Drawable = BitmapDrawable(
                resources,
                Bitmap.createScaledBitmap(tempBitmap, imageButtonHeight, imageButtonHeight, true)
        )
        actionMenuView?.overflowIcon = iconDrawable // set icon of three dots for ActionMenuView
        // supportToolbar.setOverflowIcon(iconDrawable);   // set icon of three dots for toolbar

        layoutParams = orientationImageButton?.layoutParams as MarginLayoutParams
        layoutParams.height = imageButtonHeight
        layoutParams.width = imageButtonHeight
        layoutParams.setMargins(0, 0, 0, 0)
        layoutParams = repeatImageButton?.layoutParams as MarginLayoutParams
        layoutParams.height = imageButtonHeight
        layoutParams.width = imageButtonHeight
        layoutParams.setMargins(buttonMarginLeft2, 0, 0, 0)
        layoutParams = switchToMusicImageButton?.layoutParams as MarginLayoutParams
        layoutParams.height = imageButtonHeight
        layoutParams.width = imageButtonHeight
        layoutParams.setMargins(buttonMarginLeft2, 0, 0, 0)
        layoutParams = switchToVocalImageButton?.layoutParams as MarginLayoutParams
        layoutParams.height = imageButtonHeight
        layoutParams.width = imageButtonHeight
        layoutParams.setMargins(buttonMarginLeft2, 0, 0, 0)
        layoutParams = hideVideoImageButton?.layoutParams as MarginLayoutParams
        layoutParams.height = imageButtonHeight
        layoutParams.width = imageButtonHeight
        layoutParams.setMargins(buttonMarginLeft2, 0, 0, 0)
        setMediaRouteButtonView(buttonMarginLeft2, imageButtonHeight)
        layoutParams = actionMenuImageButton?.layoutParams as MarginLayoutParams
        layoutParams.height = imageButtonHeight
        layoutParams.width = imageButtonHeight
        layoutParams.setMargins(buttonMarginLeft2, 0, 0, 0)

        layoutParams = audioChannelImageButton?.layoutParams as MarginLayoutParams
        layoutParams.height = imageButtonHeight
        layoutParams.width = imageButtonHeight
        layoutParams.setMargins(buttonMarginLeft2, 0, 0, 0)
        layoutParams = audioTrackImageButton?.layoutParams as MarginLayoutParams
        layoutParams.height = imageButtonHeight
        layoutParams.width = imageButtonHeight
        layoutParams.setMargins(buttonMarginLeft2, 0, 0, 0)

        // reset the heights of volumeBar and supportToolbar
        // val timesOfVolumeBarForPortrait = 1.5f
        /*
        if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // if orientation is portrait, then double the height of volumeBar
            volumeSeekBar?.layoutParams?.height =
                (volumeSeekBarHeightForLandscape.toFloat().times(timesOfVolumeBarForPortrait)).toInt()
        } else {
            volumeSeekBar?.layoutParams?.height = volumeSeekBarHeightForLandscape
        }
        */
        supportToolbar?.layoutParams?.height = previousMediaImageButton?.layoutParams?.height
        val bannerAdsLayout: LinearLayout? = fragmentView?.findViewById(R.id.bannerAdsLayout)
        val bannerAdsLayoutLP = bannerAdsLayout?.layoutParams as ConstraintLayout.LayoutParams
        val nativeAdLayout: FrameLayout? = fragmentView?.findViewById(R.id.nativeAdLayout)
        val nativeAdLayoutLP = nativeAdLayout?.layoutParams as ConstraintLayout.LayoutParams
        val bannerHeightPercent = bannerAdsLayoutLP.matchConstraintPercentHeight
        Log.d(TAG,"bannerHeightPercent = $bannerHeightPercent")
        val heightPercent = 1.0f - bannerHeightPercent - imageButtonHeight * 3.30f / screenSizeY
        Log.d(TAG, "heightPercent = $heightPercent")
        nativeAdLayoutLP.matchConstraintPercentHeight = (heightPercent * 100.0f).toInt() / 100.0f
        Log.d(TAG, "nativeAdLayoutLP.matchConstraintPercentHeight = " +
                nativeAdLayoutLP.matchConstraintPercentHeight)

        // setting the width and the margins for nativeAdTemplateView
        layoutParams = nativeAdTemplateView?.layoutParams as MarginLayoutParams
        // 6 buttons and 5 gaps
        layoutParams.width = imageButtonHeight * 6 + buttonMarginLeft * 5
        layoutParams.setMargins(0, 0, 0, 0)
        //
    }

    private fun closeMenu(menu: Menu?) {
        menu?.let {
            for (i in 0 until it.size()) {
                it.getItem(i)?.subMenu?.let { it2 ->
                    closeMenu(it2)
                }
            }
            it.close()
        }
    }

    private fun closeFragment() {
        Log.d(TAG, "closeFragment.isPlaySingleSong = " + mPresenter.playingParam.isPlaySingleSong)
        playBaseFragmentFunc?.returnToPrevious(mPresenter.playingParam.isPlaySingleSong)
    }

    fun showSupportToolbarAudioControlSetTimer() {
        Log.d(TAG, "showSupportToolbarAudioControlSetTimer()")
        showSupportToolbarAudioControl()
        setTimerToHideSupportAudioControl()   // reset the timer
    }

    private fun showSupportToolbarAudioControl() {
        Log.d(TAG, "showSupportToolbarAudioControl")
        bannerLinearLayout?.visibility = View.GONE
        supportToolbar?.visibility = View.VISIBLE
        audioControllerView?.visibility = View.VISIBLE
        nativeAdsFrameLayout?.visibility = nativeAdViewVisibility
    }

    private fun hideSupportToolbarAndAudioController() {
        Log.d(TAG, "hideSupportToolbarAndAudioController()")
        if (playerViewLinearLayout?.visibility == View.VISIBLE) {
            supportToolbar?.visibility = View.GONE
            audioControllerView?.visibility = View.GONE
            nativeAdsFrameLayout?.visibility = nativeAdViewVisibility
            closeMenu(mainMenu)
            MyBannerAdView.setVisible(resources.configuration.orientation, bannerLinearLayout
                , nativeAdViewVisibility)
        }
    }

    private fun disableButtonForSometime(button: View) {
        val seconds = 0.2f  // 200 ms
        button.isEnabled = false
        val handler = Handler(Looper.getMainLooper())
        val runnable = Runnable {
            handler.removeCallbacksAndMessages(null)
            button.isEnabled = true
        }
        handler.postDelayed(runnable, (seconds * 1000.0).toLong())
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setOnClickEvents() {
        /*
        volumeSeekBar?.visibility = View.INVISIBLE // default is not showing
        volumeSeekBar?.max = PlayerConstants.MaxProgress
        volumeSeekBar?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                volumeSeekBar?.setProgressAndThumb(i)
                mPresenter.setAudioVolumeInsideVolumeSeekBar(i)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        */
        /*
        volumeImageButton?.setOnClickListener {
            volumeSeekBar?.apply {
                if (visibility != View.VISIBLE) {
                    visibility = View.VISIBLE
                    nativeAdsFrameLayout?.visibility = View.GONE
                } else {
                    visibility = View.INVISIBLE
                    nativeAdsFrameLayout?.visibility = nativeAdViewVisibility
                }
            }
            setTimerToHideSupportAndAudioController()   // reset timer
        }
        */
        nonVolumeImageButton?.let{ it->
            it.setOnClickListener {
                // silence the sound
                mPresenter.playingParam.let { pIt->
                    pIt.currentVolume = 0.0f
                    playService?.setAudioVolume(pIt.currentVolume)
                    it.visibility = View.GONE
                    volumeImageButton?.visibility = View.VISIBLE
                    disableButtonForSometime(it)
                }
            }
        }
        volumeImageButton?.let { it->
            it.setOnClickListener {
                // enable the sound
                mPresenter.playingParam.let { pIt->
                    pIt.currentVolume = 1.0f
                    playService?.setAudioVolume(pIt.currentVolume)
                    it.visibility = View.GONE
                    nonVolumeImageButton?.visibility = View.VISIBLE
                    disableButtonForSometime(it)
                }
            }
        }
        previousMediaImageButton?.let { it->
            it.setOnClickListener {
                mPresenter.playPreviousSong()
                disableButtonForSometime(it)
            }
        }
        playMediaImageButton?.let { it->
            it.setOnClickListener {
                mPresenter.startPlay()
                disableButtonForSometime(it)
            }
        }
        pauseMediaImageButton?.let { it->
            it.setOnClickListener {
                mPresenter.pausePlay()
                disableButtonForSometime(it)
            }
        }
        replayMediaImageButton?.let { it->
            it.setOnClickListener {
                mPresenter.replayMedia()
                disableButtonForSometime(it)
            }
        }
        stopMediaImageButton?.let { it->
            it.setOnClickListener {
                mPresenter.stopPlay(PlayerConstants.STOPPED_BY_USER)
                disableButtonForSometime(it)
            }
        }
        nextMediaImageButton?.let { it->
            it.setOnClickListener {
                mPresenter.playNextSong()
                disableButtonForSometime(it)
            }
        }
        heartImageButton?.setOnClickListener { it->
            // add this media file to my favorite
            mPresenter.let { pIt ->
                val index = pIt.playingParam.currentSongIndex
                Log.d(TAG,"heartImageButton.onClick.currentSongIndex = $index")
                if (index>=0 && MySingleTon.orderedSongs.size>index) {
                    activity?.let {
                        SongListSQLite(it.applicationContext).also { sqlIt ->
                            MySingleTon.orderedSongs[index].run {
                                // check if this file is already in database
                                if (sqlIt.findOneSongByUriString(filePath) == null) {
                                    Log.d(TAG, "heartImageButton.onClick.findOneSongByUriString() is null")
                                    included = "1"
                                    sqlIt.addSongToSongList(this)
                                }
                            }
                            sqlIt.closeDatabase()
                        }
                        ScreenUtil.showToast(it, getString(R.string.add_to_favorites), textFontSize,
                                BaseApplication.FontSize_Scale_Type, Toast.LENGTH_SHORT)
                    }
                }
            }
            disableButtonForSometime(it)
        }

        orientationImageButton?.let { it->
            it.setOnClickListener {
                val config = resources.configuration
                val orientation =
                    if (config.orientation == Configuration.ORIENTATION_PORTRAIT) Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT
                Log.d(TAG,"orientationImageButton.onClick.orientation = $orientation")
                setScreenOrientation(orientation)
                disableButtonForSometime(it)
            }
        }
        repeatImageButton?.let { it->
            it.setOnClickListener {
                mPresenter.setRepeatSongStatus()
                disableButtonForSometime(it)
            }
        }
        switchToMusicImageButton?.let { it->
            it.setOnClickListener {
                mPresenter.switchAudioToMusic()
                disableButtonForSometime(it)
            }
        }
        switchToVocalImageButton?.let { it->
            it.setOnClickListener {
                mPresenter.switchAudioToVocal()
                disableButtonForSometime(it)
            }
        }
        hideVideoImageButton?.let { it->
            it.setOnClickListener {
                if (playerViewLinearLayout?.visibility==View.VISIBLE) hidePlayerView() else showPlayerView()
                disableButtonForSometime(it)
            }
        }

        audioChannelImageButton?.let { it->
            it.setOnClickListener {
                audioChannelButtonListener()
                disableButtonForSometime(it)
            }
        }
        audioTrackImageButton?.let { it->
            it.setOnClickListener {
                mPresenter.playingParam.apply {
                    currentAudioTrackIndexPlayed++
                    if (currentAudioTrackIndexPlayed > mPresenter.numberOfAudioTracks) currentAudioTrackIndexPlayed = 1
                    val str: String? =
                        when (currentAudioTrackIndexPlayed) {
                            1 -> activity?.getString(R.string.audioTrack1String)
                            2 -> activity?.getString(R.string.audioTrack2String)
                            3 -> activity?.getString(R.string.audioTrack3String)
                            4 -> activity?.getString(R.string.audioTrack4String)
                            5 -> activity?.getString(R.string.audioTrack5String)
                            6 -> activity?.getString(R.string.audioTrack6String)
                            7 -> activity?.getString(R.string.audioTrack7String)
                            8 -> activity?.getString(R.string.audioTrack8String)
                            else -> activity?.getString(R.string.unknown)
                        }
                    ScreenUtil.showToast(activity, str, toastTextSize, ScreenUtil.FontSize_Pixel_Type,
                        Toast.LENGTH_SHORT)
                    mPresenter.setAudioTrackAndChannel(currentAudioTrackIndexPlayed, currentChannelPlayed)
                }
                disableButtonForSometime(it)
            }
        }

        actionMenuImageButton?.let { it->
            it.setOnClickListener {
                Log.d(TAG, "actionMenuImageButton.setOnClickListener")
                actionMenuView?.showOverflowMenu()
                autoPlayMenuItem?.isChecked = mPresenter.playingParam.isAutoPlay
                setTimerToHideSupportAudioControl()   // reset the timer
                disableButtonForSometime(it)
            }
        }
        actionMenuView?.let {
            it.setOnMenuItemClickListener { item: MenuItem? ->
                item?.let { itemIt-> onOptionsItemSelected(itemIt) } == true
            }
        }
        player_duration_seekbar?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // update the duration on controller UI
                mPresenter.onDurationSeekBarProgressChanged(progress, fromUser)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        supportToolbar?.let {
            it.setOnClickListener { v: View ->
                /*
                if (v.visibility == View.VISIBLE) {
                    // use custom toolbar
                    hideSupportToolbarAndAudioController()
                    Log.d(TAG, "supportToolbar.onClick().View.VISIBLE.")
                } else {
                    // use custom toolbar
                    showSupportToolbarAndAudioController(true)
                    Log.d(TAG, "supportToolbar.onClick().View.INVISIBLE.")
                }
                */
                showSupportToolbarAudioControlSetTimer()
                // volumeSeekBar?.visibility = View.INVISIBLE
                Log.d(TAG, "supportToolbar.onClick() is called.")
            }
        }
        playerViewLinearLayout?.let { it->
            it.setOnClickListener {
                Log.d(TAG, "playerViewLinearLayout.onClick()")
                if (playerViewLinearLayout?.visibility == View.VISIBLE) {
                    supportToolbar?.performClick()
                }
            }
            it.setOnTouchListener { _, motionEvent ->
                val posX = motionEvent.x
                // Log.d(TAG, "setOnTouchListener.motionEvent.x = $posX")
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        Log.d(TAG, "setOnTouchListener.ACTION_DOWN.posX = $posX")
                        oldMotionEventX = posX
                    }
                    MotionEvent.ACTION_UP -> {
                        Log.d(TAG, "setOnTouchListener.ACTION_UP")
                    }
                    MotionEvent.ACTION_MOVE -> run {
                        if (!playService.isSeekable()) return@run
                        mPresenter.playingParam.apply {
                            if (!(currentPlaybackState == PlaybackStateCompat.STATE_PLAYING
                                        || currentPlaybackState == PlaybackStateCompat.STATE_PAUSED)) {
                                Log.d(TAG, "setOnTouchListener.ACTION_MOVE.not playing or paused")
                                return@run
                            }
                        }
                        if (posX <= 0 || posX >= screenSizeX) {
                            Log.d(TAG, "setOnTouchListener.ACTION_MOVE.out of the screen size")
                            return@run
                        }
                        val distance = posX - oldMotionEventX
                        // Log.d(TAG, "setOnTouchListener.ACTION_MOVE.distance = $distance")
                        if (distance >= -5.0 && distance <= 5.0f) {
                            Log.d(TAG, "setOnTouchListener.ACTION_MOVE.distance is too small")
                            return@run
                        }
                        val duration = playService.getMediaDuration()
                        if (duration > 0) {
                            player_duration_seekbar?.let {
                                val progress = it.progress + ((distance / screenSizeX) * duration).toInt()
                                if (progress <= (duration - 2000)) {
                                    // less than 2 seconds before the end
                                    it.progress = progress
                                    mPresenter.onDurationSeekBarProgressChanged(it.progress, true)
                                    showSupportToolbarAudioControl() // show the player buttons
                                    oldMotionEventX = posX
                                }
                            }
                        }
                    }
                    MotionEvent.ACTION_OUTSIDE -> {
                        Log.d(TAG, "setOnTouchListener.ACTION_OUTSIDE")
                    }
                    else -> {
                        Log.d(TAG, "setOnTouchListener.else")
                    }
                }
                false
            }
        }
    }

    // implementing PlayerBasePresenter.BasePresentView
    override fun setImageButtonStatus() {
        Log.d(TAG, "setImageButtonStatus()")
        val playingParam = mPresenter.playingParam
        switchToMusicImageButton?.apply {
            isEnabled = true
            visibility = View.VISIBLE
        }
        // setSwitchToVocalImageButtonVisibility() // abstract method
        switchToVocalImageButton?.visibility = View.VISIBLE
        setOrientationImageButton(resources.configuration.orientation)
        // repeatImageButton
        var backgroundColor = R.color.red
        when (playingParam.repeatStatus) {
            PlayerConstants.NoRepeatPlaying -> {
                // no repeat but show symbol of repeat all song with transparent background
                repeatImageButton?.setImageResource(R.drawable.repeat_all_white)
                backgroundColor = R.color.transparentDark
            }
            PlayerConstants.RepeatOneSong ->                 // repeat one song
                repeatImageButton?.setImageResource(R.drawable.repeat_one_white)
            PlayerConstants.RepeatAllSongs ->                 // repeat all song list
                repeatImageButton?.setImageResource(R.drawable.repeat_all_white)
        }
        activity?.let {
            repeatImageButton?.setBackgroundColor(ContextCompat.getColor(
                    it.applicationContext, backgroundColor)
            )
            repeatImageButton?.visibility = if (playingParam.isPlaySingleSong) View.GONE else View.VISIBLE
        }

        hideVideoImageButton?.apply {
            setImageResource(if (playerViewLinearLayout?.visibility==View.VISIBLE) R.drawable.hide_video
                else R.drawable.show_video)
            visibility = if (playingParam.isPlaySingleSong) View.GONE else View.VISIBLE
        }
    }

    override fun playButtonOnPauseButtonOff() {
        playMediaImageButton?.visibility = View.VISIBLE
        pauseMediaImageButton?.visibility = View.GONE
    }

    override fun playButtonOffPauseButtonOn() {
        playMediaImageButton?.visibility = View.GONE
        pauseMediaImageButton?.visibility = View.VISIBLE
    }

    override fun setPlayingTimeTextView(durationString: String?) {
        playingTimeTextView?.text = durationString
    }

    override fun update_Player_duration_seekbar(duration: Float) {
        var durationTmp = duration
        player_duration_seekbar?.progress = 0
        player_duration_seekbar?.max = durationTmp.toInt()
        durationTmp /= 1000.0f // seconds
        val minutes = (durationTmp / 60.0f).toInt() // minutes
        val seconds = durationTmp.toInt() - minutes * 60
        val durationString = String.format("%3d:%02d", minutes, seconds)
        durationTimeTextView?.text = durationString
    }

    override fun showMusicAndVocalIsNotSet() {
        Log.d(TAG, "showMusicAndVocalIsNotSet is called.")
        ScreenUtil.showToast(activity, getString(R.string.musicAndVocalNotSet),
            toastTextSize, ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_SHORT
        )
    }

    override fun update_Player_duration_seekbar_progress(progress: Int) {
        player_duration_seekbar?.progress = progress
    }

    override fun updateVolumeSeekBarProgress() {
        // volumeSeekBar?.setProgressAndThumb(mPresenter.currentProgressForVolumeSeekBar)
    }

    override fun showNativeAndHideBannerAd() {
        if (playerViewLinearLayout?.visibility == View.VISIBLE) {
            Log.d(TAG, "showNativeAndHideBannerAd.View.VISIBLE")
            mPresenter.playingParam.let {
                if (it.currentPlaybackState != PlaybackStateCompat.STATE_PLAYING) {
                    Log.d(TAG, "showNativeAndHideBannerAd.Not PlaybackStateCompat.STATE_PLAYING")
                    nativeAdViewVisibility = View.VISIBLE
                    nativeTemplate?.showNativeAd()
                    // hide the banner ad
                    bannerLinearLayout?.visibility = View.GONE
                } else {
                    hideNativeAd()
                }
            }
        } else {
            Log.d(TAG, "showNativeAndHideBannerAd.View.INVISIBLE")
            // show the banner ad if in the right place
        }
    }

    override fun hideNativeAd() {
        Log.d(TAG, "hideNativeAd")
        nativeAdViewVisibility = View.GONE
        nativeTemplate?.hideNativeAd()
    }

    override fun showBufferingMessage() {
        Log.d(TAG, "showBufferingMessage() is called.")
        message_area_LinearLayout?.visibility = View.VISIBLE
        bufferingStringTextView?.startAnimation(animationText)
    }

    override fun dismissBufferingMessage() {
        Log.d(TAG, "dismissBufferingMessage() is called.")
        animationText?.cancel()
        message_area_LinearLayout?.visibility = View.GONE
    }

    override fun buildAudioTrackMenuItem(audioTrackNumber: Int) {
        // build R.id.audioTrack submenu
        audioTrackMenuItem?.subMenu?.let {
            var index = 0
            while (index < audioTrackNumber) {
                // audio track index start from 1 for user interface
                it.getItem(index).isVisible = true
                index++
            }
            for (j in index until it.size()) {
                it.getItem(j).isVisible = false
            }
        }
    }

    override fun setTimerToHideSupportAudioControl() {
        Log.d(TAG, "setTimerToHideSupportAndAudioController() is called.")
        if (playerViewLinearLayout?.visibility == View.VISIBLE) {
            controllerTimerHandler.removeCallbacksAndMessages(null)
            controllerTimerHandler.postDelayed(controllerTimerRunnable,
                    PlayerConstants.PlayerView_Timeout.toLong()
            ) // 10 seconds
        }
    }

    override fun showInterstitialAd(isSelfFinished : Boolean) {
        interstitialAd?.let {
            it.ShowAdThread(object : DismissFunction {
                override fun backgroundWork() {
                    Log.d(TAG, "DismissFunction.backgroundWork()")
                }
                override fun executeDismiss() {
                    Log.d(TAG, "DismissFunction.executeDismiss()")
                    mPresenter.let { pIt->
                        if (!pIt.startAutoPlay(isSelfFinished) && pIt.playingParam.numPlayed == 0) {
                            // no next song and just show Interstitial Ad before
                            // Show NativeAd
                            Log.d(TAG, "DismissFunction.executeDismiss().showNativeAndHideBannerAd()")
                            try {
                                showNativeAndHideBannerAd()
                            } catch (ex: Exception) {
                                Log.d(TAG,"DismissFunction.executeDismiss().Exception" +
                                        "form showNativeAndHideBannerAd(): " + ex)
                            }
                        }
                    }
                }
                override fun afterFinished(isAdShown: Boolean) {
                    Log.d(TAG, "DismissFunction.afterFinished().isAdShown = $isAdShown")
                    if (!isAdShown) mPresenter.startAutoPlay(isSelfFinished)
                }
            }).startShowAd(0)  // AdMob first
        }
    }
    // end of implementing PlayerBasePresenter.BasePresentView

    private fun setScreenOrientation(orientation: Int) {
        activity?.requestedOrientation = when (orientation) {
            Configuration.ORIENTATION_PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            Configuration.ORIENTATION_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    private fun setOrientationImageButton(orientation : Int) {
        orientationImageButton?.let {
            it.rotation = if (orientation == Configuration.ORIENTATION_LANDSCAPE) 0.0f else 90.0f
            Log.d(TAG, "setOrientationImageButton.rotation == ${it.rotation}")
            it.setImageResource(R.drawable.phone_portrait)
        }
    }
}