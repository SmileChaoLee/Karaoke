package karaoketvplayer

import android.os.Bundle
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.smile.karaoke.BaseActivity
import com.smile.karaoke.utilities.LogUtil
import karaoketvplayer.fragments.YouTubeFragment


@OptIn(UnstableApi::class)
class YouTubeActivity : BaseActivity() {

    companion object {
        private const val TAG : String = "YouTubeActivity"
    }

    override fun getFragment(): YouTubeFragment {
        LogUtil.d(TAG, "getFragment")
        return YouTubeFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        LogUtil.d(TAG, "onCreate.finished")
    }
}