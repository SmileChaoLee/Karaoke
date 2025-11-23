package videoplayer2

import androidx.media3.common.util.UnstableApi
import com.smile.karaoke.utilities.LogUtil
import videoplayer.VlcPlayerActivity

// For Amazon Fire Tablet or Android phone
@UnstableApi
open class FPhVlcPlayerActivity : VlcPlayerActivity() {

    private var mTAG : String = "FPhVlcPlayerActivity"

    init {
        LogUtil.d(mTAG, "init")
        setTag(mTAG)
    }

    override fun needInterstitialAd(): Boolean {
        return false
    }
}