package com.smile.u2bkktool

import android.os.Bundle
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import com.smile.karaoke.R
import com.smile.karaoke.utilities.LogUtil
import com.smile.u2bkaraoke.fragments.SongListFragment
import com.smile.u2bkaraoke.u2bkaok_constants.U2bKKConstants
import com.smile.u2bkaraoke.utilities.U2bKaOkUtil
import com.smile.u2bkktool.fragments.SongToolFragment

@OptIn(UnstableApi::class)
open class U2bKkToolActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "U2bKkToolActivity"
    }

    private var fmContainerId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogUtil.d(TAG, "onCreate")
        setContentView(R.layout.activity_u2bkk_tool)

        val toolFragment = SongToolFragment().apply {
            arguments = Bundle().apply {
                putInt(U2bKKConstants.OrderedFrom, U2bKKConstants.ALL_SONG_ORDERED)
            }
        }
        fmContainerId = R.id.u2bKkToolLayout
        U2bKaOkUtil.beginTransaction(supportFragmentManager,
            fmContainerId, toolFragment)
    }

    override fun onDestroy() {
        LogUtil.i(TAG, "onDestroy")
        super.onDestroy()
    }
}
