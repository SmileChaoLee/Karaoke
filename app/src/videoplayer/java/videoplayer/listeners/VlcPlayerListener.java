package videoplayer.listeners;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.media.session.PlaybackStateCompat;
import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import com.smile.karaoke.constants.MyPlayerConstants;
import com.smile.karaoke.models.PlayingParameters;
import com.smile.karaoke.utilities.LogUtil;
import org.videolan.libvlc.MediaPlayer;
import videoplayer.presenters.VlcPlayerPresenter;
import videoplayer.services.VlcPlayService;

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
            LogUtil.d(TAG, msgStr + ".count = " + count);
            if (count < maxTimes) {
                // still true after 3 seconds, means Event.Stopped was not sent out
                if (isEndReached) {
                    LogUtil.d(TAG, msgStr + "Event.EndReached not sent out jet");
                    // check again 500 ms later
                    endReachedHandler.postDelayed(this, 500);
                } else {
                    LogUtil.d(TAG, msgStr + "Event.EndReached was sent out");
                }
                count++;
            } else {
                // sent out PlaybackStateCompat.STATE_STOPPED
                mPlayService.setMediaPlaybackState(PlaybackStateCompat.STATE_STOPPED);
                isEndReached = false;
            }
        }
    };

    public VlcPlayerListener(VlcPlayService playService) {
        mPlayService = playService;
    }

    @Override
    public synchronized void onEvent(MediaPlayer.Event event) {
        LogUtil.d(TAG, "onEvent.event = " + event);
        VlcPlayerPresenter presenter = mPlayService.getPresenter();
        if (presenter == null) {
            LogUtil.d(TAG, "onEvent().presenter = null");
            return;
        }
        final PlayingParameters playingParam = presenter.getPlayingParam();
        LogUtil.d(TAG, "onEvent.preparedStatus = " + playingParam.getPreparedStatus());
        if (playingParam.getPreparedStatus() == 5) {
            LogUtil.d(TAG, "onEvent.position = " + mPlayService.getCurrentPosition());
            if (mPlayService.getCurrentPosition() > 0.0) {
                LogUtil.d(TAG, "onEvent.length = " + mPlayService.getMediaDuration());
                // just came back from background
                int playbackState = playingParam.getCurrentPlaybackState();
                LogUtil.d(TAG, "onEvent.setMediaPlaybackState(" + playbackState + ")");
                mPlayService.setPlayerTime(playingParam.getCurrentAudioPosition());
                presenter.getPresentView().updatePlayerDurationSeekbarProgress(
                        (int) playingParam.getCurrentAudioPosition());
                playingParam.setPreparedStatus(1);  // just prepared
                mPlayService.setMediaPlaybackState(playbackState);
                switch (playbackState) {
                    case PlaybackStateCompat.STATE_PAUSED:
                        LogUtil.d(TAG, "onEvent.PlaybackStateCompat.STATE_PAUSED");
                        presenter.pausePlay();
                        break;
                    case PlaybackStateCompat.STATE_STOPPED:
                        // playing is finished
                        LogUtil.d(TAG, "onEvent.PlaybackStateCompat.STATE_STOPPED");
                        presenter.stopPlay(MyPlayerConstants.FINISHED_NORMALLY);
                        break;
                    case PlaybackStateCompat.STATE_PLAYING:
                        LogUtil.d(TAG, "onEvent.PlaybackStateCompat.STATE_PLAYING");
                        break;
                    case PlaybackStateCompat.STATE_NONE:
                        // stopped by user previously
                        LogUtil.d(TAG, "onEvent.PlaybackStateCompat.STATE_NONE");
                        presenter.stopPlay(MyPlayerConstants.STOPPED_BY_USER);
                        break;
                    default:
                        LogUtil.d(TAG, "onEvent().default.playbackState = " + playbackState);
                        break;
                }
            }
            return;
        }

        LogUtil.d(TAG, "onEvent.event.type = " + event.type);
        switch(event.type) {
            case MediaPlayer.Event.Buffering:
                break;
            case MediaPlayer.Event.Playing:
                LogUtil.d(TAG, "onEvent.Playing.playingParam.preparedStatus = " +
                        playingParam.getPreparedStatus());
                mPlayService.setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
                // added on 2025-10-22 to get fully media info
                presenter.setAudioActionSubMenu();
                break;
            case MediaPlayer.Event.Paused:
                LogUtil.d(TAG, "onEvent.Paused.playingParam.preparedStatus = " +
                        playingParam.getPreparedStatus());
                if (playingParam.getPreparedStatus() != 5) {
                    mPlayService.setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
                }
                break;
            case MediaPlayer.Event.Stopped:
                // Event.Stopped is for
                // 1. stop the playing by user
                // 2. after end of the playing (Event.EndReached)
                LogUtil.d(TAG, "onEvent.Stopped.getLength() = "
                        + mPlayService.getMediaDuration());
                Uri mediaUri = presenter.getMediaUri();
                LogUtil.d(TAG, "onEvent.Stopped.mediaUri = " + mediaUri);
                LogUtil.d(TAG, "onEvent.Stopped.playingParam.preparedStatus = " +
                        playingParam.getPreparedStatus());
                LogUtil.d(TAG, "onEvent.Stopped.playingParam.finishState = " +
                        playingParam.getFinishState());
                if (mediaUri != null && !Uri.EMPTY.equals(mediaUri)
                        && playingParam.getFinishState() == MyPlayerConstants.STOPPED_BY_USER) {
                    LogUtil.d(TAG, "onEvent.Stopped.vlcPlayer was stopped by user.");
                    mPlayService.setMediaPlaybackState(PlaybackStateCompat.STATE_NONE);
                } else {
                    // playing is finished
                    LogUtil.d(TAG, "onEvent.Stopped.vlcPlayer is finished playing.");
                    mPlayService.setMediaPlaybackState(PlaybackStateCompat.STATE_STOPPED);
                }
                LogUtil.d(TAG, "onEvent.Stopped.received");
                endReachedHandler.removeCallbacksAndMessages(null);
                isEndReached = false;   // Event.Stopped is sent after Event.EndReached
                break;
            case MediaPlayer.Event.EndReached:
                // after this event, vlcPlayer will
                // send out Event.Stopped to EventListener, sometimes may not
                LogUtil.d(TAG, "onEvent.EndReached.getLength() = " +
                        mPlayService.getMediaDuration());
                LogUtil.d(TAG, "onEvent.EndReached.playingParam.preparedStatus = " +
                        playingParam.getPreparedStatus());
                isEndReached = true;
                // 3 seconds later, check if Event.Event.Stopped is sent out
                LogUtil.d(TAG, "onEvent.EndReached.checking.isEndReached");
                endReachedHandler.postDelayed(endReachedRunnable, 3000);
                break;
            case MediaPlayer.Event.Opening:
                // Use opening as a buffering because VlcPlayer is always buffering during playing
                LogUtil.d(TAG, "onEvent.Opening.playingParam.preparedStatus = " +
                        playingParam.getPreparedStatus());
                break;
            case MediaPlayer.Event.PositionChanged:
                // LogUtil.d(TAG, "onEvent()-->PositionChanged");
                break;
            case MediaPlayer.Event.TimeChanged:
                // LogUtil.d(TAG, "onEvent.TimeChanged.");
                /* moved to VlcPlayerPresenter
                presenter.getPresentView()
                        .update_Player_duration_seekbar_progress(
                                (int) mPlayService.getCurrentPosition());
                */
                break;
            case MediaPlayer.Event.EncounteredError:
                LogUtil.d(TAG, "onEvent.EncounteredError.playingParam.preparedStatus = " +
                        playingParam.getPreparedStatus());
                mPlayService.setMediaPlaybackState(PlaybackStateCompat.STATE_ERROR);
                break;
            default:
                LogUtil.d(TAG, "onEvent.default.event.type = " + event.type);
                break;
        }
    }
}
