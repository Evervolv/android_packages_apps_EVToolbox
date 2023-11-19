/*
 * Copyright (C) 2023 The LineageOS Project
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

package com.evervolv.toolbox.health;

import static evervolv.health.HealthInterface.MODE_AUTO;
import static evervolv.health.HealthInterface.MODE_MANUAL;
import static evervolv.health.HealthInterface.MODE_LIMIT;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.ArraySet;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.evervolv.settingslib.widget.EVSystemSettingMainSwitchPreference;
import com.evervolv.toolbox.R;
import com.evervolv.toolbox.SettingsPreferenceFragment;
import com.evervolv.toolbox.search.BaseSearchIndexProvider;
import com.evervolv.toolbox.search.Searchable;

import evervolv.health.HealthInterface;
import evervolv.provider.EVSettings;
import evervolv.preference.EVSystemSettingListPreference;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

public class ChargingControlSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Searchable {
    private static final String TAG = ChargingControlSettings.class.getSimpleName();

    private static final String CHARGING_CONTROL_PREF = "charging_control";
    private static final String CHARGING_CONTROL_ENABLED_PREF = "charging_control_enabled";
    private static final String CHARGING_CONTROL_MODE_PREF = "charging_control_mode";
    private static final String CHARGING_CONTROL_START_TIME_PREF = "charging_control_start_time";
    private static final String CHARGING_CONTROL_TARGET_TIME_PREF = "charging_control_target_time";
    private static final String CHARGING_CONTROL_LIMIT_PREF = "charging_control_charging_limit";

    private EVSystemSettingMainSwitchPreference mChargingControlEnabledPref;
    private EVSystemSettingListPreference mChargingControlModePref;
    private StartTimePreference mChargingControlStartTimePref;
    private TargetTimePreference mChargingControlTargetTimePref;
    private ChargingLimitPreference mChargingControlLimitPref;

    private HealthInterface mHealthInterface;

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Resources res = getResources();

        addPreferencesFromResource(R.xml.charging_control_settings);
        getActivity().getActionBar().setTitle(R.string.charging_control_title);

        mHealthInterface = HealthInterface.getInstance(getActivity());

        final PreferenceScreen prefSet = getPreferenceScreen();

        mChargingControlEnabledPref = prefSet.findPreference(CHARGING_CONTROL_ENABLED_PREF);
        mChargingControlEnabledPref.setOnPreferenceChangeListener(this);
        mChargingControlModePref = prefSet.findPreference(CHARGING_CONTROL_MODE_PREF);
        mChargingControlModePref.setOnPreferenceChangeListener(this);
        mChargingControlStartTimePref = prefSet.findPreference(CHARGING_CONTROL_START_TIME_PREF);
        mChargingControlTargetTimePref = prefSet.findPreference(CHARGING_CONTROL_TARGET_TIME_PREF);
        mChargingControlLimitPref = prefSet.findPreference(CHARGING_CONTROL_LIMIT_PREF);

        if (mChargingControlLimitPref != null) {
            boolean allowFineGrainedSettings = mHealthInterface.allowFineGrainedSettings();
            if (allowFineGrainedSettings) {
                mChargingControlModePref.setEntries(concatStringArrays(
                        mChargingControlModePref.getEntries(),
                        res.getStringArray(
                                R.array.charging_control_mode_entries_battery_bypass_supported)));
                mChargingControlModePref.setEntryValues(concatStringArrays(
                        mChargingControlModePref.getEntryValues(),
                        res.getStringArray(
                                R.array.charging_control_mode_values_battery_bypass_supported)));
            }
        }

        setHasOptionsMenu(true);

        watch(EVSettings.System.getUriFor(EVSettings.System.CHARGING_CONTROL_ENABLED));
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshUi();
    }

    private void refreshValues() {
        if (mChargingControlEnabledPref != null) {
            mChargingControlEnabledPref.setChecked(mHealthInterface.getEnabled());
        }

        if (mChargingControlModePref != null) {
            final int chargingControlMode = mHealthInterface.getMode();
            mChargingControlModePref.setValue(Integer.toString(chargingControlMode));
            refreshUi();
        }

        if (mChargingControlStartTimePref != null) {
            mChargingControlStartTimePref.setValue(
                    mChargingControlStartTimePref.getTimeSetting());
        }

        if (mChargingControlTargetTimePref != null) {
            mChargingControlTargetTimePref.setValue(
                    mChargingControlTargetTimePref.getTimeSetting());
        }

        if (mChargingControlLimitPref != null) {
            mChargingControlLimitPref.setValue(
                    mChargingControlLimitPref.getSetting());
        }
    }

    private void refreshUi() {
        final int chargingControlMode = mHealthInterface.getMode();

        refreshUi(chargingControlMode);
    }

    private void refreshUi(final int chargingControlMode) {
        String summary = null;
        boolean isChargingControlStartTimePrefVisible = false;
        boolean isChargingControlTargetTimePrefVisible = false;
        boolean isChargingControlLimitPrefVisible = false;

        final Resources res = getResources();

        switch (chargingControlMode) {
            case MODE_AUTO:
                summary = res.getString(R.string.charging_control_mode_auto_summary);
                break;
            case MODE_MANUAL:
                summary = res.getString(R.string.charging_control_mode_custom_summary);
                isChargingControlStartTimePrefVisible = true;
                isChargingControlTargetTimePrefVisible = true;
                break;
            case MODE_LIMIT:
                summary = res.getString(R.string.charging_control_mode_limit_summary);
                isChargingControlLimitPrefVisible = true;
                break;
            default:
                return;
        }

        mChargingControlModePref.setSummary(summary);

        if (mChargingControlStartTimePref != null) {
            mChargingControlStartTimePref.setVisible(isChargingControlStartTimePrefVisible);
        }

        if (mChargingControlTargetTimePref != null) {
            mChargingControlTargetTimePref.setVisible(isChargingControlTargetTimePrefVisible);
        }

        if (mChargingControlLimitPref != null) {
            mChargingControlLimitPref.setVisible(isChargingControlLimitPrefVisible);
        }
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        menu.add(0, Menu.FIRST, 0, R.string.reset)
                .setIcon(R.drawable.ic_settings_backup_restore)
                .setAlphabeticShortcut('r')
                .setShowAsActionFlags(
                        MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == Menu.FIRST) {
            resetToDefaults();
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(final Preference preference, final Object objValue) {
        if (preference == mChargingControlEnabledPref) {
            mHealthInterface.setEnabled((Boolean) objValue);
        } else if (preference == mChargingControlModePref) {
            final int chargingControlMode = Integer.parseInt((String) objValue);
            mHealthInterface.setMode(chargingControlMode);
            refreshUi(chargingControlMode);
        }
        return true;
    }

    private void resetToDefaults() {
        mHealthInterface.reset();

        refreshValues();
    }

    private CharSequence[] concatStringArrays(CharSequence[] array1, CharSequence[] array2) {
        return Stream.concat(Arrays.stream(array1), Arrays.stream(array2)).toArray(size ->
                (CharSequence[]) Array.newInstance(CharSequence.class, size));
    }

    public static final SummaryProvider SUMMARY_PROVIDER = (context, key) -> {
        if (HealthInterface.isChargingControlSupported(context)) {
            HealthInterface healthInterface = HealthInterface.getInstance(context);
            if (healthInterface.getEnabled()) {
                return context.getString(R.string.enabled);
            }
        }
        return context.getString(R.string.disabled);
    };

    public static final Searchable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {

        @Override
        public Set<String> getNonIndexableKeys(Context context) {
            final Set<String> result = new ArraySet<>();

            if (!HealthInterface.isChargingControlSupported(context)) {
                result.add(CHARGING_CONTROL_PREF);
                result.add(CHARGING_CONTROL_ENABLED_PREF);
                result.add(CHARGING_CONTROL_MODE_PREF);
                result.add(CHARGING_CONTROL_START_TIME_PREF);
                result.add(CHARGING_CONTROL_TARGET_TIME_PREF);
                result.add(CHARGING_CONTROL_LIMIT_PREF);
            }
            return result;
        }
    };
}
