package com.smile.u2bplayer.fragments

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.scale
import androidx.lifecycle.lifecycleScope
import com.smile.karaoke.R
import com.smile.karaoke.models.SongDescription
import com.smile.karaoke.models.SongInfo
import com.smile.karaoke.utilities.ImageUtil
import com.smile.karaoke.utilities.LogUtil
import com.smile.u2bplayer.models.U2bSingleton
import com.smile.u2bplayer.retrofit.U2bPlayerRestApiSync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BkSearchFragment : SearchYTFragment() {

    companion object {
        private const val TAG : String = "BkSearchFragment"
    }

    override fun searchedToU2bVideos(act: Activity, searchTerm: String, maxResult: Int) {
        val logStr = "searchedToU2bVideos"
        lifecycleScope.launch(Dispatchers.IO) {
            U2bSingleton.videos.clear()
            val ytVideos = U2bPlayerRestApiSync.getVideos(searchTerm)
            if (ytVideos == null) {
                LogUtil.d(TAG, "$logStr.videos is null")
                return@launch
            }
            LogUtil.d(TAG, "$logStr.videos.videos = ${ytVideos.videos}")
            if (ytVideos.videos == null) {
                LogUtil.d(TAG, "$logStr.videos.videos is null")
                return@launch
            }
            LogUtil.d(TAG, "$logStr.videoList.videos.size = ${ytVideos.videos.size}")
            val fileBm = BitmapFactory.decodeResource(resources, R.drawable.video_image)
            var songInfo: SongInfo
            var bm: Bitmap?
            for (item in ytVideos.videos) {
                songInfo = SongInfo()
                bm = null
                songInfo.apply {
                    songName = item.title
                    filePath = item.id
                    included = "0"
                    bitmapUrl = item.thumbnail
                    bm = ImageUtil.getBitmapFromUri(act, bitmapUrl)
                }
                if (bm == null) bm = fileBm
                bm = bm.scale(videoThumbNailsWidth, videoThumbNailsHeight)
                val songDes = SongDescription(songInfo, bm)
                U2bSingleton.videos.add(songDes)
            }
            // update the UI
            withContext(Dispatchers.Main) {
                updateRecyclerView()
                searchCompleted = true
            }
        }
    }
}