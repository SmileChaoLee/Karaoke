package youtube.fragments

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.pierfrancescosoffritti.androidyoutubeplayer.core.customui.DefaultPlayerUiController
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.smile.karaoke.fragments.PlayerBaseFragment
import com.smile.karaoke.presenters.PlayerBasePresenter
import com.smile.karaoke.utilities.LogUtil
import youtube.presenters.YouTubePresenter
import youtube.services.YouTubeService

import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.io.infrastructure.ChromecastConnectionListener
// import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.loadOrCueVideo

@OptIn(UnstableApi::class)
class YouTubeFragment: PlayerBaseFragment(), YouTubePresenter.YouTubePresentView {

    companion object {
        private const val TAG = "YouTubeFragment"
    }

    private lateinit var presenter: YouTubePresenter
    private var playService: YouTubeService? = null
    private var youTubeView: YouTubePlayerView? = null
    // private var youTubeViewWidth = 200
    // private var youTubeViewHeight = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "onCreate")
        presenter = YouTubePresenter(this)
        // must be after YouTubePresenter(this)
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
        playService?.let { ps ->
            youTubeView?.removeFullscreenListener(ps.fullscreenListener)
            youTubeView?.removeYouTubePlayerListener(ps.playerListener)
            youTubeView?.release()
        }
    }

    private fun isEnableView(view: View, isEnable: Boolean) {
        view.let {
            it.isEnabled = isEnable
            if (it is ViewGroup) {
                for (i in 0 until it.childCount) {
                    isEnableView(it.getChildAt(i), isEnable)
                }
            }
        }
    }

    private fun hideYoutubeFeatures(yView: YouTubePlayerView?, player: YouTubePlayer?) {
        LogUtil.d(TAG, "hideYoutubeFeatures")
        if (yView == null || player == null) return
        LogUtil.d(TAG, "hideYoutubeFeatures. parameters are not null")
        val default = DefaultPlayerUiController(
            yView,
            player
        )
        // default.setVideoTitle("")
        // default.showVideoTitle(false)
        // default.showYouTubeButton(false)
        // default.showSeekBar(false)
        // default.showDuration(false)
        // default.showCurrentTime(false)
        // default.showMenuButton(false)
        // default.showFullscreenButton(false)
        // default.showPlayPauseButton(false)
        // default.showBufferingProgress(false)
        default.showUi(false)
        default.showBufferingProgress(true)
        val defaultUI = default.rootView
        // Set the now-correctly-modified UI
        yView.setCustomPlayerUi(defaultUI)
    }

    private fun initYouTubePlayerView() {
        val logStr = "initYouTubePlayerView"
        LogUtil.i(TAG, logStr)
        if (playService == null) null
        val ps = playService!!
        youTubeView = YouTubePlayerView(presenter.activity)
        youTubeView?.let {
            setVideoWindowSize()
            lifecycle.addObserver(it)
            it.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
                override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                    LogUtil.i(TAG, "$logStr.getYouTubePlayerWhenReady")
                    ps.mYouTubePlayer = youTubePlayer
                    hideYoutubeFeatures(it, youTubePlayer)
                }
            })
            it.addYouTubePlayerListener(ps.playerListener)
            it.addFullscreenListener(ps.fullscreenListener)
            playerViewLinearLayout?.let { viewIt ->
                val parent = viewIt.parent as ViewGroup
                LogUtil.d(TAG, "$logStr.parent = $parent")
                parent.removeView(viewIt)
                parent.removeView(toolbarAudioAdsLayout)
                // rearrange the view order in the FrameLayout
                parent.addView(it)
                parent.addView(viewIt)
                parent.addView(toolbarAudioAdsLayout)
            }
        }
    }

    private fun initializeCastPlayer() {
        // Create a ChromecastConnectionListener
        val connectionListener = object : ChromecastConnectionListener {
            override fun onChromecastConnecting() {
                // Optional: Show a "connecting" message to the user
            }

            override fun onChromecastConnected(chromecastYouTubePlayerContext: ChromecastYouTubePlayerContext) {
                // This is the key part! A connection has been established.
                initializeCastPlayer(chromecastYouTubePlayerContext)
            }

            override fun onChromecastDisconnected() {
                // Connection lost. Switch back to the local player.
                // playerContainer.removeAllViews()
                // playerContainer.addView(youTubePlayerView)
            }
        }

        // Initialize the ChromecastYouTubePlayerContext
        /*
        ChromecastYouTubePlayerContext(
            null,
            connectionListener
        )
        */
    }

    private fun initializeCastPlayer(chromecastYouTubePlayerContext: ChromecastYouTubePlayerContext) {
        // The context is ready. Now initialize the player with it.
        if (playService == null) return
        val ps = playService!!
        chromecastYouTubePlayerContext.initialize(ps.playerListener)
    }

    // implement YouTubePresenter.YouTubePresentView
    // end of implementing YouTubePresenter.YouTubePresentView

    override fun getPlayerPresenter(): PlayerBasePresenter? {
        LogUtil.i(TAG, "getPlayerPresenter")
        return presenter
    }

    override fun setupMenuItems() {
        LogUtil.i(TAG, "setupMenuItems")
    }

    override fun getPlayServiceIntent(): Intent {
        return Intent(activity, YouTubeService::class.java)
    }

    override fun onPlayServiceConnected(service: IBinder) {
        LogUtil.i(TAG, "onPlayServiceConnected")
        val binder = service as YouTubeService.LocalBinder
        playService = binder.getService()
        playService?.presenter = this.presenter
        playService?.initMediaControllerCompat(this.presenter)
        initYouTubePlayerView()
        LogUtil.d(TAG, "onPlayServiceConnected.Video player view")
        // Video player view
        // setVideoPlayerView()
        LogUtil.d(TAG, "onPlayServiceConnected.presenter.playSongPlayedBeforeActivityCreated()")
        presenter.playSongPlayedBeforeActivityCreated()
    }

    override fun audioChannelButtonListener() {
        LogUtil.i(TAG, "audioChannelButtonListener")
    }

    override fun setCurrentPlayerToPlayerView() {
        LogUtil.i(TAG, "setCurrentPlayerToPlayerView")
    }

    override fun getPlayService(): YouTubeService? {
        LogUtil.i(TAG, "getPlayService")
        return playService
    }

    // Implement YouTubePresenter.YouTubePresentView
    override fun setVideoWindowSize() {
        val logStr = "setVideoWindowSize"
        LogUtil.i(TAG, logStr)
        // youTubeViewWidth = screenSizeX
        // LogUtil.d(TAG, "$logStr.youTubeViewWidth = $youTubeViewWidth")
        // youTubeViewHeight = (screenSizeY * (1f - audioCtrlViewHighPercent())).toInt()
        // youTubeViewHeight = screenSizeY
        // LogUtil.d(TAG, "$logStr.youTubeViewHeight = $youTubeViewHeight")
        val nLayParams = FrameLayout.LayoutParams(screenSizeX, screenSizeY)
        nLayParams.gravity = Gravity.CENTER
        youTubeView?.layoutParams = nLayParams
    }

    override fun hideYoutubeFeatures() {
        hideYoutubeFeatures(youTubeView, playService?.mYouTubePlayer)
    }
}
