package com.smile.karaoke.chromecast

import android.net.Uri
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.util.UnstableApi
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.smile.karaoke.fragments.PlayerBaseFragment
import com.smile.karaoke.presenters.PlayerBasePresenter
import com.smile.karaoke.utilities.LogUtil

@UnstableApi
class MySManagerListener : SessionManagerListener<CastSession> {

    companion object {
        private const val TAG = "MySManagerListener"
    }

    private val mFragment: PlayerBaseFragment
    private val mPresenter: PlayerBasePresenter
    private var mContext: FragmentActivity?
    private val webServerAndCast = WebServerAndCast()

    constructor(fragment: PlayerBaseFragment,
                presenter: PlayerBasePresenter) {
        this.mFragment = fragment
        this.mPresenter = presenter
        this.mContext = fragment.activity
    }

    override fun onSessionStarting(p0: CastSession) {
        val msgString = "onSessionStarting"
        LogUtil.d(TAG, msgString)
        // Not yet connected
        Toast.makeText(mContext, msgString, Toast.LENGTH_SHORT).show()
    }

    override fun onSessionStarted(p0: CastSession, p1: String) {
        val msgString = "onSessionStarted"
        LogUtil.d(TAG, msgString)
        Toast.makeText(mContext, msgString, Toast.LENGTH_SHORT).show()
        // Session is active, you can now load media
        mContext?.invalidateOptionsMenu() // To update Cast button state
        // pause the stream on Android device side
        mPresenter.pausePlay()
        //
        val mediaUri: Uri? = mPresenter.mediaUri
        LogUtil.d(TAG,"$msgString.mediaUri = $mediaUri")
        if (mediaUri == null) return
        val filePath = mediaUri.path
        LogUtil.d(TAG,"$msgString.filePath = $filePath")
        if (filePath != null) {
            webServerAndCast.startWebServerAndCast(p0, filePath)
        }
    }

    override fun onSessionStartFailed(p0: CastSession, p1: Int) {
        val msgString = "onSessionStartFailed"
        LogUtil.d(TAG, msgString)
        // Handle start failure
        Toast.makeText(mContext, msgString, Toast.LENGTH_SHORT).show()
    }

    override fun onSessionEnding(p0: CastSession) {
        val msgString = "onSessionEnding"
        LogUtil.d(TAG, msgString)
        // Session is about to end
        Toast.makeText(mContext, msgString, Toast.LENGTH_SHORT).show()
    }

    override fun onSessionEnded(p0: CastSession, p1: Int) {
        val msgString = "onSessionEnded"
        LogUtil.d(TAG, msgString)
        // Session has ended
        Toast.makeText(mContext, msgString, Toast.LENGTH_SHORT).show()
        mContext?.invalidateOptionsMenu() // To update Cast button state
        // recover playing on Android device side
        webServerAndCast.stopWebServer()
        mPresenter.startPlay()
        //
    }

    override fun onSessionResuming(p0: CastSession, p1: String) {
        val msgString = "onSessionResuming"
        LogUtil.d(TAG, msgString)
        // Resuming a previous session
        Toast.makeText(mContext, msgString, Toast.LENGTH_SHORT).show()
    }

    override fun onSessionResumed(p0: CastSession, p1: Boolean) {
        val msgString = "onSessionResumed"
        LogUtil.d(TAG, msgString)
        // Session has resumed
        Toast.makeText(mContext, msgString, Toast.LENGTH_SHORT).show()
        mContext?.invalidateOptionsMenu()
    }

    override fun onSessionResumeFailed(p0: CastSession, p1: Int) {
        val msgString = "onSessionResumeFailed"
        LogUtil.d(TAG, msgString)
        // Handle resume failure
        Toast.makeText(mContext, msgString, Toast.LENGTH_SHORT).show()
    }

    override fun onSessionSuspended(p0: CastSession, p1: Int) {
        val msgString = "onSessionSuspended"
        LogUtil.d(TAG, msgString)
        // Session is temporarily suspended (e.g., another app started casting)
        Toast.makeText(mContext, msgString, Toast.LENGTH_SHORT).show()
    }
}