package com.evervolv.toolbox.updates.receivers;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.evervolv.toolbox.updates.db.DatabaseManager;
import com.evervolv.toolbox.updates.services.DownloadService;

public class DownloadCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isUpdate;
        long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        DatabaseManager m = new DatabaseManager(context);
        m.open();
        isUpdate = m.queryDownloads(downloadId);
        m.close();

        if (isUpdate) {
            Intent dlService = new Intent(context, DownloadService.class);
            dlService.putExtra(DownloadService.EXTRA_DOWNLOAD_ID, downloadId);
            context.startService(dlService);
        }

    }

}
