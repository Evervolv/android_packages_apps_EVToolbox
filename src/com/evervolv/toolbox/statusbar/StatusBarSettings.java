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
import androidx.preference.SwitchPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import android.text.format.DateFormat;
import android.text.TextUtils;
import android.view.View;

import evervolv.provider.EVSettings;

import com.android.settingslib.graph.BatteryMeterDrawableBase;
import com.evervolv.toolbox.R;
import com.evervolv.toolbox.SettingsPreferenceFragment;

public class StatusBarSettings extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener {

    private static final String STATUS_BAR_QUICK_QS_PULLDOWN = "qs_quick_pulldown";
    private static final String STATUS_BAR_LTE_ICON = "show_lte_fourgee";
    private static final String STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style";
    private static final String SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";

    private static final int PULLDOWN_DIR_NONE = 0;
    private static final int PULLDOWN_DIR_RIGHT = 1;
    private static final int PULLDOWN_DIR_LEFT = 2;

    private static final int BATTERY_STYLE_TEXT = 5;

    private ListPreference mQuickPulldown;
    private ListPreference mBatteryStyleIcon;
    private ListPreference mBatteryShowPercent;

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

        int batteryPct = Settings.System.getInt(getContext().getContentResolver(),
                Settings.System.SHOW_BATTERY_PERCENT, 0);
        mBatteryShowPercent = (ListPreference) findPreference(SHOW_BATTERY_PERCENT);
        mBatteryShowPercent.setValue(String.valueOf(batteryPct));
        mBatteryShowPercent.setSummary(mBatteryShowPercent.getEntry());
        mBatteryShowPercent.setOnPreferenceChangeListener(this);
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
            mBatteryShowPercent.setEnabled(value != BATTERY_STYLE_TEXT);
            return true;
        }
        if (preference == mBatteryShowPercent) {
            int value = Integer.parseInt((String) newValue);
            Settings.System.putInt(getContext().getContentResolver(),
                    Settings.System.SHOW_BATTERY_PERCENT, value);
            int index = mBatteryShowPercent.findIndexOfValue((String) newValue);
            mBatteryShowPercent.setSummary(mBatteryShowPercent.getEntries()[index]);
            return true;
        }
        return false;
    }

    private void updateQuickPulldownSummary(int value) {
        String summary="";
        switch (value) {
            case PULLDOWN_DIR_NONE:
                summary = getResources().getString(
                    R.string.status_bar_quick_qs_pulldown_off);
                break;

            case PULLDOWN_DIR_LEFT:
            case PULLDOWN_DIR_RIGHT:
                summary = getResources().getString(
                    R.string.status_bar_quick_qs_pulldown_summary,
                    getResources().getString(value == PULLDOWN_DIR_LEFT
                        ? R.string.status_bar_quick_qs_pulldown_summary_left
                        : R.string.status_bar_quick_qs_pulldown_summary_right));
                break;
        }
        mQuickPulldown.setSummary(summary);
    }
}
