/*
 * Copyright (C) 2012 The CyanogenMod Project
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

package com.evervolv.toolbox.activities.subactivities;

import java.io.File;

import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.utils.FileUtil;
import com.evervolv.toolbox.SettingsFragment;

public class MemoryManagement extends SettingsFragment implements
        OnPreferenceChangeListener {

    public static final String KSM_RUN_FILE = "/sys/kernel/mm/ksm/run";
    public static final String KSM_PREF = "pref_ksm";
    public static final String KSM_PREF_DISABLED = "0";
    public static final String KSM_PREF_ENABLED = "1";

    private static final String ZRAM_PREF = "pref_zram_size";
    private static final String ZRAM_PERSIST_PROP = "persist.sys.zram.enable";
    private static final String ZRAM_DEFAULT = "0";

    private ListPreference mzRAM;
    private CheckBoxPreference mKSMPref;

    private int swapAvailable = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getPreferenceManager() != null) {

            addPreferencesFromResource(R.xml.memory_management);

            PreferenceScreen prefSet = getPreferenceScreen();

            mzRAM = (ListPreference) prefSet.findPreference(ZRAM_PREF);
            mKSMPref = (CheckBoxPreference) prefSet.findPreference(KSM_PREF);

            if (isSwapAvailable()) {
                if (SystemProperties.get(ZRAM_PERSIST_PROP) == null)
                    SystemProperties.set(ZRAM_PERSIST_PROP, ZRAM_DEFAULT);
                mzRAM.setValue(SystemProperties.get(ZRAM_PERSIST_PROP, ZRAM_DEFAULT));
                mzRAM.setOnPreferenceChangeListener(this);
            } else {
                prefSet.removePreference(mzRAM);
            }

            if (FileUtil.fileExists(KSM_RUN_FILE)) {
                mKSMPref.setChecked(KSM_PREF_ENABLED.equals(FileUtil.fileReadOneLine(KSM_RUN_FILE)));
            } else {
                prefSet.removePreference(mKSMPref);
            }

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
        if (preference == mzRAM) {
            if (newValue != null) {
                SystemProperties.set(ZRAM_PERSIST_PROP, (String) newValue);
                return true;
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
