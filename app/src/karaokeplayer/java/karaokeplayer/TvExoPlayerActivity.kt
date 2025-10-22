package karaokeplayer

import com.smile.karaoke.utilities.LogUtil

class TvExoPlayerActivity: ExoPlayerActivity() {
    private val mTAG : String = "TvExoPlayActivity"
    init {
        LogUtil.d(mTAG, "")
        setTag(mTAG)
    }
}