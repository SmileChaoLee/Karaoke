package com.smile.karaoke.chromecast

import android.content.Context
import android.util.Log
import androidx.media3.common.util.UnstableApi
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManager
import com.smile.karaoke.fragments.PlayerBaseFragment
import com.smile.karaoke.presenters.PlayerBasePresenter
import com.smile.karaoke.services.BasePlayService

@UnstableApi
class SetupChromeCast(private val playService: BasePlayService) {
    private var castContext: CastContext? = null
    private var castStateListener: MyCastStateListener? = null
    private var sessionManager: SessionManager? = null
    private var sessionManagerListener: MySManagerListener? = null

    init {
        castContext = playService.castContext
    }

    private fun setup(fragment: PlayerBaseFragment, presenter: PlayerBasePresenter) {
        Log.d(TAG, "setup")
        castContext?.let { castIt ->
            castStateListener = MyCastStateListener()
            castIt.addCastStateListener(castStateListener!!)
            Log.d(TAG, "setup.castState = ${castIt.castState}")
            sessionManager = castIt.sessionManager
            sessionManagerListener = MySManagerListener(
                fragment, presenter
            )
            sessionManager?.addSessionManagerListener(
                sessionManagerListener!!,
                CastSession::class.java)
        }
    }

    fun release() {
        Log.d(TAG, "release")
        castContext?.let { castIt ->
            castStateListener?.let { listener ->
                castIt.removeCastStateListener(listener)
            }
            sessionManager?.let { manager ->
                sessionManagerListener?.let { listener ->
                    manager.removeSessionManagerListener(listener,
                        CastSession::class.java)
                }
            }
        }
        castStateListener = null
        sessionManager = null
        sessionManagerListener = null
    }

    companion object {
        private const val TAG = "SetupChromeCast"
    }
}