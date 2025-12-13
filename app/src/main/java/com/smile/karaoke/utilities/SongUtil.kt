package com.smile.karaoke.utilities

import androidx.core.net.toUri
import com.smile.karaoke.constants.CommonConstants
import com.smile.karaoke.models.FileDescription
import com.smile.karaoke.models.MySingleton
import com.smile.karaoke.models.SongInfo

object SongUtil {

    private const val TAG = "SongUtil"

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
        val songs = ArrayList<SongInfo>()
        var index = 0
        for (fileDes in files) {
            if (index >= MySingleton.MAX_SONGS) {
                // excess the max
                LogUtil.i(TAG, "$logStr.excess the max")
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