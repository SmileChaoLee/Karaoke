package videoplayer.Listeners;

import android.net.Uri;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.smile.karaokeplayer.models.PlayingParameters;

import org.videolan.libvlc.MediaPlayer;
import videoplayer.Presenters.VlcPlayerPresenter;
import videoplayer.services.VlcPlayService;

public class VlcPlayerEventListener implements MediaPlayer.EventListener {
    private static final String TAG = "VlcPlayerEventListener";
    private final VlcPlayService mPlayService;
    private final VlcPlayerPresenter mPresenter;
    private final MediaPlayer mVlcPlayer;

    public VlcPlayerEventListener(VlcPlayerPresenter presenter, VlcPlayService playService) {
        mPlayService = playService;
        mPresenter = presenter;
        mVlcPlayer = mPlayService.getVlcPlayer();
    }

    @Override
    public synchronized void onEvent(MediaPlayer.Event event) {
        Log.d(TAG, "onEvent().mPresenter = " + mPresenter);
        if (mPresenter == null) {
            return;
        }
        PlayingParameters playingParam = mPresenter.getPlayingParam();
        switch(event.type) {
            case MediaPlayer.Event.Buffering:
                Log.d(TAG, "onEvent()-->Buffering.");
                Log.d(TAG, "onEvent()-->Buffering.playingParam.isMediaPrepared() = " +
                        playingParam.isMediaPrepared());
                if (!mVlcPlayer.isPlaying()) {
                    Log.d(TAG, "onEvent()-->Buffering()-->setMediaPlaybackState(PlaybackStateCompat.STATE_BUFFERING)");
                    mPlayService.setMediaPlaybackState(PlaybackStateCompat.STATE_BUFFERING);
                }
                break;
            case MediaPlayer.Event.Playing:
                Log.d(TAG, "onEvent()-->Playing.");
                Log.d(TAG, "onEvent()-->Playing.playingParam.isMediaPrepared() = " +
                        playingParam.isMediaPrepared());
                mPlayService.setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
                break;
            case MediaPlayer.Event.Paused:
                Log.d(TAG, "onEvent()-->Paused.");
                Log.d(TAG, "onEvent()-->Paused.playingParam.isMediaPrepared() = " +
                        playingParam.isMediaPrepared());
                mPlayService.setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
                break;
            case MediaPlayer.Event.Stopped:
                // Event.Stopped is for
                // 1. stop the playing by user
                // 2. after end of the playing (Event.EndReached)
                Log.d(TAG, "onEvent()-->Stopped-->getLength() = " + mVlcPlayer.getLength());
                /*  // for version 3.6.0
                if (mVlcPlayer.getLength() == 0) {
                    // no legal media format
                    mPlayService.setMediaPlaybackState(PlaybackStateCompat.STATE_ERROR);
                } else {
                    Uri mediaUri = mPresenter.getMediaUri();
                    if (mediaUri != null && !Uri.EMPTY.equals(mediaUri) && playingParam.isMediaPrepared()) {
                        Log.d(TAG, "onEvent()-->Stopped--> vlcPlayer was stopped by user.");
                        mPlayService.setMediaPlaybackState(PlaybackStateCompat.STATE_NONE);
                    } else {
                        // playing is finished
                        Log.d(TAG, "onEvent()-->Stopped--> vlcPlayer is finished playing.");
                        mPlayService.setMediaPlaybackState(PlaybackStateCompat.STATE_STOPPED);
                    }
                }
                */
                // for version 4.0.0
                Uri mediaUri = mPresenter.getMediaUri();
                Log.d(TAG, "onEvent()-->Stopped.mediaUri = " + mediaUri);
                Log.d(TAG, "onEvent()-->Stopped.playingParam.isMediaPrepared() = " +
                        playingParam.isMediaPrepared());
                if (mediaUri != null && !Uri.EMPTY.equals(mediaUri) && playingParam.isMediaPrepared()) {
                    Log.d(TAG, "onEvent()-->Stopped--> vlcPlayer was stopped by user.");
                    mPlayService.setMediaPlaybackState(PlaybackStateCompat.STATE_NONE);
                } else {
                    // playing is finished
                    Log.d(TAG, "onEvent()-->Stopped--> vlcPlayer is finished playing.");
                    mPlayService.setMediaPlaybackState(PlaybackStateCompat.STATE_STOPPED);
                }
                break;
            case MediaPlayer.Event.EndReached:
                // after this event, vlcPlayer will send out Event.Stopped to EventListener
                Log.d(TAG, "onEvent()-->EndReached-->getLength() = " + mVlcPlayer.getLength());
                Log.d(TAG, "onEvent()-->EndReached.playingParam.isMediaPrepared() = " +
                        playingParam.isMediaPrepared());
                // has to be here for next event
                // Event.Stopper
                playingParam.setMediaPrepared(false);
                Log.d(TAG, "onEvent()-->EndReached.playingParam.isMediaPrepared() = " +
                        playingParam.isMediaPrepared());
                // no message has to be sent
                break;
            case MediaPlayer.Event.Opening:
                Log.d(TAG, "onEvent()-->Opening.");
                Log.d(TAG, "onEvent()-->Opening.playingParam.isMediaPrepared() = " +
                        playingParam.isMediaPrepared());
                break;
            case MediaPlayer.Event.PositionChanged:
                // Log.d(TAG, "onEvent()-->PositionChanged.");
                Log.d(TAG, "onEvent()-->PositionChanged.playingParam.isMediaPrepared() = " +
                        playingParam.isMediaPrepared());
                break;
            case MediaPlayer.Event.TimeChanged:
                // Log.d(TAG, "onEvent()-->TimeChanged.");
                mPresenter.getPresentView().update_Player_duration_seekbar_progress((int) mVlcPlayer.getTime());
                break;
            case MediaPlayer.Event.EncounteredError:
                Log.d(TAG, "onEvent()-->EncounteredError.");
                Log.d(TAG, "onEvent()-->EncounteredError.playingParam.isMediaPrepared() = " +
                        playingParam.isMediaPrepared());
                mPlayService.setMediaPlaybackState(PlaybackStateCompat.STATE_ERROR);
                break;
            default:
                Log.d(TAG, "onEvent()-->default-->event.type = " + event.type);
                Log.d(TAG, "onEvent()-->default.playingParam.isMediaPrepared() = " +
                        playingParam.isMediaPrepared());
                break;
        }
    }
}
