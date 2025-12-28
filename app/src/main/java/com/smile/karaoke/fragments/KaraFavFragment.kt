package com.smile.karaoke.fragments

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.view.View
import com.smile.karaoke.models.SongInfo
import com.smile.karaoke.utilities.CommonUtil
import com.smile.karaoke.utilities.DatabaseUtil
import com.smile.karaoke.utilities.LogUtil

class KaraFavFragment : ComFavFragment() {

    companion object {
        private const val TAG: String = "KaraFavFragment"
    }

    // overriding the methods of ComFavFragment
    override fun decoderButtonVisibility(): Int {
        return View.VISIBLE
    }

    override suspend fun getVideoThumbNail(song: SongInfo): Bitmap? {
        var bm: Bitmap? = null
        try {
            mediaRetriever.setDataSource(song.filePath)
            bm = mediaRetriever.getFrameAtTime(0,
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        } catch (ex: Exception) {
            LogUtil.e(TAG, "getVideoThumbNail.setDataSource.Exception:", ex)
        }
        return bm
    }

    override fun getFavDatabaseName(): String {
        return DatabaseUtil.getFavDatabaseName()
    }
    // end of overriding the methods of ComFavFragment

    // overriding the methods of ItemsBaseFragment
    override fun gridSpanCount(): Int {
        val act = activity ?: return 1
        return CommonUtil.gridSpanCount(act)
    }
    // end of overriding the methods of ItemsBaseFragment
}