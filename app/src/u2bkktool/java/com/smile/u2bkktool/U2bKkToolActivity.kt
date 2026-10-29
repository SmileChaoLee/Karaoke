package com.smile.u2bkktool

import androidx.media3.common.util.UnstableApi
import com.smile.karaoke.utilities.LogUtil
import com.smile.u2bkaraoke.U2bKkBaseActivity
import com.smile.u2bkaraoke.fragments.SongListFragment
import com.smile.u2bkktool.fragments.ToolSongListFragment

@UnstableApi
class U2bKkToolActivity : U2bKkBaseActivity() {

    companion object {
        private const val TAG = "U2bKkToolActivity"
    }

    override fun getSongListFragment(): SongListFragment {
        return ToolSongListFragment()
    }

    override fun onStart() {
        LogUtil.d(TAG, "onStart")
        super.onStart()
        u2bPlayerFragment.apply {
            val ps = getPlayService()
            LogUtil.d(TAG, "onStart.getPlayService() = $ps")
            if (ps != null) {
                initYouTubePlayerView()
            }
        }
    }

    override fun onStop() {
        LogUtil.d(TAG, "onStop")
        super.onStop()
        u2bPlayerFragment.apply {
            val ps = getPlayService()
            LogUtil.d(TAG, "onStop.getPlayService() = $ps")
            if (ps != null) {
                releaseYouTubePlayer()
            }
        }
    }
}
