package karaoketvplayer

import android.util.Log
import com.smile.karaoke.PlayerActivity

class PhPlayerActivity: PlayerActivity() {
    private val mTAG : String = "PhPlayerActivity"
    init {
        Log.d(mTAG, "")
        setTag(mTAG)
    }
}
