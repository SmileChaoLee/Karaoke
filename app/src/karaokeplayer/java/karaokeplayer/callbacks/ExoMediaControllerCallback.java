package karaokeplayer.callbacks;

import android.annotation.SuppressLint;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.media3.common.util.UnstableApi;

import com.smile.karaoke.utilities.LogUtil;

import karaokeplayer.presenters.ExoPlayerPresenter;

@UnstableApi
public class ExoMediaControllerCallback extends MediaControllerCompat.Callback {

    private static final String TAG = "ExoMedCtrlCallback";
    private final ExoPlayerPresenter mPresenter;

    public ExoMediaControllerCallback(ExoPlayerPresenter presenter) {
        mPresenter = presenter;
    }

    @SuppressLint("LongLogTag")
    @Override
    public synchronized void onPlaybackStateChanged(PlaybackStateCompat state) {
        LogUtil.d(TAG, "onPlaybackStateChanged().state = " + state);
        super.onPlaybackStateChanged(state);
        if( state == null ) {
            return;
        }
        LogUtil.d(TAG, "onPlaybackStateChanged().mPresenter.updateStatusAndUi(state)");
        mPresenter.updateStatusAndUi(state);
    }
}
