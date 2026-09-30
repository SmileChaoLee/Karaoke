package com.smile.videoplayer.fragments

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import com.google.android.gms.cast.framework.CastContext
import com.smile.karaoke.fragments.PlayerBaseFragment
import com.smile.karaoke.utilities.DatabaseUtil
import com.smile.karaoke.utilities.LogUtil
import org.videolan.libvlc.util.VLCVideoLayout
import com.smile.videoplayer.presenters.VlcPlayerPresenter
import com.smile.videoplayer.services.VlcPlayService
import com.smile.videoplayer.services.VlcPlayService.LocalBinder

@OptIn(UnstableApi::class)
class VlcPlayerFragment : PlayerBaseFragment(), VlcPlayerPresenter.VlcPresentView {

    companion object {
        private const val TAG: String = "VlcPlayerFragment"
    }

    private lateinit var presenter: VlcPlayerPresenter
    private lateinit var videoVLCPlayerView: VLCVideoLayout
    private var playService: VlcPlayService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.d(TAG, "onCreate() is called")
        presenter = VlcPlayerPresenter(this)
        // must be after VlcPlayerPresenter(this)
        super.onCreate(savedInstanceState)
        LogUtil.d(TAG, "onCreate.finished")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LogUtil.d(TAG, "onViewCreated() is called.")
        // Video player view
        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        layoutParams.gravity = Gravity.CENTER
        activity?.let {
            val context = it.applicationContext
            videoVLCPlayerView = VLCVideoLayout(context)
            videoVLCPlayerView.layoutParams = layoutParams
            videoVLCPlayerView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.black))
            playerViewLinearLayout?.addView(videoVLCPlayerView)
            videoVLCPlayerView.visibility = View.VISIBLE
        }
        LogUtil.d(TAG, "onViewCreated() is finished.")
    }

    override fun onStart() {
        super.onStart()
        LogUtil.d(TAG, "onStart")
        presenter.playingParam.let {
            LogUtil.d(TAG, "onStart.preparedStatus = ${it.preparedStatus}")
            LogUtil.d(TAG, "onStart.isPlaySingleSong = ${it.isPlaySingleSong}")
            LogUtil.d(TAG, "onStart.singleSongPlayingStatus = ${it.singleSongPlayingStatus}")
            LogUtil.d(TAG, "onStart.wentToFavorite = ${it.wentToFavorite}")
            if (!it.wentToFavorite) {   // not back from favorite activity
                if (!it.isPlaySingleSong || it.singleSongPlayingStatus == 2) {
                    // singleSongPlayingStatus = 2 means playing single song
                    LogUtil.d(TAG, "onStart.playSongPlayedBeforeActivityCreated")
                    presenter.playSongPlayedBeforeActivityCreated()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        LogUtil.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        LogUtil.d(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        LogUtil.d(TAG, "onStop")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        LogUtil.d(TAG, "onConfigurationChanged() is called.")
        super.onConfigurationChanged(newConfig)
        setVideoWindowSize()
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.d(TAG, "onDestroy")
        playService?.detachPlayerViews()
        if (mPlayServiceIntent != null) {
            activity?.stopService(mPlayServiceIntent)
        }
    }

    // implement abstract methods of super class
    override fun getPlayerPresenter() : VlcPlayerPresenter {
        return presenter
    }

    override fun setupMenuItems() {
        softDecoderFirstMenuItem?.isVisible = false
        softDecoderFirstMenuItem?.isEnabled = false
        channelMenuItem?.isVisible = true
        channelMenuItem?.isEnabled = true
    }

    override fun getPlayServiceIntent(): Intent {
        return Intent(activity, VlcPlayService::class.java)
    }

    override fun onPlayServiceConnected(service: IBinder) {
        LogUtil.d(TAG, "onPlayServiceConnected")
        val binder = service as LocalBinder
        playService = binder.getService()
        // Test code here for ExoPlayService
        playService?.presenter = this.presenter
        playService?.initVlcPlayer()
        playService?.initMediaControllerCompat(this.presenter)
        presenter.playSongPlayedBeforeActivityCreated()
    }

    override fun getFavDatabaseName(): String {
        return DatabaseUtil.getFavDatabaseName()
    }

    override fun obtainCastContext(): CastContext? {
        return null  // disable cast for VLC player for now
    }

    override fun isCasting(): Boolean {
        return getPlayService()?.isCastSession ?: false
    }
    // end of implementing abstract methods of super class

    // Implement VlcPlayerPresenter.VlcPresentView
    override fun setCurrentPlayerToPlayerView() {
        // do nothing for now
    }

    override fun getPlayService(): VlcPlayService? {
        return playService
    }

    override fun setVideoWindowSize() {
        LogUtil.d(TAG, "setVideoWindowSize")
        playService?.apply {
            setVideoWindowSize(videoVLCPlayerView)
        }
    }
}