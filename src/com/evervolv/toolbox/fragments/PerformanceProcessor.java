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
import android.os.Message;
import android.os.SystemProperties;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.Toolbox;
import com.evervolv.toolbox.misc.FileUtil;

public class PerformanceProcessor extends PreferenceFragment implements
        OnPreferenceChangeListener,
        Toolbox.DisabledListener {
    private static final String TAG = "EVToolbox";

    public static final String FREQ_CUR_PREF = "pref_cpu_freq_cur";
    public static final String SCALE_CUR_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";
    public static final String FREQINFO_CUR_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_cur_freq";
    private static String FREQ_CUR_FILE = SCALE_CUR_FILE;
    public static final String GOV_PREF = "pref_cpu_gov";
    public static final String GOV_LIST_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors";
    public static final String GOV_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
    public static final String GOV_CHANGED_PROP = "sys.governor.changed";
    public static final String FREQ_MIN_PREF = "pref_cpu_freq_min";
    public static final String FREQ_MAX_PREF = "pref_cpu_freq_max";
    public static final String FREQ_LIST_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
    public static String FREQ_MAX_FILE = null;
    public static String FREQ_MIN_FILE = null;
    public static final String SOB_PREF = "pref_cpu_set_on_boot";

    public static boolean freqCapFilesInitialized = false;

    private PreferenceScreen mPrefSet;

    private String mGovernorFormat;
    private String mMinFrequencyFormat;
    private String mMaxFrequencyFormat;

    private Preference mCurFrequencyPref;
    private ListPreference mGovernorPref;
    private ListPreference mMinFrequencyPref;
    private ListPreference mMaxFrequencyPref;

    private class CurCPUThread extends Thread {
        private boolean mInterrupt = false;

        public void interrupt() {
            mInterrupt = true;
        }

        @Override
        public void run() {
            try {
                while (!mInterrupt) {
                    sleep(500);
                    final String curFreq = FileUtil.fileReadOneLine(FREQ_CUR_FILE);
                    if (curFreq != null)
                        mCurCPUHandler.sendMessage(mCurCPUHandler.obtainMessage(0, curFreq));
                }
            } catch (InterruptedException e) {
            }
        }
    };

    private CurCPUThread mCurCPUThread = new CurCPUThread();

    private Handler mCurCPUHandler = new Handler() {
        public void handleMessage(Message msg) {
            mCurFrequencyPref.setSummary(toMHz((String) msg.obj));
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

        mGovernorFormat = getString(R.string.pref_cpu_governors_summary);
        mMinFrequencyFormat = getString(R.string.pref_cpu_min_freq_summary);
        mMaxFrequencyFormat = getString(R.string.pref_cpu_max_freq_summary);

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
        /* Governor
        Some systems might not use governors */
        if (!FileUtil.fileExists(GOV_LIST_FILE) || !FileUtil.fileExists(GOV_FILE) || (temp = FileUtil.fileReadOneLine(GOV_FILE)) == null || (availableGovernorsLine = FileUtil.fileReadOneLine(GOV_LIST_FILE)) == null) {
            mPrefSet.removePreference(mGovernorPref);

        } else {
            availableGovernors = availableGovernorsLine.split(" ");

            mGovernorPref = (ListPreference) mPrefSet.findPreference(GOV_PREF);
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
                mPrefSet.removePreference(mMinFrequencyPref);

            } else {
                mMinFrequencyPref.setEntryValues(availableFrequencies);
                mMinFrequencyPref.setEntries(frequencies);
                mMinFrequencyPref.setValue(temp);
                mMinFrequencyPref.setSummary(String.format(mMinFrequencyFormat, toMHz(temp)));
                mMinFrequencyPref.setOnPreferenceChangeListener(this);
            }

            // Max frequency
            if (!FileUtil.fileExists(FREQ_MAX_FILE) || (temp = FileUtil.fileReadOneLine(FREQ_MAX_FILE)) == null) {
                mPrefSet.removePreference(mMaxFrequencyPref);

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

            mCurCPUThread.start();
        }
    }

    @Override
    public void onResume() {
        String temp;

        super.onResume();

        initFreqCapFiles();

        if (FileUtil.fileExists(FREQ_MIN_FILE) && (temp = FileUtil.fileReadOneLine(FREQ_MIN_FILE)) != null) {
            mMinFrequencyPref.setValue(temp);
            mMinFrequencyPref.setSummary(String.format(mMinFrequencyFormat, toMHz(temp)));
        }

        if (FileUtil.fileExists(FREQ_MAX_FILE) && (temp = FileUtil.fileReadOneLine(FREQ_MAX_FILE)) != null) {
            mMaxFrequencyPref.setValue(temp);
            mMaxFrequencyPref.setSummary(String.format(mMaxFrequencyFormat, toMHz(temp)));
        }

        if (FileUtil.fileExists(GOV_FILE) && (temp = FileUtil.fileReadOneLine(GOV_FILE)) != null) {
            mGovernorPref.setSummary(String.format(mGovernorFormat, temp));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mPrefSet.setEnabled(Toolbox.isEnabled(getActivity()));
        ((Toolbox) getActivity()).registerCallback(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        ((Toolbox) getActivity()).unRegisterCallback(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCurCPUThread.interrupt();
        try {
            mCurCPUThread.join();
        } catch (InterruptedException e) {
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        initFreqCapFiles();
        String fname = "";
        if (newValue != null) {
            if (preference == mGovernorPref) {
                fname = GOV_FILE;
            } else if (preference == mMinFrequencyPref) {
                fname = FREQ_MIN_FILE;
            } else if (preference == mMaxFrequencyPref) {
                fname = FREQ_MAX_FILE;
            }

            if (FileUtil.fileWriteOneLine(fname, (String) newValue)) {
                if (preference == mGovernorPref) {
                    mGovernorPref.setSummary(String.format(mGovernorFormat, (String) newValue));
                    SystemProperties.set(GOV_CHANGED_PROP, (String) newValue);
                } else if (preference == mMinFrequencyPref) {
                    mMinFrequencyPref.setSummary(String.format(mMinFrequencyFormat,
                            toMHz((String) newValue)));
                } else if (preference == mMaxFrequencyPref) {
                    mMaxFrequencyPref.setSummary(String.format(mMaxFrequencyFormat,
                            toMHz((String) newValue)));
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
