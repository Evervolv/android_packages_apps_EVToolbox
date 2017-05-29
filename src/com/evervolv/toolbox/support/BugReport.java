/*
 * Copyright (C) 2013-2017 The Evervolv Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.evervolv.toolbox.support;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.utils.Constants;

public class BugReport extends Fragment {

    private static final String TAG = "BugReport";

    private static final String PREF_LOGCAT_FILE = "logcat_filename_pref";
    private static final String PREF_PLAINTEXT = "logcat_upload_plaintext";
    private static final String PREF_REPORT_TYPE = "report_type";

    private static final int SYSTEM_LOG = 0;
    private static final int ALL_LOG  = 1;

    private Context mCtx;
    private SharedPreferences mPrefs;

    private TextView mPasteUrl;
    private ProgressBar mProgressBar;

    private Button mUploadButton;
    private Button mFetchButton;

    private ActionMode mActionMode = null;
    private String mLogCommand;

    private BroadcastReceiver mFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(Constants.ACTION_DUMPLOGCAT_FINISHED)) {
                    mPrefs.edit().putString(PREF_LOGCAT_FILE,
                            intent.getStringExtra(Constants.EXTRA_LOGCAT)).apply();
                    Toast.makeText(mCtx, R.string.bugreport_toast_ready, Toast.LENGTH_SHORT).show();
                    mUploadButton.setEnabled(true);
                    mFetchButton.setEnabled(true);
                    mFetchButton.setText(R.string.bugreport_fetch_button);
                    mProgressBar.setVisibility(View.GONE);
                } else if (action.equals(Constants.ACTION_UPLOAD_FINISHED)) {
                    String url = intent.getStringExtra(Constants.EXTRA_URL);
                    if (url != null) {
                        mPasteUrl.setText(url);
                        mPrefs.edit().putString(PREF_LOGCAT_FILE, null).apply();
                        mPrefs.edit().putBoolean(PREF_PLAINTEXT, false).apply();
                        mUploadButton.setEnabled(false);
                        mUploadButton.setText(R.string.bugreport_upload_button);
                        mFetchButton.setEnabled(true);
                        mProgressBar.setVisibility(View.GONE);
                    }
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCtx = getActivity();
        mPrefs = mCtx.getSharedPreferences(Constants.APP_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter f = new IntentFilter();
        f.addAction(Constants.ACTION_DUMPLOGCAT_FINISHED);
        f.addAction(Constants.ACTION_UPLOAD_FINISHED);
        mCtx.registerReceiver(mFinishedReceiver, f, Constants.PERMISSION_DUMPLOGCAT, null);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    @Override
    public void onStop() {
        mCtx.unregisterReceiver(mFinishedReceiver);
        super.onStop();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bugreport, container, false);

        Spinner mReportType = (Spinner) v.findViewById(R.id.report_type_spinner);
        mReportType.setSelection(mPrefs.getInt(PREF_REPORT_TYPE, SYSTEM_LOG));
        mReportType.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                switch (position) {
                    case SYSTEM_LOG:
                        mLogCommand = Constants.MAIN_BUFFER;
                        // Force as plaintext for now
                        mPrefs.edit().putBoolean(PREF_PLAINTEXT, true).apply();
                        break;
                    case ALL_LOG:
                        mLogCommand = Constants.ALL_LOGS_PULLED;
                        mPrefs.edit().putBoolean(PREF_PLAINTEXT, false).apply();
                        break;
                }
                mPrefs.edit().putInt(PREF_REPORT_TYPE, position).apply();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        mProgressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
        mProgressBar.setIndeterminate(true);

        mPasteUrl = (TextView) v.findViewById(R.id.generated_log);
        mPasteUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mPasteUrl.getText().equals("")) {
                    mActionMode = getActivity().startActionMode(mActionModeCallback);
                }
            }
        });

        mFetchButton = (Button) v.findViewById(R.id.button_fetch);
        mFetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String serviceCall = mLogCommand;
                mFetchButton.setText(R.string.bugreport_fetching_button);
                mFetchButton.setEnabled(false);
                mUploadButton.setEnabled(false);
                Toast.makeText(mCtx, R.string.bugreport_toast_please_wait, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Calling " + serviceCall);
                SystemProperties.set("ctl.start", serviceCall);
            }
        });

        mUploadButton = (Button) v.findViewById(R.id.button_upload);
        mUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressBar.setVisibility(View.VISIBLE);
                mUploadButton.setText(R.string.bugreport_uploading_button);
                mUploadButton.setEnabled(false);
                mFetchButton.setEnabled(false);
                Intent i = new Intent(mCtx, UploadService.class);
                i.putExtra(Constants.EXTRA_LOGCAT, mPrefs.getString(PREF_LOGCAT_FILE, null));
                i.putExtra(Constants.EXTRA_PLAINTEXT, mPrefs.getBoolean(PREF_PLAINTEXT, false));
                mCtx.startService(i);
            }
        });

        if (mPrefs.getString(PREF_LOGCAT_FILE, null) == null) {
            mUploadButton.setEnabled(false);
        }
        return v;
    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.bugreport_url_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_copy_url:
                    String url = mPasteUrl.getText().toString();
                    ClipboardManager clipboard = (ClipboardManager)
                            mCtx.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText(mCtx.getResources()
                            .getString(R.string.log_url_label), url);
                    clipboard.setPrimaryClip(clip);

                    Toast.makeText(mCtx, R.string.bugreport_toast_url_copied, Toast.LENGTH_SHORT).show();
                    mode.finish();
                    return true;
                case R.id.menu_open_url:
                    Intent i = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(mPasteUrl.getText().toString()));
                    startActivity(i);
                    mode.finish();
                    return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
        }

    };

}
