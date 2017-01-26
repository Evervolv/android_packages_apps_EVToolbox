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

import android.content.ContentResolver;
import android.os.Bundle;
import android.preference.SwitchPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.Toolbox;

public class StatusbarQuickSettings extends PreferenceFragment implements
        Toolbox.DisabledListener {

    private static final String STATUSBAR_QUICK_PULLDOWN = "pref_statusbar_quick_pulldown";

    private ContentResolver mCr;
    private PreferenceScreen mPrefSet;

    private SwitchPreference mQuickPulldown;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.statusbar_qs);

        mPrefSet = getPreferenceScreen();

        mCr = getActivity().getContentResolver();

        /* Quick Pulldown */
        mQuickPulldown = (SwitchPreference) mPrefSet.findPreference(STATUSBAR_QUICK_PULLDOWN);
        mQuickPulldown.setChecked(Settings.System.getInt(mCr,
                Settings.System.STATUS_BAR_QUICK_QS_PULLDOWN, 1) == 1);
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
        if (preference == mQuickPulldown) {
            boolean value = mQuickPulldown.isChecked();
            Settings.System.putInt(mCr, Settings.System.STATUS_BAR_QUICK_QS_PULLDOWN,
                    value ? 1 : 0);
            return true;
        }
        return false;
    }

    @Override
    public void onToolboxDisabled(boolean enabled) {
        mPrefSet.setEnabled(enabled);
    }

}
