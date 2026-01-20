package com.smile.karaokeplayer.listeners;

import android.support.v4.media.session.PlaybackStateCompat;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import com.smile.karaoke.constants.MyPlayerConstants;
import com.smile.karaokeplayer.services.ExoPlayService;
import com.smile.karaoke.utilities.LogUtil;

@UnstableApi
public class ExoPlayerListener implements Player.Listener {

    private String mTAG = "ExoPlayerListener";
    private final ExoPlayService mService;

    public ExoPlayerListener(ExoPlayService service) {
        mService = service;
        LogUtil.d(mTAG, mTAG + " is created.");
    }

    // to be overridden by CastPlayerListener
    public void setTAG(String tagStr) {
        mTAG = tagStr;
    }

    public void onPlayerPaused() {
        String msgStr = "onPlayWhenReadyChanged";
        LogUtil.d(mTAG, msgStr + ".send PlaybackStateCompat.STATE_PAUSED");
        mService.setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
    }

    @Override
    public synchronized void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
        String msgStr = "onPlayWhenReadyChanged";
        LogUtil.d(mTAG, msgStr + "playWhenReady = " + playWhenReady
                        + ", reason = " + reason);
        int state = mService.getPlaybackState();
        LogUtil.d(mTAG, msgStr + ".state = " + state);
        boolean isPlaying = mService.isPlaying();
        LogUtil.d(mTAG, msgStr + ".isPlaying = " + isPlaying);
        if (playWhenReady) {
            // Start playing
            if (isPlaying) {
                LogUtil.d(mTAG, msgStr + ".send PlaybackStateCompat.STATE_PLAYING");
                mService.setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
            }
        } else {
            // Paused
            if (!isPlaying) {
                onPlayerPaused();
            }
        }
    }

    @Override
    public synchronized void onPlaybackStateChanged(int state) {
        String msgStr = "onPlaybackStateChanged";
        LogUtil.d(mTAG, msgStr + ".state = " + state);
        boolean playWhenReady = mService.getPlayWhenReady();
        LogUtil.d(mTAG, msgStr + ".playWhenReady = " + playWhenReady);
        float duration = mService.getMediaDuration();
        LogUtil.d(mTAG, msgStr + ".duration = " + duration);
        switch (state) {
            case Player.STATE_BUFFERING:
                LogUtil.d(mTAG, msgStr + "send .Player.STATE_BUFFERING");
                mService.setMediaPlaybackState(PlaybackStateCompat.STATE_BUFFERING);
                break;
            case Player.STATE_READY:
                LogUtil.d(mTAG, msgStr + ".Player.STATE_READY");
                if (playWhenReady) {
                    LogUtil.d(mTAG, msgStr + ".send PlaybackStateCompat.STATE_PLAYING");
                    mService.setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
                } else {
                    LogUtil.d(mTAG, msgStr + ".send PlaybackStateCompat.STATE_PAUSED");
                    mService.setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
                }
                break;
            case Player.STATE_ENDED:
                // playing is finished and send PlaybackStateCompat.STATE_STOPPED
                // to MediaControllerCallback
                // this casse: finishState = PlayerConstants.FINISHED_NORMALLY (0)
                LogUtil.d(mTAG, msgStr + ".Player.STATE_ENDED");
                LogUtil.d(mTAG, msgStr + ".send PlaybackStateCompat.STATE_STOPPED");
                mService.setMediaPlaybackState(PlaybackStateCompat.STATE_STOPPED);
                break;
            case Player.STATE_IDLE:
                // user stops the playing and send PlaybackStateCompat.STATE_NONE
                // to MediaControllerCallback
                // or stopPlay(PlayerConstants.FINISHED_BY_PROGRAM) because of playPreviousSong() or playNextSong()
                LogUtil.d(mTAG, msgStr + ".Player.STATE_IDLE");
                if (mService.getPresenter() != null
                        && mService.getPresenter().getPlayingParam().getFinishState()
                        == MyPlayerConstants.STOPPED_BY_USER) {
                    // stopped by user
                    LogUtil.d(mTAG, msgStr + ".send PlaybackStateCompat.STATE_NONE");
                    mService.setMediaPlaybackState(PlaybackStateCompat.STATE_NONE);
                } else {
                    // finishState = PlayerConstants.FINISHED_BY_PROGRAM (2), stopped by program
                    LogUtil.d(mTAG, msgStr + ".send PlaybackStateCompat.STATE_STOPPED");
                    mService.setMediaPlaybackState(PlaybackStateCompat.STATE_STOPPED);
                }
                break;
            default:
                LogUtil.d(mTAG, msgStr + ".Playback state (Default)");
                mService.stopCasting();
                break;
        }
    }

    @Override
    public synchronized void onIsPlayingChanged(boolean isPlaying) {
        LogUtil.d(mTAG,"onIsPlayingChanged.isPlaying = " + isPlaying);
        float duration = mService.getMediaDuration();
        LogUtil.d(mTAG, "onIsPlayingChanged.duration = " + duration);
    }

    @Override
    public void onPlayerErrorChanged(@Nullable PlaybackException error) {
        LogUtil.d(mTAG,"onPlayerErrorChanged().error = " + error);
        LogUtil.d(mTAG,"onPlayerErrorChanged().send PlaybackStateCompat.STATE_ERROR");
        mService.setMediaPlaybackState(PlaybackStateCompat.STATE_ERROR);
    }

    @Override
    public synchronized void onPlayerError(@NonNull PlaybackException error) {
        LogUtil.d(mTAG,"onPlayerError().error = " + error);
        LogUtil.d(mTAG,"onPlayerError().send PlaybackStateCompat.STATE_ERROR");
        mService.setMediaPlaybackState(PlaybackStateCompat.STATE_ERROR);
    }
}
