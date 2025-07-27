package com.smile.karaoke.googlecast

import android.util.Log
import androidx.media3.common.util.UnstableApi
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.smile.karaoke.fragments.PlayerBaseFragment
import com.smile.karaoke.presenters.PlayerBasePresenter

@UnstableApi
class SetupGoogleCast(
    private val fragment: PlayerBaseFragment,
    private val presenter: PlayerBasePresenter,
    private val castContext: CastContext?
) {
    private var castStateListener: MyCastStateListener? = null
    private var sessionManager: com.google.android.gms.cast.framework.SessionManager? = null
    private var sessionManagerListener: MySManagerListener? = null

    init {
        Log.d(TAG, "SetupGoogleCast.init.castContext = $castContext")
        setup()
    }

    private fun setup() {
        fragment.activity?.let { actIt ->
            Log.d(TAG, "setup.castContext = $castContext")
            castContext?.also { castIt ->
                castStateListener = MyCastStateListener()
                castIt.addCastStateListener(castStateListener!!)
                Log.d(TAG, "setup.castContext.castState = ${castIt.castState}")
                sessionManager = castIt.sessionManager
                sessionManagerListener = MySManagerListener(
                    fragment, presenter)
                sessionManager?.addSessionManagerListener(
                    sessionManagerListener!!,
                    CastSession::class.java)
            }
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
        private const val TAG = "SetupGoogleCast"
    }
}