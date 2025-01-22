package videoplayer

import android.os.Bundle
import android.util.Log
import com.smile.karaokeplayer.BaseActivity
import videoplayer.fragments.VlcPlayerFragment

private const val TAG : String = "VlcPlayerActivity"

class VlcPlayerActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
    }

    override fun getFragment() = VlcPlayerFragment()
}