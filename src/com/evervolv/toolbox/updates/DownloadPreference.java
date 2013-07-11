package com.evervolv.toolbox.updates;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.PowerManager;
import android.preference.Preference;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.activities.subactivities.UpdatesFragment;
import com.evervolv.toolbox.updates.db.ManifestEntry;
import com.evervolv.toolbox.updates.misc.Constants;
import com.evervolv.toolbox.updates.misc.Utils;


public class DownloadPreference extends Preference implements OnClickListener {

    private static final String TAG = Constants.TAG;

    public static final int STATE_NOTHING     = 0;
    public static final int STATE_DOWNLOADING = 1;
    public static final int STATE_DOWNLOADED  = 2;

    private Context mContext;
    private UpdatesFragment mParent;
    private LinearLayout mDownloadPref;
    private ProgressBar mProgress;
    private TextView mSummary;
    private ImageView mDownloadButton;

    private ManifestEntry mEntry;
    private String mDate;
    private String mFileName;
    private String mMd5Sum;
    private String mMessage;
    private String mBuildType;
    private int    mSize;

    private int mDownloadStatus;
    private long mDownloadId = -1;
    private String mStorageDir;
    private boolean mInstalled;
    private boolean mNew;

    private File[] mGappsList;
    private int mWhichGapps;
    private CharSequence[] mZipItems;

    public DownloadPreference(Context context, UpdatesFragment parent, ManifestEntry entry) {
        super(context);
        mContext = context;
        mParent = parent;

        mEntry = entry;
        mDate = entry.getDate();
        mFileName = entry.getName();
        mMd5Sum = entry.getMd5sum();
        mMessage = entry.getMessage();
        mBuildType = entry.getType();
        mSize = entry.getSize();

        mStorageDir = UpdatesFragment.BASE_STORAGE_LOCATION + mBuildType + "/";
        mInstalled = Utils.getInstalledVersion().equals(mFileName.replace(".zip", ""));
        mNew = Utils.isNewerThanInstalled(mDate);

        setLayoutResource(R.layout.update_download);
    }

    @Override
    public void onBindView(View view) {
        super.onBindView(view);
        mProgress = (ProgressBar) view.findViewById(R.id.download_progress_bar);
        mSummary = (TextView) view.findViewById(android.R.id.summary);
        mDownloadButton = (ImageView) view.findViewById(R.id.updates_button);
        mDownloadButton.setOnClickListener(this);
        mDownloadPref = (LinearLayout) view.findViewById(R.id.updates_pref);
        mDownloadPref.setOnClickListener(this);

        File nightly = new File(mStorageDir + mFileName);
        File nightlyPartial = new File(mStorageDir + mFileName + ".partial");

        if (nightly.exists()) {
            updateDownloadUI(STATE_DOWNLOADED);
        } else if (nightlyPartial.exists()) {
            mDownloadId = mParent.checkDownload(mMd5Sum);
            if ( mDownloadId > 0 ) {
                mParent.startDownloadService(mDownloadId);
                updateDownloadUI(STATE_DOWNLOADING);
            } else {
                /* File exists but not being tracked
                   just delete it and start over */
                nightlyPartial.delete();
                updateDownloadUI(STATE_NOTHING);
            }
        } else {
            updateDownloadUI(STATE_NOTHING);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mDownloadButton) {
            switch (mDownloadStatus) {
                case STATE_NOTHING:
                    String url = Constants.FETCH_URL + mFileName;
                    String fullFilename = "file://" + mStorageDir + mFileName + ".partial";
                    File downloadDir = new File(mStorageDir);
                    if (!downloadDir.exists()) {
                        downloadDir.mkdirs();
                    }
                    mDownloadId = mParent.downloadUpdate(url, fullFilename, mMd5Sum);
                    updateDownloadUI(STATE_DOWNLOADING);
                    break;
                case STATE_DOWNLOADING:
                    mParent.showDialog(UpdatesFragment.DIALOG_CONFIRM_CANCEL, this);
                    break;
                case STATE_DOWNLOADED:
                    mParent.showDialog(UpdatesFragment.DIALOG_CONFIRM_DELETE, this);
                    break;
            }
        } else if (v == mDownloadPref) {
            ChangelogInfoDialog dlg = new ChangelogInfoDialog(mEntry, this);
            dlg.show(mParent.getChildFragmentManager(), mMd5Sum);
        }
    }

    public void updateDownloadUI(int state) {
        mDownloadStatus = state;
        switch (state) {
            case STATE_NOTHING:
                mDownloadButton.setImageResource(R.drawable.ic_pref_download);
                mProgress.setVisibility(View.GONE);
                if (mInstalled) {
                    mSummary.setText(R.string.status_installed);
                    //mSummary.setTextColor(Color.WHITE);
                    mSummary.setVisibility(View.VISIBLE);
                } else if (mNew) {
                    mSummary.setText(R.string.status_new);
                    mSummary.setVisibility(View.VISIBLE);
                } else {
                    mSummary.setVisibility(View.GONE);
                }
                break;
            case STATE_DOWNLOADING:
                mDownloadButton.setImageResource(R.drawable.ic_pref_cancel);
                mProgress.setVisibility(View.VISIBLE);
                mSummary.setVisibility(View.GONE);
                break;
            case STATE_DOWNLOADED:
                mDownloadButton.setImageResource(R.drawable.ic_pref_delete);
                mProgress.setVisibility(View.GONE);
                if (mInstalled) {
                    mSummary.setText(R.string.status_downloaded_and_installed);
                } else {
                    mSummary.setText(R.string.status_downloaded);
                }
                //mSummary.setTextColor(Color.GREEN);
                mSummary.setVisibility(View.VISIBLE);
                break;
        }
    }

