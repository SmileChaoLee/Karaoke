package com.smile.karaoke.chromecast

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.util.UnstableApi
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.smile.karaoke.SmileAppBase
import com.smile.karaoke.fragments.PlayerBaseFragment
import com.smile.karaoke.presenters.PlayerBasePresenter
import com.smile.smilelibraries.utilities.ScreenUtil

@UnstableApi
class MySManagerListener : SessionManagerListener<CastSession> {

    private val mFragment: PlayerBaseFragment
    private val mPresenter: PlayerBasePresenter
    private var mContext: FragmentActivity?
    private val toastTextSize = SmileAppBase.Companion.toastTextSize
    private val webServerAndCast = WebServerAndCast()

    constructor(fragment: PlayerBaseFragment, presenter: PlayerBasePresenter) {
        this.mFragment = fragment
        this.mPresenter = presenter
        this.mContext = fragment.activity
    }

    override fun onSessionStarting(p0: CastSession) {
        val msgString = "onSessionStarting"
        Log.d(TAG, msgString)
        // Not yet connected
        ScreenUtil.showToast(
            mContext, msgString,
            toastTextSize,
            ScreenUtil.FontSize_Pixel_Type,
            Toast.LENGTH_SHORT
        )
    }

    override fun onSessionStarted(p0: CastSession, p1: String) {
        val msgString = "onSessionStarted"
        Log.d(TAG, msgString)
        ScreenUtil.showToast(
            mContext, msgString,
            toastTextSize,
            ScreenUtil.FontSize_Pixel_Type,
            Toast.LENGTH_SHORT
        )
        // Session is active, you can now load media
        mContext?.invalidateOptionsMenu() // To update Cast button state
        // pause the stream on Android device side
        mPresenter.pausePlay()
        //
        val mediaUri: Uri? = mPresenter.mediaUri
        Log.d(TAG,"$msgString.mediaUri = $mediaUri")
        if (mediaUri == null) return
        val filePath = mediaUri.path
        Log.d(TAG,"$msgString.filePath = $filePath")
        if (filePath != null) {
            webServerAndCast.startWebServerAndCast(p0, filePath)
        }
    }

    override fun onSessionStartFailed(p0: CastSession, p1: Int) {
        val msgString = "onSessionStartFailed"
        Log.d(TAG, msgString)
        // Handle start failure
        ScreenUtil.showToast(
            mContext, msgString,
            toastTextSize,
            ScreenUtil.FontSize_Pixel_Type,
            Toast.LENGTH_SHORT
        )
    }

    override fun onSessionEnding(p0: CastSession) {
        val msgString = "onSessionEnding"
        Log.d(TAG, msgString)
        // Session is about to end
        ScreenUtil.showToast(
            mContext, msgString,
            toastTextSize,
            ScreenUtil.FontSize_Pixel_Type,
            Toast.LENGTH_SHORT
        )
    }

    override fun onSessionEnded(p0: CastSession, p1: Int) {
        val msgString = "onSessionEnded"
        Log.d(TAG, msgString)
        ScreenUtil.showToast(
            mContext, msgString,
            toastTextSize,
            ScreenUtil.FontSize_Pixel_Type,
            Toast.LENGTH_SHORT
        )
        // Session has ended
        mContext?.invalidateOptionsMenu() // To update Cast button state
        // recover playing on Android device side
        webServerAndCast.stopWebServer()
        mPresenter.startPlay()
        //
    }

    override fun onSessionResuming(p0: CastSession, p1: String) {
        val msgString = "onSessionResuming"
        Log.d(TAG, msgString)
        // Resuming a previous session
        ScreenUtil.showToast(
            mContext, msgString,
            toastTextSize,
            ScreenUtil.FontSize_Pixel_Type,
            Toast.LENGTH_SHORT
        )
    }

    override fun onSessionResumed(p0: CastSession, p1: Boolean) {
        val msgString = "onSessionResumed"
        Log.d(TAG, msgString)
        ScreenUtil.showToast(
            mContext, msgString,
            toastTextSize,
            ScreenUtil.FontSize_Pixel_Type,
            Toast.LENGTH_SHORT
        )
        // Session has resumed
        mContext?.invalidateOptionsMenu()
    }

    override fun onSessionResumeFailed(p0: CastSession, p1: Int) {
        val msgString = "onSessionResumeFailed"
        Log.d(TAG, msgString)
        // Handle resume failure
        ScreenUtil.showToast(
            mContext, msgString,
            toastTextSize,
            ScreenUtil.FontSize_Pixel_Type,
            Toast.LENGTH_SHORT
        )
    }

    override fun onSessionSuspended(p0: CastSession, p1: Int) {
        val msgString = "onSessionSuspended"
        Log.d(TAG, msgString)
        // Session is temporarily suspended (e.g., another app started casting)
        ScreenUtil.showToast(
            mContext, msgString,
            toastTextSize,
            ScreenUtil.FontSize_Pixel_Type,
            Toast.LENGTH_SHORT
        )
    }

    companion object {
        private const val TAG = "MySManagerListener"
    }
}