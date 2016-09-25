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
package com.evervolv.toolbox.utils;

import android.content.ContentResolver;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.SwitchPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.Toolbox;

public class ToolboxSettings extends PreferenceFragment {

    private static final String TAG = "EVToolbox";

    private static final String PREF_TOOLBOX_DISABLE = "pref_settings_toolbox_disable";

    private ContentResolver mCr;
    private PreferenceScreen mPrefSet;
    private SwitchPreference mToolboxDisabled;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_general);

        mPrefSet = getPreferenceScreen();

        mCr = getActivity().getContentResolver();

        /* Settings menu Switches */
        mToolboxDisabled = (SwitchPreference) mPrefSet.findPreference(PREF_TOOLBOX_DISABLE);
        mToolboxDisabled.setChecked(Settings.System.getInt(mCr,
                Settings.System.DISABLE_TOOLBOX, 0) == 1);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(
                getResources().getString(R.string.tab_title_settings));
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;

        if (preference == mToolboxDisabled) {
            value = mToolboxDisabled.isChecked();
            Settings.System.putInt(mCr, Settings.System.DISABLE_TOOLBOX,
                    value ? 1 : 0);
            // Inform children of state change
            ((Toolbox) getActivity()).updateListeners(value);
            return true;
        }

        return false;
    }
}
