package com.example.harish.geomindr.broadcast.message;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.widget.Toast;

public class MessageConfirmSendReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Send SMS to the specified user.
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(intent.getStringExtra("number"), null,
                intent.getStringExtra("msg"), null, null);
        // Set message sent success toast.
        Toast.makeText(context, "Message sent.", Toast.LENGTH_LONG).show();
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(3333);
    }
}