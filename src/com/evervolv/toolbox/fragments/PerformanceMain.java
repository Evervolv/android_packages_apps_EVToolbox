package com.evervolv.toolbox.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.misc.FileUtil;

import java.io.File;

public class PerformanceMain extends PreferenceFragment implements Preference.OnPreferenceChangeListener  {
    private static final String TAG = "EVToolbox";

    public static final String KSM_RUN_FILE = "/sys/kernel/mm/ksm/run";
    public static final String KSM_PREF = "pref_ksm";
    public static final String KSM_PREF_DISABLED = "0";
    public static final String KSM_PREF_ENABLED = "1";
    public static final String FREQ_CUR_PREF = "pref_cpu_freq_cur";
    public static final String SCALE_CUR_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";
    public static final String FREQINFO_CUR_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_cur_freq";
    public static final String GOV_PREF = "pref_cpu_gov";
    public static final String GOV_LIST_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors";
    public static final String GOV_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
    public static final String GOV_CHANGED_PROP = "sys.governor.changed";
    public static final String FREQ_MIN_PREF = "pref_cpu_freq_min";
    public static final String FREQ_MAX_PREF = "pref_cpu_freq_max";
    public static final String FREQ_LIST_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
    public static final String FREQ_MAX_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
    public static final String FREQ_MIN_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq";
    public static final String SOB_PREF = "pref_cpu_set_on_boot";

    private static final String PERFORMANCE_PROCESSOR_CATEGORY = "pref_performance_processor";
    private static final String PERFORMANCE_MEMORY_CATEGORY = "pref_performance_memory";

    private static final String ZRAM_PREF = "pref_zram_size";
    private static final String ZRAM_PERSIST_PROP = "persist.service.zram";
    private static final String ZRAM_DEFAULT = "0";

    private static String FREQ_CUR_FILE = SCALE_CUR_FILE;

    private int swapAvailable = -1;

    private String mGovernorFormat;
    private String mMinFrequencyFormat;
    private String mMaxFrequencyFormat;

    private PreferenceScreen mPrefSet;

    private PreferenceCategory mProcessor;
    private PreferenceCategory mMemory;

