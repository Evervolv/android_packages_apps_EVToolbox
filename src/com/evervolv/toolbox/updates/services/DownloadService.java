package com.evervolv.toolbox.updates.services;

import android.app.DownloadManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.util.Log;

import com.evervolv.toolbox.updates.db.DatabaseManager;
import com.evervolv.toolbox.updates.misc.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadService extends Service {

    private static final String TAG = Constants.TAG;

    public static final String ACTION_UPDATE_DOWNLOAD = "com.evervolv.toolbox.actions.ACTION_UPDATE_DOWNLOAD";

    public static final String EXTRA_DOWNLOAD_ID = "download_id";
    public static final String EXTRA_DOWNLOAD_STATUS = "download_status";
    public static final String EXTRA_DOWNLOAD_PROGRESS = "download_progress";

    List<Download> mDownloads = new ArrayList<Download>();
    private boolean mDestroyed = false;

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDestroyed = true; /* Tell our threads to die */
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        long downloadId = intent.getLongExtra(EXTRA_DOWNLOAD_ID, -1);
        if (downloadId > 0) {
            for (Download dl : mDownloads) {
                if (downloadId == dl.id) {
                    /* We are already running for this download... do nothing */
                    Log.i(TAG, "DownloadService: already tracking download " + downloadId);
                    return START_NOT_STICKY;
                }
            }
            mDownloads.add(new Download(downloadId));
            /* Start new thread for each download */
            ProgressThread p = new ProgressThread(downloadId);
            p.start();
        }
        return START_NOT_STICKY; /* We handle restarting */
    }

    private class ProgressThread extends Thread {

        private long id;

        public ProgressThread(long id) {
            super("EVUpdates Download Service");
            this.id = id;
        }

        @Override
        public void run() {
            Log.i(TAG, "ProgressThread: download " + id + " thread starting");
            boolean end = false;
            boolean deferUpdate;
            long completed;
            long total;
            int status;
            int progress;
            int previousProgress = -1;

            DatabaseManager databaseManager = new DatabaseManager(getApplicationContext());
            databaseManager.open();

            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Query q = new DownloadManager.Query();
            q.setFilterById(id);

            while (!mDestroyed) {
                if (end) break;
                Cursor c = downloadManager.query(q);
                if (!c.moveToFirst()) { c.close(); break; }
                /* Reset all our intent variables */
                deferUpdate = false;
                progress = -1;
                status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                switch (status) {
                    case DownloadManager.STATUS_RUNNING:
                        completed = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                        total = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                        progress = (int) ((completed * 100) / total);
                        if (progress == previousProgress) {
                            deferUpdate = true;
                            break;
                        }
                        previousProgress = progress;
                        Log.d(TAG, "ProgressThread: download " + id + " at " + progress + "%");
                        break;
                    case DownloadManager.STATUS_PENDING:
                    case DownloadManager.STATUS_PAUSED:
                        Log.d(TAG, "ProgressThread: download " + id + " paused/pending");
                        deferUpdate = true;
                        break;
                    case DownloadManager.STATUS_SUCCESSFUL:
                        String file = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                        File partialFile = new File(file);
                        File newFile = new File(file.replace(".partial", ""));
                        partialFile.renameTo(newFile);
                        databaseManager.removeDownload(id);
                        end = true;
                        Log.d(TAG, "ProgressThread: download " + id + " completed successfully");
                        break;
                    case DownloadManager.STATUS_FAILED:
                    default: /* We don't want this running forever if something goes wrong */
                        Log.d(TAG, "ProgressThread: download " + id + " failed");
                        databaseManager.removeDownload(id);
                        end = true;
                        break;
                }
                c.close();
                /* build the broadcast */
                if (!deferUpdate) {
                    Intent dlIntent = new Intent();
                    dlIntent.setAction(ACTION_UPDATE_DOWNLOAD);
                    dlIntent.putExtra(EXTRA_DOWNLOAD_ID, id);
                    dlIntent.putExtra(EXTRA_DOWNLOAD_STATUS, status);
                    dlIntent.putExtra(EXTRA_DOWNLOAD_PROGRESS, progress);
                    sendBroadcast(dlIntent);
                }
                if (mDestroyed) break;
                if (end) break;
                synchronized (this) {
                    try {
                        wait(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            databaseManager.close();
            Log.i(TAG, "ProgressThread: download " + id + " thread ending");
        }
    }

    private class Download {
        public long id;
        public Download(long id) {
            this.id  = id;
        }
    }

}
