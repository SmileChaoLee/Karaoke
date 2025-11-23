package videoplayer2

import com.smile.karaoke.utilities.LogUtil
import videoplayer.VlcPlayerActivity

// For Amazon Fire Tablet or Android phone
class FPhVlcPlayerActivity : VlcPlayerActivity() {

    private var mTAG : String = "FPhVlcPlayerActivity"

    init {
        LogUtil.d(mTAG, "")
        setTag(mTAG)
    }
}