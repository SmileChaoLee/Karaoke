package com.smile.karaoke.fragments

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.session.PlaybackStateCompat
import android.view.KeyEvent
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
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.scale
import androidx.core.view.get
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.media3.common.util.UnstableApi
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.smile.karaoke.R
import com.smile.karaoke.SmileAppBase
import com.smile.karaoke.constants.CommonConstants
import com.smile.karaoke.constants.PlayerConstants
import com.smile.karaoke.models.MySingleTon
import com.smile.karaoke.models.SongInfo
import com.smile.karaoke.models.SongListSQLite
import com.smile.karaoke.presenters.PlayerBasePresenter
import com.smile.karaoke.presenters.PlayerBasePresenter.BasePresentView
import com.smile.karaoke.utilities.DatabaseAccessUtil
import com.smile.karaoke.utilities.LogUtil
import com.smile.karaoke.utilities.MyBannerTool
import com.smile.nativetemplates_models.GoogleAdMobNativeTemplate
import com.smile.smilelibraries.models.ExitAppTimer
import com.smile.smilelibraries.privacy_policy.PrivacyPolicyUtil
import com.smile.smilelibraries.show_banner_ads.SetBannerAdView
import com.smile.smilelibraries.utilities.ScreenUtil
import java.util.Locale

private const val TAG: String = "PlayerBaseFragment"

