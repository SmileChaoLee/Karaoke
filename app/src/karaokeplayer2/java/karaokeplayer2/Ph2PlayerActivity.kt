package karaokeplayer2

import com.smile.karaoke.R
import com.smile.karaoke.utilities.LogUtil
import karaoketvplayer.PhPlayerActivity

open class Ph2PlayerActivity : PhPlayerActivity() {

    private var mTAG : String = "Ph2PlayerActivity"
    override fun setTag(tag: String) {
        LogUtil.d(mTAG, "setTag.tag = $tag")
        mTAG = tag
    }

    override fun getAppName(): String {
        return resources.getString(R.string.karaoke_app_name)
    }
}