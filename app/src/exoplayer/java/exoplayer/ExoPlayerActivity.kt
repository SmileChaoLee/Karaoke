package exoplayer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.dynamite.DynamiteModule.LoadingException
import com.smile.karaokeplayer.BaseActivity
import exoplayer.fragments.ExoPlayerFragment

private const val TAG : String = "ExoPlayerActivity"

class ExoPlayerActivity : BaseActivity() {

    var castContext: CastContext? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        if (com.smile.karaokeplayer.BuildConfig.DEBUG) {
            Log.d(TAG, "com.smile.karaokeplayer.BuildConfig.DEBUG")
            try {
                castContext = CastContext.getSharedInstance(this)
                Log.d(TAG, "castContext = $castContext")
            } catch (e: RuntimeException) {
                castContext = null
                var cause = e.cause
                while (cause != null) {
                    if (cause is LoadingException) {
                        Log.d(TAG,"onCreate.Failed to get CastContext." +
                                "Try updating Google Play Services and restart the app.")
                    }
                    cause = cause.cause
                }
                // Unknown error. We propagate it.
                Log.d(TAG, "onCreate.Failed to get CastContext. Unknown error.")
            }
        }
    }

    override fun getFragment() = ExoPlayerFragment()
    override fun comeBackFromFavorite(playData : Bundle?) {
        onReceiveFunc(isSingleSong = false, needPlay = true, intent = null, pData = playData)
    }

    // implementing interface PlayMyFavorites
    override fun intentForFavoriteListActivity(): Intent {
        return Intent(this, FavoriteListActivity::class.java)
    }
    // Finishes implementing interface PlayMyFavorites

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
    }
}