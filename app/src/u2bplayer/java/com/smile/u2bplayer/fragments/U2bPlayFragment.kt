package com.smile.u2bplayer.fragments

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
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.smile.karaoke.fragments.PlayerBaseFragment
import com.smile.karaoke.presenters.PlayerBasePresenter
import com.smile.karaoke.utilities.LogUtil
import com.smile.u2bplayer.presenters.U2bPresenter
import com.smile.u2bplayer.services.U2bService
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.io.infrastructure.ChromecastConnectionListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.smile.u2bplayer.U2bUtil
import com.smile.u2bplayer.listeners.FScreenListener
import com.smile.u2bplayer.listeners.U2bCastPlayerListener
import com.smile.u2bplayer.listeners.U2bPlayerListener

@OptIn(UnstableApi::class)
class U2bPlayFragment: PlayerBaseFragment(), U2bPresenter.U2bPresentView {

    companion object {
        private const val TAG = "U2bPlayFragment"
    }

    private lateinit var presenter: U2bPresenter
    private var playService: U2bService? = null
    private var youTubeView: YouTubePlayerView? = null
    // private var youTubeViewWidth = 200
    // private var youTubeViewHeight = 200
    private var fScreenListener: FScreenListener? = null
    private var u2bPlayerListener: U2bPlayerListener? = null
    private var chromecastContext: ChromecastYouTubePlayerContext? = null
    private var castPlayerListener: U2bPlayerListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "onCreate")
        presenter = U2bPresenter(this)
        // must be after YouTubePresenter(this)
        super.onCreate(savedInstanceState)
        castContext = null  // disable cast for VLC player for now
        LogUtil.i(TAG, "onCreate.finished")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "onViewCreated")
        super.onViewCreated(view, savedInstanceState)
        LogUtil.i(TAG, "onViewCreated.finished")
    }

    override fun onResume() {
        LogUtil.i(TAG, "onResume")
        super.onResume()
    }

    override fun onPause() {
        LogUtil.i(TAG, "onPause")
        super.onPause()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        LogUtil.i(TAG, "onConfigurationChanged")
        super.onConfigurationChanged(newConfig)
        setVideoWindowSize()
    }

    override fun onDestroy() {
        LogUtil.i(TAG, "onDestroy")
        if (mPlayServiceIntent != null) {
            activity?.stopService(mPlayServiceIntent)
        }
        youTubeView?.apply {
            fScreenListener?.let { fsListener ->
                removeFullscreenListener(fsListener)
            }
            u2bPlayerListener?.let { playerListener ->
                removeYouTubePlayerListener(playerListener)
            }
            release()
        }
        super.onDestroy()
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

    private fun initYouTubePlayerView() {
        val logStr = "initYouTubePlayerView"
        LogUtil.i(TAG, logStr)
        val act = activity ?: return
        val ps = playService ?: return
        u2bPlayerListener = ps.initU2bPlayerListener()
        fScreenListener = ps.initFScreenListener()
        val options = IFramePlayerOptions.Builder(act)
            .controls(1)
            .ccLoadPolicy(0) // 1 enables captions by default, 0 disables
            .rel(1)
            // .langPref("en")
            .build()
        youTubeView = ps.initYouTubePlayerView(true)
        youTubeView?.let {
            it.initialize(u2bPlayerListener!!, options)
            it.addFullscreenListener(fScreenListener!!)
            setVideoWindowSize()
            lifecycle.addObserver(it)
            playerViewLinearLayout?.let { viewIt ->
                val parent = viewIt.parent as ViewGroup
                LogUtil.d(TAG, "$logStr.parent = $parent")
                parent.removeView(viewIt)
                parent.removeView(adsMsgLayout)
                parent.removeView(toolbarAudioLayout)
                // rearrange the view order in the FrameLayout
                // this order does not have banner ad, must be fixed
                parent.addView(it)
                parent.addView(viewIt)
                parent.addView(toolbarAudioLayout)
                parent.addView(adsMsgLayout)
            }
        }
    }

    private fun initChromecastContext() {
        LogUtil.i(TAG, "initChromecastContext")
        castContext?.let {
            chromecastContext = ChromecastYouTubePlayerContext(it.sessionManager)
            chromecastContext?.addChromecastConnectionListener(
                CastConnectListener()
            )
        }
    }

    private fun initU2bCastPlayer() {
        LogUtil.i(TAG, "initU2bCastPlayer")
        // The context is ready. Now initialize the player with it.
        val ps = playService ?: return
        castPlayerListener = U2bCastPlayerListener(ps)
        chromecastContext?.initialize(castPlayerListener!!)
    }

    // overriding methods of super class
    override fun getPlayerPresenter(): PlayerBasePresenter? {
        LogUtil.i(TAG, "getPlayerPresenter")
        return presenter
    }

    override fun setupMenuItems() {
        channelMenuItem?.isVisible = false
        channelMenuItem?.isEnabled = false
    }

    override fun getPlayServiceIntent(): Intent {
        return Intent(activity, U2bService::class.java)
    }

    override fun onPlayServiceConnected(service: IBinder) {
        LogUtil.i(TAG, "onPlayServiceConnected")
        val binder = service as U2bService.LocalBinder
        playService = binder.getService()
        playService?.presenter = this.presenter
        playService?.initMediaControllerCompat(this.presenter)
        initYouTubePlayerView()
        initChromecastContext()
        LogUtil.d(TAG, "onPlayServiceConnected.Video player view")
        // Video player view
        // setVideoPlayerView()
        LogUtil.d(TAG, "onPlayServiceConnected.presenter.playSongPlayedBeforeActivityCreated()")
        presenter.playSongPlayedBeforeActivityCreated()
    }

    override fun audioChannelButtonListener() {
        LogUtil.i(TAG, "audioChannelButtonListener")
    }

    override fun getFavDatabaseName(): String {
        return U2bUtil.getFavDatabaseName()
    }

    override fun switchToMusicVisibility(): Int {
        return View.GONE
    }

    override fun switchToVocalVisibility(): Int {
        return View.GONE
    }

    override fun audioChannelVisibility(): Int {
        return View.GONE
    }
    // end of overriding methods of super class

    // Implement U2bPresenter.YouTubePresentView
    override fun setCurrentPlayerToPlayerView() {
        LogUtil.i(TAG, "setCurrentPlayerToPlayerView")
    }

    override fun getPlayService(): U2bService? {
        LogUtil.i(TAG, "getPlayService")
        return playService
    }
    // End of implementing YouTubePresenter.YouTubePresentView

    private fun setVideoWindowSize() {
        val logStr = "setVideoWindowSize"
        LogUtil.i(TAG, logStr)
        val nLayParams = FrameLayout.LayoutParams(screenSizeX, screenSizeY)
        nLayParams.gravity = Gravity.CENTER
        youTubeView?.layoutParams = nLayParams
    }

    private inner class CastConnectListener: ChromecastConnectionListener {
        val logStr = "CastConnectListener"
        override fun onChromecastConnecting() {
            // Optional: Show a "connecting" message to the user
            LogUtil.i(TAG, "$logStr.onChromecastConnecting")
        }

        override fun onChromecastConnected(
            chromecastYouTubePlayerContext: ChromecastYouTubePlayerContext) {
            LogUtil.i(TAG, "$logStr.onChromecastConnected")
            // This is the key part! A connection has been established.
            initU2bCastPlayer()
        }

        override fun onChromecastDisconnected() {
            LogUtil.i(TAG, "$logStr.onChromecastDisconnected")
            // Connection lost. Switch back to the local player.
            // playerContainer.removeAllViews()
            // playerContainer.addView(youTubePlayerView)
        }
    }
}
