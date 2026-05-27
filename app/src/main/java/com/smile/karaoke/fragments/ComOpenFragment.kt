package com.smile.karaoke.fragments

import android.widget.Toast
import com.smile.karaoke.R
import com.smile.karaoke.models.FileDescription
import com.smile.karaoke.models.MySingleton
import com.smile.karaoke.models.SongInfo
import com.smile.karaoke.utilities.CommonUtil
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil

open class ComOpenFragment: ItemsBaseFragment() {

    companion object {
        private const val TAG = "ComOpenFragment"
    }

    // overriding the methods of ItemsBaseFragment
    override fun gridSpanCount(): Int {
        val act = activity ?: return 1
        return CommonUtil.gridSpanCount(act)
    }
    // end of overriding the methods of ItemsBaseFragment

    fun fileDescriptionsToSongList(files: ArrayList<FileDescription>): ArrayList<SongInfo> {
        val logStr = "fileDescriptionsToSongList"
        LogUtil.i(TAG, logStr)
        val act = activity ?: return ArrayList()
        val songs = ArrayList<SongInfo>()
        var index = 0
        for (fileDes in files) {
            if (index >= MySingleton.MAX_SONGS) {
                // excess the max
                LogUtil.i(TAG, "$logStr.excess the max")
                ScreenUtil.showToast(act,
                    act.getString(R.string.excess_max) + ", ${MySingleton.MAX_SONGS}",
                    textFontSize,Toast.LENGTH_SHORT)
                break
            } else {
                if (fileDes.selected) {
                    songs.add(CommonUtil.fileDescriptionToSongInfo(fileDes))
                    index++
                }
            }
        }
        return songs
    }
}