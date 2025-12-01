package videoplayer.callbacks;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import androidx.media3.common.util.UnstableApi;
import com.smile.karaoke.constants.PlayerConstants;
import com.smile.karaoke.models.PlayingParameters;
import com.smile.karaoke.utilities.LogUtil;
import org.videolan.libvlc.interfaces.IMedia;
import videoplayer.presenters.VlcPlayerPresenter;
import videoplayer.services.VlcPlayService;

@UnstableApi
public class VlcMediaSessionCallback extends MediaSessionCompat.Callback {

    private static final String TAG = "VlcMedSeCallback";
    private final VlcPlayService mPlayService;

    public VlcMediaSessionCallback(VlcPlayService playService) {
        mPlayService = playService;
    }

    @Override
    public synchronized void onCommand(String command, Bundle extras, ResultReceiver cb) {
        super.onCommand(command, extras, cb);
    }

    @Override
    public synchronized void onPrepare() {
        super.onPrepare();
        LogUtil.d(TAG, "onPrepare() is called.");
    }

    @Override
    public synchronized void onPrepareFromMediaId(String mediaId, Bundle extras) {
        super.onPrepareFromMediaId(mediaId, extras);
        LogUtil.d(TAG, "onPrepareFromMediaId() is called.");
    }

    @Override
    public synchronized void onPrepareFromUri(Uri uri, Bundle extras) {
        LogUtil.i(TAG, "onPrepareFromUri.uri = " + uri);
        super.onPrepareFromUri(uri, extras);
        VlcPlayerPresenter presenter = mPlayService.getPresenter();
        if (presenter == null) return;

        PlayingParameters playingParam = presenter.getPlayingParam();
        mPlayService.detachPlayerViews();
        final IMedia media = mPlayService.createMedia(uri);
        mPlayService.prepare(media);
        mPlayService.onPlay();
        media.release();
        long currentAudioPosition = playingParam.getCurrentAudioPosition();
        float currentVolume = playingParam.getCurrentVolume();
        int playbackState = playingParam.getCurrentPlaybackState();
        if (extras != null) {
            LogUtil.d(TAG, "extras is not null.");
            PlayingParameters playingParamOrigin;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                playingParamOrigin = extras.getParcelable(PlayerConstants.PlayingParamOrigin,
                        PlayingParameters.class);
            } else playingParamOrigin = extras.getParcelable(PlayerConstants.PlayingParamOrigin);
            if (playingParamOrigin != null) {
                LogUtil.d(TAG, "playingParamOrigin is not null.");
                playbackState = playingParamOrigin.getCurrentPlaybackState();
                LogUtil.d(TAG, "playingParamOrigin.playbackState = " + playbackState);
                currentAudioPosition = playingParamOrigin.getCurrentAudioPosition();
                currentVolume = playingParamOrigin.getCurrentVolume();
                presenter.getPlayingParam().setCurrentAudioPosition(currentAudioPosition);
                presenter.getPlayingParam().setCurrentVolume(currentVolume);
            }
        }

        try {
            switch (playbackState) {
                case PlaybackStateCompat.STATE_PAUSED:
                    LogUtil.d(TAG, "onPrepareFromUri.PlaybackStateCompat.STATE_PAUSED");
                    break;
                case PlaybackStateCompat.STATE_STOPPED:
                    // playing is finished
                    LogUtil.d(TAG, "onPrepareFromUri.PlaybackStateCompat.STATE_STOPPED");
                    break;
                case PlaybackStateCompat.STATE_PLAYING:
                    LogUtil.d(TAG, "onPrepareFromUri.PlaybackStateCompat.STATE_PLAYING");
                    break;
                case PlaybackStateCompat.STATE_NONE:
                    // stopped by user previously
                    LogUtil.d(TAG, "onPrepareFromUri.PlaybackStateCompat.STATE_NONE");
                    break;
                case PlayerConstants.PREPARE_MEDIA:
                    // prepare media for playing
                    LogUtil.d(TAG, "onPrepareFromUri.PlayerConstants.PREPARE_MEDIA");
                    break;
                default:
                    LogUtil.d(TAG, "onPrepareFromUri.default.playbackState = " + playbackState);
                    break;
            }
            // the following must be after vlcPlayer.play()
            LogUtil.d(TAG, "onPrepareFromUri.preparedStatus = " + playingParam.getPreparedStatus());
            LogUtil.d(TAG, "onPrepareFromUri.currentVolume = " + currentVolume +
                            ", currentAudioPosition = " + currentAudioPosition);
            if (playingParam.getPreparedStatus() == 4) {
                // just prepared but just came back from background
                playingParam.setPreparedStatus(5);  // VlcPlayerListener
            } else {
                playingParam.setPreparedStatus(1);
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "onPrepareFromUri.Invalid mediaId", e);
        }
    }

    @Override
    public synchronized void onPlay() {
        super.onPlay();
        LogUtil.i(TAG, "onPlay() is called.");
        mPlayService.onPlay();
    }

    @Override
    public synchronized void onPlayFromMediaId(String mediaId, Bundle extras) {
        super.onPlayFromMediaId(mediaId, extras);
        LogUtil.d(TAG, "onPlayFromMediaId() is called.");
    }

    @Override
    public synchronized void onPlayFromUri(Uri uri, Bundle extras) {
        super.onPlayFromUri(uri, extras);
        LogUtil.d(TAG, "onPlayFromUri() is called.");
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        LogUtil.i(TAG, "onPause() is called.");
        mPlayService.onPause();
    }

    @Override
    public synchronized void onStop() {
        super.onStop();
        LogUtil.i(TAG, "onStop() is called.");
        mPlayService.onStop();
    }

    @Override
    public synchronized void onFastForward() {
        super.onFastForward();
        LogUtil.d(TAG, "onFastForward() is called.");
        mPlayService.setMediaPlaybackState(PlaybackStateCompat.STATE_FAST_FORWARDING);
    }

    @Override
    public synchronized void onRewind() {
        super.onRewind();
        LogUtil.d(TAG, "onRewind() is called.");
        mPlayService.setMediaPlaybackState(PlaybackStateCompat.STATE_REWINDING);
    }
}
