package com.coal.projects.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AppInnerReceiver extends BroadcastReceiver {

    private OnReceiveCallback callback;

    public void setCallback(OnReceiveCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (callback != null)
            callback.onReceive(intent);
    }

    public interface OnReceiveCallback {
        void onReceive(Intent intent);
    }
}
