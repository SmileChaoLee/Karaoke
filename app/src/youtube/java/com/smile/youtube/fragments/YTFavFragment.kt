package com.smile.youtube.fragments

import android.graphics.Bitmap
import android.view.View
import com.smile.karaoke.fragments.ComFavFragment
import com.smile.karaoke.models.SongInfo
import com.smile.karaoke.utilities.ImageUtil
import com.smile.karaoke.utilities.LogUtil
import com.smile.youtube.YTUtil

class YTFavFragment : ComFavFragment() {

    companion object {
        private const val TAG: String = "YTFavFragment"
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
        return YTUtil.getFavDatabaseName()
    }
    // end of overriding the methods of ComFavFragment

    // overriding the methods of ItemsBaseFragment
    override fun gridSpanCount(): Int {
        val act = activity ?: return 1
        return YTUtil.gridSpanCount(act)
    }
    // end of overriding the methods of ItemsBaseFragment
}