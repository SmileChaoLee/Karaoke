package com.smile.karaokeplayer.vlcplayer.Listeners;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;

import com.smile.karaokeplayer.constants.PlayerConstants;
import com.smile.karaokeplayer.models.PlayingParameters;

import org.videolan.libvlc.MediaPlayer;
import com.smile.karaokeplayer.vlcplayer.Presenters.VlcPlayerPresenter;
import com.smile.karaokeplayer.vlcplayer.services.VlcPlayService;

import java.util.Objects;

@OptIn(markerClass = UnstableApi.class)
public class VlcPlayerListener implements MediaPlayer.EventListener {

    private static final String TAG = "VlcPlayerListener";
    private final VlcPlayService mPlayService;

    private boolean isEndReached = false;
    private final Handler endReachedHandler = new Handler(Looper.getMainLooper());
    private final Runnable endReachedRunnable = new Runnable() {
        final int maxTimes = 3;
        int count = 0;
        final String msgStr = "endReachedRunnable";
        @Override
        public void run() {
            endReachedHandler.removeCallbacksAndMessages(null);
            Log.d(TAG, msgStr + ".count = " + count);
            if (count < maxTimes) {
                // still true after 3 seconds, means Event.Stopped was not sent out
                if (isEndReached) {
                    Log.d(TAG, msgStr + "Event.EndReached not sent out jet");
                    // check again 500 ms later
                    endReachedHandler.postDelayed(this, 500);
                } else {
                    Log.d(TAG, msgStr + "Event.EndReached was sent out");
                }
                count++;
            } else {
                isEndReached = false;
            }
        }
    };

    public VlcPlayerListener(VlcPlayService playService) {
        mPlayService = playService;
    }

    private void releaseMedia() {
        if (mPlayService.getVlcPlayer() != null) {
            Objects.requireNonNull(mPlayService.getVlcPlayer()
                    .getMedia()).release();
        }
    }