@UnstableApi
abstract class PlayerBaseFragment : Fragment(),
    BasePresentView {

    interface PlayBaseFragmentFunc {
        fun baseHidePlayerView()
        fun baseShowPlayerView()
        fun returnToPrevious(isSingleSong : Boolean)
    }

    lateinit var mPresenter: PlayerBasePresenter
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
    private var volumeImageButton: ImageButton? = null
    private var previousMediaImageButton: ImageButton? = null
    private var playMediaImageButton: ImageButton? = null
    private var replayMediaImageButton: ImageButton? = null
    private var stopMediaImageButton: ImageButton? = null
    private var nextMediaImageButton: ImageButton? = null
    private var heartImageButton: ImageButton? = null

    private var playingTimeTextView: TextView? = null
    private var playerDurationSeekbar: AppCompatSeekBar? = null
    private var durationTimeTextView: TextView? = null

    private var orientationImageButton: ImageButton? = null
    private var repeatImageButton: ImageButton? = null
    private var switchToMusicImageButton: ImageButton? = null
    private var switchToVocalImageButton: ImageButton? = null
    private var hideVideoImageButton: ImageButton? = null
    private var actionMenuImageButton: ImageButton? = null
    private var audioChannelImageButton: ImageButton? = null
    private var audioTrackImageButton: ImageButton? = null

    private var mediaRouteButton: MediaRouteButton? = null
    protected var castContext: CastContext? = null

    private var bannerAdsLayout: LinearLayout? = null
    private var bannerLinearLayout: LinearLayout? = null
    private var myBannerAdView: SetBannerAdView? = null
    private var nativeTemplate: GoogleAdMobNativeTemplate? = null

    private var messageAreaLinearLayout: LinearLayout? = null
    private var bufferingStringTextView: TextView? = null
    private var animationText: Animation? = null
    private var nativeAdsFrameLayout: FrameLayout? = null
    private var nativeAdViewVisibility = 0
    private var nativeAdTemplateView: TemplateView? = null
    protected var mainMenu: Menu? = null

    // submenu of file
    private var softDecoderFirstMenuItem: MenuItem? = null
    private var autoPlayMenuItem: MenuItem? = null
    private var audioMenuItem: MenuItem? = null
    // submenu of audio
    private var audioTrackMenuItem: MenuItem? = null
    // submenu of channel
    private var leftChannelMenuItem: MenuItem? = null
    private var rightChannelMenuItem: MenuItem? = null
    private var stereoChannelMenuItem: MenuItem? = null
    private var oldMotionEventX = 0.0f
    private var orgOrientation = Configuration.ORIENTATION_PORTRAIT
    private var lastFocusView: ImageButton? = null

    private val controllerTimerHandler = Handler(Looper.getMainLooper())
    private val controllerTimerRunnable = Runnable {
        LogUtil.d(TAG, "controllerTimerRunnable")
        controllerTimerHandler.removeCallbacksAndMessages(null)
        mPresenter.playingParam?.let {
            LogUtil.d(TAG, "controllerTimerRunnable.playingParam")
            if (supportToolbar?.visibility == View.VISIBLE) {
                // hide supportToolbar
                hideSupportToolbarAndAudioController()
            }
            if (it.currentPlaybackState != PlaybackStateCompat.STATE_PLAYING) {
                // there is no media playing or playing is finished
                showNativeAndHideBannerAd()
                setTimerToHideSupportAudioControl()   // reset the timer
            }
        }
    }

    abstract fun getPlayerPresenter(): PlayerBasePresenter?
    abstract fun setMenuItemsVisibility()
    abstract fun onPlayServiceConnected(service: IBinder)
    abstract fun onPlayServiceDisconnected()
    abstract fun startAndBindPlayService()
    abstract fun unbindAndStopPlayService()
    abstract fun audioChannelButtonListener()

    protected var isServiceBound: Boolean = false
    protected var isServiceDestroyed: Boolean = true
    protected val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            LogUtil.i(TAG, "onServiceConnected")
            onPlayServiceConnected(service)
            isServiceBound = true
            isServiceDestroyed = false
            LogUtil.d(TAG, "onServiceConnected.isAutoPlay = " +
                    "${mPresenter.playingParam.isAutoPlay}")
        }

        override fun onServiceDisconnected(name: ComponentName) {
            LogUtil.i(TAG, "onServiceDisconnected")
            isServiceBound = false
            onPlayServiceDisconnected()
        }
    }

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        MySingleTon.clearSingleton()
        if (SmileAppBase.deviceType != CommonConstants.DEVICE_TYPE_PHONE) {
            LogUtil.d(TAG, "onCreate.deviceType is not phone")
            orgOrientation = resources.configuration.orientation
        }
        arguments?.let {
            LogUtil.d(TAG, "onCreate.arguments is not null")
        }
        /*
        // keep the screen on all the time, added on 2021-02-18
        activity?.window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        */
        setHasOptionsMenu(true) // must have because it has menu

        activity?.let {
            if (it is PlayBaseFragmentFunc) playBaseFragmentFunc = it
            LogUtil.d(TAG, "onCreate.playBaseFragmentFunc = $playBaseFragmentFunc")
        }
        
        getPlayerPresenter()?.let {
            mPresenter = it
        } ?: run {
            LogUtil.d(TAG, "onCreate.presenter is null so exit activity.")
            playBaseFragmentFunc?.returnToPrevious(false)
            return
        }
        textFontSize = SmileAppBase.textFontSize
        fontScale = SmileAppBase.fontSize
        toastTextSize = SmileAppBase.toastTextSize

        activity?.let { actIt ->
            castContext = (actIt.application as SmileAppBase).castContext
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        LogUtil.i(TAG, "onCreateView")
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_player_base_view,
            container, false)

        // Make the root view focusable
        // Allows it to receive focus when touched
        view.isFocusableInTouchMode = true
        view.isFocusable = true
        view.requestFocus()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "onViewCreated")
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
        }

        actionMenuView = supportToolbar?.findViewById(R.id.actionMenuViewLayout) // main menu
        fragmentView?.apply {
            audioControllerView = findViewById(R.id.audioControllerView)
            volumeImageButton = findViewById(R.id.volumeImageButton)
            previousMediaImageButton = findViewById(R.id.previousMediaImageButton)
            playMediaImageButton = findViewById(R.id.playMediaImageButton)
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
                    showBannerAd()
                }
            }

            // message area
            messageAreaLinearLayout = findViewById(R.id.message_area_LinearLayout)
            messageAreaLinearLayout?.visibility = View.GONE
            bufferingStringTextView = findViewById(R.id.bufferingStringTextView)
            ScreenUtil.resizeTextSize(bufferingStringTextView, textFontSize, ScreenUtil.FontSize_Pixel_Type)
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
            playerDurationSeekbar = findViewById(R.id.player_duration_seekbar)
            durationTimeTextView = findViewById(R.id.durationTimeTextView)
            durationTimeTextView?.text = "000:00"
            ScreenUtil.resizeTextSize(durationTimeTextView, durationTextSize, ScreenUtil.FontSize_Pixel_Type)
            nativeAdsFrameLayout = findViewById(R.id.nativeAdsFrameLayout)
            nativeAdsFrameLayout?.let {
                nativeAdViewVisibility = it.visibility
            }
            nativeAdTemplateView = findViewById(R.id.nativeAdTemplateView)
            activity?.let { actIt ->
                nativeTemplate = (actIt.application as SmileAppBase)
                    .geNativeTemplate(actIt,
                        nativeAdsFrameLayout,
                        nativeAdTemplateView)
            }
        }

        // must before setImageButtonStatus() and showNativeAndBannerAd
        mPresenter.playingParam.let {
            if (it.isPlayerViewVisible) showPlayerView() else hidePlayerView()
        }

        setImageButtonStatus() // must before setButtonsPositionAndSize()
        setButtonsPositionAndSize(resources.configuration)
        setOnClickEvents()
        showNativeAndHideBannerAd()

        fragmentView?.setOnKeyListener {
                _, keyCode, event ->
            LogUtil.d(TAG, "onViewCreated.setOnKeyListener.keyCode = $keyCode, event = $event")
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP,
                KeyEvent.KEYCODE_DPAD_DOWN,
                KeyEvent.KEYCODE_DPAD_LEFT,
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    if (event?.action == KeyEvent.ACTION_DOWN) {
                        supportToolbar?.performClick()
                        // D-pad move started
                        LogUtil.d(TAG, "setOnKeyListener.D-pad move started: $keyCode")
                        // Handle your logic here
                        if (lastFocusView == null) {
                            hideVideoImageButton?.requestFocus()
                        } else {
                            lastFocusView?.requestFocus()
                        }
                        return@setOnKeyListener true
                    }
                }
            }
            return@setOnKeyListener false
        }

        LogUtil.d(TAG, "onViewCreated is finished.")
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        LogUtil.i(TAG, "onCreateOptionsMenu")
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
            // ScreenUtil.buildActionViewClassMenu(activity, wrapper, mainMenu, fontScale, SmileAppBase.FontSize_Scale_Type);
            ScreenUtil.resizeMenuTextIconSize(wrapper, mainMenu, fontScale)
        }

        // submenu of file
        mainMenu?.let {
            softDecoderFirstMenuItem = it.findItem(R.id.softDecoderFirst)
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

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val playingParam = mPresenter.playingParam
        val currentChannelPlayed = playingParam.currentChannelPlayed
        if (item.hasSubMenu()) {
            item.subMenu?.clearHeader()
        }
        val id = item.itemId
        if (id == R.id.softDecoderFirst) {
            // setting if use soft decoder
            playingParam.softDecoderFirst = !playingParam.softDecoderFirst
            softDecoderFirstMenuItem?.isChecked = playingParam.softDecoderFirst
            playService?.switchDecoder()
        } else if (id == R.id.autoPlay) {
            autoPlayMenuItem?.let {
                // print the original check status
                LogUtil.d(TAG, "autoPlayMenuItem?.isChecked = ${it.isChecked}")
                if (!it.isChecked) {
                    it.isChecked = mPresenter.setAutoPlayStatusAndAction()
                } else {
                    mPresenter.stopAutoPlay()
                    it.isChecked = false
                }
            }
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
                for (i in 0 until it.size) {
                    val mItem = it[i]
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
                    if (currentChannelPlayed == CommonConstants.LEFT_CHANNEL) {
                        leftChannelMenuItem?.isCheckable = true
                        leftChannelMenuItem?.isChecked = true
                    } else {
                        leftChannelMenuItem?.isCheckable = false
                        leftChannelMenuItem?.isChecked = false
                    }
                    if (currentChannelPlayed == CommonConstants.RIGHT_CHANNEL) {
                        rightChannelMenuItem?.isCheckable = true
                        rightChannelMenuItem?.isChecked = true
                    } else {
                        rightChannelMenuItem?.isCheckable = false
                        rightChannelMenuItem?.isChecked = false
                    }
                    if (currentChannelPlayed == CommonConstants.STEREO) {
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
        LogUtil.i(TAG, "onStart")
        super.onStart()
        mPresenter.playingParam?.let {
            LogUtil.d(TAG, "onStart.preparedStatus = ${it.preparedStatus}")
            if (it.preparedStatus == 3) { // running in the background before
                // set to come back from background
                it.preparedStatus = 4
            }
        }
    }

    override fun onResume() {
        LogUtil.i(TAG, "onResume")
        super.onResume()
        myBannerAdView?.resume()
        MyBannerTool.setVisible(bannerAdsLayout
            , nativeAdViewVisibility)
        startAndBindPlayService()
    }

    override fun onPause() {
        LogUtil.i(TAG, "onPause")
        super.onPause()
        myBannerAdView?.pause()
        bannerAdsLayout?.visibility = View.GONE
    }

    override fun onStop() {
        LogUtil.i(TAG, "onStop")
        super.onStop()
        mPresenter.playingParam?.let {
            LogUtil.d(TAG, "onStop.isPlaySingleSong = ${it.isPlaySingleSong}")
            it.preparedStatus = 3 // running in background
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        LogUtil.i(TAG, "onConfigurationChanged")
        closeMenu(mainMenu)
        setOrientationImageButton(newConfig.orientation)
        setButtonsPositionAndSize(newConfig)
        activity?.let {actIt ->
            screenSizeX = ScreenUtil.getScreenSize(actIt).x
            screenSizeY = ScreenUtil.getScreenSize(actIt).y
            myBannerAdView?.destroy()
            bannerLinearLayout?.also {layoutIt ->
                layoutIt.visibility = View.VISIBLE // Show Banner Ad
                showBannerAd()
            }
        }
        MyBannerTool.setVisible(bannerAdsLayout
            , nativeAdViewVisibility)

        super.onConfigurationChanged(newConfig)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        LogUtil.i(TAG, "onSaveInstanceState")
        mPresenter.saveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        LogUtil.i(TAG, "onDestroy")
        super.onDestroy()
        MySingleTon.clearSingleton()
        // cancel the timer
        mPresenter.removeMsgFromDurationBarHandler()
        controllerTimerHandler.removeCallbacksAndMessages(null)
        myBannerAdView?.destroy()
        nativeTemplate?.release()
        // clear the screen on, added on 2021-02-18
        activity?.window?.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        unbindAndStopPlayService()
        // setupCast?.release()
    }

    fun onBackPressed() {
        LogUtil.d(TAG, "onBackPressed")
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
        LogUtil.i(TAG, "setMainMenu")
        softDecoderFirstMenuItem?.isVisible = true    // always visible
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

    private fun setMediaRouteButtonVisible() {
        LogUtil.i(TAG, "setMediaRouteButtonVisible")
        mediaRouteButton?.visibility =
            if (castContext != null)
            View.VISIBLE else View.GONE
    }

    private fun setMediaRouteButtonView(buttonMarginLeft: Int, imageButtonHeight: Int) {
        LogUtil.i(TAG, "setMediaRouteButtonView.castContext = $castContext")
        if (castContext == null) return
        try {
            mediaRouteButton = fragmentView?.findViewById(R.id.media_route_button)
            mediaRouteButton?.let {
                activity?.applicationContext?.let { ctxIt ->
                    CastButtonFactory.setUpMediaRouteButton(ctxIt, it)
                    setMediaRouteButtonVisible()
                }
            }
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.cast)
            val buttonDrawable: Drawable =
                bitmap.scale(imageButtonHeight, imageButtonHeight)
                    .toDrawable(resources)
            mediaRouteButton?.setRemoteIndicatorDrawable(buttonDrawable)
            val linearParam = LinearLayout.LayoutParams(imageButtonHeight, imageButtonHeight)
            linearParam.setMargins(buttonMarginLeft, 0, 0, 0)
            mediaRouteButton?.layoutParams = linearParam
        } catch (ex: Exception) {
            LogUtil.e(TAG, "setMediaRouteButtonView.Exception", ex)
        }
    }

    private fun setButtonsPositionAndSize(config: Configuration) {
        LogUtil.i(TAG, "setButtonsPositionAndSize")
        var buttonMarginLeft = (50.0f * fontScale).toInt() // 60 pixels = 20dp on Nexus 5
        var buttonMarginLeft2 = buttonMarginLeft
        // val screenSize = ScreenUtil.getScreenSize(activity)
        // LogUtil.d(TAG, "screenSize.x = ${screenSize.x}, screenSize.y = ${screenSize.y}, buttonMarginLeft = $buttonMarginLeft")
        LogUtil.d(TAG, "screenSize.x = $screenSizeX, screenSize.y = $screenSizeX, buttonMarginLeft = $buttonMarginLeft")

        val audioControllerViewLP = audioControllerView?.layoutParams as ConstraintLayout.LayoutParams
        audioControllerViewLP.matchConstraintPercentHeight = 0.18f
        val emptyLinearLayout = fragmentView?.findViewById<LinearLayout>(R.id.emptyLinearLayout)
        val emptyLinearLayoutLP = emptyLinearLayout?.layoutParams as ConstraintLayout.LayoutParams
        emptyLinearLayoutLP.matchConstraintPercentHeight = 0.05f

        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            buttonMarginLeft =
                (buttonMarginLeft.toFloat() * (screenSizeX.toFloat() / screenSizeY.toFloat())).toInt()
            audioControllerViewLP.matchConstraintPercentHeight = 0.30f
            emptyLinearLayoutLP.matchConstraintPercentHeight = 0.01f
        }
        if (buttonMarginLeft<0) buttonMarginLeft = 0
        LogUtil.d(TAG, "buttonMarginLeft = $buttonMarginLeft")
        val buttonNum = 8 // 8 buttons
        var imageButtonHeight = (textFontSize * 1.2f).toInt()
        LogUtil.d(TAG, "imageButtonHeight = $imageButtonHeight")
        // added to avoid crashing on some devices
        if (imageButtonHeight <= 0 ) imageButtonHeight = (24.0f * 1.2f).toInt()
        //
        val maxWidth = buttonNum * imageButtonHeight + (buttonNum - 1) * buttonMarginLeft
        if (maxWidth > screenSizeX) {
            LogUtil.d(TAG, "maxWidth > screenSize.x")
            // greater than the width of screen
            buttonMarginLeft = (screenSizeX - 10 - buttonNum * imageButtonHeight) / (buttonNum-1)
        }
        if (buttonMarginLeft<0) buttonMarginLeft = 0
        LogUtil.d(TAG, "buttonMarginLeft = $buttonMarginLeft")
        val buttonNum2 = 8
        val maxWidth2 = buttonNum2 * imageButtonHeight + (buttonNum2 - 1) * buttonMarginLeft2
        if (maxWidth2 > screenSizeX) {
            // greater than the width of screen
            buttonMarginLeft2 = (screenSizeX - 10 - buttonNum2 * imageButtonHeight) / (buttonNum2 - 1)
        }
        if (buttonMarginLeft2<0) buttonMarginLeft2 = 0
        LogUtil.d(TAG, "buttonMarginLeft2 = $buttonMarginLeft2")

        val linearParam = LinearLayout.LayoutParams(imageButtonHeight, imageButtonHeight)
        linearParam.setMargins(0, 0, 0, 0)
        volumeImageButton?.layoutParams = linearParam

        linearParam.setMargins(buttonMarginLeft, 0, 0, 0)
        previousMediaImageButton?.layoutParams = linearParam
        playMediaImageButton?.layoutParams = linearParam
        replayMediaImageButton?.layoutParams = linearParam
        stopMediaImageButton?.layoutParams = linearParam
        nextMediaImageButton?.layoutParams = linearParam
        heartImageButton?.layoutParams = linearParam
        actionMenuView?.layoutParams = linearParam

        val tempBitmap = BitmapFactory.decodeResource(resources, R.drawable.circle_and_three_dots)
        val iconDrawable: Drawable =
            tempBitmap.scale(imageButtonHeight, imageButtonHeight)
                .toDrawable(resources)
        actionMenuView?.overflowIcon = iconDrawable // set icon of three dots for ActionMenuView
        actionMenuImageButton?.layoutParams = linearParam

        linearParam.setMargins(0, 0, 0, 0)
        orientationImageButton?.apply {
            layoutParams = linearParam
            visibility = if (SmileAppBase.deviceType == CommonConstants.DEVICE_TYPE_PHONE)
                View.VISIBLE else View.GONE
        }

        linearParam.setMargins(buttonMarginLeft2, 0, 0, 0)
        repeatImageButton?.layoutParams = linearParam
        switchToMusicImageButton?.layoutParams = linearParam
        switchToVocalImageButton?.layoutParams = linearParam
        hideVideoImageButton?.layoutParams = linearParam
        audioChannelImageButton?.layoutParams = linearParam
        audioTrackImageButton?.layoutParams = linearParam

        setMediaRouteButtonView(buttonMarginLeft2, imageButtonHeight)

        supportToolbar?.layoutParams?.height = previousMediaImageButton?.layoutParams?.height!!
        bannerAdsLayout = fragmentView?.findViewById(R.id.bannerAdsLayout)
        val bannerAdsLayoutLP = bannerAdsLayout?.layoutParams as ConstraintLayout.LayoutParams
        val nativeAdLayout: FrameLayout? = fragmentView?.findViewById(R.id.nativeAdLayout)
        val nativeAdLayoutLP = nativeAdLayout?.layoutParams as ConstraintLayout.LayoutParams
        val bannerHeightPercent = bannerAdsLayoutLP.matchConstraintPercentHeight
        LogUtil.d(TAG,"bannerHeightPercent = $bannerHeightPercent")
        val heightPercent = 1.0f - bannerHeightPercent - imageButtonHeight * 3.30f / screenSizeY
        LogUtil.d(TAG, "heightPercent = $heightPercent")
        nativeAdLayoutLP.matchConstraintPercentHeight = (heightPercent * 100.0f).toInt() / 100.0f
        LogUtil.d(TAG, "nativeAdLayoutLP.matchConstraintPercentHeight = " +
                nativeAdLayoutLP.matchConstraintPercentHeight)

        // setting the width and the margins for nativeAdTemplateView
        val layoutParams = nativeAdTemplateView?.layoutParams as MarginLayoutParams
        // 6 buttons and 5 gaps
        layoutParams.width = imageButtonHeight * 6 + buttonMarginLeft * 5
        layoutParams.setMargins(0, 0, 0, 0)
        nativeAdTemplateView?.layoutParams = layoutParams
        //
    }

    private fun closeMenu(menu: Menu?) {
        menu?.let {
            for (i in 0 until it.size) {
                it[i].subMenu?.let { it2 ->
                    closeMenu(it2)
                }
            }
            it.close()
        }
    }

    private fun closeFragment() {
        LogUtil.i(TAG, "closeFragment.isPlaySingleSong = " + mPresenter.playingParam.isPlaySingleSong)
        playBaseFragmentFunc?.returnToPrevious(mPresenter.playingParam.isPlaySingleSong)
    }

    private fun showBannerAd() {
        LogUtil.d(TAG, "showBannerAd")
        activity?.let { actIt ->
            myBannerAdView?.destroy()
            myBannerAdView = (actIt.application as SmileAppBase)
                .showBannerAd(actIt, bannerLinearLayout)
            myBannerAdView?.showBannerAdView(0) // AdMob first
        }
    }

    fun showSupportToolbarAudioControlSetTimer() {
        LogUtil.d(TAG, "showSupportToolbarAudioControlSetTimer")
        showSupportToolbarAudioControl()
        setTimerToHideSupportAudioControl()   // reset the timer
    }

    private fun showSupportToolbarAudioControl() {
        LogUtil.d(TAG, "showSupportToolbarAudioControl")
        // bannerLinearLayout?.visibility = View.GONE
        bannerAdsLayout?.visibility = View.GONE
        hideNativeAd()
        supportToolbar?.visibility = View.VISIBLE
        audioControllerView?.visibility = View.VISIBLE
        nativeAdsFrameLayout?.visibility = nativeAdViewVisibility
    }

    private fun hideSupportToolbarAndAudioController() {
        LogUtil.d(TAG, "hideSupportToolbarAndAudioController.context = $context")
        if (context == null) return
        if (playerViewLinearLayout?.visibility == View.VISIBLE) {
            supportToolbar?.visibility = View.GONE
            audioControllerView?.visibility = View.GONE
            nativeAdsFrameLayout?.visibility = nativeAdViewVisibility
            closeMenu(mainMenu)
            MyBannerTool.setVisible(bannerAdsLayout
                , nativeAdViewVisibility)
        }
        fragmentView?.requestFocus()
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
        volumeImageButton?.setOnClickListener {
            LogUtil.d(TAG,"volumeImageButton.onClick")
            mPresenter.playingParam.let { pIt->
                LogUtil.d(TAG,"volumeImageButton.onClick.currentVolume = ${pIt.currentVolume}")
                if (pIt.currentVolume > 0.0f) {
                    pIt.currentVolume = 0.0f
                    volumeImageButton?.setImageResource(R.drawable.volume)
                } else {
                    pIt.currentVolume = 1.0f
                    volumeImageButton?.setImageResource(R.drawable.non_volume)
                }
                playService?.setAudioVolume(pIt.currentVolume)
            }
            disableButtonForSometime(it)
            lastFocusView = volumeImageButton
            fragmentView?.requestFocus()
        }
        previousMediaImageButton?.setOnClickListener {
            mPresenter.playPreviousSong()
            disableButtonForSometime(it)
            lastFocusView = previousMediaImageButton
            fragmentView?.requestFocus()
        }
        playMediaImageButton?.setOnClickListener {
            if (mPresenter.playingParam.currentPlaybackState ==
                PlaybackStateCompat.STATE_PLAYING) {
                mPresenter.pausePlay()
            } else {
                mPresenter.startPlay()
            }
            disableButtonForSometime(it)
            lastFocusView = playMediaImageButton
            fragmentView?.requestFocus()
        }
        replayMediaImageButton?.setOnClickListener {
            mPresenter.replayMedia()
            disableButtonForSometime(it)
            lastFocusView = replayMediaImageButton
            fragmentView?.requestFocus()
        }
        stopMediaImageButton?.setOnClickListener {
            mPresenter.stopPlay(PlayerConstants.STOPPED_BY_USER)
            disableButtonForSometime(it)
            lastFocusView = stopMediaImageButton
            fragmentView?.requestFocus()
        }
        nextMediaImageButton?.setOnClickListener {
            mPresenter.playNextSong()
            disableButtonForSometime(it)
            lastFocusView = nextMediaImageButton
            fragmentView?.requestFocus()
        }
        heartImageButton?.setOnClickListener { it->
            // add this media file to my favorite
            mPresenter.let { pIt ->
                val index = pIt.playingParam.currentSongIndex
                LogUtil.d(TAG,"heartImageButton.onClick.currentSongIndex = $index")
                if (index>=0 && MySingleTon.orderedSongs.size>index) {
                    activity?.let {
                        SongListSQLite(it.applicationContext).also { sqlIt ->
                            MySingleTon.orderedSongs[index].run {
                                // check if this file is already in database
                                if (sqlIt.findOneSongByUriString(filePath) == null) {
                                    LogUtil.d(TAG, "heartImageButton.onClick.findOneSongByUriString() is null")
                                    included = "1"
                                    sqlIt.addSongToSongList(this)
                                }
                            }
                            sqlIt.closeDatabase()
                        }
                        ScreenUtil.showToast(it, getString(R.string.add_to_favorites), textFontSize,
                            ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_SHORT)
                    }
                }
            }
            disableButtonForSometime(it)
            lastFocusView = heartImageButton
            fragmentView?.requestFocus()
        }

        orientationImageButton?.setOnClickListener {
            val org = resources.configuration.orientation
            val orientation = if (org == Configuration.ORIENTATION_PORTRAIT)
                    Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT
            LogUtil.d(TAG,"orientationImageButton.onClick.orientation = $orientation")
            setScreenOrientation(orientation)
            disableButtonForSometime(it)
            lastFocusView = orientationImageButton
            fragmentView?.requestFocus()
        }
        repeatImageButton?.setOnClickListener {
            mPresenter.setRepeatSongStatus()
            disableButtonForSometime(it)
            lastFocusView = repeatImageButton
            fragmentView?.requestFocus()
        }
        switchToMusicImageButton?.setOnClickListener {
            mPresenter.switchAudioToMusic()
            disableButtonForSometime(it)
            lastFocusView = switchToMusicImageButton
            fragmentView?.requestFocus()
        }
        switchToVocalImageButton?.setOnClickListener {
            mPresenter.switchAudioToVocal()
            disableButtonForSometime(it)
            lastFocusView = switchToVocalImageButton
            fragmentView?.requestFocus()
        }
        hideVideoImageButton?.setOnClickListener {
            if (playerViewLinearLayout?.visibility==View.VISIBLE) {
                hidePlayerView()
            } else {
                showPlayerView()
            }
            disableButtonForSometime(it)
            lastFocusView = hideVideoImageButton
            fragmentView?.requestFocus()
        }

        audioChannelImageButton?.setOnClickListener {
            audioChannelButtonListener()
            disableButtonForSometime(it)
            lastFocusView = audioChannelImageButton
            fragmentView?.requestFocus()
        }
        audioTrackImageButton?.setOnClickListener {
            mPresenter.playingParam.apply {
                LogUtil.d(TAG, "audioTrackImageButton.currentAudioTrackIndexPlayed = $currentAudioTrackIndexPlayed")
                currentAudioTrackIndexPlayed++
                LogUtil.d(TAG, "audioTrackImageButton.currentAudioTrackIndexPlayed = $currentAudioTrackIndexPlayed")
                val numAudioTracks = mPresenter.numberOfAudioTracks
                LogUtil.d(TAG, "audioTrackImageButton.numAudioTracks = $numAudioTracks")
                if (currentAudioTrackIndexPlayed > numAudioTracks)
                    currentAudioTrackIndexPlayed = 1
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
            lastFocusView = audioTrackImageButton
            fragmentView?.requestFocus()
        }

        actionMenuImageButton?.setOnClickListener {
            LogUtil.d(TAG, "actionMenuImageButton.setOnClickListener")
            actionMenuView?.showOverflowMenu()
            softDecoderFirstMenuItem?.isChecked = mPresenter.playingParam.softDecoderFirst
            autoPlayMenuItem?.isChecked = mPresenter.playingParam.isAutoPlay
            setTimerToHideSupportAudioControl()   // reset the timer
            disableButtonForSometime(it)
            lastFocusView = actionMenuImageButton
            fragmentView?.requestFocus()
        }
        actionMenuView?.setOnMenuItemClickListener { item: MenuItem? ->
            item?.let { itemIt->
                onOptionsItemSelected(itemIt)
                // lastFocusView = actionMenuView
                fragmentView?.requestFocus()
            } == true
        }
        playerDurationSeekbar?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // update the duration on controller UI
                mPresenter.onDurationSeekBarProgressChanged(progress, fromUser)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        supportToolbar?.let {
            it.setOnClickListener { v: View ->
                LogUtil.i(TAG, "supportToolbar.onClick() is called.")
                showSupportToolbarAudioControlSetTimer()
            }
        }
        playerViewLinearLayout?.let { it->
            it.setOnClickListener {
                LogUtil.d(TAG, "playerViewLinearLayout.onClick()")
                if (playerViewLinearLayout?.visibility == View.VISIBLE) {
                    supportToolbar?.performClick()
                }
                fragmentView?.requestFocus() // request focus for fragment view
            }
            it.setOnTouchListener { _, motionEvent ->
                val posX = motionEvent.x
                // LogUtil.d(TAG, "setOnTouchListener.motionEvent.x = $posX")
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        LogUtil.d(TAG, "setOnTouchListener.ACTION_DOWN.posX = $posX")
                        oldMotionEventX = posX
                        mPresenter.removeMsgFromDurationBarHandler()
                    }
                    MotionEvent.ACTION_UP -> {
                        LogUtil.d(TAG, "setOnTouchListener.ACTION_UP")
                        mPresenter.startDurationBarHandler()
                    }
                    MotionEvent.ACTION_MOVE -> run {
                        if (!playService.isSeekable()) return@run
                        mPresenter.playingParam.apply {
                            if (currentPlaybackState != PlaybackStateCompat.STATE_PLAYING
                                        && currentPlaybackState != PlaybackStateCompat.STATE_PAUSED) {
                                LogUtil.d(TAG, "setOnTouchListener.ACTION_MOVE.not playing and not paused")
                                return@run
                            }
                        }
                        if (posX <= 0 || posX >= screenSizeX) {
                            LogUtil.d(TAG, "setOnTouchListener.ACTION_MOVE.out of the screen size")
                            return@run
                        }
                        val distance = posX - oldMotionEventX
                        LogUtil.d(TAG, "setOnTouchListener.ACTION_MOVE.distance = $distance")
                        if (distance >= -20.0 && distance <= 20.0f) {
                            LogUtil.d(TAG, "setOnTouchListener.ACTION_MOVE.distance is too small")
                            return@run
                        }
                        val duration = playService.getMediaDuration()
                        if (duration > 0) {
                            val currentTime = playService.getCurrentPosition()
                            val progress = currentTime + ((distance / screenSizeX) * duration).toInt()
                            if (progress <= (duration - 2000)) {
                                // less than 2 seconds before the end
                                mPresenter.onDurationSeekBarProgressChanged(progress.toInt(),
                                    true)
                                update_Player_duration_seekbar_progress(progress.toInt())
                                showSupportToolbarAudioControl() // show the player buttons
                                // oldMotionEventX = posX
                            }
                        }
                        /*
                        if (duration > 0) {
                            playerDurationSeekbar?.let {
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
                        */
                    }
                    MotionEvent.ACTION_OUTSIDE -> {
                        LogUtil.d(TAG, "setOnTouchListener.ACTION_OUTSIDE")
                    }
                    else -> {
                        LogUtil.d(TAG, "setOnTouchListener.else")
                    }
                }
                false
            }
        }
    }

    // implementing PlayerBasePresenter.BasePresentView
    override fun setImageButtonStatus() {
        LogUtil.i(TAG, "setImageButtonStatus")
        val playingParam = mPresenter.playingParam
        if (playingParam.currentVolume > 0.0f) volumeImageButton?.setImageResource(R.drawable.non_volume)
        else volumeImageButton?.setImageResource(R.drawable.volume)
        switchToMusicImageButton?.apply {
            isEnabled = true
            visibility = View.VISIBLE
        }
        switchToVocalImageButton?.visibility = View.VISIBLE
        setOrientationImageButton(resources.configuration.orientation)
        // repeatImageButton
        when (playingParam.repeatStatus) {
            PlayerConstants.NoRepeatPlaying -> {
                // no repeat but show symbol of repeat all song with transparent background
                repeatImageButton?.setImageResource(R.drawable.repeat_no)
            }
            PlayerConstants.RepeatOneSong ->                 // repeat one song
                repeatImageButton?.setImageResource(R.drawable.repeat_one)
            PlayerConstants.RepeatAllSongs ->                 // repeat all song list
                repeatImageButton?.setImageResource(R.drawable.repeat_all)
        }
        activity?.let {
            repeatImageButton?.visibility = if (playingParam.isPlaySingleSong) View.GONE else View.VISIBLE
        }

        hideVideoImageButton?.apply {
            setImageResource(if (playerViewLinearLayout?.visibility==View.VISIBLE) R.drawable.hide_video
                else R.drawable.show_video)
            visibility = if (playingParam.isPlaySingleSong) View.GONE else View.VISIBLE
        }
    }

    override fun playButtonOnPauseButtonOff() {
        playMediaImageButton?.setImageResource(R.drawable.play_media_button_image)
    }

    override fun playButtonOffPauseButtonOn() {
        playMediaImageButton?.setImageResource(R.drawable.pause_media_button_image)
    }

    override fun setPlayingTimeTextView(playingTimeString: String?) {
        playingTimeTextView?.text = playingTimeString
    }

    override fun update_Player_duration_seekbar(duration: Float) {
        var durationTmp = duration
        playerDurationSeekbar?.progress = 0
        playerDurationSeekbar?.max = durationTmp.toInt()
        durationTmp /= 1000.0f // seconds
        val minutes = (durationTmp / 60.0f).toInt() // minutes
        val seconds = durationTmp.toInt() - minutes * 60
        val durationString = String.format(Locale.ENGLISH,"%3d:%02d",
            minutes, seconds)
        durationTimeTextView?.text = durationString
    }

    override fun update_Player_duration_seekbar_progress(progress: Int) {
        playerDurationSeekbar?.progress = progress
    }

    override fun updateVolumeSeekBarProgress() {
        // volumeSeekBar?.setProgressAndThumb(mPresenter.currentProgressForVolumeSeekBar)
    }

    override fun showNativeAndHideBannerAd() {
        val msgStr = "showNativeAndHideBannerAd"
        if (playerViewLinearLayout?.visibility == View.VISIBLE) {
            LogUtil.d(TAG, "${msgStr}.View.VISIBLE")
            val numVideoTracks =  mPresenter.numberOfVideoTracks
            LogUtil.d(TAG, "${msgStr}.numVideoTracks = $numVideoTracks")
            mPresenter.playingParam.let {
                LogUtil.d(TAG, "${msgStr}.playbackState = ${it.currentPlaybackState}")
                if (it.currentPlaybackState != PlaybackStateCompat.STATE_PLAYING
                    || numVideoTracks == 0
                    || playService.isCastSessionAvailable) {
                    // Not playing, No video tracks, or casting session is available
                    nativeAdViewVisibility = View.VISIBLE
                    nativeTemplate?.showNativeAd()
                    // hide the banner ad
                    // bannerLinearLayout?.visibility = View.GONE
                    bannerAdsLayout?.visibility = View.GONE
                } else {
                    hideNativeAd()
                    MyBannerTool.setVisible(bannerAdsLayout
                        , nativeAdViewVisibility)
                }
            }
        } else {
            LogUtil.d(TAG, "${msgStr}.View.INVISIBLE")
            // show the banner ad if in the right place
            MyBannerTool.setVisible(bannerAdsLayout
                , nativeAdViewVisibility)
        }
    }

    override fun hideNativeAd() {
        LogUtil.d(TAG, "hideNativeAd")
        nativeAdViewVisibility = View.GONE
        nativeTemplate?.hideNativeAd()
    }

    override fun showBufferingMessage() {
        LogUtil.d(TAG, "showBufferingMessage")
        messageAreaLinearLayout?.visibility = View.VISIBLE
        bufferingStringTextView?.startAnimation(animationText)
    }

    override fun dismissBufferingMessage() {
        LogUtil.d(TAG, "dismissBufferingMessage")
        animationText?.cancel()
        messageAreaLinearLayout?.visibility = View.GONE
    }

    override fun buildAudioTrackMenuItem(audioTrackNumber: Int) {
        LogUtil.d(TAG, "buildAudioTrackMenuItem.audioTrackNumber = $audioTrackNumber")
        // build R.id.audioTrack submenu
        audioTrackMenuItem?.subMenu?.let {
            var index = 0
            while (index < audioTrackNumber) {
                // audio track index start from 1 for user interface
                it[index].isVisible = true
                index++
            }
            for (j in index until it.size) {
                it[j].isVisible = false
            }
        }
    }

    override fun setTimerToHideSupportAudioControl() {
        LogUtil.d(TAG, "setTimerToHideSupportAndAudioController")
        if (playerViewLinearLayout?.visibility == View.VISIBLE) {
            controllerTimerHandler.removeCallbacksAndMessages(null)
            // 10 seconds
            controllerTimerHandler.postDelayed(controllerTimerRunnable,
                    PlayerConstants.PlayerView_Timeout.toLong())
        }
    }

    override fun showMusicAndVocalIsNotSet() {
        LogUtil.d(TAG, "showMusicAndVocalIsNotSet")
        ScreenUtil.showToast(activity, getString(R.string.musicAndVocalNotSet),
            toastTextSize, ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_SHORT
        )
    }

    override fun hidePlayerView() {
        LogUtil.d(TAG, "hidePlayerView")
        playerViewLinearLayout?.visibility = View.INVISIBLE
        hideNativeAd()
        setImageButtonStatus()
        // must be after statement of playerViewLinearLayout.visibility = View.INVISIBLE
        controllerTimerHandler.removeCallbacksAndMessages(null) // cancel the timer
        playBaseFragmentFunc?.baseHidePlayerView()
        mPresenter.playingParam.isPlayerViewVisible = false
        if (SmileAppBase.deviceType == CommonConstants.DEVICE_TYPE_PHONE) {
            setScreenOrientation(Configuration.ORIENTATION_PORTRAIT)
        }
    }

    override fun showPlayerView() {
        LogUtil.i(TAG, "showPlayerView")
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
        if (SmileAppBase.deviceType == CommonConstants.DEVICE_TYPE_PHONE) {
            LogUtil.d(TAG, "showPlayerView.orgOrientation = $orgOrientation")
            setScreenOrientation(orgOrientation)
        }
        LogUtil.d(TAG, "showPlayerView.fragmentView?.requestFocus()")
        fragmentView?.requestFocus()
    }

    override fun showToastNoFilesSelected() {
        activity?.let {
            ScreenUtil.showToast(it,
                it.getString(R.string.noFilesSelectedString),
                toastTextSize, ScreenUtil.FontSize_Pixel_Type,
                Toast.LENGTH_SHORT
            )
        }
    }

    override fun showToastNoPrevious() {
        activity?.let {
            ScreenUtil.showToast(it,
                it.getString(R.string.noPreviousSongString),
                toastTextSize, ScreenUtil.FontSize_Pixel_Type,
                Toast.LENGTH_SHORT
            )
        }
    }

    override fun showToastNoNext() {
        activity?.let {
            ScreenUtil.showToast(it,
                it.getString(R.string.noNextSongString),
                toastTextSize, ScreenUtil.FontSize_Pixel_Type,
                Toast.LENGTH_SHORT
            )
        }
    }

    override fun showToastNotSupported() {
        activity?.let {
            ScreenUtil.showToast(it,
                it.getString(R.string.formatNotSupportedString),
                toastTextSize, ScreenUtil.FontSize_Pixel_Type,
                Toast.LENGTH_SHORT
            )
        }
    }

    override fun isActivityFinishing(): Boolean {
        return activity?.isFinishing ?: true
    }

    override fun getFavoriteSongs(): ArrayList<SongInfo> {
        return DatabaseAccessUtil.readSavedSongList(
            activity, true)
    }

    override fun getFragment(): Fragment {
        return this
    }
    // end of implementing PlayerBasePresenter.BasePresentView

    private fun setScreenOrientation(orientation: Int) {
        orgOrientation = resources.configuration.orientation
        LogUtil.d(TAG, "setScreenOrientation.orgOrientation = $orgOrientation")
        activity?.requestedOrientation = when (orientation) {
            Configuration.ORIENTATION_PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            Configuration.ORIENTATION_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    private fun setOrientationImageButton(orientation : Int) {
        orientationImageButton?.let {
            it.rotation = if (orientation == Configuration.ORIENTATION_LANDSCAPE) 0.0f else 90.0f
            LogUtil.d(TAG, "setOrientationImageButton.rotation == ${it.rotation}")
            it.setImageResource(R.drawable.phone_portrait)
        }
    }
}