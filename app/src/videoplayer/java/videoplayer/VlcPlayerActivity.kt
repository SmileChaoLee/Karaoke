package videoplayer

import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.smile.karaoke.UpBaseActivity
import com.smile.karaoke.utilities.LogUtil
import videoplayer.fragments.VlcPlayerFragment

@OptIn(UnstableApi::class)
open class VlcPlayerActivity : UpBaseActivity() {

    private var mTAG : String = "VlcPlayerActivity"
    fun setTag(tag: String) {
        LogUtil.d(mTAG, "setTag.tag = $tag")
        mTAG = tag
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.i(mTAG, "onCreate")
        super.onCreate(savedInstanceState)
    }

    override fun getFragment() = VlcPlayerFragment()
}