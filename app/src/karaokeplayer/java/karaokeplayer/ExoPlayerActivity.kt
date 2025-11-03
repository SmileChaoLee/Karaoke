package karaokeplayer

import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.smile.karaoke.BaseActivity
import com.smile.karaoke.utilities.LogUtil
import karaokeplayer.fragments.ExoPlayerFragment

@OptIn(UnstableApi::class)
open class ExoPlayerActivity : BaseActivity() {

    private var mTAG : String = "ExoPlayerActivity"
    fun setTag(tag: String) {
        LogUtil.d(mTAG, "setTag.tag = $tag")
        mTAG = tag
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.d(mTAG, "onCreate")
        super.onCreate(savedInstanceState)
    }

    @OptIn(UnstableApi::class)
    override fun getFragment() = ExoPlayerFragment()
}