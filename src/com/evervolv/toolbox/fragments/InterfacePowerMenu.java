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
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.Toolbox;
import com.evervolv.toolbox.custom.NumberPickerPreference;

public class InterfacePowerMenu extends PreferenceFragment implements
        OnPreferenceChangeListener,
        Toolbox.DisabledListener {

    private static final String TAG = "EVToolbox";

    private static final String PREF_HIDE_SCREENSHOT        =    "pref_power_menu_hide_screenshot";
    private static final String PREF_HIDE_SOUND             =    "pref_power_menu_hide_sound";
    private static final String PREF_HIDE_AIRPLANE_MODE     =    "pref_power_menu_hide_airplane_mode";
    private static final String PREF_HIDE_REBOOT_MENU       =    "pref_power_menu_hide_reboot_menu";
    private static final String PREF_SCREENSHOT_DELAY       =    "pref_power_menu_screenshot_delay";

    private static final int HIDE_REBOOT = 1;
    private static final int HIDE_SCREENSHOT = 2;
    private static final int HIDE_SOUND = 4;
    private static final int HIDE_AIRPLANE = 8;

    private static final int MIN_DELAY_VALUE = 1;
    private static final int MAX_DELAY_VALUE = 30;

    private ContentResolver mCr;
    private PreferenceScreen mPrefSet;

    private CheckBoxPreference mHideScreenshot;
    private CheckBoxPreference mHideSound;
    private CheckBoxPreference mHideAirplaneMode;
    private CheckBoxPreference mHideRebootMenu;
    private NumberPickerPreference mScreenshotDelay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.interface_power_menu);

        mPrefSet = getPreferenceScreen();

        mCr = getActivity().getContentResolver();

        int hiddenOptions = Settings.System.getInt(mCr,
                Settings.System.HIDDEN_POWER_MENU_OPTIONS, 0);

        mHideScreenshot = (CheckBoxPreference) mPrefSet.findPreference(
                PREF_HIDE_SCREENSHOT);
        mHideScreenshot.setChecked((hiddenOptions & HIDE_SCREENSHOT) != 0);

        mHideSound = (CheckBoxPreference) mPrefSet.findPreference(
                PREF_HIDE_SOUND);
        mHideSound.setChecked((hiddenOptions & HIDE_SOUND) != 0);

        mHideAirplaneMode = (CheckBoxPreference) mPrefSet.findPreference(
                PREF_HIDE_AIRPLANE_MODE);
        mHideAirplaneMode.setChecked((hiddenOptions & HIDE_AIRPLANE) != 0);

        mHideRebootMenu = (CheckBoxPreference) mPrefSet.findPreference(
                PREF_HIDE_REBOOT_MENU);
        mHideRebootMenu.setChecked((hiddenOptions & HIDE_REBOOT) != 0);

        mScreenshotDelay = (NumberPickerPreference) mPrefSet.findPreference(
                PREF_SCREENSHOT_DELAY);
        mScreenshotDelay.setOnPreferenceChangeListener(this);
        mScreenshotDelay.setMinValue(MIN_DELAY_VALUE);
        mScreenshotDelay.setMaxValue(MAX_DELAY_VALUE);
        int ssDelay = Settings.System.getInt(mCr,
                Settings.System.POWER_MENU_SCREENSHOT_DELAY, 1);
        mScreenshotDelay.setCurrentValue(ssDelay);
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

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if (preference == mHideScreenshot ||
                preference == mHideSound ||
                preference == mHideAirplaneMode ||
                preference == mHideRebootMenu) {
            int options = 0;
            if (mHideScreenshot.isChecked()) options |= HIDE_SCREENSHOT;
            if (mHideAirplaneMode.isChecked()) options |= HIDE_AIRPLANE;
            if (mHideRebootMenu.isChecked()) options |= HIDE_REBOOT;
            if (mHideSound.isChecked()) options |= HIDE_SOUND;
            Settings.System.putInt(mCr, Settings.System.HIDDEN_POWER_MENU_OPTIONS,
                    options);
        }
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mScreenshotDelay) {
            int value = Integer.parseInt(newValue.toString());
            Settings.System.putInt(mCr, Settings.System.POWER_MENU_SCREENSHOT_DELAY,
                    value);
            return true;
        }
        return false;
    }

    @Override
    public void onToolboxDisabled(boolean enabled) {
        mPrefSet.setEnabled(enabled);
    }

}
