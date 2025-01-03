package exoplayer.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastState
import com.smile.karaokeplayer.R
import com.smile.karaokeplayer.constants.CommonConstants
import com.smile.karaokeplayer.fragments.PlayerBaseViewFragment
import com.smile.smilelibraries.utilities.ScreenUtil
import exoplayer.presenters.ExoPlayerPresenter
import exoplayer.presenters.ExoPlayerPresenter.ExoPlayerPresentView
import exoplayer.services.ExoPlayService
import exoplayer.services.ExoPlayService.LocalBinder

private const val TAG: String = "ExoPlayerFragment"

class ExoPlayerFragment : PlayerBaseViewFragment(), ExoPlayerPresentView {
    private lateinit var presenter: ExoPlayerPresenter
    private var playerView: StyledPlayerView? = null
    private var playService: ExoPlayService? = null
    private var mediaRouteButton: MediaRouteButton? = null
    private var mPlayServiceIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        presenter = ExoPlayerPresenter(this, this)
        super.onCreate(savedInstanceState)  // must be after ExoPlayerPresenter(this, this)
        arguments?.let {}
        // must be after super.onCreate(savedInstanceState)
        activity?.let {
            mPlayServiceIntent = Intent(it, ExoPlayService::class.java)
            val callingIntent: Intent? = it.intent
            Log.d(TAG, "onCreate.callingIntent = $callingIntent")
            mPresenter.initializeVariables(savedInstanceState, callingIntent)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated")
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
        super.onDestroy()
        playerView?.player = null
    }

