package com.smile.karaokeplayer.vlcplayer.fragments

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.smile.karaokeplayer.R
import com.smile.karaokeplayer.constants.PlayerConstants
import com.smile.karaokeplayer.fragments.PlayerBaseViewFragment
import com.smile.smilelibraries.utilities.ScreenUtil
import org.videolan.libvlc.util.VLCVideoLayout
import com.smile.karaokeplayer.vlcplayer.Presenters.VlcPlayerPresenter
import com.smile.karaokeplayer.vlcplayer.services.VlcPlayService
import com.smile.karaokeplayer.vlcplayer.services.VlcPlayService.LocalBinder

private const val TAG: String = "VlcPlayerFragment"

class VlcPlayerFragment : PlayerBaseViewFragment(), VlcPlayerPresenter.VlcPresentView {
    // private val enableSubtitles = true
    // private val useTextureView = false
    private lateinit var presenter: VlcPlayerPresenter
    private lateinit var videoVLCPlayerView: VLCVideoLayout
    private var playService: VlcPlayService? = null
    private var mPlayServiceIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        presenter = VlcPlayerPresenter(this, this)

        super.onCreate(savedInstanceState)  // must be after VlcPlayerPresenter(this, this)
        Log.d(TAG, "onCreate() is called")
        var isAutoPlay = false
        arguments?.let {
            isAutoPlay = it.getBoolean(PlayerConstants.IS_AUTOPLAY_STATE, false)
        }

        // must be after super.onCreate(savedInstanceState)
        // must be before volumeSeekBar settings
        // presenter.initVLCPlayer() // must be before volumeSeekBar settings
        // mPresenter.initMediaSessionCompat()
        activity?.let {
            mPlayServiceIntent = Intent(it, VlcPlayService::class.java)
            val callingIntent: Intent? = it.intent
            Log.d(TAG, "onCreate.callingIntent = $callingIntent")
            mPresenter.initializeVariables(savedInstanceState, callingIntent, isAutoPlay)
        }

        Log.d(TAG, "onCreate() is finished")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated() is called.")
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

        /*
        val currentProgress = presenter.currentProgressForVolumeSeekBar
        volumeSeekBar?.setProgressAndThumb(currentProgress)
        */
        // presenter.playSongPlayedBeforeActivityCreated()  // moved to onResume()

        Log.d(TAG, "onViewCreated() is finished.")
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
        Log.d(TAG, "onStart.isShowingInterstitialAd = $isShowingInterstitialAd")
        if (!isShowingInterstitialAd) {
            presenter.playingParam.let {
                Log.d(TAG, "onStart.preparedStatus = ${it.preparedStatus}")
                Log.d(TAG, "onStart.isPlaySingleSong = ${it.isPlaySingleSong}")
                Log.d(TAG, "onStart.isSingleSongOpened = ${it.singleSongPlayingStatus}")
                if (!it.isPlaySingleSong || (it.isPlaySingleSong && it.singleSongPlayingStatus == 2)) {
                    // isSingleSongOpened = 2 means playing single song
                    Log.d(TAG, "onStart.playSongPlayedBeforeActivityCreated")
                    presenter.playSongPlayedBeforeActivityCreated()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        Log.d(TAG, "onConfigurationChanged() is called.")
        super.onConfigurationChanged(newConfig)
        setVideoWindowSize()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() is called.")
        playService?.detachPlayerViews()
    }

    // implement abstract methods of super class
    override fun setCurrentPlayerToPlayerView() {
        // do nothing for now
    }

    override fun getPlayService(): VlcPlayService? {
        return playService
    }

    override fun setMediaRouteButtonView(buttonMarginLeft: Int, imageButtonHeight: Int) {}

    override fun setMenuItemsVisibility() {
        val channelMenuItem = mainMenu?.findItem(R.id.channel)
        channelMenuItem?.isVisible = true
        channelMenuItem?.isEnabled = false
    }

    /*
    override fun setSwitchToVocalImageButtonVisibility() {
        switchToVocalImageButton?.visibility = View.GONE
    }
    */

    override fun onPlayServiceConnected(service: IBinder) {
        Log.d(TAG, "onPlayServiceConnected")
        val binder = service as LocalBinder
        playService = binder.getService()
        // Test code here for ExoPlayService
        playService?.presenter = this.presenter
        playService?.initVlcPlayer()
        playService?.initMediaControllerCompat(this.presenter)
        presenter.playSongPlayedBeforeActivityCreated()
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
        Log.d(TAG, "setVideoWindowSize")
        playService?.apply {
            videoVLCPlayerView?.let {
                setVideoWindowSize(it)
            }
        }
    }
}