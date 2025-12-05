package com.smile.videoplayer.fragments

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import com.smile.karaoke.R
import com.smile.karaoke.fragments.PlayerBaseFragment
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil
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
        LogUtil.i(TAG, "onCreate() is called")
        presenter = VlcPlayerPresenter(this)

        // must be after VlcPlayerPresenter(this)
        super.onCreate(savedInstanceState)

        /*
        arguments?.let {
            isAutoPlay = it.getBoolean(PlayerConstants.IS_AUTOPLAY_STATE, false)
        }

        // must be after super.onCreate(savedInstanceState)
        activity?.let {
            mPlayServiceIntent = Intent(it, VlcPlayService::class.java)
            val callingIntent: Intent? = it.intent
            LogUtil.d(TAG, "onCreate.callingIntent = $callingIntent")
            mPresenter.initializeVariables(savedInstanceState, callingIntent, isAutoPlay)
        }
        */

        castContext = null  // disable cast for VLC player for now

        LogUtil.i(TAG, "onCreate.finished")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LogUtil.i(TAG, "onViewCreated() is called.")
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
        LogUtil.i(TAG, "onViewCreated() is finished.")
    }

    override fun onStart() {
        super.onStart()
        LogUtil.i(TAG, "onStart")
        presenter.playingParam.let {
            LogUtil.d(TAG, "onStart.preparedStatus = ${it.preparedStatus}")
            LogUtil.d(TAG, "onStart.isPlaySingleSong = ${it.isPlaySingleSong}")
            LogUtil.d(TAG, "onStart.isSingleSongOpened = ${it.singleSongPlayingStatus}")
            LogUtil.d(TAG, "onStart.wentToFavorite = ${it.wentToFavorite}")
            if (!it.wentToFavorite) {   // not back from favorite activity
                if (!it.isPlaySingleSong || it.singleSongPlayingStatus == 2) {
                    // isSingleSongOpened = 2 means playing single song
                    LogUtil.d(TAG, "onStart.playSongPlayedBeforeActivityCreated")
                    presenter.playSongPlayedBeforeActivityCreated()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        LogUtil.i(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        LogUtil.i(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        LogUtil.i(TAG, "onStop")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        LogUtil.i(TAG, "onConfigurationChanged() is called.")
        super.onConfigurationChanged(newConfig)
        setVideoWindowSize()
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.i(TAG, "onDestroy")
        playService?.detachPlayerViews()
        if (mPlayServiceIntent != null) {
            activity?.stopService(mPlayServiceIntent)
        }
    }

    // implement abstract methods of super class
    override fun setCurrentPlayerToPlayerView() {
        // do nothing for now
    }

    override fun getPlayService(): VlcPlayService? {
        return playService
    }

    override fun setupMenuItems() {
        channelMenuItem?.isVisible = true
        channelMenuItem?.isEnabled = false
    }

    override fun getPlayServiceIntent(): Intent {
        return Intent(activity, VlcPlayService::class.java)
    }

    override fun onPlayServiceConnected(service: IBinder) {
        LogUtil.i(TAG, "onPlayServiceConnected")
        val binder = service as LocalBinder
        playService = binder.getService()
        // Test code here for ExoPlayService
        playService?.presenter = this.presenter
        playService?.initVlcPlayer()
        playService?.initMediaControllerCompat(this.presenter)
        presenter.playSongPlayedBeforeActivityCreated()
    }

    override fun getPlayerPresenter() : VlcPlayerPresenter {
        return presenter
    }

    override fun audioChannelButtonListener() {
        // not support yet
        activity?.let {
            val str = it.getString(R.string.notSupportedString)
            ScreenUtil.showToast(it, str, toastTextSize, ScreenUtil.FontSize_Pixel_Type,
                Toast.LENGTH_SHORT)
        }
    }
    // end of implementing methods of super class

    // Implement VlcPlayerPresenter.VlcPresentView
    override fun setVideoWindowSize() {
        LogUtil.i(TAG, "setVideoWindowSize")
        playService?.apply {
            setVideoWindowSize(videoVLCPlayerView)
        }
    }
}