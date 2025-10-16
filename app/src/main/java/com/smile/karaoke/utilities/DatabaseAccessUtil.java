package com.smile.karaoke.utilities;

import android.content.Context;
import com.smile.karaoke.models.SongInfo;
import com.smile.karaoke.models.SongListSQLite;

import java.util.ArrayList;

public final class DatabaseAccessUtil {

    private DatabaseAccessUtil() {}
    public static ArrayList<SongInfo> readSavedSongList(Context callingContext, boolean isIncluded) {
        ArrayList<SongInfo> playlist;
        SongListSQLite songListSQLite = new SongListSQLite(callingContext);
        playlist = songListSQLite.readPlaylist(isIncluded);
        songListSQLite.closeDatabase();
        return playlist;
    }
}
