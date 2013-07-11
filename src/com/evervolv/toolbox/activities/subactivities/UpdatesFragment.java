package com.evervolv.toolbox.activities.subactivities;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceCategory;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.evervolv.toolbox.SettingsFragment;
import com.evervolv.toolbox.updates.DownloadPreference;
import com.evervolv.toolbox.updates.db.DatabaseManager;
import com.evervolv.toolbox.updates.db.DownloadEntry;
import com.evervolv.toolbox.updates.db.ManifestEntry;
import com.evervolv.toolbox.updates.misc.Constants;
import com.evervolv.toolbox.updates.services.DownloadService;
import com.evervolv.toolbox.updates.services.UpdateManifestService;
import com.evervolv.toolbox.R;

import java.io.File;
import java.util.List;

public class UpdatesFragment extends SettingsFragment {

    protected static final String TAG = Constants.TAG;

    public static final int DIALOG_MANIFEST_PROGRESS = 0;
    public static final int DIALOG_MANIFEST_ERROR    = 1;
    public static final int DIALOG_CONFIRM_CANCEL    = 2;
    public static final int DIALOG_CONFIRM_DELETE    = 3;

    public static String BASE_STORAGE_LOCATION;

    protected Context mContext;
    protected Resources mRes;
    protected PreferenceCategory mAvailableCategory;
    protected ProgressDialog mUpdateDialog;
    protected AlertDialog mAlertDialog;

    protected int mDbType;

    private DownloadManager mDownloadManager;
    private DatabaseManager mDatabaseManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mContext = getContext();
        mRes = getResources();

        BASE_STORAGE_LOCATION = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/" + Constants.DOWNLOAD_DIRECTORY;

        mDownloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);

        mDatabaseManager = new DatabaseManager(mContext);
        mDatabaseManager.open();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDatabaseManager.close();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateLayout();
        IntentFilter filter = new IntentFilter();
        filter.addAction(UpdateManifestService.ACTION_CHECK_FINISHED);
        filter.addAction(DownloadService.ACTION_UPDATE_DOWNLOAD);
        mContext.registerReceiver(mUpdateCheckReceiver, filter);
    }

    @Override
    public void onStop() {
        super.onStop();
        mContext.stopService(new Intent(mContext, DownloadService.class));
        mContext.unregisterReceiver(mUpdateCheckReceiver);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.updates_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                checkForUpdates();
                return true;
        }
        return false;
    }

    protected BroadcastReceiver mUpdateCheckReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(DownloadService.ACTION_UPDATE_DOWNLOAD)) {
                long id = intent.getLongExtra(DownloadService.EXTRA_DOWNLOAD_ID, -1);
                int status = intent.getIntExtra(DownloadService.EXTRA_DOWNLOAD_STATUS, -1);
                int progress = intent.getIntExtra(DownloadService.EXTRA_DOWNLOAD_PROGRESS, -1);

                for (int i = 0; i < mAvailableCategory.getPreferenceCount(); i++) {
                    DownloadPreference dlPref = (DownloadPreference) mAvailableCategory.getPreference(i);
                    if (dlPref.getDownloadId() == id) {
                        dlPref.setState(status, progress);
                    }
                }
            } else if (action.equals(UpdateManifestService.ACTION_CHECK_FINISHED)) {
                if (mUpdateDialog != null) {
                    if (mUpdateDialog.isShowing()) {
                        mUpdateDialog.dismiss();
                    }
                }
                if (!intent.getBooleanExtra(UpdateManifestService.EXTRA_MANIFEST_ERROR, false)) {
                    updateLayout();
                } else {
                    mAvailableCategory.removeAll();
                    showDialog(DIALOG_MANIFEST_ERROR, null);
                }
            }
        }
    };

    protected String getUpdateCheckAction() { return null; }

    protected void startUpdateManifestService(boolean scheduleUpdate) {
        Intent updateCheck = new Intent(mContext, UpdateManifestService.class);
        updateCheck.setAction(getUpdateCheckAction());
        if (scheduleUpdate) {
            updateCheck.putExtra(UpdateManifestService.EXTRA_SCHEDULE_UPDATE, true);
        }
        mContext.startService(updateCheck);
    }

    protected void checkForUpdates() {
        mAvailableCategory.removeAll();
        showDialog(DIALOG_MANIFEST_PROGRESS, null);
        startUpdateManifestService(false);

    }

    protected void updateLayout() {
        mAvailableCategory.removeAll();
        try {
            List<ManifestEntry> entries = mDatabaseManager.fetchManifest(mDbType);
            for (ManifestEntry entry : entries) {
                DownloadPreference item = new DownloadPreference(mContext, this, entry);
                item.setTitle(entry.getName());
                mAvailableCategory.addPreference(item);
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    protected String getFriendlyUpdateInterval(int interval) {
        String[] names = mRes.getStringArray(R.array.updates_schedule_entries);
        String[] values = mRes.getStringArray(R.array.updates_schedule_entry_values);
        for (int i = 0; i < values.length; i++) {
            if (Integer.valueOf(values[i]).equals(interval)) {
                return names[i];
            }
        }
        return "";
    }

    public long downloadUpdate(String url, String filename, String md5Sum) {
        DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url));
        req.setTitle(new File(filename).getName().replace(".partial", ""));
        req.setDestinationUri(Uri.parse(filename));
        req.setAllowedOverRoaming(false); //TODO: Make this a setting?
        req.setVisibleInDownloadsUi(false);
        long id = mDownloadManager.enqueue(req);
        try {
            DownloadEntry entry = new DownloadEntry();
            entry.setDownloadId(id);
            entry.setMd5sum(md5Sum);
            entry.setLocationUri(filename);
            mDatabaseManager.addDownload(entry);
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        startDownloadService(id);
        Log.i(TAG,"downloadUpdate: started download " + id);
        return id;
    }

    public void stopDownload(long downloadId) {
        if (downloadId < 0 ) {
            Log.e(TAG, "stopDownload: not a valid download " + downloadId);
            return;
        }
        Log.i(TAG, "stopDownload: dequeuing download " + downloadId);
        mDownloadManager.remove(downloadId);
        removeDownload(downloadId);
    }

    public void removeDownload(long downloadId) {
        try {
            mDatabaseManager.removeDownload(downloadId);
            Log.i(TAG, "removed download " + downloadId + " from database");
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    public long checkDownload(String md5sum) {
        long id = -1;
        try {
            id = mDatabaseManager.queryDownloads(md5sum);
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        if (id > 0) {
            Log.i(TAG, "checkDownload: found active download " + id);
        }
        return id;
    }

    public void startDownloadService(long downloadId) {
        Intent dlService = new Intent(mContext, DownloadService.class);
        dlService.putExtra(DownloadService.EXTRA_DOWNLOAD_ID, downloadId);
        mContext.startService(dlService);
    }

    public void showDialog(int id, final DownloadPreference pref) {
        switch (id) {
            case DIALOG_MANIFEST_PROGRESS:
                mUpdateDialog = new ProgressDialog(getActivity());
                mUpdateDialog.setMessage("Retrieving updates..."); //TODO resources
                mUpdateDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mUpdateDialog.show();
                break;
            case DIALOG_MANIFEST_ERROR:
                mAlertDialog = new AlertDialog.Builder(getActivity()).create();
                mAlertDialog.setTitle(R.string.alert_dialog_error_title);
                mAlertDialog.setMessage(getString(R.string
                        .alert_dialog_manifest_download_error));
                mAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                        getString(R.string.okay),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                mAlertDialog.show();
                break;
            case DIALOG_CONFIRM_CANCEL:
                mAlertDialog = new AlertDialog.Builder(getActivity()).create();
                mAlertDialog.setTitle(R.string.alert_dialog_cancel_title);
                mAlertDialog.setMessage(getString(R.string
                        .alert_dialog_confirm_cancel));
                mAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                        getString(R.string.okay),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        stopDownload(pref.getDownloadId());
                        pref.setDownloadId(-1);
                        pref.updateDownloadUI(DownloadPreference.STATE_NOTHING);
                        dialog.dismiss();
                    }
                });
                mAlertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                        getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // Do nothing
                    }
                });
                mAlertDialog.show();
                break;
            case DIALOG_CONFIRM_DELETE:
                mAlertDialog = new AlertDialog.Builder(getActivity()).create();
                mAlertDialog.setTitle(R.string.alert_dialog_delete_title);
                mAlertDialog.setMessage(getString(R.string
                        .alert_dialog_confirm_delete));
                mAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                        getString(R.string.okay),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        File downloadFile = new File(pref.getStorageLocation(),
                                pref.getFileName());
                        if (downloadFile.exists()) {
                            downloadFile.delete();
                        } else {
                            Log.e(TAG, downloadFile.getName() + ": Not found");
                        }
                        pref.updateDownloadUI(DownloadPreference.STATE_NOTHING);
                        dialog.dismiss();
                    }
                });
                mAlertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                        getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // Do nothing
                    }
                });
                mAlertDialog.show();
                break;
        }
    }
}
