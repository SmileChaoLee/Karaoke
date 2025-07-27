package com.smile.karaoke.exoplayer.fragments

import android.content.Context
import android.content.Intent

import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.smile.karaoke.R
import com.smile.karaoke.constants.CommonConstants
import com.smile.karaoke.constants.PlayerConstants
import com.smile.karaoke.exoplayer.presenters.ExoPlayerPresenter
import com.smile.karaoke.exoplayer.presenters.ExoPlayerPresenter.ExoPlayerPresentView
import com.smile.karaoke.exoplayer.services.ExoPlayService
import com.smile.karaoke.exoplayer.services.ExoPlayService.LocalBinder
import com.smile.karaoke.fragments.PlayerBaseFragment
import com.smile.smilelibraries.utilities.ScreenUtil

@UnstableApi
class ExoPlayerFragment : PlayerBaseFragment(), ExoPlayerPresentView {
    companion object {
        private const val TAG: String = "ExoPlayerFragment"
    }
    private lateinit var presenter: ExoPlayerPresenter
    private var playerView: PlayerView? = null
    private var playService: ExoPlayService? = null
    private var mPlayServiceIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        presenter = ExoPlayerPresenter(this)
        super.onCreate(savedInstanceState)  // must be after ExoPlayerPresenter(this, this)
        var isAutoPlay = false
        arguments?.let {
            isAutoPlay = it.getBoolean(PlayerConstants.IS_AUTOPLAY_STATE, false)
        }
        // must be after super.onCreate(savedInstanceState)
        activity?.let {
            mPlayServiceIntent = Intent(it, ExoPlayService::class.java)
            val callingIntent: Intent? = it.intent
            Log.d(TAG, "onCreate.callingIntent = $callingIntent")
            mPresenter.initializeVariables(savedInstanceState, callingIntent, isAutoPlay)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated")
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        playerView?.player = null
        if (mPlayServiceIntent != null) {
            activity?.stopService(mPlayServiceIntent)
        }
    }

    private fun setVideoPlayerView() {
        Log.d(TAG, "setVideoPlayerView")
        val layParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT)
        layParams.gravity = Gravity.CENTER
        activity?.let {
            playerView = PlayerView(it.applicationContext)
            Log.d(TAG, "setVideoPlayerView.playerView = $playerView")
            playerView?.apply {
                layoutParams = layParams
                setBackgroundColor(ContextCompat.getColor(it.applicationContext,
                    android.R.color.black))
                playerViewLinearLayout?.addView(this)
                visibility = View.VISIBLE
                // useArtwork = true
                setArtworkDisplayMode(PlayerView.ARTWORK_DISPLAY_MODE_OFF)
                useController = false
                // must be after super.onCreate(savedInstanceState)
                // player = playService?.exoPlayer
                Log.d(TAG, "setVideoPlayerView.playService = $playService")
                player = playService?.getCurrentPlayer()
                requestFocus()
            }
        }
    }

    // implementing methods of ExoPlayerPresenter.ExoPlayerPresentView
    override fun setCurrentPlayerToPlayerView() {
        Log.d(TAG, "setCurrentPlayerToPlayerView")
        playerView?.apply {
            Log.d(TAG, "setCurrentPlayerToPlayerView.playService?.currentPlayer")
            player = playService?.getCurrentPlayer()
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

    override fun setMenuItemsVisibility() {
        val channelMenuItem = mainMenu?.findItem(R.id.channel)
        channelMenuItem?.isVisible = true
        channelMenuItem?.isEnabled = true
    }

    override fun onPlayServiceConnected(service: IBinder) {
        Log.d(TAG, "onPlayServiceConnected")
        val binder = service as LocalBinder
        playService = binder.getService()
        // Test code here for ExoPlayService
        playService?.presenter = this.presenter
        playService?.initMediaControllerCompat(this.presenter)
        playService?.initPlayers()
        Log.d(TAG, "onPlayServiceConnected.Video player view")
        // Video player view
        setVideoPlayerView()
        Log.d(TAG, "onPlayServiceConnected.presenter.playSongPlayedBeforeActivityCreated()")
        presenter.playSongPlayedBeforeActivityCreated()
    }

    override fun onPlayServiceDisconnected() {
        Log.d(TAG, "onPlayServiceDisconnected")
        activity?.stopService(mPlayServiceIntent)
        isServiceDestroyed = true
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
                it.stopService(mPlayServiceIntent)
                isServiceBound = false
                isServiceDestroyed = true
            } else {
                Log.d(TAG, "unbindAndStopPlayService.PlayService is not bound")
            }
        }
    }

    override fun audioChannelButtonListener() {
        mPresenter.playingParam.apply {
            when (currentChannelPlayed) {
                CommonConstants.LEFT_CHANNEL -> {
                    currentChannelPlayed = CommonConstants.RIGHT_CHANNEL
                }
                CommonConstants.RIGHT_CHANNEL -> {
                    currentChannelPlayed = CommonConstants.STEREO
                }
                CommonConstants.STEREO -> {
                    currentChannelPlayed = CommonConstants.LEFT_CHANNEL
                }
            }
            activity?.let{
                val str =
                    when (currentChannelPlayed) {
                        CommonConstants.LEFT_CHANNEL -> it.getString(R.string.leftChannelString)
                        CommonConstants.RIGHT_CHANNEL -> it.getString(R.string.rightChannelString)
                        CommonConstants.STEREO -> it.getString(R.string.stereoChannelString)
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