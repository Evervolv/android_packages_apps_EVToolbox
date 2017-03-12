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

package com.evervolv.toolbox.fragments;

import android.content.ContentResolver;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v14.preference.PreferenceFragment;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.Toolbox;

public class InterfaceRotation extends PreferenceFragment implements
        Toolbox.DisabledListener {

    private static final String ROTATION_0_PREF = "pref_rotation_0";
    private static final String ROTATION_90_PREF = "pref_rotation_90";
    private static final String ROTATION_180_PREF = "pref_rotation_180";
    private static final String ROTATION_270_PREF = "pref_rotation_270";

    private static final int ROTATION_0 = 1;
    private static final int ROTATION_90 = 2;
    private static final int ROTATION_180 = 4;
    private static final int ROTATION_270 = 8;

    private SwitchPreference mRotation0Pref;
    private SwitchPreference mRotation90Pref;
    private SwitchPreference mRotation180Pref;
    private SwitchPreference mRotation270Pref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.interface_rotation);

        PreferenceScreen mPrefSet = getPreferenceScreen();

        mRotation0Pref = (SwitchPreference) mPrefSet.findPreference(ROTATION_0_PREF);
        mRotation90Pref = (SwitchPreference) mPrefSet.findPreference(ROTATION_90_PREF);
        mRotation180Pref = (SwitchPreference) mPrefSet.findPreference(ROTATION_180_PREF);
        mRotation270Pref = (SwitchPreference) mPrefSet.findPreference(ROTATION_270_PREF);

        int defaultAngles = ROTATION_0 | ROTATION_90 | ROTATION_270;
        if (getResources().getBoolean(com.android.internal.R.bool.config_allowAllRotations)) {
            // 180 is default enabled on tablets, disabled on phones
            defaultAngles |= ROTATION_180;
        }

        int angles = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION_ANGLES, defaultAngles);
        mRotation0Pref.setChecked((angles & ROTATION_0) != 0);
        mRotation90Pref.setChecked((angles & ROTATION_90) != 0);
        mRotation180Pref.setChecked((angles & ROTATION_180) != 0);
        mRotation270Pref.setChecked((angles & ROTATION_270) != 0);
    }

    @Override
    public void onStart() {
        super.onStart();
        getPreferenceScreen().setEnabled(Toolbox.isEnabled(getActivity()));
        ((Toolbox) getActivity()).registerCallback(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        ((Toolbox) getActivity()).unRegisterCallback(this);
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mRotation0Pref ||
            preference == mRotation90Pref ||
            preference == mRotation180Pref ||
            preference == mRotation270Pref) {
            int angles = 0;
            if (mRotation0Pref.isChecked()) angles |= ROTATION_0;
            if (mRotation90Pref.isChecked()) angles |= ROTATION_90;
            if (mRotation180Pref.isChecked()) angles |= ROTATION_180;
            if (mRotation270Pref.isChecked()) angles |= ROTATION_270;

            Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.ACCELEROMETER_ROTATION_ANGLES, angles);
        }

        return true;
    }

    @Override
    public void onToolboxDisabled(boolean enabled) {
        getPreferenceScreen().setEnabled(enabled);
    }

}
