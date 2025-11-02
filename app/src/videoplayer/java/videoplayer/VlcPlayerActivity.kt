package videoplayer

import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.smile.karaoke.BaseActivity
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.AppLinkUtil
import videoplayer.fragments.VlcPlayerFragment

@OptIn(UnstableApi::class)
open class VlcPlayerActivity : BaseActivity() {

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