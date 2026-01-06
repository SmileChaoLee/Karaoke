package com.smile.u2bplayer.fragments

import android.graphics.Bitmap
import android.view.View
import com.smile.karaoke.fragments.ComFavFragment
import com.smile.karaoke.models.SongInfo
import com.smile.karaoke.utilities.ImageUtil
import com.smile.karaoke.utilities.LogUtil
import com.smile.u2bplayer.U2bUtil

class U2bFavFragment : ComFavFragment() {

    companion object {
        private const val TAG: String = "U2bFavFragment"
    }

    // overriding the methods of ComFavFragment
    override fun decoderButtonVisibility(): Int {
        return View.GONE
    }

    override suspend fun getVideoThumbNail(song: SongInfo): Bitmap? {
        val act = activity ?: return null
        var bm: Bitmap? = null
        try {
            bm = ImageUtil.getBitmapFromUri(act, song.bitmapUrl)
        } catch (ex: Exception) {
            LogUtil.e(TAG, "getVideoThumbNail.setDataSource.Exception:", ex)
        }
        return bm
    }

    override fun getFavDatabaseName(): String {
        return U2bUtil.getFavDatabaseName()
    }
    // end of overriding the methods of ComFavFragment

    // overriding the methods of ItemsBaseFragment
    override fun gridSpanCount(): Int {
        val act = activity ?: return 1
        return U2bUtil.gridSpanCount(act)
    }
    // end of overriding the methods of ItemsBaseFragment
}