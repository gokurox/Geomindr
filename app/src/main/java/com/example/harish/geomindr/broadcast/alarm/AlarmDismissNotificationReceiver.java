package com.example.harish.geomindr.broadcast.alarm;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

// Broadcast Receiver for various notification events.
public class AlarmDismissNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Alarm dismissed.", Toast.LENGTH_SHORT).show();
        // Dismiss the notification.
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(2222);
    }
}