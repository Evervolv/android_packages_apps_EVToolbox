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

package com.evervolv.toolbox.perf;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.os.SystemService;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.util.Log;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.Toolbox;
import com.evervolv.toolbox.ToolboxPreferenceFragment;
import com.evervolv.toolbox.utils.FileUtil;

import java.lang.Runtime;

public class KernelTuner extends ToolboxPreferenceFragment implements
        OnPreferenceChangeListener, Toolbox.DisabledListener {
    private static final String TAG = "EVToolbox";

    public static boolean freqCapFilesInitialized = false;
    public static final String CPU_ONLINE = "/sys/devices/system/cpu/cpu0/online";
    public static final String SCALE_CUR_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";
    public static final String FREQINFO_CUR_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_cur_freq";
    public static final String GOV_LIST_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors";
    public static final String GOV_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
    public static final String FREQ_LIST_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
    public static String FREQ_MAX_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
    public static String FREQ_MIN_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq";
    private static String FREQ_CUR_FILE = SCALE_CUR_FILE;
    public static final String IOSCHED_PREF = "pref_cpu_io_sched";
    public static final String GOV_PREF = "pref_cpu_gov";
    public static final String FREQ_CUR_PREF = "pref_cpu_freq_cur";
    public static final String FREQ_MIN_PREF = "pref_cpu_freq_min";
    public static final String FREQ_MAX_PREF = "pref_cpu_freq_max";
    public static final String SOB_PREF = "pref_cpu_set_on_boot";

    public static final String KSM_RUN_FILE = "/sys/kernel/mm/ksm/run";
    public static final String KSM_PREF = "pref_ksm";
    public static final String KSM_PREF_DISABLED = "0";
    public static final String KSM_PREF_ENABLED = "1";

    public static final String KEY_PERF_PROFILE = "pref_perf_profile";

    private SwitchPreference mKSMPref;

    private PreferenceScreen mPrefSet;

    private static final int UI_UPDATE_DELAY = 500;

    private Preference mCurFrequencyPref;
    private ListPreference mIOSchedulerPref;
    private ListPreference mGovernorPref;
    private ListPreference mMinFrequencyPref;
    private ListPreference mMaxFrequencyPref;

    private String mIOSchedulerFormat;
    private String mGovernorFormat;
    private String mMinFrequencyFormat;
    private String mMaxFrequencyFormat;

    private HandlerThread mCpuInfoThread;
    private Handler mCpuInfoHandler;
    private CpuUiUpdate mCpuUiUpdate;
    private static String mIoListFile;

    private SharedPreferences mSharedPreferences;

    private SwitchPreference mSetOnBootPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.perf_kernel);

        mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity().getApplicationContext());

        mPrefSet = getPreferenceScreen();

        /* Set on boot */
        mSetOnBootPref = (SwitchPreference) mPrefSet.findPreference(SOB_PREF);
        boolean mSetOnBootEnabled = mSharedPreferences.getBoolean(SOB_PREF, false);
        mSetOnBootPref.setChecked(mSetOnBootEnabled);

        /* KSM */
        mKSMPref = (SwitchPreference) mPrefSet.findPreference(KSM_PREF);
        if (FileUtil.fileExists(KSM_RUN_FILE)) {
            mKSMPref.setChecked(KSM_PREF_ENABLED.equals(FileUtil.fileReadOneLine(KSM_RUN_FILE)));
        } else {
            mPrefSet.removePreference(mKSMPref);
        }

        mIoListFile = findIoScheduler();

        /* CPU Tunables */
        createCpuTuningPrefs();
    }

    @Override
    public void onResume() {
        String availableIOSchedulersLine;
        int bropen, brclose;
        String currentIOScheduler;
        super.onResume();

        if (mCpuInfoHandler != null) {
            mCpuInfoHandler.post(mUpdateCpuFreqValues);
        }

        if (FileUtil.fileExists(mIoListFile) &&
            (availableIOSchedulersLine = FileUtil.fileReadOneLine(mIoListFile)) != null) {
            bropen = availableIOSchedulersLine.indexOf("[");
            brclose = availableIOSchedulersLine.lastIndexOf("]");
            if (bropen >= 0 && brclose >= 0) {
                currentIOScheduler = availableIOSchedulersLine.substring(bropen + 1, brclose);
                mIOSchedulerPref.setSummary(String.format(mIOSchedulerFormat, currentIOScheduler));
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCpuInfoHandler != null) {
            mCpuInfoHandler.removeCallbacks(mUpdateCpuFreqValues);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCpuInfoThread != null) {
            mCpuInfoThread.quit();
        }
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mKSMPref) {
            FileUtil.fileWriteOneLine(KSM_RUN_FILE, mKSMPref.isChecked() ? "1" : "0");
            return true;
        }
        if (preference == mSetOnBootPref) {
            mSharedPreferences.edit().putBoolean(SOB_PREF, mSetOnBootPref.isChecked()).apply();
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (newValue != null) {
            if (preference == mIOSchedulerPref) {
                updateCpuTunables(mIoListFile, (String) newValue);
                mIOSchedulerPref.setSummary(String.format(mIOSchedulerFormat, (String) newValue));
                return true;
            }
            if (preference == mGovernorPref) {
                updateCpuTunables(GOV_FILE, (String) newValue);
                mGovernorPref.setSummary(String.format(mGovernorFormat, (String) newValue));
                return true;
            }
            if (preference == mMinFrequencyPref) {
                updateCpuTunables(FREQ_MIN_FILE, (String) newValue);
                mMinFrequencyPref.setSummary(String.format(mMinFrequencyFormat,
                        toMHz((String) newValue)));
                return true;
            }
            if (preference == mMaxFrequencyPref) {
                updateCpuTunables(FREQ_MAX_FILE, (String) newValue);
                mMaxFrequencyPref.setSummary(String.format(mMaxFrequencyFormat,
                        toMHz((String) newValue)));
                return true;
            }
        }
        return false;
    }

    public static String findIoScheduler() {
        String validChoices[] = {
            "/sys/block/mmcblk0/queue/scheduler",
            "/sys/block/sda/queue/scheduler",
            "/sys/block/sde/queue/scheduler",
            "/sys/block/dm-0/queue/scheduler"
        };
        for (String choice: validChoices) {
            if (FileUtil.fileExists(choice)) {
                return choice;
            }
        }
        return null;
    }

    private void createCpuTuningPrefs() {
        mIOSchedulerFormat = getString(R.string.io_sched_summary);
        mGovernorFormat = getString(R.string.cpu_governors_summary);
        mMinFrequencyFormat = getString(R.string.cpu_min_freq_summary);
        mMaxFrequencyFormat = getString(R.string.cpu_max_freq_summary);

        String[] availableIOSchedulers = new String[0];
        String availableIOSchedulersLine;
        int bropen, brclose;
        String currentIOScheduler = null;

        String[] availableFrequencies = new String[0];
        String[] availableGovernors = new String[0];
        String[] frequencies;
        String availableGovernorsLine;
        String availableFrequenciesLine;
        String temp;

        mIOSchedulerPref = (ListPreference) mPrefSet.findPreference(IOSCHED_PREF);
        mGovernorPref = (ListPreference) mPrefSet.findPreference(GOV_PREF);
        mCurFrequencyPref = (Preference) mPrefSet.findPreference(FREQ_CUR_PREF);
        mMinFrequencyPref = (ListPreference) mPrefSet.findPreference(FREQ_MIN_PREF);
        mMaxFrequencyPref = (ListPreference) mPrefSet.findPreference(FREQ_MAX_PREF);

        /* I/O scheduler
        Some systems might not use I/O schedulers */
        if (!FileUtil.fileExists(mIoListFile) ||
            (availableIOSchedulersLine = FileUtil.fileReadOneLine(mIoListFile)) == null) {
            mPrefSet.removePreference(mIOSchedulerPref);
        } else {
            availableIOSchedulers = availableIOSchedulersLine.replace("[", "").replace("]", "").split(" ");
            bropen = availableIOSchedulersLine.indexOf("[");
            brclose = availableIOSchedulersLine.lastIndexOf("]");
            if (bropen >= 0 && brclose >= 0)
                currentIOScheduler = availableIOSchedulersLine.substring(bropen + 1, brclose);

            mIOSchedulerPref.setEntryValues(availableIOSchedulers);
            mIOSchedulerPref.setEntries(availableIOSchedulers);
            if (currentIOScheduler != null)
                mIOSchedulerPref.setValue(currentIOScheduler);
            mIOSchedulerPref.setSummary(String.format(mIOSchedulerFormat, currentIOScheduler));
            mIOSchedulerPref.setOnPreferenceChangeListener(this);
        }

        /* Governor
        Some systems might not use governors */
        if (!FileUtil.fileExists(GOV_LIST_FILE) || !FileUtil.fileExists(GOV_FILE) ||
                (temp = FileUtil.fileReadOneLine(GOV_FILE)) == null || (availableGovernorsLine = FileUtil.fileReadOneLine(GOV_LIST_FILE)) == null) {
            mPrefSet.removePreference(mGovernorPref);

        } else {
            availableGovernors = availableGovernorsLine.split(" ");

            mGovernorPref.setEntryValues(availableGovernors);
            mGovernorPref.setEntries(availableGovernors);
            mGovernorPref.setValue(temp);
            mGovernorPref.setSummary(String.format(mGovernorFormat, temp));
            mGovernorPref.setOnPreferenceChangeListener(this);
        }

        // Disable the min/max list if we dont have a list file
        if (!FileUtil.fileExists(FREQ_LIST_FILE) || (availableFrequenciesLine = FileUtil.fileReadOneLine(FREQ_LIST_FILE)) == null) {
            mMinFrequencyPref.setEnabled(false);
            mMaxFrequencyPref.setEnabled(false);

        } else {
            availableFrequencies = availableFrequenciesLine.split(" ");

            frequencies = new String[availableFrequencies.length];
            for (int i = 0; i < frequencies.length; i++) {
                frequencies[i] = toMHz(availableFrequencies[i]);
            }

            // Min frequency
            if (!FileUtil.fileExists(FREQ_MIN_FILE) || (temp = FileUtil.fileReadOneLine(FREQ_MIN_FILE)) == null) {
                mMinFrequencyPref.setEnabled(false);

            } else {
                mMinFrequencyPref.setEntryValues(availableFrequencies);
                mMinFrequencyPref.setEntries(frequencies);
                mMinFrequencyPref.setValue(temp);
                mMinFrequencyPref.setSummary(String.format(mMinFrequencyFormat, toMHz(temp)));
                mMinFrequencyPref.setOnPreferenceChangeListener(this);
            }

            // Max frequency
            if (!FileUtil.fileExists(FREQ_MAX_FILE) || (temp = FileUtil.fileReadOneLine(FREQ_MAX_FILE)) == null) {
                mMaxFrequencyPref.setEnabled(false);

            } else {
                mMaxFrequencyPref.setEntryValues(availableFrequencies);
                mMaxFrequencyPref.setEntries(frequencies);
                mMaxFrequencyPref.setValue(temp);
                mMaxFrequencyPref.setSummary(String.format(mMaxFrequencyFormat, toMHz(temp)));
                mMaxFrequencyPref.setOnPreferenceChangeListener(this);
            }
        }

        // Cur frequency
        if (!FileUtil.fileExists(FREQ_CUR_FILE)) {
            FREQ_CUR_FILE = FREQINFO_CUR_FILE;
        }

        if (!FileUtil.fileExists(FREQ_CUR_FILE) || (temp = FileUtil.fileReadOneLine(FREQ_CUR_FILE)) == null) {
            mCurFrequencyPref.setEnabled(false);
        } else {
            mCurFrequencyPref.setSummary(toMHz(temp));

            mCpuInfoThread = new HandlerThread("CPUInfoThread");
            mCpuInfoThread.start();
            mCpuInfoHandler = new Handler(mCpuInfoThread.getLooper());
            mCpuUiUpdate = new CpuUiUpdate();
        }
    }

    private class CpuUiUpdate implements Runnable {
        public String currentFrequency;
        public String maxFrequency;
        public String minFrequency;
        public String currentGovernor;

        public void run() {
            if (currentFrequency != null) {
                mCurFrequencyPref.setSummary(toMHz(currentFrequency));
            }
            if (maxFrequency != null) {
                mMaxFrequencyPref.setValue(maxFrequency);
                mMaxFrequencyPref.setSummary(String.format(mMaxFrequencyFormat,
                    toMHz(maxFrequency)));
            }
            if (minFrequency != null) {
                mMinFrequencyPref.setValue(minFrequency);
                mMinFrequencyPref.setSummary(String.format(mMinFrequencyFormat,
                    toMHz(minFrequency)));
            }
            if (currentGovernor != null) {
                mGovernorPref.setSummary(String.format(mGovernorFormat, currentGovernor));
            }
        }
    };

    private Runnable mUpdateCpuFreqValues = new Runnable() {

        @Override
        public void run() {
            if (FileUtil.fileExists(FREQ_CUR_FILE)) {
                mCpuUiUpdate.currentFrequency = FileUtil.fileReadOneLine(FREQ_CUR_FILE);
            }

            if (FileUtil.fileExists(FREQ_MIN_FILE)) {
                mCpuUiUpdate.minFrequency = FileUtil.fileReadOneLine(FREQ_MIN_FILE);
            }

            if (FileUtil.fileExists(FREQ_MAX_FILE)) {
                mCpuUiUpdate.maxFrequency = FileUtil.fileReadOneLine(FREQ_MAX_FILE);
            }

            if (FileUtil.fileExists(GOV_FILE)) {
                mCpuUiUpdate.currentGovernor = FileUtil.fileReadOneLine(GOV_FILE);
            }

            getActivity().runOnUiThread(mCpuUiUpdate);
            mCpuInfoHandler.postDelayed(mUpdateCpuFreqValues, UI_UPDATE_DELAY);
        }
    };

    private void updateCpuTunables(String fname, String value) {
        if (FileUtil.fileWriteOneLine(fname, value)) {
            final int nrcpus = Runtime.getRuntime().availableProcessors();
            if (nrcpus > 1) {
                new Thread() {
                    public void run() {
                        int count = 0;
                        int maxcount = 5;
                        String on = "1";
                        String off = "0";
                        String onfile = "";
                        String cpufile = "";
                        String savedstate = "";
                        String state = "";
                        String mpdec = "mpdecision";
                        SystemService.State mpdecstate = SystemService.getState(mpdec);
                        // Dumb down to a running mpdecision service
                        if (mpdecstate.equals(SystemService.State.RUNNING)) {
                            SystemService.stop(mpdec);
                        }
                        try {
                            for (int i = 1; i < nrcpus; i++) {
                                onfile = CPU_ONLINE.replace("cpu0", "cpu" + i);
                                cpufile = fname.replace("cpu0", "cpu" + i);
                                savedstate = FileUtil.fileReadOneLine(onfile);
                                // Writing on to already online cpu throws EINVAL exception
                                if (savedstate.equals(off)) {
                                    if (FileUtil.fileIsWritable(onfile)) {
                                        FileUtil.fileWriteOneLine(onfile, on);
                                    } else {
                                        String hw = SystemProperties.get("ro.hardware");
                                        Log.e(TAG, onfile +
                                        " not writable, did you set ownership in init." +
                                        hw + ".rc?");
                                    }
                                }
                                // Give ueventd a little time to set perms
                                while (count < maxcount) {
                                    Thread.sleep(10);
                                    if (FileUtil.fileExists(cpufile)) {
                                        if (FileUtil.fileIsWritable(cpufile)) {
                                            FileUtil.fileWriteOneLine(cpufile, value);
                                            break;
                                        } else {
                                            Log.e(TAG, cpufile +
                                            " not writable, did you set ueventd rules?");
                                        }
                                    }
                                    count++;
                                    if (count == maxcount) {
                                        Log.e(TAG, "Failed setting new value to " + cpufile);
                                    }
                                }
                                count = 0;
                                state = FileUtil.fileReadOneLine(onfile);
                                // Restore prior state of onlined cpu
                                if (state.equals(on) && !state.equals(savedstate)) {
                                    FileUtil.fileWriteOneLine(onfile, off);
                                }
                            }
                        } catch (InterruptedException e) {
                        }
                        // Restart mpdec
                        if (mpdecstate.equals(SystemService.State.RUNNING)) {
                            SystemService.start(mpdec);
                        }
                    }
                }.start();
            }
        }
    }

    private String toMHz(String mhzString) {
        return new StringBuilder().append(Integer.valueOf(mhzString) / 1000).append(" MHz")
                .toString();
    }
}
