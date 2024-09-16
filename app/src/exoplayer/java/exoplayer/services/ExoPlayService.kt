package exoplayer.services

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.smile.karaokeplayer.services.BasePlayService

class ExoPlayService : BasePlayService() {

    companion object {
        private const val TAG = "ExoPlayService"
    }

    // Binder given to clients.
    private val binder = LocalBinder()
    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods.
        fun getService(): ExoPlayService = this@ExoPlayService
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "onBind.binder= $binder")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind().intent = $intent")
        return super.onUnbind(intent)
    }
}