package karaoketvplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import com.smile.karaoke.BasePlayerActivity
import com.smile.karaoke.R
import com.smile.karaoke.utilities.LogUtil
import karaokeplayer.ExoPlayerActivity
import videoplayer.VlcPlayerActivity

open class PhPlayerActivity : BasePlayerActivity() {

    private var mTAG : String = "PhPlayerActivity"
    open fun setTag(tag: String) {
        LogUtil.d(mTAG, "setTag.tag = $tag")
        mTAG = tag
    }

    override fun getAppName(): String {
        return resources.getString(R.string.karaoke_tv_app_name)
    }

    override fun getExoButtonName(): String {
        return resources.getString(R.string.exoPlayerName)
    }

    override fun getVlcButtonName(): String {
        return resources.getString(R.string.vlcPlayerName)
    }

    override fun startExoPlayer() {
        LogUtil.i(mTAG, "startExoPlayer()")
        Intent(
            this@PhPlayerActivity,
            ExoPlayerActivity::class.java
        ).also {
            loadingMessage.value = getString(R.string.loadingStr)
            exoLauncher.launch(it)
        }
    }

    override fun startVlcPlayer() {
        LogUtil.i(mTAG, "startVlcPlayer()")
        Intent(
            this@PhPlayerActivity,
            VlcPlayerActivity::class.java
        ).also {
            loadingMessage.value = getString(R.string.loadingStr)
            vlcLauncher.launch(it)
        }
    }

    @SuppressLint("ConfigurationScreenWidthHeight", "SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.d(mTAG,"onCreate")
        super.onCreate(savedInstanceState)
    }
}