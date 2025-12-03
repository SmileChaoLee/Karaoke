package youtube.listeners

import android.view.View
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener
import com.smile.karaoke.utilities.LogUtil
import youtube.services.YouTubeService

@OptIn(UnstableApi::class)
class FScreenListener(private val playService: YouTubeService): FullscreenListener {

    companion object {
        private const val TAG = "FScreenListener"
    }

    override fun onEnterFullscreen(
        fullscreenView: View,
        exitFullscreen: () -> Unit) {
        LogUtil.d(TAG, "FullscreenListener.onEnterFullscreen")
    }

    override fun onExitFullscreen() {
        LogUtil.d(TAG, "FullscreenListener.onExitFullscreen")
    }
}