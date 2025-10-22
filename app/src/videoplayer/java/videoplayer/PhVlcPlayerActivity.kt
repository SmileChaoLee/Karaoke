package videoplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import com.smile.karaoke.BasePlayerActivity
import com.smile.karaoke.R
import com.smile.karaoke.utilities.LogUtil

open class PhVlcPlayerActivity : BasePlayerActivity() {

    private var mTAG : String = "PhVlcPlayerActivity"
    fun setTag(tag: String) {
        LogUtil.d(mTAG, "setTag.tag = $tag")
        mTAG = tag
    }

    override fun getAppName(): String {
        return resources.getString(R.string.video_app_name)
    }

    override fun startExoPlayer() {
        LogUtil.i(mTAG, "startExoPlayer()")
    }

    override fun startVlcPlayer() {
        LogUtil.i(mTAG, "startVlcPlayer()")
        Intent(
            this@PhVlcPlayerActivity,
            VlcPlayerActivity::class.java
        ).also {
            loadingMessage.value = getString(R.string.loadingStr)
            exoLauncher.launch(it)
        }
    }

    @SuppressLint("ConfigurationScreenWidthHeight", "SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.d(mTAG,"onCreate")
        super.onCreate(savedInstanceState)
    }
}