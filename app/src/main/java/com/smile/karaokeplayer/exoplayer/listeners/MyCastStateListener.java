package com.smile.karaokeplayer.exoplayer.listeners;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.widget.Toast;
import androidx.media3.common.util.UnstableApi;
import com.google.android.gms.cast.framework.CastState;
import com.smile.karaokeplayer.R;
import com.smile.karaokeplayer.SmileApp;
import com.smile.karaokeplayer.fragments.PlayerBaseFragment;
import com.smile.smilelibraries.utilities.ScreenUtil;

@UnstableApi
public class MyCastStateListener implements
        com.google.android.gms.cast.framework.CastStateListener {
    private static final String TAG = "MyCastStateListener";
    private final Activity mActivity;
    private final PlayerBaseFragment mFragment;
    private final float toastTextSize;

    public MyCastStateListener(PlayerBaseFragment fragment) {
        mFragment = fragment;
        mActivity = mFragment.getActivity();
        toastTextSize = SmileApp.toastTextSize;
        Log.d(TAG, "MyCastStateListener is created");
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onCastStateChanged(int i) {
        Log.d(TAG, "onCastStateChanged");
        // presenter.getPlayService().setCurrentCastState(i);
        switch (i) {
            case CastState.NO_DEVICES_AVAILABLE:
                Log.d(TAG, "CastState is NO_DEVICES_AVAILABLE.");
                ScreenUtil.showToast(mActivity, mActivity.getString(R.string.no_chromecast_devices_avaiable), toastTextSize, ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_SHORT);
                break;
            case CastState.NOT_CONNECTED:
                Log.d(TAG, "CastState is NOT_CONNECTED.");
                ScreenUtil.showToast(mActivity,
                        mActivity.getString(R.string.chromecast_not_connected),
                        toastTextSize, ScreenUtil.FontSize_Pixel_Type,
                        Toast.LENGTH_SHORT);
                break;
            case CastState.CONNECTING:
                Log.d(TAG, "CastState is CONNECTING.");
                ScreenUtil.showToast(mActivity,
                        mActivity.getString(R.string.chromecast_is_connecting),
                        toastTextSize, ScreenUtil.FontSize_Pixel_Type,
                        Toast.LENGTH_SHORT);
                break;
            case CastState.CONNECTED:
                Log.d(TAG, "CastState is CONNECTED.");
                ScreenUtil.showToast(mActivity,
                        mActivity.getString(R.string.chromecast_is_connected),
                        toastTextSize, ScreenUtil.FontSize_Pixel_Type,
                        Toast.LENGTH_SHORT);
                break;
            default:
                Log.d(TAG, "CastState is unknown.");
                break;
        }
        mFragment.setMediaRouteButtonVisible();
    }
}
