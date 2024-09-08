package exoplayer.services

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.google.android.exoplayer2.ExoPlayer
import com.smile.karaokeplayer.services.BasePlayService
import exoplayer.presenters.ExoPlayerPresenter

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

    private var presenter: ExoPlayerPresenter? = null
    private var exoPlayer: ExoPlayer? = null

    fun setPresenter(presenter: ExoPlayerPresenter) {
        Log.d(TAG, "setPresenter")
        this.presenter = presenter
        exoPlayer = this.presenter?.exoPlayer
    }

    override fun specificPlayerReplayMedia(currentAudioPosition: Long) {
        Log.d(TAG, "specificPlayerReplayMedia")
        presenter?.specificPlayerReplayMedia(currentAudioPosition)
    }

    override fun initMediaCallback() {
        Log.d(TAG, "initMediaCallback")
        presenter?.initMediaCallback(this)
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