    /* TODO FEATURE:
     * Turn this into a dialog or activity to get a build "ready" to flash, including other zips to flash
     * and options while flashing ( wiping, etc ).
     */
    public void getReadyToFlash() {
        int pickedId;
        mGappsList = Utils.getFilesInDir(UpdatesFragment.BASE_STORAGE_LOCATION + "/gapps/", ".zip");
        mZipItems = new CharSequence[mGappsList.length + 1];
        mZipItems[0] = "None"; // Hack
        int i = 1;
        for (File zip : mGappsList) {
            mZipItems[i] = zip.getName();
            i++;
        }
        Resources res = mContext.getResources();
        AlertDialog.Builder flashDialog = new AlertDialog.Builder(mParent.getActivity());
        flashDialog.setTitle(R.string.alert_dialag_gapps_title);
        flashDialog.setSingleChoiceItems(mZipItems, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mWhichGapps = which;
            }
        });

        flashDialog.setPositiveButton(R.string.reboot,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //buildRecoveryScript(zipItems[mWhichGapps].toString());
                tempTwrpDialog(); //TODO: Temporary dialog warning for TWRP support only
                dialog.dismiss();
            }
        });
        flashDialog.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        flashDialog.show();
    }

    /* TODO: Temporary dialog warning for TWRP support only,
     * remove when necessary
     */
    private void tempTwrpDialog() {
        AlertDialog.Builder twrpDialog = new AlertDialog.Builder(mParent.getActivity());
        twrpDialog.setTitle(R.string.alert_dialag_warning_title);
        twrpDialog.setMessage(R.string.alert_dialag_warning_message);

        twrpDialog.setPositiveButton(R.string.okay,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                buildRecoveryScript(mZipItems[mWhichGapps].toString());
                dialog.dismiss();
            }
        });
        twrpDialog.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        twrpDialog.show();
    }

    /*
     * TODO: We should create an OpenRecoveryScript class to house all or most of this.
     */
    private void buildRecoveryScript(String gappsZip) {
        try {
            Process p = Runtime.getRuntime().exec("sh");
            OutputStream o = p.getOutputStream();
            o.write("mkdir -p /cache/recovery/\n".getBytes());
            o.write("echo -n > /cache/recovery/openrecoveryscript\n".getBytes());
            if (false) { //TODO prompt
                o.write("echo 'backup SDBO' >> /cache/recovery/openrecoveryscript\n".getBytes());
            }
            /* Using local path should prevent fuckups from different recovery mount points */
            o.write(String.format("echo 'install %s' >> %s\n",
                    Constants.DOWNLOAD_DIRECTORY + mBuildType + "/" + mFileName,
                    "/cache/recovery/openrecoveryscript").getBytes());
            if (!gappsZip.equals("None")) { // Hack
                o.write(String.format("echo 'install %s' >> %s\n",
                        Constants.DOWNLOAD_DIRECTORY + "gapps/" + gappsZip,
                        "/cache/recovery/openrecoveryscript").getBytes());
            }
            /* TODO FEATURE:
             * Add cache / dalvik cache wiping options 
             */
            o.flush();
            PowerManager pm = (PowerManager) mParent.getContext().getSystemService(Context.POWER_SERVICE);
            pm.reboot("recovery");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /* TODO FEATURE:
     * Use mSummary to show download percentage
     */
    public void setState(int state, int progress) {
        switch (state) {
            case DownloadManager.STATUS_RUNNING:
                if (mDownloadStatus != STATE_DOWNLOADING) {
                    updateDownloadUI(STATE_DOWNLOADING);
                }
                mProgress.setIndeterminate(progress <= 0);
                mProgress.setProgress(progress);
                break;
            case DownloadManager.STATUS_PENDING:
            case DownloadManager.STATUS_PAUSED:
                // Blocked in service, leaving for possible future implementation
                break;
            case DownloadManager.STATUS_SUCCESSFUL:
                if (mDownloadStatus != STATE_DOWNLOADED) {
                    updateDownloadUI(STATE_DOWNLOADED);
                }
                mDownloadId = -1;
                break;
            case DownloadManager.STATUS_FAILED:
            default:
                if (mDownloadStatus != STATE_NOTHING) {
                    updateDownloadUI(STATE_NOTHING);
                }
                mDownloadId = -1;
                break;
        }
    }

    public void setDate(String date) {
        mDate = date;
    }

    public String getDate() {
        return mDate;
    }

    public void setFileName(String name) {
        mFileName = name;
    }

    public String getFileName() {
        return mFileName;
    }

    public void setMd5Sum(String sum) {
        mMd5Sum = sum;
    }

    public String getMd5Sum() {
        return mMd5Sum;
    }

    public void setMessage(String msg) {
        mMessage = msg;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setBuildType(String type) {
        mBuildType = type;
    }

    public String getBuildType() {
        return mBuildType;
    }

    public void setSize(int size) {
        mSize = size;
    }

    public int getSize() {
        return mSize;
    }

    public void setDownloadId(long id) {
        mDownloadId = id;
    }

    public long getDownloadId() {
        return mDownloadId;
    }

    public String getStorageLocation() {
        return mStorageDir;
    }

}
