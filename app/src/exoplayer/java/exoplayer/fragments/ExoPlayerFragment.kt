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
import androidx.core.content.ContextCompat
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastState
import com.smile.karaokeplayer.R
import com.smile.karaokeplayer.fragments.PlayerBaseViewFragment
import exoplayer.presenters.ExoPlayerPresenter
import exoplayer.presenters.ExoPlayerPresenter.ExoPlayerPresentView
import exoplayer.services.ExoPlayService
import exoplayer.services.ExoPlayService.LocalBinder

private const val TAG: String = "ExoPlayerFragment"

class ExoPlayerFragment : PlayerBaseViewFragment(), ExoPlayerPresentView {
    private lateinit var presenter: ExoPlayerPresenter
    private var playerView: StyledPlayerView? = null
    private var mediaRouteButton: MediaRouteButton? = null

    private var playService: ExoPlayService? = null
    override fun getPlayService() : ExoPlayService? {
        return playService
    }

    private var mPlayServiceIntent: Intent? = null
    override fun onPlayServiceConnected(service: IBinder) {
        Log.d(TAG, "onPlayServiceConnected")
        val binder = service as LocalBinder
        playService = binder.getService()
        // Test code here for ExoPlayService
        playService?.setPresenter(presenter)
        playService?.initMediaControllerCompat(presenter)
        playService?.initExoPlayer()
        // presenter.initExoPlayer()   // the original one
        Log.d(TAG, "onPlayServiceConnected.Video player view")
        // Video player view
        setVideoPlayerView()
        Log.d(TAG, "onPlayServiceConnected.presenter.playSongPlayedBeforeActivityCreated()")
        presenter.playSongPlayedBeforeActivityCreated()
        presenter.castPlayer?.let {
            Log.d(TAG, "onPlayServiceConnected.castPlayer != null && exoPlayer != null")
            playService?.currentPlayer =
                if (it.isCastSessionAvailable) presenter.castPlayer else playService?.exoPlayer
        }
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
            presenter.initCastPlayer()
            // presenter.initExoPlayer() commented out for testing
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated")
        super.onViewCreated(view, savedInstanceState)

        /* commented out for testing
        // Video player view
        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT)
        layoutParams.gravity = Gravity.CENTER
        activity?.let {
            playerView = StyledPlayerView(it.applicationContext)
            playerView.layoutParams = layoutParams
            playerView.setBackgroundColor(ContextCompat.getColor(it.applicationContext, android.R.color.black))
            playerViewLinearLayout?.addView(playerView)

            playerView.visibility = View.VISIBLE
            playerView.useArtwork = true
            playerView.useController = false

            // must be after super.onCreate(savedInstanceState)
            setExoPlayerAndCastPlayer()
        }
        */
        /*  commented out for testing
        Log.d(TAG, "presenter.playSongPlayedBeforeActivityCreated()")
        presenter.playSongPlayedBeforeActivityCreated()

        presenter.castPlayer?.let {
            Log.d(TAG, "castPlayer != null && exoPlayer != null")
            presenter.currentPlayer =
                if (it.isCastSessionAvailable) presenter.castPlayer else presenter.exoPlayer
        }
        */

        Log.d(TAG, "onViewCreated() is finished.")
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        presenter.setSessionAvailabilityListener()
        presenter.addBaseCastStateListener()
        super.onResume()
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        presenter.releaseSessionAvailabilityListener()
        presenter.removeBaseCastStateListener()
        super.onPause()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
        releaseExoPlayerAndCastPlayer()
        // presenter.releaseMediaCallback() commented out for testing
        super.onDestroy()
    }

    private fun setVideoPlayerView() {
        Log.d(TAG, "setVideoPlayerView")
        val layParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT)
        layParams.gravity = Gravity.CENTER
        activity?.let {
            playerView = StyledPlayerView(it.applicationContext)
            playerView?.apply {
                layoutParams = layParams
                setBackgroundColor(ContextCompat.getColor(it.applicationContext, android.R.color.black))
                playerViewLinearLayout?.addView(this)
                visibility = View.VISIBLE
                useArtwork = true
                useController = false
                // must be after super.onCreate(savedInstanceState)
                // player = presenter.exoPlayer
                player = playService?.exoPlayer
                requestFocus()
            }
        }
    }

    private fun releaseExoPlayerAndCastPlayer() {
        Log.d(TAG, "releaseExoPlayerAndCastPlayer")
        // presenter.releaseMediaSessionCompat()
        presenter.releaseExoPlayerAndCastPlayer()
        playerView?.player = null
    }

    // implementing methods of ExoPlayerPresenter.ExoPlayerPresentView
    override fun setCurrentPlayerToPlayerView() {
        Log.d(TAG, "setCurrentPlayerToPlayerView")
        val currentPlayer = presenter.currentPlayer ?: return
        if (currentPlayer === presenter.exoPlayer) {
            Log.d(TAG, "setCurrentPlayerToPlayerView.Current player is exoPlayer.")
        } else  /* currentPlayer == castPlayer */ {
            Log.d(TAG, "setCurrentPlayerToPlayerView.Current player is castPlayer.")
        }
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
            Log.d(TAG, "setMediaRouteButtonView.BuildConfig.DEBUG")
            val deviceAvailable = presenter.currentCastState != CastState.NO_DEVICES_AVAILABLE
            Log.d(TAG, "setMediaRouteButtonView.deviceAvailable = $deviceAvailable")
            setMediaRouteButtonVisible(deviceAvailable)
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

    override fun setMediaRouteButtonVisible(isVisible: Boolean) {
        if (!com.smile.karaokeplayer.BuildConfig.DEBUG) {
            return
        }
        mediaRouteButton?.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    override fun setMenuItemsVisibility() {
        // do nothing
    }

    override fun setSwitchToVocalImageButtonVisibility() {
        // do nothing
    }
    // end of implementing methods of super class
}