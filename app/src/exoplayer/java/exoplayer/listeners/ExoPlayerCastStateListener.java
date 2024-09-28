package exoplayer.listeners;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.cast.framework.CastState;
import com.smile.karaokeplayer.R;
import com.smile.smilelibraries.utilities.ScreenUtil;
import exoplayer.fragments.ExoPlayerFragment;
import exoplayer.presenters.ExoPlayerPresenter;

public class ExoPlayerCastStateListener implements
        com.google.android.gms.cast.framework.CastStateListener {
    private static final String TAG = "ExoPlayerCastStateListener";
    private final ExoPlayerFragment mFragment;
    private final Activity mActivity;
    private final ExoPlayerPresenter presenter;
    private final float toastTextSize;

    public ExoPlayerCastStateListener(ExoPlayerPresenter presenter) {
        this.presenter = presenter;
        mFragment = this.presenter.getFragment();
        mActivity = mFragment.getActivity();
        toastTextSize = this.presenter.getToastTextSize();
        Log.d(TAG, "ExoPlayerCastStateListener is created");
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onCastStateChanged(int i) {
        Log.d(TAG, "onCastStateChanged");
        if (presenter.getPlayService() == null) {
            Log.d(TAG, "onCastStateChanged.presenter.getPlayService() = null");
            return;
        }
        presenter.getPlayService().setCurrentCastState(i);
        switch (i) {
            case CastState.NO_DEVICES_AVAILABLE:
                Log.d(TAG, "CastState is NO_DEVICES_AVAILABLE.");
                ScreenUtil.showToast(mActivity, mActivity.getString(R.string.no_chromecast_devices_avaiable), toastTextSize, ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_SHORT);
                break;
            case CastState.NOT_CONNECTED:
                Log.d(TAG, "CastState is NOT_CONNECTED.");
                ScreenUtil.showToast(mActivity, mActivity.getString(R.string.chromecast_not_connected), toastTextSize, ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_SHORT);
                break;
            case CastState.CONNECTING:
                Log.d(TAG, "CastState is CONNECTING.");
                ScreenUtil.showToast(mActivity, mActivity.getString(R.string.chromecast_is_connecting), toastTextSize, ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_SHORT);
                break;
            case CastState.CONNECTED:
                Log.d(TAG, "CastState is CONNECTED.");
                ScreenUtil.showToast(mActivity, mActivity.getString(R.string.chromecast_is_connected), toastTextSize, ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_SHORT);
                break;
            default:
                Log.d(TAG, "CastState is unknown.");
                break;
        }
        mFragment.setMediaRouteButtonVisible();
    }
}
