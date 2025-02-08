package com.smile.karaokeplayer.vlcplayer.Callbacks;

import android.annotation.SuppressLint;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.smile.karaokeplayer.vlcplayer.Presenters.VlcPlayerPresenter;

public class VlcMediaControllerCallback extends MediaControllerCompat.Callback {

    private static final String TAG = "VlcMediaControllerCallback";
    private final VlcPlayerPresenter presenter;

    public VlcMediaControllerCallback(VlcPlayerPresenter presenter) {
        this.presenter = presenter;
    }

    @SuppressLint("LongLogTag")
    @Override
    public synchronized void onPlaybackStateChanged(PlaybackStateCompat state) {
        Log.d(TAG, "onPlaybackStateChanged() --> state = " + state);
        super.onPlaybackStateChanged(state);
        if( state == null ) {
            return;
        }
        presenter.updateStatusAndUi(state);
    }
}
