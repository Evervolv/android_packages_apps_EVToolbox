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
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.Gravity;
import android.widget.Toast;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.Toolbox;
import com.evervolv.toolbox.custom.GalleryPickerPreference;

public class LockscreenStyle extends PreferenceFragment implements
        OnPreferenceChangeListener,
        Toolbox.DisabledListener {

    private static final String LOCKSCREEN_STYLE_PREF = "pref_lockscreen_style_picker";

    private static final int LOCK_STYLE_JB      = 0;
    private static final int LOCK_STYLE_ICS     = 1;
    private static final int LOCK_STYLE_GB      = 2;
    private static final int LOCK_STYLE_ECLAIR  = 3;
    private static final int LOCK_STYLE_DEFAULT = LOCK_STYLE_JB;

    private ContentResolver mCr;
    private PreferenceScreen mPrefSet;
    private GalleryPickerPreference mLockscreenStyle;

    private int mCurrLockscreen;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.lockscreen_style);

        mPrefSet = getPreferenceScreen();

        mCr = getActivity().getContentResolver();

        mCurrLockscreen = LOCK_STYLE_DEFAULT; //Settings.System.getInt(mCr,
        //        Settings.System.LOCKSCREEN_STYLE , LOCK_STYLE_DEFAULT);
        //mMaxCustomApps = Settings.System.LOCKSCREEN_CUSTOM_APP_ACTIVITIES.length;

        /* Lockscreen style */
        String position = null; //Settings.System.getString(mCr,
        //        Settings.System.LOCKSCREEN_STYLE);
        mLockscreenStyle = (GalleryPickerPreference) mPrefSet.findPreference(LOCKSCREEN_STYLE_PREF);
        mLockscreenStyle.setCurrPos(position == null ? 0 : Integer.valueOf(position));
        mLockscreenStyle.setSharedPrefs(mPrefSet.getSharedPreferences());
        mLockscreenStyle.setOnPreferenceChangeListener(this);
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
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mLockscreenStyle) {
            int value = Integer.parseInt((String) newValue);
            switch (value) {
                case LOCK_STYLE_ECLAIR:
                case LOCK_STYLE_GB:
                    if (getResources().getBoolean(R.bool.config_disableOldLocks)) {
                        Toast toast = Toast.makeText(getActivity().getApplicationContext(), R.string
                                .pref_lockscreen_style_picker_unavailable,
                                Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        return true;
                    }
                default:
;
            }
            //Settings.System.putInt(mCr, Settings.System.LOCKSCREEN_STYLE, value);
            mCurrLockscreen = value;
            return true;
        }
        return false;
    }

    @Override
    public void onToolboxDisabled(boolean enabled) {
        mPrefSet.setEnabled(enabled);
    }

}
