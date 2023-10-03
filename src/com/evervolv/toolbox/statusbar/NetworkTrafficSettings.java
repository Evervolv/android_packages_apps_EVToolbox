/*
 * SPDX-FileCopyrightText: 2017-2023 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.evervolv.toolbox.statusbar;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.SettingsPreferenceFragment;

import evervolv.preference.EVSecureSettingSwitchPreference;
import evervolv.provider.EVSettings;

public class NetworkTrafficSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener  {

    private static final String TAG = "NetworkTrafficSettings";
    private static final String STATUS_BAR_CLOCK_STYLE = "status_bar_clock";

    private ListPreference mNetTrafficMode;
    private EVSecureSettingSwitchPreference mNetTrafficAutohide;
    private ListPreference mNetTrafficUnits;
    private EVSecureSettingSwitchPreference mNetTrafficShowUnits;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.network_traffic_settings);
        final ContentResolver resolver = getActivity().getContentResolver();

        mNetTrafficMode = findPreference(EVSettings.Secure.NETWORK_TRAFFIC_MODE);
        mNetTrafficMode.setOnPreferenceChangeListener(this);
        int mode = EVSettings.Secure.getInt(resolver,
                EVSettings.Secure.NETWORK_TRAFFIC_MODE, 0);
        mNetTrafficMode.setValue(String.valueOf(mode));

        mNetTrafficAutohide = findPreference(EVSettings.Secure.NETWORK_TRAFFIC_AUTOHIDE);
        mNetTrafficAutohide.setOnPreferenceChangeListener(this);

        mNetTrafficUnits = findPreference(EVSettings.Secure.NETWORK_TRAFFIC_UNITS);
        mNetTrafficUnits.setOnPreferenceChangeListener(this);
        int units = EVSettings.Secure.getInt(resolver,
                EVSettings.Secure.NETWORK_TRAFFIC_UNITS, /* Mbps */ 1);
        mNetTrafficUnits.setValue(String.valueOf(units));

        mNetTrafficShowUnits = findPreference(EVSettings.Secure.NETWORK_TRAFFIC_SHOW_UNITS);
        mNetTrafficShowUnits.setOnPreferenceChangeListener(this);

        updateEnabledStates(mode);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mNetTrafficMode) {
            int mode = Integer.parseInt((String) newValue);
            EVSettings.Secure.putInt(getActivity().getContentResolver(),
                    EVSettings.Secure.NETWORK_TRAFFIC_MODE, mode);
            updateEnabledStates(mode);
        } else if (preference == mNetTrafficUnits) {
            int units = Integer.parseInt((String) newValue);
            EVSettings.Secure.putInt(getActivity().getContentResolver(),
                    EVSettings.Secure.NETWORK_TRAFFIC_UNITS, units);
        }
        return true;
    }

    private void updateEnabledStates(int mode) {
        final boolean enabled = mode != 0;
        mNetTrafficAutohide.setEnabled(enabled);
        mNetTrafficUnits.setEnabled(enabled);
        mNetTrafficShowUnits.setEnabled(enabled);
    }

    public static final SummaryProvider SUMMARY_PROVIDER = new SummaryProvider() {
        @Override
        public String getSummary(Context context, String key) {
            final int mode = EVSettings.Secure.getInt(context.getContentResolver(),
                    EVSettings.Secure.NETWORK_TRAFFIC_MODE, 0);
            switch (mode) {
                case 1:
                    return context.getResources()
                        .getString(R.string.network_traffic_mode_up);
                case 2:
                    return context.getResources()
                        .getString(R.string.network_traffic_mode_down);
                case 3:
                    return context.getResources()
                        .getString(R.string.network_traffic_mode_all);
                default:
                    return context.getResources()
                        .getString(R.string.network_traffic_mode_disable);
            }
        }
    };
}
