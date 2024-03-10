/*
 * Copyright (C) 2014-2015 The CyanogenMod Project
 *               2017-2018 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.evervolv.toolbox.statusbar;

import android.os.Bundle;
import android.provider.Settings;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreferenceCompat;
import android.view.View;

import evervolv.provider.EVSettings;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.SettingsPreferenceFragment;

public class StatusBarSettings extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener {

    private static final String STATUS_BAR_QUICK_QS_PULLDOWN = "qs_quick_pulldown";
    private static final String STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style";
    private static final String STATUS_BAR_BATTERY_PERCENT = "status_bar_battery_percentage";

    private static final String CATEGORY_BATTERY = "status_bar_battery";
    private static final String CATEGORY_ICONS = "status_bar_icons";
    private static final String CATEGORY_QUICK_SETTINGS = "status_bar_quick_settings";

    private ListPreference mQuickPulldown;
    private ListPreference mBatteryStyleIcon;
    private SwitchPreferenceCompat mBatteryShowPercent;
    private boolean mShowPercentAvailable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.status_bar_settings);

        int qsPullDown = EVSettings.System.getInt(getContext().getContentResolver(),
                EVSettings.System.STATUS_BAR_QUICK_QS_PULLDOWN, 0);
        mQuickPulldown = (ListPreference) findPreference(STATUS_BAR_QUICK_QS_PULLDOWN);
        mQuickPulldown.setValue(String.valueOf(qsPullDown));
        updateQuickPulldownSummary(qsPullDown);
        mQuickPulldown.setOnPreferenceChangeListener(this);

        int batteryStyle = EVSettings.System.getInt(getContext().getContentResolver(),
                EVSettings.System.STATUS_BAR_BATTERY_STYLE, 0);
        mBatteryStyleIcon = (ListPreference) findPreference(STATUS_BAR_BATTERY_STYLE);
        mBatteryStyleIcon.setValue(String.valueOf(batteryStyle));
        mBatteryStyleIcon.setSummary(mBatteryStyleIcon.getEntry());
        mBatteryStyleIcon.setOnPreferenceChangeListener(this);

        boolean batteryShowPercent = Settings.System.getInt(getContext().getContentResolver(),
                Settings.System.SHOW_BATTERY_PERCENT, 0) == 1;
        mBatteryShowPercent = (SwitchPreferenceCompat) findPreference(STATUS_BAR_BATTERY_PERCENT);
        mBatteryShowPercent.setChecked(batteryShowPercent);
        mBatteryShowPercent.setEnabled(batteryStyle != 5 /* BATTERY_STYLE_TEXT */);

        mShowPercentAvailable = getResources().getBoolean(
                com.android.internal.R.bool.config_battery_percentage_setting_available);
        if (!mShowPercentAvailable) {
            getPreferenceScreen().removePreference(mBatteryShowPercent);
        }

        updateCategories();
    }

    private void updateCategories() {
        final PreferenceScreen prefScreen = getPreferenceScreen();

        final PreferenceGroup quickSettingsCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_QUICK_SETTINGS);
        if (quickSettingsCategory.getPreferenceCount() == 0) {
            prefScreen.removePreference(quickSettingsCategory);
        }

        final PreferenceGroup iconsCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_ICONS);
        if (iconsCategory.getPreferenceCount() == 0) {
            prefScreen.removePreference(iconsCategory);
        }

        final PreferenceGroup batteryCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_BATTERY);
        if (batteryCategory.getPreferenceCount() == 0) {
            prefScreen.removePreference(batteryCategory);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Adjust status bar preferences for RTL
        if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            mQuickPulldown.setEntries(R.array.status_bar_quick_qs_pulldown_entries_rtl);
            mQuickPulldown.setEntryValues(R.array.status_bar_quick_qs_pulldown_values_rtl);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mQuickPulldown) {
            int value = Integer.parseInt((String) newValue);
            EVSettings.System.putInt(getContext().getContentResolver(),
                    EVSettings.System.STATUS_BAR_QUICK_QS_PULLDOWN, value);
            updateQuickPulldownSummary(value);
            return true;
        }
        if (preference == mBatteryStyleIcon) {
            int value = Integer.parseInt((String) newValue);
            EVSettings.System.putInt(getContext().getContentResolver(),
                    EVSettings.System.STATUS_BAR_BATTERY_STYLE, value);
            int index = mBatteryStyleIcon.findIndexOfValue((String) newValue);
            mBatteryStyleIcon.setSummary(mBatteryStyleIcon.getEntries()[index]);
            if (mShowPercentAvailable) {
                mBatteryShowPercent.setEnabled(value != 5 /* BATTERY_STYLE_TEXT */);
            }
            return true;
        }
        return false;
    }

    private void updateQuickPulldownSummary(int value) {
        String summary="";
        if (value == 0) {
            summary = getResources().getString(
                R.string.status_bar_quick_qs_pulldown_off);
        } else {
                summary = getResources().getString(
                    R.string.status_bar_quick_qs_pulldown_summary,
                    getResources().getString(value == 2
                        ? R.string.status_bar_quick_qs_pulldown_summary_left
                        : R.string.status_bar_quick_qs_pulldown_summary_right));
        }
        mQuickPulldown.setSummary(summary);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mBatteryShowPercent) {
            Settings.System.putInt(getContext().getContentResolver(),
                    Settings.System.SHOW_BATTERY_PERCENT,
                    mBatteryShowPercent.isChecked() ? 1 : 0);
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }
}
