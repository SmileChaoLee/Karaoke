package videoplayer.Callbacks;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.core.os.BundleCompat;

import com.smile.karaokeplayer.constants.PlayerConstants;
import com.smile.karaokeplayer.models.PlayingParameters;
import org.videolan.libvlc.interfaces.IMedia;

import videoplayer.Presenters.VlcPlayerPresenter;
import videoplayer.services.VlcPlayService;

public class VlcMediaSessionCallback extends MediaSessionCompat.Callback {

    private static final String TAG = "VlcMediaSessionCallback";
    private final VlcPlayerPresenter mPresenter;
    private final VlcPlayService mPlayService;

    public VlcMediaSessionCallback(VlcPlayerPresenter presenter, VlcPlayService playService) {
        mPresenter = presenter;
        mPlayService = playService;
    }

    @Override
    public synchronized void onCommand(String command, Bundle extras, ResultReceiver cb) {
        super.onCommand(command, extras, cb);
    }

    @Override
    public synchronized void onPrepare() {
        super.onPrepare();
        Log.d(TAG, "onPrepare() is called.");
    }

    @Override
    public synchronized void onPrepareFromMediaId(String mediaId, Bundle extras) {
        super.onPrepareFromMediaId(mediaId, extras);
        Log.d(TAG, "onPrepareFromMediaId() is called.");
    }

    @Override
    public synchronized void onPrepareFromUri(Uri uri, Bundle extras) {
        Log.d(TAG, "onPrepareFromUri.uri = " + uri);
        super.onPrepareFromUri(uri, extras);

        PlayingParameters playingParam = mPresenter.getPlayingParam();

        mPlayService.detachPlayerViews();
        final IMedia media = mPlayService.createMedia(uri);
        mPlayService.prepare(media);
        media.release();

        long currentAudioPosition = playingParam.getCurrentAudioPosition();
        float currentVolume = playingParam.getCurrentVolume();
        int playbackState = playingParam.getCurrentPlaybackState();
        if (extras != null) {
            Log.d(TAG, "extras is not null.");
            PlayingParameters playingParamOrigin;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                playingParamOrigin = BundleCompat.getParcelable(extras, PlayerConstants.PlayingParamOrigin,
                        PlayingParameters.class);
            } else playingParamOrigin = extras.getParcelable(PlayerConstants.PlayingParamOrigin);
            if (playingParamOrigin != null) {
                Log.d(TAG, "playingParamOrigin is not null.");
                playbackState = playingParamOrigin.getCurrentPlaybackState();
                Log.d(TAG, "playingParamOrigin.playbackState = " + playbackState);
                currentAudioPosition = playingParamOrigin.getCurrentAudioPosition();
                currentVolume = playingParamOrigin.getCurrentVolume();
            }
        }

        try {
            switch (playbackState) {
                case PlaybackStateCompat.STATE_PAUSED:
                    Log.d(TAG, "onPrepareFromUri.PlaybackStateCompat.STATE_PAUSED");
                    // mPlayService.onPause();
                    mPresenter.pausePlay();
                    break;
                case PlaybackStateCompat.STATE_STOPPED:
                    // playing is finished
                    Log.d(TAG, "onPrepareFromUri.PlaybackStateCompat.STATE_STOPPED");
                    // mPlayService.onStop();
                    mPresenter.stopPlay(PlayerConstants.FINISHED_NORMALLY);
                    break;
                case PlaybackStateCompat.STATE_PLAYING:
                    Log.d(TAG, "onPrepareFromUri.PlaybackStateCompat.STATE_PLAYING");
                    mPlayService.onPlay();
                    break;
                case PlaybackStateCompat.STATE_NONE:
                    // stopped by user previously
                    Log.d(TAG, "onPrepareFromUri.PlaybackStateCompat.STATE_NONE");
                    // mPlayService.onStop();
                    mPresenter.stopPlay(PlayerConstants.STOPPED_BY_USER);
                    break;
                case PlayerConstants.PREPARE_MEDIA:
                    // prepare media for playing
                    Log.d(TAG, "onPrepareFromUri.PlayerConstants.PREPARE_MEDIA");
                    mPlayService.onPlay();
                    break;
                default:
                    Log.d(TAG, "onPrepareFromUri.default.playbackState = " + playbackState);
                    break;
            }
            // the following must be after vlcPlayer.play()
            Log.d(TAG, "onPrepareFromUri.preparedStatus = " + playingParam.getPreparedStatus());
            Log.d(TAG, "onPrepareFromUri.currentVolume = " + currentVolume +
                            ", currentAudioPosition = " + currentAudioPosition);
            // thw following 2 statements are useless because
            // mPlayService.setAudioVolume(currentVolume);
            // mPlayService.setPlayerTime(currentAudioPosition);
            playingParam.setPreparedStatus(1);  // just prepared
        } catch (Exception e) {
            Log.d(TAG, "onPrepareFromUri.Invalid mediaId");
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void onPlay() {
        super.onPlay();
        Log.d(TAG, "onPlay() is called.");
        mPlayService.onPlay();
    }

    @Override
    public synchronized void onPlayFromMediaId(String mediaId, Bundle extras) {
        super.onPlayFromMediaId(mediaId, extras);
        Log.d(TAG, "onPlayFromMediaId() is called.");
    }

    @Override
    public synchronized void onPlayFromUri(Uri uri, Bundle extras) {
        super.onPlayFromUri(uri, extras);
        Log.d(TAG, "onPlayFromUri() is called.");
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() is called.");
        mPlayService.onPause();
    }

    @Override
    public synchronized void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() is called.");
        mPlayService.onStop();
    }

    @Override
    public synchronized void onFastForward() {
        super.onFastForward();
        Log.d(TAG, "onFastForward() is called.");
        mPlayService.setMediaPlaybackState(PlaybackStateCompat.STATE_FAST_FORWARDING);
    }

    @Override
    public synchronized void onRewind() {
        super.onRewind();
        Log.d(TAG, "onRewind() is called.");
        mPlayService.setMediaPlaybackState(PlaybackStateCompat.STATE_REWINDING);
    }
}
