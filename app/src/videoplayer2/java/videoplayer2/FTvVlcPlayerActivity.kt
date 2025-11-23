package videoplayer2

import videoplayer.VlcPlayerActivity
import com.smile.karaoke.utilities.LogUtil

// For Amazon Fire TV
class FTvVlcPlayerActivity: VlcPlayerActivity() {
    private val mTAG : String = "FTvVlcPlayerActivity"

    init {
        LogUtil.d(mTAG, "")
        setTag(mTAG)
    }
}