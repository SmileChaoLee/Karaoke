package com.smile.karaoke.fragments

import android.app.Activity
import android.widget.Toast
import com.smile.karaoke.R
import com.smile.karaoke.constants.CommonConstants
import com.smile.karaoke.models.MySingleton
import com.smile.karaoke.utilities.DatabaseUtil
import com.smile.karaoke.utilities.LogUtil
import com.smile.karaoke.utilities.SongUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

open class ComOpenFragment: ItemsBaseFragment() {

    companion object {
        private const val TAG = "ComOpenFragment"
    }

    suspend fun startPlaySelectedSong(act: Activity?) {
        LogUtil.d(TAG, "startPlaySelectedSong.act = $act")
        if (act == null) return
        val songs = SongUtil.fileDescriptionsToSongList(MySingleton.fileList)
        if (songs.isEmpty()) {
            withContext(Dispatchers.Main) {
                ScreenUtil.showToast(act,
                    getString(R.string.noFilesSelectedString),
                    textFontSize, Toast.LENGTH_SHORT)
            }
        } else {
            // Check if song is in database
            DatabaseUtil.getSongsToPlay(act,
                CommonConstants.FAVORITE_DB_NAME, songs)
            playSongs?.playSelectedSongList(ArrayList(songs))
        }
    }
}