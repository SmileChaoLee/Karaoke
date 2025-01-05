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
    private boolean isSentPlaybackState = false;

    public VlcPlayerEventListener(VlcPlayerPresenter presenter, VlcPlayService playService) {
        mPlayService = playService;
        mPresenter = presenter;
        mVlcPlayer = mPlayService.getVlcPlayer();
    }

    @Override
    public synchronized void onEvent(MediaPlayer.Event event) {
        if (mPresenter == null) {
            Log.d(TAG, "onEvent().mPresenter = null");
            return;
        }
        PlayingParameters playingParam = mPresenter.getPlayingParam();
        if (playingParam.getPreparedStatus() == 2) {
            // media is being played
            isSentPlaybackState = false;
        }
        Log.d(TAG, "onEvent()-->position = " + mVlcPlayer.getPosition());
        Log.d(TAG, "onEvent()-->time = " + mVlcPlayer.getTime());
        if (mVlcPlayer.getPosition() == 0.0) {
            // start playing
            Log.d(TAG, "onEvent()-->length = " + mVlcPlayer.getLength());
        }
        Log.d(TAG, "onEvent()-->playingParam.getCurrentAudioPosition() = "
                + playingParam.getCurrentAudioPosition());
        switch(event.type) {
            case MediaPlayer.Event.Buffering:
                Log.d(TAG, "onEvent()-->Buffering.playingParam.preparedStatus = " +
                        playingParam.getPreparedStatus());
                Log.d(TAG, "onEvent()-->Buffering-->mVlcPlayer.isPlaying() = " + mVlcPlayer.isPlaying());
                if (playingParam.getPreparedStatus() == 1) {
                    // The media was just opened (prepared)
                    if (!isSentPlaybackState) {
                        Log.d(TAG, "onEvent()-->Buffering-->setMediaPlaybackState(PlaybackStateCompat.STATE_CONNECTING");
                        mPlayService.setMediaPlaybackState(PlaybackStateCompat.STATE_CONNECTING);
                        isSentPlaybackState = true;
                    }
                }
                break;
            case MediaPlayer.Event.Playing:
                Log.d(TAG, "onEvent()-->Playing.playingParam.preparedStatus = " +
                        playingParam.getPreparedStatus());
                mPlayService.setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
                break;
            case MediaPlayer.Event.Paused:
                Log.d(TAG, "onEvent()-->Paused.playingParam.preparedStatus = " +
                        playingParam.getPreparedStatus());
                mPlayService.setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
                break;
            case MediaPlayer.Event.Stopped:
                // Event.Stopped is for
                // 1. stop the playing by user
                // 2. after end of the playing (Event.EndReached)
                Log.d(TAG, "onEvent()-->Stopped-->getLength() = " + mVlcPlayer.getLength());
                Uri mediaUri = mPresenter.getMediaUri();
                Log.d(TAG, "onEvent()-->Stopped.mediaUri = " + mediaUri);
                Log.d(TAG, "onEvent()-->Stopped.playingParam.preparedStatus = " +
                        playingParam.getPreparedStatus());
                Log.d(TAG, "onEvent()-->Stopped.playingParam.isSelfFinished() = " +
                        playingParam.getFinishState());
                if (mediaUri != null && !Uri.EMPTY.equals(mediaUri) && playingParam.getFinishState() == 1) {
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
                Log.d(TAG, "onEvent()-->EndReached.playingParam.preparedStatus = " +
                        playingParam.getPreparedStatus());
                // has to be here for next event
                // Event.Stopper
                // playingParam.setMediaPrepared(false);
                // no message has to be sent
                break;
            case MediaPlayer.Event.Opening:
                // Use opening as a buffering because VlcPlayer is always buffering during playing
                Log.d(TAG, "onEvent()-->Opening.playingParam.preparedStatus = " +
                        playingParam.getPreparedStatus());
                break;
            case MediaPlayer.Event.PositionChanged:
                Log.d(TAG, "onEvent()-->PositionChanged.playingParam.preparedStatus = " +
                        playingParam.getPreparedStatus());
                break;
            case MediaPlayer.Event.TimeChanged:
                // Log.d(TAG, "onEvent()-->TimeChanged.");
                mPresenter.getPresentView().update_Player_duration_seekbar_progress((int) mVlcPlayer.getTime());
                break;
            case MediaPlayer.Event.EncounteredError:
                Log.d(TAG, "onEvent()-->EncounteredError.playingParam.preparedStatus = " +
                        playingParam.getPreparedStatus());
                mPlayService.setMediaPlaybackState(PlaybackStateCompat.STATE_ERROR);
                break;
            default:
                Log.d(TAG, "onEvent()-->default-->event.type = " + event.type);
                Log.d(TAG, "onEvent()-->default.playingParam.preparedStatus = " +
                        playingParam.getPreparedStatus());
                break;
        }
    }
}
