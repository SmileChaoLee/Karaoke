package exoplayer.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastState
import com.smile.karaokeplayer.R
import com.smile.karaokeplayer.fragments.PlayerBaseViewFragment
import exoplayer.presenters.ExoPlayerPresenter
import exoplayer.presenters.ExoPlayerPresenter.ExoPlayerPresentView

private const val TAG: String = "ExoPlayerFragment"

class ExoPlayerFragment : PlayerBaseViewFragment(), ExoPlayerPresentView {
    private lateinit var presenter: ExoPlayerPresenter
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var playerView: StyledPlayerView
    private var mediaRouteButton: MediaRouteButton? = null
    private var castPlayer: CastPlayer? = null

    /*
    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.d(TAG, "onServiceConnected()")
            if (service != null) {
                (service as LocalBinder)?.let {
                    val playService: ExoPlayService = it.getService()
                    presenter.setPlayService(playService)
                    playService?.initMediaControllerCompat(activity)
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.d(TAG, "onServiceDisconnected()")
        }
    }
    */

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        presenter = ExoPlayerPresenter(this, this)
        super.onCreate(savedInstanceState)  // must be after ExoPlayerPresenter(this, this)
        arguments?.let {
        }
        // must be after super.onCreate(savedInstanceState)
        val callingIntent: Intent? = activity?.intent
        Log.d(TAG, "onCreate.callingIntent = $callingIntent")
        mPresenter.initializeVariables(savedInstanceState, callingIntent)
        presenter.initCastPlayer()
        presenter.initExoPlayer()

        /*
        // Bind ExoPlayService
        Log.d(TAG, "onCreate.bind ExoPlayService")
        activity?.let {
            Intent(it, ExoPlayService::class.java)?.apply {
                it.bindService(this, connection, Context.BIND_AUTO_CREATE)
            }
        }
        */
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated() is called.")
        super.onViewCreated(view, savedInstanceState)

        // Video player view
        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
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

        presenter.playSongPlayedBeforeActivityCreated()

        // presenter.addBaseCastStateListener();   // moved to onResume() on 2021-03-26
        castPlayer?.let {
            Log.d(TAG, "castPlayer != null && exoPlayer != null")
            presenter.currentPlayer =
                if (it.isCastSessionAvailable) castPlayer else exoPlayer
        }

        Log.d(TAG, "onViewCreated() is finished.")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        presenter.onResume()
        presenter.setSessionAvailabilityListener()
        presenter.addBaseCastStateListener()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
        presenter.onPause();
        presenter.releaseSessionAvailabilityListener()
        presenter.removeBaseCastStateListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy()")
        presenter.onDestroy()
        playerView.player = null
    }

    private fun setExoPlayerAndCastPlayer() {
        // presenter.initExoPlayer() // must be before volumeSeekBar settings
        // presenter.initMediaSessionCompat()
        exoPlayer = presenter.exoPlayer
        castPlayer = presenter.castPlayer
        playerView.player = exoPlayer
        playerView.requestFocus()
    }

    // implementing methods of ExoPlayerPresenter.ExoPlayerPresentView
    override fun setCurrentPlayerToPlayerView() {
        val currentPlayer = presenter.currentPlayer ?: return
        if (currentPlayer === exoPlayer) {
            Log.d(TAG, "Current player is exoPlayer.")
        } else  /* currentPlayer == castPlayer */ {
            Log.d(TAG, "Current player is castPlayer.")
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