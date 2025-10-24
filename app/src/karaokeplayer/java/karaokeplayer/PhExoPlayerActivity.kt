package karaokeplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import com.smile.karaoke.BasePlayerActivity
import com.smile.karaoke.R
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.AppLinkUtil

open class PhExoPlayerActivity : BasePlayerActivity() {

    private var mTAG : String = "PhExoPlayerActivity"
    fun setTag(tag: String) {
        LogUtil.d(mTAG, "setTag.tag = $tag")
        mTAG = tag
    }

    override fun getAppName(): String {
        return resources.getString(R.string.karaoke_app_name)
    }

    override fun getExoButtonName(): String {
        return resources.getString(R.string.karaoke_app_name)
    }

    override fun getVlcButtonName(): String {
        return resources.getString(R.string.installVideoPlayer)
    }

    override fun startExoPlayer() {
        LogUtil.i(mTAG, "startExoPlayer()")
        Intent(
            this@PhExoPlayerActivity,
            ExoPlayerActivity::class.java
        ).also {
            loadingMessage.value = getString(R.string.loadingStr)
            exoLauncher.launch(it)
        }
    }

    override fun startVlcPlayer() {
        LogUtil.i(mTAG, "startVlcPlayer()")
        AppLinkUtil.startAppLinkOnStore(this@PhExoPlayerActivity,
            AppLinkUtil.VIDEO_LINK)
    }

    @SuppressLint("ConfigurationScreenWidthHeight", "SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.d(mTAG,"onCreate")
        super.onCreate(savedInstanceState)
    }
}