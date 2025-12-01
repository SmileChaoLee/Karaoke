package com.smile.karaoke.fragments

import android.app.Activity
import android.widget.Toast
import androidx.core.net.toUri
import com.smile.karaoke.R
import com.smile.karaoke.constants.CommonConstants
import com.smile.karaoke.models.MySingleton
import com.smile.karaoke.models.SongInfo
import com.smile.karaoke.models.SongListSQLite
import com.smile.karaoke.utilities.LogUtil
import com.smile.smilelibraries.utilities.ScreenUtil

open class ComOpenFragment: ItemsBaseFragment() {

    companion object {
        private const val TAG = "ComOpenFragment"
    }

    fun startPlaySelectedSong(act: Activity?, msg: String) {
        if (act == null) return
        val songListSQLite = SongListSQLite(act)
        getSongs(songListSQLite, msg).let { songsIt ->
            if (songsIt.isEmpty()) {
                ScreenUtil.showToast(
                    act,
                    getString(R.string.noFilesSelectedString),
                    textFontSize,Toast.LENGTH_SHORT)
            } else {
                playSongs?.playSelectedSongList(ArrayList(songsIt))
            }
        }
        songListSQLite.closeDatabase()
    }

    fun getSongs(songListSQLite : SongListSQLite, msg : String): ArrayList<SongInfo> {
        val songs = ArrayList<SongInfo>().also {songIt ->
            var index = 0
            for (fileDes in MySingleton.fileList) {
                if (fileDes.selected) {
                    val path = fileDes.file.path
                    LogUtil.d(TAG, "$msg.file.path = $path")
                    val fileUri = fileDes.file.toUri().toString()
                    LogUtil.d(TAG, "$msg.file.toUri() = $fileUri")
                    var song = SongInfo().apply {
                        songName = fileDes.file.name
                        filePath = fileUri
                        musicTrackNo = 1    // guess
                        musicChannel = CommonConstants.STEREO
                        vocalTrackNo = 2    // guess
                        vocalChannel = CommonConstants.STEREO
                        included = "0"
                    }
                    songListSQLite.findOneSongByUriString(fileUri)?.apply {
                        LogUtil.d(TAG, "$msg.found")
                        included = "1"
                        song = this
                    }
                    songIt.add(song)
                    index++
                    if (index >= MySingleton.MAX_SONGS) {
                        // excess the max
                        ScreenUtil.showToast(
                            activity, getString(R.string.excess_max) +
                                    " ${MySingleton.MAX_SONGS}", textFontSize,
                            Toast.LENGTH_SHORT)
                        break
                    }
                }
            }
        }
        return songs
    }
}