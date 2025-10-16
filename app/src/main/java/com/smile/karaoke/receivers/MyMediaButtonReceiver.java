package com.smile.karaoke.receivers;

import android.content.Context;
import android.content.Intent;
import androidx.media.session.MediaButtonReceiver;
import com.smile.karaoke.utilities.LogUtil;

public class MyMediaButtonReceiver extends MediaButtonReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            LogUtil.d(this.getClass().getName(), "MyMediaButtonReceiver.onReceive()");
            super.onReceive(context, intent);
        // } catch (IllegalStateException e) {
        } catch (Exception e) {
            LogUtil.e(this.getClass().getName(), "onReceive.Exception", e);
        }
    }
}
