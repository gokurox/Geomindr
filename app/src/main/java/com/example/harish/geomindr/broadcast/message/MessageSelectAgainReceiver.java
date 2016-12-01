package com.example.harish.geomindr.broadcast.message;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MessageSelectAgainReceiver extends BroadcastReceiver {
    public MessageSelectAgainReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Please select either YES or NO.", Toast.LENGTH_SHORT).show();
    }
}