    @Override
    public synchronized void onEvent(MediaPlayer.Event event) {
        Log.d(TAG, "onEvent");
        VlcPlayerPresenter presenter = mPlayService.getPresenter();
        if (presenter == null) {
            Log.d(TAG, "onEvent().presenter = null");
            return;
        }
        final PlayingParameters playingParam = presenter.getPlayingParam();
        if (playingParam.getPreparedStatus() == 5) {
            mPlayService.getCurrentPosition();
            if (mPlayService.getCurrentPosition() > 0.0) {
                Log.d(TAG, "onEvent.preparedStatus = " + playingParam.getPreparedStatus());
                Log.d(TAG, "onEvent.position = " + mPlayService.getCurrentPosition());
                Log.d(TAG, "onEvent.length = " + mPlayService.getMediaDuration());
                // just came back from background
                int playbackState = playingParam.getCurrentPlaybackState();
                Log.d(TAG, "onEvent.setMediaPlaybackState(" + playbackState + ")");
                mPlayService.setPlayerTime(playingParam.getCurrentAudioPosition());
                presenter.getPresentView().update_Player_duration_seekbar_progress(
                        (int) playingParam.getCurrentAudioPosition());
                playingParam.setPreparedStatus(1);  // just prepared
                mPlayService.setMediaPlaybackState(playbackState);
                switch (playbackState) {
                    case PlaybackStateCompat.STATE_PAUSED:
                        Log.d(TAG, "onEvent.PlaybackStateCompat.STATE_PAUSED");
                        presenter.pausePlay();
                        break;
                    case PlaybackStateCompat.STATE_STOPPED:
                        // playing is finished
                        Log.d(TAG, "onEvent.PlaybackStateCompat.STATE_STOPPED");
                        presenter.stopPlay(PlayerConstants.FINISHED_NORMALLY);
                        break;
                    case PlaybackStateCompat.STATE_PLAYING:
                        Log.d(TAG, "onEvent.PlaybackStateCompat.STATE_PLAYING");
                        break;
                    case PlaybackStateCompat.STATE_NONE:
                        // stopped by user previously
                        Log.d(TAG, "onEvent.PlaybackStateCompat.STATE_NONE");
                        presenter.stopPlay(PlayerConstants.STOPPED_BY_USER);
                        break;
                    default:
                        Log.d(TAG, "onEvent().default.playbackState = " + playbackState);
                        break;
                }
            }
            return;
        }

        switch(event.type) {
            case MediaPlayer.Event.Buffering:
                break;
            case MediaPlayer.Event.Playing:
                Log.d(TAG, "onEvent.Playing.playingParam.preparedStatus = " +
                        playingParam.getPreparedStatus());
                mPlayService.setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
                break;
            case MediaPlayer.Event.Paused:
                Log.d(TAG, "onEvent.Paused.playingParam.preparedStatus = " +
                        playingParam.getPreparedStatus());
                if (playingParam.getPreparedStatus() != 5) {
                    mPlayService.setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
                }
                break;
            case MediaPlayer.Event.Stopped:
                // Event.Stopped is for
                // 1. stop the playing by user
                // 2. after end of the playing (Event.EndReached)
                Log.d(TAG, "onEvent.Stopped.getLength() = "
                        + mPlayService.getMediaDuration());
                Uri mediaUri = presenter.getMediaUri();
                Log.d(TAG, "onEvent.Stopped.mediaUri = " + mediaUri);
                Log.d(TAG, "onEvent.Stopped.playingParam.preparedStatus = " +
                        playingParam.getPreparedStatus());
                Log.d(TAG, "onEvent.Stopped.playingParam.finishState = " +
                        playingParam.getFinishState());
                if (mediaUri != null && !Uri.EMPTY.equals(mediaUri)
                        && playingParam.getFinishState() == PlayerConstants.STOPPED_BY_USER) {
                    Log.d(TAG, "onEvent.Stopped.vlcPlayer was stopped by user.");
                    mPlayService.setMediaPlaybackState(PlaybackStateCompat.STATE_NONE);
                } else {
                    // playing is finished
                    Log.d(TAG, "onEvent.Stopped.vlcPlayer is finished playing.");
                    mPlayService.setMediaPlaybackState(PlaybackStateCompat.STATE_STOPPED);
                }
                releaseMedia();
                Log.d(TAG, "onEvent.Stopped.received");
                endReachedHandler.removeCallbacksAndMessages(null);
                isEndReached = false;   // Event.Stopped is sent after Event.EndReached
                break;
            case MediaPlayer.Event.EndReached:
                // after this event, vlcPlayer will
                // send out Event.Stopped to EventListener, sometimes may not
                Log.d(TAG, "onEvent.EndReached.getLength() = " +
                        mPlayService.getMediaDuration());
                Log.d(TAG, "onEvent.EndReached.playingParam.preparedStatus = " +
                        playingParam.getPreparedStatus());
                releaseMedia();
                isEndReached = true;
                // 3 seconds later, check if Event.Event.Stopped is sent out
                Log.d(TAG, "onEvent.EndReached.checking.isEndReached");
                endReachedHandler.postDelayed(endReachedRunnable, 3000);
                break;
            case MediaPlayer.Event.Opening:
                // Use opening as a buffering because VlcPlayer is always buffering during playing
                Log.d(TAG, "onEvent.Opening.playingParam.preparedStatus = " +
                        playingParam.getPreparedStatus());
                break;
            case MediaPlayer.Event.PositionChanged:
                // Log.d(TAG, "onEvent()-->PositionChanged");
                break;
            case MediaPlayer.Event.TimeChanged:
                // Log.d(TAG, "onEvent.TimeChanged.");
                /* moved to VlcPlayerPresenter
                presenter.getPresentView()
                        .update_Player_duration_seekbar_progress(
                                (int) mPlayService.getCurrentPosition());
                */
                break;
            case MediaPlayer.Event.EncounteredError:
                Log.d(TAG, "onEvent.EncounteredError.playingParam.preparedStatus = " +
                        playingParam.getPreparedStatus());
                mPlayService.setMediaPlaybackState(PlaybackStateCompat.STATE_ERROR);
                releaseMedia();
                break;
            default:
                Log.d(TAG, "onEvent.default.event.type = " + event.type);
                break;
        }
    }
}
