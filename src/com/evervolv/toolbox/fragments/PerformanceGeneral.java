/*
 * Copyright (C) 2013 The Evervolv Project
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

package com.evervolv.toolbox.fragments;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.preference.SwitchPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.Toolbox;
import com.evervolv.toolbox.utils.FileUtil;

public class PerformanceGeneral extends PreferenceFragment implements
        OnPreferenceChangeListener,
        Toolbox.DisabledListener {
    private static final String TAG = "PerformanceGeneral";

    public static final String KSM_RUN_FILE = "/sys/kernel/mm/ksm/run";
    public static final String KSM_PREF = "pref_ksm";
    public static final String KSM_PREF_DISABLED = "0";
    public static final String KSM_PREF_ENABLED = "1";

    public static final String ZRAM_PREF = "pref_zram_size";
    public static final String ZRAM_FSTAB_FILENAME = "/data/system/fstab.zram";
    private static final String ZRAM_FSTAB_ENTRY =
            "/dev/block/zram0 none swap defaults zramsize=";

    public static final String KEY_PERF_PROFILE = "pref_perf_profile";

    private SwitchPreference mKSMPref;
    private ListPreference mzRAM;
    private PreferenceScreen mPrefSet;

    private PowerManager mPowerManager;
    private ListPreference mPerfProfilePref;
    private String[] mPerfProfileEntries;
    private String[] mPerfProfileValues;
    private String mPerfProfileDefaultEntry;
    private PerformanceProfileObserver mPerformanceProfileObserver = null;

    private class PerformanceProfileObserver extends ContentObserver {
        public PerformanceProfileObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            updatePerformanceValue();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mPowerManager = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.performance_general);

        mPerfProfileEntries = getResources().getStringArray(
                com.android.internal.R.array.perf_profile_entries);

        mPerfProfileValues = getResources().getStringArray(
                com.android.internal.R.array.perf_profile_values);

        mPrefSet = getPreferenceScreen();

        /* Power Profiles */
        mPerfProfilePref = (ListPreference) mPrefSet.findPreference(KEY_PERF_PROFILE);
        if (mPerfProfilePref != null && !mPowerManager.hasPowerProfiles()) {
            mPrefSet.removePreference(mPerfProfilePref);
            mPerfProfilePref = null;
        } else if (mPerfProfilePref != null) {
            mPerfProfilePref.setOrder(-1);
            mPerfProfilePref.setEntries(mPerfProfileEntries);
            mPerfProfilePref.setEntryValues(mPerfProfileValues);
            updatePerformanceValue();
            mPerfProfilePref.setOnPreferenceChangeListener(this);
        }
        mPerformanceProfileObserver = new PerformanceProfileObserver(new Handler());

        /* KSM */

        mKSMPref = (SwitchPreference) mPrefSet.findPreference(KSM_PREF);
        if (FileUtil.fileExists(KSM_RUN_FILE)) {
            mKSMPref.setChecked(KSM_PREF_ENABLED.equals(FileUtil.fileReadOneLine(KSM_RUN_FILE)));
        } else {
            mPrefSet.removePreference(mKSMPref);
        }

        /* Zram */

        mzRAM = (ListPreference) mPrefSet.findPreference(ZRAM_PREF);
        if (isZramAvailable()) {
            mzRAM.setOnPreferenceChangeListener(this);
        } else {
            mPrefSet.removePreference(mzRAM);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        mPrefSet.setEnabled(Toolbox.isEnabled(getActivity()));
        ((Toolbox) getActivity()).registerCallback(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPerfProfilePref != null) {
            updatePerformanceValue();
            ContentResolver resolver = getActivity().getContentResolver();
            resolver.registerContentObserver(Settings.Secure.getUriFor(
                    Settings.Secure.PERFORMANCE_PROFILE), false, mPerformanceProfileObserver);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPerfProfilePref != null) {
            ContentResolver resolver = getActivity().getContentResolver();
            resolver.unregisterContentObserver(mPerformanceProfileObserver);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        ((Toolbox) getActivity()).unRegisterCallback(this);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mKSMPref) {
            FileUtil.fileWriteOneLine(KSM_RUN_FILE, mKSMPref.isChecked() ? "1" : "0");
            return true;
        }
        return false;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (newValue != null) {
            if (preference == mPerfProfilePref) {
                mPowerManager.setPowerProfile(String.valueOf(newValue));
                updatePerformanceSummary();
                return true;
            } else if (preference == mzRAM) {
                String percent = (String) newValue;
                if (!"0".equals(percent)) {
                    FileUtil.fileWriteOneLine(ZRAM_FSTAB_FILENAME,
                            ZRAM_FSTAB_ENTRY + getZramBytes(percent) + "\n");
                    new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.reboot_required)
                        .setMessage(getString(R.string.pref_zram_reboot_text))
                        .setPositiveButton(getString(R.string.reboot),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    PowerManager power = (PowerManager) getActivity()
                                            .getSystemService(Context.POWER_SERVICE);
                                    power.reboot("UI Change");
                                }
                            })
                        .setNegativeButton(getString(R.string.later),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                        .show();
                } else {
                    FileUtil.fileDelete(ZRAM_FSTAB_FILENAME);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Check if swap support is available on the system
     */
    private static boolean isSwapAvailable() {
        return FileUtil.fileExists("/proc/swaps");
    }

    public static boolean isZramAvailable() {
        return isSwapAvailable() && FileUtil.fileExists("/sys/block/zram0/disksize");
    }

    private int getZramBytes(String percent) {
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) getActivity().getSystemService(Activity.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(memoryInfo);
        long totalMem = memoryInfo.totalMem;
        int zramBytes = (int) (totalMem * Long.valueOf(percent) / 100L);
        Log.i(TAG, "totalMem=" + totalMem + " zramMem=" + zramBytes + " " + percent + "%");
        return zramBytes;
    }

    private void updatePerformanceSummary() {
        String value = mPowerManager.getPowerProfile();
        String summary = "";
        int count = mPerfProfileValues.length;
        for (int i = 0; i < count; i++) {
            try {
                if (mPerfProfileValues[i].equals(value)) {
                    summary = mPerfProfileEntries[i];
                }
            } catch (IndexOutOfBoundsException ex) {
                // Ignore
            }
        }
        mPerfProfilePref.setSummary(String.format("%s", summary));
    }

    private void updatePerformanceValue() {
        if (mPerfProfilePref == null) {
            return;
        }
        mPerfProfilePref.setValue(mPowerManager.getPowerProfile());
        updatePerformanceSummary();
    }

    @Override
    public void onToolboxDisabled(boolean enabled) {
        mPrefSet.setEnabled(enabled);
    }

}
