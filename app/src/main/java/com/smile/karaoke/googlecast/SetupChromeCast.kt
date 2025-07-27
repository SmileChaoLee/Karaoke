package com.smile.karaoke.googlecast

import android.util.Log
import androidx.media3.common.util.UnstableApi
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManager
import com.smile.karaoke.services.BasePlayService

@UnstableApi
class SetupChromeCast(
    private val playService: BasePlayService,
    private val castContext: CastContext?
) {
    private var castStateListener: MyCastStateListener? = null
    private var sessionManager: SessionManager? = null
    private var sessionManagerListener: MySManagerListener? = null

    init {
        Log.d(TAG, "SetupChromeCast.init.castContext = $castContext")
        setup()
    }

    private fun setup() {
        Log.d(TAG, "setup")
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