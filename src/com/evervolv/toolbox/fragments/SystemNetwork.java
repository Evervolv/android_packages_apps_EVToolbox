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
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.Toolbox;

public class SystemNetwork extends PreferenceFragment implements
        Toolbox.DisabledListener  {

    private static final String PREF_HOSTNAME = "pref_system_hostname";
    public static final String PREF_SSHD = "pref_system_sshd";

    private PreferenceScreen mPrefSet;
    CheckBoxPreference mSshd;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.system_network);

        mPrefSet = getPreferenceScreen();

        mSshd = (CheckBoxPreference) findPreference(PREF_SSHD);
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
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mSshd) {
            if (mSshd.isChecked()) {
                SystemProperties.set("ctl.start", "sshd");
            } else {
                SystemProperties.set("ctl.stop", "sshd");
            }
            return true;
        }
        return false;
    }

    @Override
    public void onToolboxDisabled(boolean enabled) {

    }
}
