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
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.Toolbox;
import com.evervolv.toolbox.ToolboxPreferenceFragment;
import com.evervolv.toolbox.preference.CustomSeekBarPreference;

public class InterfacePowerMenu extends ToolboxPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String PREF_HIDE_SCREENSHOT = "pref_power_menu_hide_screenshot";
    private static final String PREF_HIDE_SOUND = "pref_power_menu_hide_sound";
    private static final String PREF_HIDE_AIRPLANE_MODE = "pref_power_menu_hide_airplane_mode";
    private static final String PREF_HIDE_REBOOT_MENU = "pref_power_menu_hide_reboot_menu";
    private static final String PREF_SCREENSHOT_DELAY = "pref_power_menu_screenshot_delay";

    private static final int HIDE_REBOOT = 1;
    private static final int HIDE_SCREENSHOT = 2;
    private static final int HIDE_SOUND = 4;
    private static final int HIDE_AIRPLANE = 8;

    private ContentResolver mCr;

    private SwitchPreference mHideScreenshot;
    private SwitchPreference mHideSound;
    private SwitchPreference mHideAirplaneMode;
    private SwitchPreference mHideRebootMenu;
    private CustomSeekBarPreference mScreenshotDelay;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.interface_power_menu);

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
