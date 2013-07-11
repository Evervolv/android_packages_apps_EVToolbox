package com.evervolv.toolbox.updates.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.evervolv.toolbox.Settings.UpdatesActivity;

public class DownloadNotificationClickReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, UpdatesActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}
