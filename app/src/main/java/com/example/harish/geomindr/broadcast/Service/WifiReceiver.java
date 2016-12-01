package com.example.harish.geomindr.broadcast.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.example.harish.geomindr.service.main.ReminderService;

public class WifiReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        final android.net.NetworkInfo wifi = connMgr
                .getActiveNetworkInfo();

        if (wifi != null) {
            ReminderService.stopService = false;
            context.startService(new Intent(context, ReminderService.class));
        }
        else {
            ReminderService.stopService = true;
            context.stopService(new Intent(context, ReminderService.class));
        }
    }
}
