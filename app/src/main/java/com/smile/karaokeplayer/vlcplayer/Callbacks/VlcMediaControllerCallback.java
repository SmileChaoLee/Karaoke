package com.smile.karaokeplayer.vlcplayer.Callbacks;

import android.annotation.SuppressLint;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import com.smile.karaokeplayer.vlcplayer.services.VlcPlayService;

@OptIn(markerClass = UnstableApi.class)
public class VlcMediaControllerCallback extends MediaControllerCompat.Callback {

    private static final String TAG = "VlcMedControlCallback";
    private final VlcPlayService mPlayService;

    public VlcMediaControllerCallback(VlcPlayService playService) {
        mPlayService = playService;
    }

    @SuppressLint("LongLogTag")
    @Override
    public synchronized void onPlaybackStateChanged(PlaybackStateCompat state) {
        Log.d(TAG, "onPlaybackStateChanged.state = " + state);
        super.onPlaybackStateChanged(state);
        if( state == null ) {
            return;
        }
        if (mPlayService.getPresenter() != null) {
            mPlayService.getPresenter().updateStatusAndUi(state);
        }
    }
}
