package videoplayer2

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.smile.karaoke.utilities.LogUtil

// For Amazon Fire TV
@OptIn(UnstableApi::class)
class FTvVlcPlayerActivity: FPhVlcPlayerActivity() {
    private val mTAG : String = "FTvVlcPlayerActivity"

    init {
        LogUtil.d(mTAG, "init")
        setTag(mTAG)
    }
}