    private CheckBoxPreference mKSMPref;
    private Preference mCurFrequencyPref;
    private ListPreference mGovernorPref;
    private ListPreference mMinFrequencyPref;
    private ListPreference mMaxFrequencyPref;
    private ListPreference mzRAM;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.performance_main);

        mPrefSet = getPreferenceScreen();

        mProcessor = (PreferenceCategory) mPrefSet.findPreference(
                PERFORMANCE_PROCESSOR_CATEGORY);
        mMemory = (PreferenceCategory) mPrefSet.findPreference(
                PERFORMANCE_MEMORY_CATEGORY);

        /* KSM */

        mKSMPref = (CheckBoxPreference) mPrefSet.findPreference(KSM_PREF);

        /* Zram */

        mzRAM = (ListPreference) mPrefSet.findPreference(ZRAM_PREF);
        if (isSwapAvailable()) {
            if (SystemProperties.get(ZRAM_PERSIST_PROP) == null)
                SystemProperties.set(ZRAM_PERSIST_PROP, ZRAM_DEFAULT);
            mzRAM.setValue(SystemProperties.get(ZRAM_PERSIST_PROP, ZRAM_DEFAULT));
            mzRAM.setOnPreferenceChangeListener(this);
        } else {
            mMemory.removePreference(mzRAM);
        }

        if (FileUtil.fileExists(KSM_RUN_FILE)) {
            mKSMPref.setChecked(KSM_PREF_ENABLED.equals(FileUtil.fileReadOneLine(KSM_RUN_FILE)));
        } else {
            mMemory.removePreference(mKSMPref);
        }

        /* Governor */

        mGovernorFormat = getString(R.string.pref_cpu_governors_summary);
        mMinFrequencyFormat = getString(R.string.pref_cpu_min_freq_summary);
        mMaxFrequencyFormat = getString(R.string.pref_cpu_max_freq_summary);

        String[] availableGovernors = FileUtil.fileReadOneLine(GOV_LIST_FILE).split(" ");
        String[] availableFrequencies = new String[0];
        String availableFrequenciesLine = FileUtil.fileReadOneLine(FREQ_LIST_FILE);
        if (availableFrequenciesLine != null)
            availableFrequencies = availableFrequenciesLine.split(" ");
        String[] frequencies;
        String temp;

        frequencies = new String[availableFrequencies.length];
        for (int i = 0; i < frequencies.length; i++) {
            frequencies[i] = toMHz(availableFrequencies[i]);
        }

        // Governor
        temp = FileUtil.fileReadOneLine(GOV_FILE);

        mGovernorPref = (ListPreference) mPrefSet.findPreference(GOV_PREF);
        mGovernorPref.setEntryValues(availableGovernors);
        mGovernorPref.setEntries(availableGovernors);
        mGovernorPref.setValue(temp);
        mGovernorPref.setSummary(String.format(mGovernorFormat, temp));
        mGovernorPref.setOnPreferenceChangeListener(this);

        // Some systems might not use governors
        if (temp == null) {
            mProcessor.removePreference(mGovernorPref);
        }

        if (!FileUtil.fileExists(FREQ_CUR_FILE)) {
            FREQ_CUR_FILE = FREQINFO_CUR_FILE;
        }

        /* CPU Frequency */

        // Cur frequency
        temp = FileUtil.fileReadOneLine(FREQ_CUR_FILE);

        mCurFrequencyPref = mPrefSet.findPreference(FREQ_CUR_PREF);
        mCurFrequencyPref.setSummary(toMHz(temp));

        // Min frequency
        temp = FileUtil.fileReadOneLine(FREQ_MIN_FILE);

        mMinFrequencyPref = (ListPreference) mPrefSet.findPreference(FREQ_MIN_PREF);
        mMinFrequencyPref.setEntryValues(availableFrequencies);
        mMinFrequencyPref.setEntries(frequencies);
        mMinFrequencyPref.setValue(temp);
        mMinFrequencyPref.setSummary(String.format(mMinFrequencyFormat, toMHz(temp)));
        mMinFrequencyPref.setOnPreferenceChangeListener(this);

        if (temp == null) {
            mProcessor.removePreference(mMinFrequencyPref);
        }

        // Max frequency
        temp = FileUtil.fileReadOneLine(FREQ_MAX_FILE);

        mMaxFrequencyPref = (ListPreference) mPrefSet.findPreference(FREQ_MAX_PREF);
        mMaxFrequencyPref.setEntryValues(availableFrequencies);
        mMaxFrequencyPref.setEntries(frequencies);
        mMaxFrequencyPref.setValue(temp);
        mMaxFrequencyPref.setSummary(String.format(mMaxFrequencyFormat, toMHz(temp)));
        mMaxFrequencyPref.setOnPreferenceChangeListener(this);

        if (temp == null) {
            mProcessor.removePreference(mMaxFrequencyPref);
        }

        // Disable the min/max list if we dont have a list file
        if (availableFrequenciesLine == null) {
            mMinFrequencyPref.setEnabled(false);
            mMaxFrequencyPref.setEnabled(false);
        }

        mCurCPUThread.start();

    }


    @Override
    public void onResume() {
        String temp;

        super.onResume();

        temp = FileUtil.fileReadOneLine(FREQ_MAX_FILE);
        mMaxFrequencyPref.setValue(temp);
        mMaxFrequencyPref.setSummary(String.format(mMaxFrequencyFormat, toMHz(temp)));

        temp = FileUtil.fileReadOneLine(FREQ_MIN_FILE);
        mMinFrequencyPref.setValue(temp);
        mMinFrequencyPref.setSummary(String.format(mMinFrequencyFormat, toMHz(temp)));

        temp = FileUtil.fileReadOneLine(GOV_FILE);
        mGovernorPref.setSummary(String.format(mGovernorFormat, temp));
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

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mKSMPref) {
            FileUtil.fileWriteOneLine(KSM_RUN_FILE, mKSMPref.isChecked() ? "1" : "0");
            return true;
        }
        return false;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String fname = "";
        if (newValue != null) {
            if (preference == mzRAM) {
                if (newValue != null) {
                    SystemProperties.set(ZRAM_PERSIST_PROP, (String) newValue);
                    return true;
                }
            } else if (preference == mGovernorPref) {
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

    /**
     * Check if swap support is available on the system
     */
    private boolean isSwapAvailable() {
        if (swapAvailable < 0) {
            swapAvailable = new File("/proc/swaps").exists() ? 1 : 0;
        }
        return swapAvailable > 0;
    }

    private String toMHz(String mhzString) {
        return new StringBuilder().append(Integer.valueOf(mhzString) / 1000).append(" MHz")
                .toString();
    }

}
