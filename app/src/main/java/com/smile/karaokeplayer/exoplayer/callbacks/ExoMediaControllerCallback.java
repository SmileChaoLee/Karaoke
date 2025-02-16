package com.smile.karaokeplayer.exoplayer.callbacks;

import android.annotation.SuppressLint;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.media3.common.util.UnstableApi;

import com.smile.karaokeplayer.exoplayer.presenters.ExoPlayerPresenter;

@UnstableApi
public class ExoMediaControllerCallback extends MediaControllerCompat.Callback {

    private static final String TAG = "ExoMediaControllerCallback";
    private final ExoPlayerPresenter mPresenter;

    public ExoMediaControllerCallback(ExoPlayerPresenter presenter) {
        mPresenter = presenter;
    }

    @SuppressLint("LongLogTag")
    @Override
    public synchronized void onPlaybackStateChanged(PlaybackStateCompat state) {
        Log.d(TAG, "onPlaybackStateChanged().state = " + state);
        super.onPlaybackStateChanged(state);
        if( state == null ) {
            return;
        }
        Log.d(TAG, "onPlaybackStateChanged().mPresenter.updateStatusAndUi(state)");
        mPresenter.updateStatusAndUi(state);
    }
}