    fun setVideoPlayerView() {
        Log.d(TAG, "setVideoPlayerView")
        val layParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT)
        layParams.gravity = Gravity.CENTER
        activity?.let {
            playerView = StyledPlayerView(it.applicationContext)
            Log.d(TAG, "setVideoPlayerView.playerView = $playerView")
            playerView?.apply {
                layoutParams = layParams
                setBackgroundColor(ContextCompat.getColor(it.applicationContext, android.R.color.black))
                playerViewLinearLayout?.addView(this)
                visibility = View.VISIBLE
                useArtwork = true
                useController = false
                // must be after super.onCreate(savedInstanceState)
                // player = playService?.exoPlayer
                Log.d(TAG, "setVideoPlayerView.playService = $playService")
                Log.d(TAG, "setVideoPlayerView.playService?.currentPlayer = ${playService?.currentPlayer}")
                player = playService?.currentPlayer
                requestFocus()
            }
        }
    }

    fun setMediaRouteButtonVisible() {
        Log.d(TAG, "setMediaRouteButtonVisible")
        if (!com.smile.karaokeplayer.BuildConfig.DEBUG) {
            return
        }
        val castState = playService?.currentCastState
        Log.d(TAG, "setMediaRouteButtonVisible.castState = $castState")
        val deviceAvailable = if (castState != null)  castState != CastState.NO_DEVICES_AVAILABLE else false
        Log.d(TAG, "setMediaRouteButtonVisible.deviceAvailable = $deviceAvailable")
        mediaRouteButton?.visibility = if (deviceAvailable) View.VISIBLE else View.GONE
        // mediaRouteButton?.visibility = View.VISIBLE
        // mediaRouteButton?.isEnabled = true
        Log.d(TAG, "setMediaRouteButtonVisible.mediaRouteButton?.isEnabled = ${mediaRouteButton?.isEnabled}")
    }

    // implementing methods of ExoPlayerPresenter.ExoPlayerPresentView
    override fun setCurrentPlayerToPlayerView() {
        Log.d(TAG, "setCurrentPlayerToPlayerView")
        playerView?.apply {
            Log.d(TAG, "setCurrentPlayerToPlayerView.playService?.currentPlayer")
            player = playService?.currentPlayer
            requestFocus()
        }
    }

    override fun getPlayService(): ExoPlayService? {
        return playService
    }
    // end of implementing methods of ExoPlayerPresenter.ExoPlayerPresentView

    // implement abstract methods of super class
    override fun getPlayerPresenter() : ExoPlayerPresenter {
        return presenter
    }

    override fun setMediaRouteButtonView(buttonMarginLeft: Int, imageButtonHeight: Int) {
        // MediaRouteButton View
        Log.d(TAG, "setMediaRouteButtonView")
        if (!com.smile.karaokeplayer.BuildConfig.DEBUG) {
            return
        }
        Log.d(TAG, "setMediaRouteButtonView.BuildConfig.DEBUG")
        try {
            mediaRouteButton = fragmentView?.findViewById(R.id.media_route_button)
            setMediaRouteButtonVisible()
            mediaRouteButton?.let {
                activity?.applicationContext?.let { ctxIt ->
                    CastButtonFactory.setUpMediaRouteButton(ctxIt, it)
                }
            }

            val layoutParams: MarginLayoutParams = mediaRouteButton?.layoutParams as MarginLayoutParams
            layoutParams.setMargins(buttonMarginLeft, 0, 0, 0)
            val mediaRouteButtonBitmap = BitmapFactory.decodeResource(resources, R.drawable.cast)
            val mediaRouteButtonDrawable: Drawable = BitmapDrawable(
                resources,
                Bitmap.createScaledBitmap(
                    mediaRouteButtonBitmap,
                    imageButtonHeight,
                    imageButtonHeight,
                    true
                )
            )
            mediaRouteButton?.setRemoteIndicatorDrawable(mediaRouteButtonDrawable)
        } catch (ex: Exception) {
            Log.d(TAG, "setMediaRouteButtonView.Exception")
            ex.printStackTrace()
        }
    }

    override fun setMenuItemsVisibility() {
        val channelMenuItem = mainMenu?.findItem(R.id.channel)
        channelMenuItem?.isVisible = true
        channelMenuItem?.isEnabled = true
    }

    /*
    override fun setSwitchToVocalImageButtonVisibility() {
        // do nothing
    }
    */

    override fun onPlayServiceConnected(service: IBinder) {
        Log.d(TAG, "onPlayServiceConnected")
        val binder = service as LocalBinder
        playService = binder.getService()
        // Test code here for ExoPlayService
        playService?.presenter = this.presenter
        playService?.initMediaControllerCompat(this.presenter)
        playService?.initCastPlayerAndExoPlayer()
        Log.d(TAG, "onPlayServiceConnected.Video player view")
        // Video player view
        setVideoPlayerView()
        Log.d(TAG, "onPlayServiceConnected.presenter.playSongPlayedBeforeActivityCreated()")
        presenter.playSongPlayedBeforeActivityCreated()
        setMediaRouteButtonVisible()
    }

    override fun onPlayServiceDisconnected() {
        Log.d(TAG, "onPlayServiceDisconnected")
        startAndBindPlayService()
    }

    override fun startAndBindPlayService() {
        activity?.let {
            if (isServiceDestroyed) {
                Log.d(TAG, "startAndBindPlayService.startService()")
                it.startService(mPlayServiceIntent)
                isServiceDestroyed = false
            } else {
                Log.d(TAG, "startAndBindPlayService.PlayService already started")
            }
            if (!isServiceBound) {
                val result: Boolean = it.bindService(mPlayServiceIntent!!, connection, Context.BIND_IMPORTANT)
                Log.d(TAG, "startAndBindPlayService.isBound = $result")
            } else {
                Log.d(TAG, "startAndBindPlayService.PlayService already bound")
            }
        }
    }

    override fun unbindAndStopPlayService() {
        activity?.let {
            if (isServiceBound) {
                Log.d(TAG, "unbindAndStopPlayService.unbindService()")
                it.unbindService(connection)
                // playService = null;
                isServiceBound = false
            } else {
                Log.d(TAG, "unbindAndStopPlayService.PlayService is not bound")
            }
            if (!isServiceDestroyed) {
                Log.d(TAG, "unbindAndStopPlayService.stopService()")
                it.stopService(mPlayServiceIntent)
                isServiceDestroyed = true
            } else {
                Log.d(TAG,"unbindAndStopPlayService.PlayService is destroyed or not started")
            }
        }
    }

    override fun audioChannelButtonListener() {
        mPresenter.playingParam.apply {
            when (currentChannelPlayed) {
                CommonConstants.LeftChannel -> {
                    currentChannelPlayed = CommonConstants.RightChannel
                }
                CommonConstants.RightChannel -> {
                    currentChannelPlayed = CommonConstants.StereoChannel
                }
                CommonConstants.StereoChannel -> {
                    currentChannelPlayed = CommonConstants.LeftChannel
                }
            }
            activity?.let{
                val str =
                    when (currentChannelPlayed) {
                        CommonConstants.LeftChannel -> it.getString(R.string.leftChannelString)
                        CommonConstants.RightChannel -> it.getString(R.string.rightChannelString)
                        CommonConstants.StereoChannel -> it.getString(R.string.stereoChannelString)
                        else -> it.getString(R.string.unknown)
                    }
                ScreenUtil.showToast(it, str, toastTextSize, ScreenUtil.FontSize_Pixel_Type,
                    Toast.LENGTH_SHORT)
            }
            mPresenter.setAudioTrackAndChannel(currentAudioTrackIndexPlayed, currentChannelPlayed)
        }
    }
    // end of implementing methods of super class
}