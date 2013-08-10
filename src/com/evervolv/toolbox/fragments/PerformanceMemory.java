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
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.misc.FileUtil;

import java.io.File;

public class PerformanceMemory extends PreferenceFragment implements Preference.OnPreferenceChangeListener  {
    private static final String TAG = "EVToolbox";

    public static final String SOB_PREF = "pref_cpu_set_on_boot";    
    public static final String KSM_RUN_FILE = "/sys/kernel/mm/ksm/run";
    public static final String KSM_PREF = "pref_ksm";
    public static final String KSM_PREF_DISABLED = "0";
    public static final String KSM_PREF_ENABLED = "1";

    private static final String ZRAM_PREF = "pref_zram_size";
    private static final String ZRAM_PERSIST_PROP = "persist.service.zram";
    private static final String ZRAM_DEFAULT = "0";

    private CheckBoxPreference mKSMPref;
    private ListPreference mzRAM;
    private int swapAvailable = -1;
    private PreferenceScreen mPrefSet;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.performance_memory);

        mPrefSet = getPreferenceScreen();

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
            mPrefSet.removePreference(mzRAM);
        }

        if (FileUtil.fileExists(KSM_RUN_FILE)) {
            mKSMPref.setChecked(KSM_PREF_ENABLED.equals(FileUtil.fileReadOneLine(KSM_RUN_FILE)));
        } else {
            mPrefSet.removePreference(mKSMPref);
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
        if (newValue != null) {
            if (preference == mzRAM) {
                if (newValue != null) {
                    SystemProperties.set(ZRAM_PERSIST_PROP, (String) newValue);
                    return true;
                }
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

}
