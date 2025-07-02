package com.smile.karaokeplayer.exoplayer.listeners;

import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;

import com.smile.karaokeplayer.constants.PlayerConstants;

import com.smile.karaokeplayer.exoplayer.services.ExoPlayService;

import java.util.Objects;

@UnstableApi
public class ExoPlayerListener implements Player.Listener {

    private static final String TAG = "ExoPlayerListener";
    private final ExoPlayService mService;

    public ExoPlayerListener(ExoPlayService service) {
        mService = service;
        Log.d(TAG, "ExoPlayerListener is created.");
    }

    @Override
    public synchronized void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
        Log.d(TAG, "onPlayWhenReadyChanged().playWhenReady = " + playWhenReady
                        + ", reason = " + reason);
        int state = mService.getPlaybackState();
        Log.d(TAG, "onPlayWhenReadyChanged().state = " + state);
        boolean isPlaying = mService.isPlaying();
        Log.d(TAG, "onPlayWhenReadyChanged().isPlaying = " + isPlaying);
        if (playWhenReady) {
            // Start playing
            if (isPlaying) {
                Log.d(TAG, "onPlayWhenReadyChanged().PlaybackStateCompat.STATE_PLAYING");
                mService.setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
            }
        } else {
            // Paused
            if (!isPlaying) {
                Log.d(TAG, "onPlayWhenReadyChanged().PlaybackStateCompat.STATE_PAUSED");
                mService.setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
            }
        }
    }

    @Override
    public synchronized void onPlaybackStateChanged(int state) {
        Log.d(TAG, "onPlaybackStateChanged.state = " + state);
        boolean playWhenReady = mService.getPlayWhenReady();
        Log.d(TAG, "onPlaybackStateChanged.playWhenReady = " + playWhenReady);
        switch (state) {
            case Player.STATE_BUFFERING:
                Log.d(TAG, "onPlaybackStateChanged.Player.STATE_BUFFERING");
                mService.setMediaPlaybackState(PlaybackStateCompat.STATE_BUFFERING);
                break;
            case Player.STATE_READY:
                Log.d(TAG, "onPlaybackStateChanged.Player.STATE_READY");
                if (playWhenReady) {
                    Log.d(TAG, "onPlaybackStateChanged.PlaybackStateCompat.STATE_PLAYING");
                    mService.setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
                } else {
                    Log.d(TAG, "onPlaybackStateChanged.PlaybackStateCompat.STATE_PAUSED");
                    mService.setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
                }
                break;
            case Player.STATE_ENDED:
                // playing is finished and send PlaybackStateCompat.STATE_STOPPED
                // to MediaControllerCallback
                Log.d(TAG, "onPlaybackStateChanged.Player.STATE_ENDED");
                Log.d(TAG, "onPlaybackStateChanged.PlaybackStateCompat.STATE_STOPPED");
                mService.setMediaPlaybackState(PlaybackStateCompat.STATE_STOPPED);
                break;
            case Player.STATE_IDLE:
                // user stops the playing and send PlaybackStateCompat.STATE_NONE
                // to MediaControllerCallback
                // or stopPlay(2) because of playPreviousSong() or playNextSong()
                Log.d(TAG, "onPlaybackStateChanged().Player.STATE_IDLE");
                if (mService.getPresenter() != null
                        && mService.getPresenter().getPlayingParam().getFinishState()
                        == PlayerConstants.STOPPED_BY_USER) {
                    // stopped by user
                    Log.d(TAG, "onPlaybackStateChanged.PlaybackStateCompat.STATE_NONE");
                    mService.setMediaPlaybackState(PlaybackStateCompat.STATE_NONE);
                } else {
                    // finishState = PlayerConstants.FINISHED_BY_PROGRAM (2), stopped by program
                    Log.d(TAG, "onPlaybackStateChanged.PlaybackStateCompat.STATE_STOPPED");
                    mService.setMediaPlaybackState(PlaybackStateCompat.STATE_STOPPED);
                }
                break;
            default:
                Log.d(TAG, "onPlaybackStateChanged().Playback state (Default)");
                break;
        }
    }

    @Override
    public synchronized void onIsPlayingChanged(boolean isPlaying) {
        Log.d(TAG,"onIsPlayingChanged().isPlaying = " + isPlaying);
    }

    @Override
    public void onPlayerErrorChanged(@Nullable PlaybackException error) {
        Log.d(TAG,"onPlayerErrorChanged().error = " + error);
        mService.setMediaPlaybackState(PlaybackStateCompat.STATE_STOPPED);
    }

    @Override
    public synchronized void onPlayerError(@NonNull PlaybackException error) {
        Log.d(TAG,"onPlayerError().error = " + error);
        mService.setMediaPlaybackState(PlaybackStateCompat.STATE_STOPPED);
    }
}
