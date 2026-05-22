package com.smile.karaoke.fragments

import android.widget.Toast
import androidx.core.net.toUri
import com.smile.karaoke.R
import com.smile.karaoke.constants.CommonConstants
import com.smile.karaoke.models.FileDescription
import com.smile.karaoke.models.MySingleton
import com.smile.karaoke.models.SongInfo
import com.smile.karaoke.utilities.CommonUtil
import com.smile.karaoke.utilities.DatabaseUtil
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    suspend fun startPlaySelectedSong(songs: ArrayList<SongInfo>) {
        LogUtil.d(TAG, "startPlaySelectedSong")
        val act = activity ?: return
        if (songs.isEmpty()) {
            withContext(Dispatchers.Main) {
                ScreenUtil.showToast(act,
                    getString(R.string.noFilesSelectedString),
                    textFontSize, Toast.LENGTH_SHORT)
            }
        } else {
            // Check if song is in database
            val vSongs = ArrayList(songs.take(MySingleton.MAX_SONGS))
            DatabaseUtil.getSongsToPlay(act,
                CommonConstants.FAVORITE_DB_NAME, vSongs)
            playSongs?.playSelectedSongList(vSongs)
        }
    }

    fun fileDescriptionToSongInfo(fileDes: FileDescription): SongInfo {
        val fileUri = fileDes.file.toUri().toString()
        return SongInfo().apply {
            songName = fileDes.file.name
            filePath = fileUri
            musicTrackNo = 1    // guess
            musicChannel = CommonConstants.STEREO
            vocalTrackNo = 2    // guess
            vocalChannel = CommonConstants.STEREO
            included = "0"
        }
    }

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
                    songs.add(fileDescriptionToSongInfo(fileDes))
                    index++
                }
            }
        }
        return songs
    }
}