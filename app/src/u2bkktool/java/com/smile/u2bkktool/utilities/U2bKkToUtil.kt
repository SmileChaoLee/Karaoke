package com.smile.u2bkktool.utilities

import com.smile.karaoke.utilities.LogUtil
import com.smile.u2bkaraoke.model.Song
import com.smile.u2bkaraoke.retrofit.U2bKkRestApiSync

object U2bKkToUtil {
    private const val TAG = "U2bKkToUtil"

    fun updateOneSongToCloud(song: Song): Boolean {
        val logStr = "updateOneSongToCloud"
        val mMpegBack = song.mMpeg
        val nMpegBack = song.nMpeg
        song.mMpeg = "00"   // data fed by TOOL
        song.nMpeg = "00"   // and data fed by TOOL
        val result = U2bKkRestApiSync.getApiSync().updateOneSong(song.id, song)
        LogUtil.d(TAG, "$logStr.result = $result")
        if (result != 1) {
            // update failed
            song.mMpeg = mMpegBack  // restore the original value
            song.nMpeg = nMpegBack  // restore the original value
            return false
        }
        return true
    }
}