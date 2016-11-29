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
import android.graphics.Color;
import android.os.Bundle;
import android.preference.SwitchPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.Toolbox;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class StatusbarGeneral extends PreferenceFragment implements
        OnPreferenceChangeListener,
        Toolbox.DisabledListener {

    private static final String STATUSBAR_BATTERY_STYLE = "pref_statusbar_batt_style";
    private static final String STATUSBAR_BATTERY_PERCENT = "pref_statusbar_batt_percent_style";
    private static final String STATUSBAR_BATTERY_STYLE_TILE = "pref_status_bar_battery_style_tile";
    private static final String STATUSBAR_CHARGE_COLOR = "pref_status_bar_charge_color";
    private static final String STATUSBAR_BATTERY_CHARGE_TEXT = "pref_status_bar_battery_charge_text";
    private static final String STATUSBAR_BATTERY_TEXT_SYMBOL = "pref_status_bar_battery_text_symbol";
    private static final String STATUSBAR_CLOCK_AM_PM_STYLE = "pref_statusbar_clock_am_pm_style";
    private static final String STATUSBAR_QUICK_PULLDOWN = "pref_statusbar_quick_pulldown";

    private static final int BATT_STYLE_DEFAULT = 0;
    private static final int BATT_PERCENT_DEFAULT = 0;
    private static final int AM_PM_STYLE_DEFAULT = 2;

    private ContentResolver mCr;
    private PreferenceScreen mPrefSet;

    private ColorPickerPreference mChargeColor;
    private SwitchPreference mQuickPulldown;
    private SwitchPreference mQsBatteryTitle;
    private SwitchPreference mForceChargeText;
    private ListPreference mBattStyle;
    private ListPreference mBattPercent;
    private ListPreference mClockAmPmStyle;
    private ListPreference mTextChargingSymbol;

    private int mBattStyleValue;
    private int mBattPercentValue;
    private int mTextChargingSymbolValue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.statusbar_general);

        mPrefSet = getPreferenceScreen();

        mCr = getActivity().getContentResolver();

        /* Battery Icon Style */
        mBattStyle = (ListPreference) mPrefSet.findPreference(STATUSBAR_BATTERY_STYLE);
        mBattStyleValue = Settings.Secure.getInt(mCr,
                Settings.Secure.STATUS_BAR_BATTERY_STYLE, BATT_STYLE_DEFAULT);
        mBattStyle.setValue(Integer.toString(mBattStyleValue));
        mBattStyle.setOnPreferenceChangeListener(this);

        /* Battery Percent */
        mBattPercent = (ListPreference) mPrefSet.findPreference(STATUSBAR_BATTERY_PERCENT);
        mBattPercentValue = Settings.Secure.getInt(mCr,
                Settings.Secure.STATUS_BAR_SHOW_BATTERY_PERCENT, BATT_PERCENT_DEFAULT);
        mBattPercent.setValue(Integer.toString(mBattPercentValue));
        mBattPercent.setOnPreferenceChangeListener(this);

        mQsBatteryTitle = (SwitchPreference) findPreference(STATUSBAR_BATTERY_STYLE_TILE);
        mQsBatteryTitle.setChecked((Settings.Secure.getInt(mCr,
                Settings.Secure.STATUS_BAR_BATTERY_STYLE_TILE, 1) == 1));

        mChargeColor = (ColorPickerPreference) findPreference(STATUSBAR_CHARGE_COLOR);
        mChargeColor.setNewPreviewColor(Settings.Secure.getInt(mCr,
                Settings.Secure.STATUS_BAR_CHARGE_COLOR, Color.WHITE));
        mChargeColor.setOnPreferenceChangeListener(this);

        mForceChargeText = (SwitchPreference) findPreference(STATUSBAR_BATTERY_CHARGE_TEXT);
        mForceChargeText.setChecked((Settings.Secure.getInt(mCr,
                Settings.Secure.FORCE_CHARGE_BATTERY_TEXT, 1) == 1));

        mTextChargingSymbol = (ListPreference) findPreference(STATUSBAR_BATTERY_TEXT_SYMBOL);
        mTextChargingSymbolValue = Settings.Secure.getInt(mCr,
                Settings.Secure.TEXT_CHARGING_SYMBOL, 0);
        mTextChargingSymbol.setValue(Integer.toString(mTextChargingSymbolValue));
        mTextChargingSymbol.setSummary(mTextChargingSymbol.getEntry());
        mTextChargingSymbol.setOnPreferenceChangeListener(this);

        /* Clock AM/PM Style */
        mClockAmPmStyle = (ListPreference) mPrefSet.findPreference(STATUSBAR_CLOCK_AM_PM_STYLE);
        mClockAmPmStyle.setValue(Integer.toString(Settings.System.getInt(mCr,
                Settings.System.STATUSBAR_CLOCK_AM_PM_STYLE, AM_PM_STYLE_DEFAULT)));
        mClockAmPmStyle.setOnPreferenceChangeListener(this);

        /* Quick Pulldown */
        mQuickPulldown = (SwitchPreference) mPrefSet.findPreference(STATUSBAR_QUICK_PULLDOWN);
        mQuickPulldown.setChecked(Settings.System.getInt(mCr,
                Settings.System.STATUS_BAR_QUICK_QS_PULLDOWN, 1) == 1);

        enableStatusBarBatteryDependents();
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
        if (preference == mBattStyle) {
            mBattStyleValue = Integer.valueOf((String) newValue);
            Settings.Secure.putInt(mCr, Settings.Secure.STATUS_BAR_BATTERY_STYLE,
                    mBattStyleValue);
            enableStatusBarBatteryDependents();
            return true;
        }
        if (preference == mBattPercent) {
            mBattPercentValue = Integer.valueOf((String) newValue);
            Settings.Secure.putInt(mCr, Settings.Secure.STATUS_BAR_SHOW_BATTERY_PERCENT,
                    mBattPercentValue);
            enableStatusBarBatteryDependents();
            return true;
        }
        if (preference == mClockAmPmStyle) {
            Settings.System.putInt(mCr, Settings.System.STATUSBAR_CLOCK_AM_PM_STYLE,
                    Integer.valueOf((String) newValue));
            return true;
        }
        if (preference.equals(mChargeColor)) {
            Settings.Secure.putInt(mCr, Settings.Secure.STATUS_BAR_CHARGE_COLOR,
                    ((Integer) newValue).intValue());
            enableStatusBarBatteryDependents();
            return true;
        }
        if (preference == mTextChargingSymbol) {
            mTextChargingSymbolValue = Integer.valueOf((String) newValue);
            Settings.Secure.putInt(mCr, Settings.Secure.TEXT_CHARGING_SYMBOL,
                    mTextChargingSymbolValue);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;
        if (preference == mQuickPulldown) {
            value = mQuickPulldown.isChecked();
            Settings.System.putInt(mCr, Settings.System.STATUS_BAR_QUICK_QS_PULLDOWN,
                    value ? 1 : 0);
            return true;
        }
        if  (preference == mQsBatteryTitle) {
            value = mQsBatteryTitle.isChecked();
            Settings.Secure.putInt(mCr, Settings.Secure.STATUS_BAR_BATTERY_STYLE_TILE,
                    value ? 1 : 0);
        }
        if (preference == mForceChargeText) {
            value = mForceChargeText.isChecked();
            Settings.Secure.putInt(mCr, Settings.Secure.FORCE_CHARGE_BATTERY_TEXT,
                    value ? 1 : 0);
            return true;
        }
        return false;
    }

    private void enableStatusBarBatteryDependents() {
        switch (mBattStyleValue) {
        case 0: // PORTRAIT
            mBattPercent.setEnabled(true);
            mQsBatteryTitle.setEnabled(false);
            mChargeColor.setEnabled(true);
            mForceChargeText.setEnabled(mBattPercentValue == 2 ? false : true);
            mTextChargingSymbol.setEnabled(true);
            break;
        case 4: // HIDDEN
            mBattPercent.setEnabled(false);
            mQsBatteryTitle.setEnabled(false);
            mForceChargeText.setEnabled(false);
            mChargeColor.setEnabled(false);
            mTextChargingSymbol.setEnabled(false);
            break;
        case 6: // TEXT
            mBattPercent.setEnabled(false);
            mQsBatteryTitle.setEnabled(false);
            mForceChargeText.setEnabled(false);
            mChargeColor.setEnabled(false);
            mTextChargingSymbol.setEnabled(true);
            break;
        default:
            mBattPercent.setEnabled(true);
            mQsBatteryTitle.setEnabled(true);
            mChargeColor.setEnabled(true);
            mForceChargeText.setEnabled(mBattPercentValue == 2 ? false : true);
            mTextChargingSymbol.setEnabled(true);
            break;
        }
    }

    @Override
    public void onToolboxDisabled(boolean enabled) {
        mPrefSet.setEnabled(enabled);
    }

}
