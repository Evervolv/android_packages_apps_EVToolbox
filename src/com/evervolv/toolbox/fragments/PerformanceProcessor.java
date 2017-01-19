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

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.os.SystemService;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.Toolbox;
import com.evervolv.toolbox.utils.Constants;
import com.evervolv.toolbox.utils.FileUtil;

import java.lang.Runtime;

public class PerformanceProcessor extends PreferenceFragment implements
        OnPreferenceChangeListener,
        Toolbox.DisabledListener {

    private static final String TAG = "PerformanceProcessor";

    public static final String CPU_ONLINE = "/sys/devices/system/cpu/cpu0/online";
    public static final String FREQ_CUR_PREF = "pref_cpu_freq_cur";
    public static final String SCALE_CUR_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";
    public static final String FREQINFO_CUR_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_cur_freq";
    private static String FREQ_CUR_FILE = SCALE_CUR_FILE;
    public static final String GOV_PREF = "pref_cpu_gov";
    public static final String GOV_LIST_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors";
    public static final String GOV_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
    public static final String FREQ_MIN_PREF = "pref_cpu_freq_min";
    public static final String FREQ_MAX_PREF = "pref_cpu_freq_max";
    public static final String FREQ_LIST_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
    public static String FREQ_MAX_FILE = null;
    public static String FREQ_MIN_FILE = null;

    public static final String IOSCHED_PREF = "pref_cpu_io_sched";
    public static final String IOSCHED_LIST_FILE = "/sys/block/mmcblk0/queue/scheduler";
    public static final String SOB_PREF = "pref_cpu_set_on_boot";

    public static boolean freqCapFilesInitialized = false;

    private static final int UI_UPDATE_DELAY = 500;

    private PreferenceScreen mPrefSet;

    private String mIOSchedulerFormat;
    private String mGovernorFormat;
    private String mMinFrequencyFormat;
    private String mMaxFrequencyFormat;

    private Preference mCurFrequencyPref;
    private ListPreference mIOSchedulerPref;
    private ListPreference mGovernorPref;
    private ListPreference mMinFrequencyPref;
    private ListPreference mMaxFrequencyPref;

    private HandlerThread mCpuInfoThread;
    private Handler mCpuInfoHandler;
    private CpuUiUpdate mCpuUiUpdate;

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

            PerformanceProcessor.this.getActivity().runOnUiThread(mCpuUiUpdate);
            mCpuInfoHandler.postDelayed(mUpdateCpuFreqValues, UI_UPDATE_DELAY);
        }
    };

    private void initFreqCapFiles()
    {
        if (freqCapFilesInitialized) return;
        FREQ_MAX_FILE = getString(R.string.pref_max_cpu_freq_file);
        FREQ_MIN_FILE = getString(R.string.pref_min_cpu_freq_file);
        freqCapFilesInitialized = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initFreqCapFiles();

        mIOSchedulerFormat = getString(R.string.pref_cpu_io_sched_summary);
        mGovernorFormat = getString(R.string.pref_cpu_governors_summary);
        mMinFrequencyFormat = getString(R.string.pref_cpu_min_freq_summary);
        mMaxFrequencyFormat = getString(R.string.pref_cpu_max_freq_summary);


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

        addPreferencesFromResource(R.xml.performance_processor);
        mPrefSet = getPreferenceScreen();

        mGovernorPref = (ListPreference) mPrefSet.findPreference(GOV_PREF);
        mCurFrequencyPref = (Preference) mPrefSet.findPreference(FREQ_CUR_PREF);
        mMinFrequencyPref = (ListPreference) mPrefSet.findPreference(FREQ_MIN_PREF);
        mMaxFrequencyPref = (ListPreference) mPrefSet.findPreference(FREQ_MAX_PREF);

        mIOSchedulerPref = (ListPreference) mPrefSet.findPreference(IOSCHED_PREF);

        /* I/O scheduler
        Some systems might not use I/O schedulers */
        if (!FileUtil.fileExists(IOSCHED_LIST_FILE) ||
            (availableIOSchedulersLine = FileUtil.fileReadOneLine(IOSCHED_LIST_FILE)) == null) {
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
        if (!FileUtil.fileExists(GOV_LIST_FILE) || !FileUtil.fileExists(GOV_FILE) || (temp = FileUtil.fileReadOneLine(GOV_FILE)) == null || (availableGovernorsLine = FileUtil.fileReadOneLine(GOV_LIST_FILE)) == null) {
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

    @Override
    public void onResume() {
        String availableIOSchedulersLine;
        int bropen, brclose;
        String currentIOScheduler;
        super.onResume();

        initFreqCapFiles();
        if (mCpuInfoHandler != null) {
            mCpuInfoHandler.post(mUpdateCpuFreqValues);
        }

        if (FileUtil.fileExists(IOSCHED_LIST_FILE) &&
            (availableIOSchedulersLine = FileUtil.fileReadOneLine(IOSCHED_LIST_FILE)) != null) {
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

    public boolean onPreferenceChange(Preference preference, Object value) {
        initFreqCapFiles();

        final String newValue = (String) value;
        String fname = "";

        if (newValue != null) {
            if (preference == mIOSchedulerPref) {
                fname = IOSCHED_LIST_FILE;
            } else if (preference == mGovernorPref) {
                fname = GOV_FILE;
            } else if (preference == mMinFrequencyPref) {
                fname = FREQ_MIN_FILE;
            } else if (preference == mMaxFrequencyPref) {
                fname = FREQ_MAX_FILE;
            }

            if (FileUtil.fileWriteOneLine(fname, newValue)) {
                final String file = fname;
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
                                    cpufile = file.replace("cpu0", "cpu" + i);
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
                                                FileUtil.fileWriteOneLine(cpufile, newValue);
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

                if (preference == mIOSchedulerPref) {
                    mIOSchedulerPref.setSummary(String.format(mIOSchedulerFormat, newValue));
                } else if (preference == mGovernorPref) {
                    mGovernorPref.setSummary(String.format(mGovernorFormat, newValue));
                } else if (preference == mMinFrequencyPref) {
                    mMinFrequencyPref.setSummary(String.format(mMinFrequencyFormat,
                            toMHz(newValue)));
                } else if (preference == mMaxFrequencyPref) {
                    mMaxFrequencyPref.setSummary(String.format(mMaxFrequencyFormat,
                            toMHz(newValue)));
                }
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    private String toMHz(String mhzString) {
        return new StringBuilder().append(Integer.valueOf(mhzString) / 1000).append(" MHz")
                .toString();
    }

    @Override
    public void onToolboxDisabled(boolean enabled) {
        mPrefSet.setEnabled(enabled);
    }

}
