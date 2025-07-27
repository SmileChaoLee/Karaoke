package karaoketvplayer

import android.util.Log
import com.smile.karaoke.PlayerActivity

class TvPhPlayerActivity: PlayerActivity() {
    private val mTAG : String = "TvPhPlayerActivity"
    init {
        Log.d(mTAG, "")
        setTag(mTAG)
    }
}
