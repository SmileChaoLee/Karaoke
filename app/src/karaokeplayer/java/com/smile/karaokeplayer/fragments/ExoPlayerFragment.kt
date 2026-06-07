package com.smile.karaokeplayer.fragments

import android.content.Intent
import android.content.res.Configuration

import android.os.Bundle
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.google.android.gms.cast.framework.CastContext
import com.smile.karaoke.R
import com.smile.karaoke.SmileAppBase
import com.smile.karaoke.constants.CommonConstants
import com.smile.karaokeplayer.presenters.ExoPlayerPresenter
import com.smile.karaokeplayer.services.ExoPlayService
import com.smile.karaoke.fragments.PlayerBaseFragment
import com.smile.karaoke.utilities.DatabaseUtil
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil

@UnstableApi
class ExoPlayerFragment : PlayerBaseFragment(),
    ExoPlayerPresenter.ExoPlayerPresentView {
    companion object {
        private const val TAG: String = "ExoPlayerFragment"
    }
    private lateinit var presenter: ExoPlayerPresenter
    private var playerView: PlayerView? = null
    private var playService: ExoPlayService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "onCreate")
        presenter = ExoPlayerPresenter(this)
        // must be after ExoPlayerPresenter(this)
        super.onCreate(savedInstanceState)
        LogUtil.i(TAG, "onCreate.finished")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "onViewCreated")
        super.onViewCreated(view, savedInstanceState)
        LogUtil.i(TAG, "onViewCreated.finished")
    }

    override fun onResume() {
        super.onResume()
        LogUtil.i(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        LogUtil.i(TAG, "onPause")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        LogUtil.i(TAG, "onConfigurationChanged")
        super.onConfigurationChanged(newConfig)
        setVideoWindowSize()
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.i(TAG, "onDestroy")
        if (mPlayServiceIntent != null) {
            activity?.stopService(mPlayServiceIntent)
        }
        playerView?.player = null
        playerView = null
    }

    // implementing methods of ExoPlayerPresenter.ExoPlayerPresentView
    override fun setVideoPlayerView() {
        LogUtil.i(TAG, "setVideoPlayerView")
        val layParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT)
        layParams.gravity = Gravity.CENTER
        activity?.let {
            playerView = PlayerView(it.applicationContext)
            LogUtil.d(TAG, "setVideoPlayerView.playerView = $playerView")
            playerView?.apply {
                setVideoWindowSize()
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
                LogUtil.d(TAG, "setVideoPlayerView.playService = $playService")
                player = playService?.getCurrentPlayer()
                requestFocus()
            }
        }
    }

    override fun removeVideoPlayerView() {
        LogUtil.i(TAG, "removeVideoPlayerView")
        playerView?.apply {
            playerViewLinearLayout?.removeView(this)
            player = null
        }
        playerView = null
    }

    override fun setVideoWindowSize() {
        val logStr = "setVideoWindowSize"
        LogUtil.i(TAG, logStr)
        playerView?.resizeMode =
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                AspectRatioFrameLayout.RESIZE_MODE_FIT
            else AspectRatioFrameLayout.RESIZE_MODE_FILL
        playService?.setVideoWindowSize()
    }

    override fun setCurrentPlayerToPlayerView() {
        LogUtil.i(TAG, "setCurrentPlayerToPlayerView")
        playerView?.apply {
            LogUtil.d(TAG, "setCurrentPlayerToPlayerView.playService?.currentPlayer")
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

    override fun setupMenuItems() {
        softDecoderFirstMenuItem?.isVisible = true
        softDecoderFirstMenuItem?.isEnabled = true
        channelMenuItem?.isVisible = true
        channelMenuItem?.isEnabled = true
    }

    override fun getPlayServiceIntent(): Intent {
        return Intent(activity, ExoPlayService::class.java)
    }

    override fun onPlayServiceConnected(service: IBinder) {
        LogUtil.i(TAG, "onPlayServiceConnected")
        val binder = service as ExoPlayService.LocalBinder
        playService = binder.getService()
        // Test code here for ExoPlayService
        playService?.presenter = this.presenter
        playService?.initMediaControllerCompat(this.presenter)
        playService?.initPlayers()
        LogUtil.d(TAG, "onPlayServiceConnected.Video player view")
        // Video player view
        setVideoPlayerView()
        LogUtil.d(TAG, "onPlayServiceConnected.presenter.playSongPlayedBeforeActivityCreated()")
        presenter.playSongPlayedBeforeActivityCreated()
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
                ScreenUtil.showToast(it, str,
                    toastTextSize, ScreenUtil.FontSize_Pixel_Type,
                    Toast.LENGTH_SHORT)
            }
            mPresenter.setAudioTrackAndChannel(currentAudioTrackIndexPlayed,
                currentChannelPlayed)
        }
    }

    override fun getFavDatabaseName(): String {
        return DatabaseUtil.getFavDatabaseName()
    }

    override fun obtainCastContext(): CastContext? {
        activity?.let {
            return (it.application as SmileAppBase).castContext
        }
        return null
    }
    // end of implementing abstract methods of super class
}