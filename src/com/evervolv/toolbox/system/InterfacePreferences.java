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

package com.evervolv.toolbox.system;

import android.content.ContentResolver;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.Toolbox;
import com.evervolv.toolbox.ToolboxPreferenceFragment;
import com.evervolv.toolbox.preference.CustomSeekBarPreference;

public class InterfacePreferences extends ToolboxPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String PREF_HIDE_SCREENSHOT = "pref_power_menu_hide_screenshot";
    private static final String PREF_HIDE_SOUND = "pref_power_menu_hide_sound";
    private static final String PREF_HIDE_AIRPLANE_MODE = "pref_power_menu_hide_airplane_mode";
    private static final String PREF_HIDE_REBOOT_MENU = "pref_power_menu_hide_reboot_menu";
    private static final String PREF_SCREENSHOT_DELAY = "pref_power_menu_screenshot_delay";

    private static final String ROTATION_0_PREF = "pref_rotation_0";
    private static final String ROTATION_90_PREF = "pref_rotation_90";
    private static final String ROTATION_180_PREF = "pref_rotation_180";
    private static final String ROTATION_270_PREF = "pref_rotation_270";

    private static final int HIDE_REBOOT = 1;
    private static final int HIDE_SCREENSHOT = 2;
    private static final int HIDE_SOUND = 4;
    private static final int HIDE_AIRPLANE = 8;

    private static final int ROTATION_0 = 1;
    private static final int ROTATION_90 = 2;
    private static final int ROTATION_180 = 4;
    private static final int ROTATION_270 = 8;

    private ContentResolver mCr;

    private SwitchPreference mHideScreenshot;
    private SwitchPreference mHideSound;
    private SwitchPreference mHideAirplaneMode;
    private SwitchPreference mHideRebootMenu;
    private CustomSeekBarPreference mScreenshotDelay;

    private SwitchPreference mRotation0Pref;
    private SwitchPreference mRotation90Pref;
    private SwitchPreference mRotation180Pref;
    private SwitchPreference mRotation270Pref;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.user_interface);

        PreferenceScreen mPrefSet = getPreferenceScreen();

        mCr = getActivity().getContentResolver();

        int hiddenOptions = Settings.System.getInt(mCr,
                Settings.System.HIDDEN_POWER_MENU_OPTIONS, 0);

        mHideScreenshot = (SwitchPreference) mPrefSet.findPreference(
                PREF_HIDE_SCREENSHOT);
        mHideScreenshot.setChecked((hiddenOptions & HIDE_SCREENSHOT) != 0);

        mHideSound = (SwitchPreference) mPrefSet.findPreference(
                PREF_HIDE_SOUND);
        mHideSound.setChecked((hiddenOptions & HIDE_SOUND) != 0);

        mHideAirplaneMode = (SwitchPreference) mPrefSet.findPreference(
                PREF_HIDE_AIRPLANE_MODE);
        mHideAirplaneMode.setChecked((hiddenOptions & HIDE_AIRPLANE) != 0);

        mHideRebootMenu = (SwitchPreference) mPrefSet.findPreference(
                PREF_HIDE_REBOOT_MENU);
        mHideRebootMenu.setChecked((hiddenOptions & HIDE_REBOOT) != 0);

        mScreenshotDelay = (CustomSeekBarPreference) mPrefSet.findPreference(PREF_SCREENSHOT_DELAY);
        int screenshotDelay = Settings.System.getInt(mCr,
                Settings.System.POWER_MENU_SCREENSHOT_DELAY, 1000);
        mScreenshotDelay.setValue(screenshotDelay / 1);
        mScreenshotDelay.setOnPreferenceChangeListener(this);

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

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mHideScreenshot || preference == mHideSound ||
                preference == mHideAirplaneMode || preference == mHideRebootMenu) {
            int options = 0;
            if (mHideScreenshot.isChecked()) options |= HIDE_SCREENSHOT;
            if (mHideAirplaneMode.isChecked()) options |= HIDE_AIRPLANE;
            if (mHideRebootMenu.isChecked()) options |= HIDE_REBOOT;
            if (mHideSound.isChecked()) options |= HIDE_SOUND;
            Settings.System.putInt(mCr, Settings.System.HIDDEN_POWER_MENU_OPTIONS,
                    options);
            return true;
        }
        if (preference == mRotation0Pref || preference == mRotation90Pref ||
                preference == mRotation180Pref || preference == mRotation270Pref) {
            int angles = 0;
            if (mRotation0Pref.isChecked()) angles |= ROTATION_0;
            if (mRotation90Pref.isChecked()) angles |= ROTATION_90;
            if (mRotation180Pref.isChecked()) angles |= ROTATION_180;
            if (mRotation270Pref.isChecked()) angles |= ROTATION_270;

            Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.ACCELEROMETER_ROTATION_ANGLES, angles);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mScreenshotDelay) {
            int value = (Integer) newValue;
            Settings.System.putInt(mCr,
                Settings.System.POWER_MENU_SCREENSHOT_DELAY, value * 1);
            return true;
        }
        return false;
    }
}
