/*
 * Copyright (C) 2016 The CyanogenMod Project
 *               2021 The LineageOS Project
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

package com.evervolv.toolbox.display;

import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.internal.view.RotationPolicy;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.SettingsPreferenceFragment;

public class RotationSettings extends SettingsPreferenceFragment {
    private static final String TAG = "RotationSettings";

    private static final String ROTATION_0_PREF = "display_rotation_0";
    private static final String ROTATION_90_PREF = "display_rotation_90";
    private static final String ROTATION_180_PREF = "display_rotation_180";
    private static final String ROTATION_270_PREF = "display_rotation_270";

    private CheckBoxPreference mRotation0Pref;
    private CheckBoxPreference mRotation90Pref;
    private CheckBoxPreference mRotation180Pref;
    private CheckBoxPreference mRotation270Pref;

    public static final int ROTATION_0_MODE = 1;
    public static final int ROTATION_90_MODE = 2;
    public static final int ROTATION_180_MODE = 4;
    public static final int ROTATION_270_MODE = 8;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        addPreferencesFromResource(R.xml.display_rotation);

        PreferenceScreen prefSet = getPreferenceScreen();

        mRotation0Pref = (CheckBoxPreference) prefSet.findPreference(ROTATION_0_PREF);
        mRotation90Pref = (CheckBoxPreference) prefSet.findPreference(ROTATION_90_PREF);
        mRotation180Pref = (CheckBoxPreference) prefSet.findPreference(ROTATION_180_PREF);
        mRotation270Pref = (CheckBoxPreference) prefSet.findPreference(ROTATION_270_PREF);

        int mode = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION_ANGLES,
                ROTATION_0_MODE | ROTATION_90_MODE | ROTATION_270_MODE, UserHandle.USER_CURRENT);

        mRotation0Pref.setChecked((mode & ROTATION_0_MODE) != 0);
        mRotation90Pref.setChecked((mode & ROTATION_90_MODE) != 0);
        mRotation180Pref.setChecked((mode & ROTATION_180_MODE) != 0);
        mRotation270Pref.setChecked((mode & ROTATION_270_MODE) != 0);
    }

    private int getRotationBitmask() {
        int mode = 0;
        if (mRotation0Pref.isChecked()) {
            mode |= ROTATION_0_MODE;
        }
        if (mRotation90Pref.isChecked()) {
            mode |= ROTATION_90_MODE;
        }
        if (mRotation180Pref.isChecked()) {
            mode |= ROTATION_180_MODE;
        }
        if (mRotation270Pref.isChecked()) {
            mode |= ROTATION_270_MODE;
        }
        return mode;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mRotation0Pref ||
                preference == mRotation90Pref ||
                preference == mRotation180Pref ||
                preference == mRotation270Pref) {
            int mode = getRotationBitmask();
            if (mode == 0) {
                mode |= ROTATION_0_MODE;
                mRotation0Pref.setChecked(true);
            }
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION_ANGLES, mode, UserHandle.USER_CURRENT);
            return true;
        }

        return super.onPreferenceTreeClick(preference);
    }
}
