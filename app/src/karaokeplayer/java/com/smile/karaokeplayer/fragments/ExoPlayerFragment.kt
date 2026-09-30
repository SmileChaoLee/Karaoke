package com.smile.karaokeplayer.fragments

import android.content.Intent
import android.content.res.Configuration

import android.os.Bundle
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.google.android.gms.cast.CastMediaControlIntent
import com.google.android.gms.cast.framework.CastContext
import com.smile.karaoke.SmileAppBase
import com.smile.karaokeplayer.presenters.ExoPlayerPresenter
import com.smile.karaokeplayer.services.ExoPlayService
import com.smile.karaoke.fragments.PlayerBaseFragment
import com.smile.karaoke.utilities.DatabaseUtil
import com.smile.karaoke.utilities.LogUtil

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
        LogUtil.d(TAG, "onCreate")
        presenter = ExoPlayerPresenter(this)
        // must be after ExoPlayerPresenter(this)
        super.onCreate(savedInstanceState)
        LogUtil.d(TAG, "onCreate.finished")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        LogUtil.d(TAG, "onViewCreated")
        super.onViewCreated(view, savedInstanceState)
        LogUtil.d(TAG, "onViewCreated.finished")
    }

    override fun onResume() {
        super.onResume()
        LogUtil.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        LogUtil.d(TAG, "onPause")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        LogUtil.d(TAG, "onConfigurationChanged")
        super.onConfigurationChanged(newConfig)
        setVideoWindowSize()
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.d(TAG, "onDestroy")
        if (mPlayServiceIntent != null) {
            activity?.stopService(mPlayServiceIntent)
        }
        playerView?.player = null
        playerView = null
    }

    // implementing methods of ExoPlayerPresenter.ExoPlayerPresentView
    override fun setVideoPlayerView() {
        LogUtil.d(TAG, "setVideoPlayerView")
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
        LogUtil.d(TAG, "removeVideoPlayerView")
        playerView?.apply {
            playerViewLinearLayout?.removeView(this)
            player = null
        }
        playerView = null
    }

    override fun setVideoWindowSize() {
        val logStr = "setVideoWindowSize"
        LogUtil.d(TAG, logStr)
        playerView?.resizeMode =
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                AspectRatioFrameLayout.RESIZE_MODE_FIT
            else AspectRatioFrameLayout.RESIZE_MODE_FILL
        playService?.setVideoWindowSize()
    }

    override fun setCurrentPlayerToPlayerView() {
        LogUtil.d(TAG, "setCurrentPlayerToPlayerView")
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
        LogUtil.d(TAG, "onPlayServiceConnected")
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

    override fun getFavDatabaseName(): String {
        return DatabaseUtil.getFavDatabaseName()
    }

    override fun obtainCastContext(): CastContext? {
        var castCtx: CastContext? = null
        activity?.let {
            castCtx = (it.application as SmileAppBase).castContext
            castCtx?.setReceiverApplicationId(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID)
        }
        return castCtx
    }
    override fun isCasting(): Boolean {
        return getPlayService()?.isCastSession ?: false
    }
    // end of implementing abstract methods of super class
}