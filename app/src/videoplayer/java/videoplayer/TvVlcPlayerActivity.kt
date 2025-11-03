package videoplayer

import com.smile.karaoke.utilities.LogUtil

class TvVlcPlayerActivity: PhVlcPlayerActivity() {
    private val mTAG : String = "TvVlcPlayActivity"
    init {
        LogUtil.d(mTAG, "")
        setTag(mTAG)
    }
}