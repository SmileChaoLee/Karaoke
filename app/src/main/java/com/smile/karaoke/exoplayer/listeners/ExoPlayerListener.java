package com.smile.karaoke.exoplayer.listeners;

import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import com.smile.karaoke.constants.PlayerConstants;
import com.smile.karaoke.exoplayer.services.ExoPlayService;

@UnstableApi
public class ExoPlayerListener implements Player.Listener {

    private String mTAG = "ExoPlayerListener";
    private final ExoPlayService mService;

    public ExoPlayerListener(ExoPlayService service) {
        mService = service;
        Log.d(mTAG, mTAG + " is created.");
    }

    // to be overridden by CastPlayerListener
    public void setTAG(String tagStr) {
        mTAG = tagStr;
    }

    public void onPlayerPaused() {
        String msgStr = "onPlayWhenReadyChanged";
        Log.d(mTAG, msgStr + ".send PlaybackStateCompat.STATE_PAUSED");
        mService.setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
    }

    @Override
    public synchronized void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
        String msgStr = "onPlayWhenReadyChanged";
        Log.d(mTAG, msgStr + "playWhenReady = " + playWhenReady
                        + ", reason = " + reason);
        int state = mService.getPlaybackState();
        Log.d(mTAG, msgStr + ".state = " + state);
        boolean isPlaying = mService.isPlaying();
        Log.d(mTAG, msgStr + ".isPlaying = " + isPlaying);
        if (playWhenReady) {
            // Start playing
            if (isPlaying) {
                Log.d(mTAG, msgStr + ".send PlaybackStateCompat.STATE_PLAYING");
                mService.setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
            }
        } else {
            // Paused
            if (!isPlaying) {
                /*
                Log.d(mTAG, msgStr + ".send PlaybackStateCompat.STATE_PAUSED");
                mService.setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
                */
                onPlayerPaused();
            }
        }
    }

    @Override
    public synchronized void onPlaybackStateChanged(int state) {
        String msgStr = "onPlaybackStateChanged";
        Log.d(mTAG, msgStr + ".state = " + state);
        boolean playWhenReady = mService.getPlayWhenReady();
        Log.d(mTAG, msgStr + ".playWhenReady = " + playWhenReady);
        float duration = mService.getMediaDuration();
        Log.d(mTAG, msgStr + ".duration = " + duration);
        switch (state) {
            case Player.STATE_BUFFERING:
                Log.d(mTAG, msgStr + "send .Player.STATE_BUFFERING");
                mService.setMediaPlaybackState(PlaybackStateCompat.STATE_BUFFERING);
                break;
            case Player.STATE_READY:
                Log.d(mTAG, msgStr + ".Player.STATE_READY");
                if (playWhenReady) {
                    Log.d(mTAG, msgStr + ".send PlaybackStateCompat.STATE_PLAYING");
                    mService.setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
                } else {
                    Log.d(mTAG, msgStr + ".send PlaybackStateCompat.STATE_PAUSED");
                    mService.setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
                }
                break;
            case Player.STATE_ENDED:
                // playing is finished and send PlaybackStateCompat.STATE_STOPPED
                // to MediaControllerCallback
                // this casse: finishState = PlayerConstants.FINISHED_NORMALLY (0)
                Log.d(mTAG, msgStr + ".Player.STATE_ENDED");
                Log.d(mTAG, msgStr + ".send PlaybackStateCompat.STATE_STOPPED");
                mService.setMediaPlaybackState(PlaybackStateCompat.STATE_STOPPED);
                break;
            case Player.STATE_IDLE:
                // user stops the playing and send PlaybackStateCompat.STATE_NONE
                // to MediaControllerCallback
                // or stopPlay(PlayerConstants.FINISHED_BY_PROGRAM) because of playPreviousSong() or playNextSong()
                Log.d(mTAG, msgStr + ".Player.STATE_IDLE");
                if (mService.getPresenter() != null
                        && mService.getPresenter().getPlayingParam().getFinishState()
                        == PlayerConstants.STOPPED_BY_USER) {
                    // stopped by user
                    Log.d(mTAG, msgStr + ".send PlaybackStateCompat.STATE_NONE");
                    mService.setMediaPlaybackState(PlaybackStateCompat.STATE_NONE);
                } else {
                    // finishState = PlayerConstants.FINISHED_BY_PROGRAM (2), stopped by program
                    Log.d(mTAG, msgStr + ".send PlaybackStateCompat.STATE_STOPPED");
                    mService.setMediaPlaybackState(PlaybackStateCompat.STATE_STOPPED);
                }
                break;
            default:
                Log.d(mTAG, msgStr + ".Playback state (Default)");
                mService.stopCasting();
                break;
        }
    }

    @Override
    public synchronized void onIsPlayingChanged(boolean isPlaying) {
        Log.d(mTAG,"onIsPlayingChanged.isPlaying = " + isPlaying);
        float duration = mService.getMediaDuration();
        Log.d(mTAG, "onIsPlayingChanged.duration = " + duration);
    }

    @Override
    public void onPlayerErrorChanged(@Nullable PlaybackException error) {
        Log.d(mTAG,"onPlayerErrorChanged().error = " + error);
        Log.d(mTAG,"onPlayerErrorChanged().send PlaybackStateCompat.STATE_STOPPED");
        mService.setMediaPlaybackState(PlaybackStateCompat.STATE_STOPPED);
    }

    @Override
    public synchronized void onPlayerError(@NonNull PlaybackException error) {
        Log.d(mTAG,"onPlayerError().error = " + error);
        Log.d(mTAG,"onPlayerError().send PlaybackStateCompat.STATE_STOPPED");
        mService.setMediaPlaybackState(PlaybackStateCompat.STATE_STOPPED);
    }
}
