package videoplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import com.smile.karaoke.BasePlayerActivity
import com.smile.karaoke.R
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.AppLinkUtil

open class PhVlcPlayerActivity : BasePlayerActivity() {

    private var mTAG : String = "PhVlcPlayerActivity"
    fun setTag(tag: String) {
        LogUtil.d(mTAG, "setTag.tag = $tag")
        mTAG = tag
    }

    override fun getAppName(): String {
        return resources.getString(R.string.video_app_name)
    }

    override fun getExoButtonName(): String {
        return resources.getString(R.string.installKaraokePlayer)
    }

    override fun getVlcButtonName(): String {
        return resources.getString(R.string.video_app_name)
    }

    override fun startExoPlayer() {
        LogUtil.i(mTAG, "startExoPlayer()")
        AppLinkUtil.startAppLinkOnStore(this@PhVlcPlayerActivity,
            AppLinkUtil.KARAOKE_LINK)
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