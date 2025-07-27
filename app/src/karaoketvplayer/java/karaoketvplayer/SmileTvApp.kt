package karaoketvplayer

import android.util.Log
import com.smile.karaoke.SmileAppBase

class SmileTvApp : SmileAppBase() {

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        initAds()
        initCastContext()
    }

    override fun initAds() {
        // No ads in this version
        Log.d(TAG, "initAds.do nothing")
    }

    override fun initCastContext() {
        Log.d(TAG, "initCastContext.do nothing")
        castContext = null
    }

    companion object {
        private const val TAG = "SmileTvApp"
    }
}
