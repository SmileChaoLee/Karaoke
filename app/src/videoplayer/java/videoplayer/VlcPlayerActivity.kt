package videoplayer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.smile.karaokeplayer.BaseActivity
import com.smile.karaokeplayer.BaseFavoriteListActivity
import videoplayer.fragments.VlcPlayerFragment

private const val TAG : String = "VlcPlayerActivity"

class VlcPlayerActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate() is called")
        super.onCreate(savedInstanceState)
    }

    override fun getFragment() = VlcPlayerFragment()
    override fun comeBackFromFavorite(playData : Bundle?) {
        onReceiveFunc(isSingleSong = false, needPlay = false, intent = null, pData = playData)
    }
}