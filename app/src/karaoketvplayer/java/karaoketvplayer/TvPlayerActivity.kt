package karaoketvplayer

import android.os.Bundle
import android.util.Log
import com.smile.karaoke.PlayerActivity

class TvPlayerActivity: PlayerActivity() {
    private val mTAG : String = "TvPlayerActivity"
    init {
        Log.d(mTAG, "")
        setTag(mTAG)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(mTAG, "onCreate.Started")
    }
}
