package com.patent.ftpservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class RequestStartStopReceiver extends BroadcastReceiver {

    static final String TAG = RequestStartStopReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "Received: " + intent.getAction());

        // analog code as in ServerPreferenceActivity.start/stopServer(), refactor
        try {
            if (intent.getAction().equals(FsService.ACTION_START_FTPSERVER)) {
                Intent serverService = new Intent(context, FsService.class);
                if (!FsService.isRunning()) {
                    context.startService(serverService);
                }
            } else if (intent.getAction().equals(FsService.ACTION_STOP_FTPSERVER)) {
                Intent serverService = new Intent(context, FsService.class);
                context.stopService(serverService);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to start/stop on intent " + e.getMessage());
        }
    }

}
