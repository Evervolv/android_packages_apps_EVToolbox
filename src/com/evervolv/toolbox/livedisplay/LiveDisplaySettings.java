/*
 * Copyright (C) 2015 The CyanogenMod Project
 *               2017-2019 The LineageOS Project
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
package com.evervolv.toolbox.livedisplay;

import android.content.Context;
import android.content.res.Resources;
import android.hardware.display.ColorDisplayManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.internal.util.ArrayUtils;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.SettingsPreferenceFragment;
import com.evervolv.toolbox.search.BaseSearchIndexProvider;
import com.evervolv.toolbox.search.SearchIndexableRaw;
import com.evervolv.toolbox.search.Searchable;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import evervolv.hardware.LiveDisplayConfig;
import evervolv.hardware.LiveDisplayManager;
import evervolv.preference.SettingsHelper;
import evervolv.provider.EVSettings;

import static evervolv.hardware.LiveDisplayManager.MODE_AUTO;
import static evervolv.hardware.LiveDisplayManager.MODE_OFF;
import static evervolv.hardware.LiveDisplayManager.MODE_DAY;
import static evervolv.hardware.LiveDisplayManager.MODE_NIGHT;
import static evervolv.hardware.LiveDisplayManager.MODE_OUTDOOR;

public class LiveDisplaySettings extends SettingsPreferenceFragment implements Searchable,
        Preference.OnPreferenceChangeListener, SettingsHelper.OnSettingsChangeListener {

    private static final String TAG = "LiveDisplay";

    private static final String KEY_LIVE_DISPLAY = "live_display";
    private static final String KEY_LIVE_DISPLAY_ADVANCED = "live_display_advanced";
    private static final String KEY_LIVE_DISPLAY_AUTO_OUTDOOR_MODE =
            "display_auto_outdoor_mode";
    private static final String KEY_LIVE_DISPLAY_TEMPERATURE = "live_display_color_temperature";

    private final Uri DISPLAY_TEMPERATURE_DAY_URI =
            EVSettings.System.getUriFor(EVSettings.System.DISPLAY_TEMPERATURE_DAY);
    private final Uri DISPLAY_TEMPERATURE_NIGHT_URI =
            EVSettings.System.getUriFor(EVSettings.System.DISPLAY_TEMPERATURE_NIGHT);
    private final Uri DISPLAY_TEMPERATURE_MODE_URI =
            EVSettings.System.getUriFor(EVSettings.System.DISPLAY_TEMPERATURE_MODE);

    private ListPreference mLiveDisplay;
    private SwitchPreference mOutdoorMode;
    private DisplayTemperature mDisplayTemperature;

    private String[] mModeEntries;
    private String[] mModeValues;
    private String[] mModeSummaries;

    private LiveDisplayManager mLiveDisplayManager;
    private LiveDisplayConfig mConfig;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Resources res = getResources();
        final boolean isNightDisplayAvailable =
                ColorDisplayManager.isNightDisplayAvailable(getContext());

        mLiveDisplayManager = LiveDisplayManager.getInstance(getActivity());
        mConfig = mLiveDisplayManager.getConfig();

        addPreferencesFromResource(R.xml.livedisplay);

        int adaptiveMode = mLiveDisplayManager.getMode();

        mLiveDisplay = findPreference(KEY_LIVE_DISPLAY);
        mLiveDisplay.setValue(String.valueOf(adaptiveMode));

        mModeEntries = res.getStringArray(
                com.evervolv.platform.internal.R.array.live_display_entries);
        mModeValues = res.getStringArray(
                com.evervolv.platform.internal.R.array.live_display_values);
        mModeSummaries = res.getStringArray(
                com.evervolv.platform.internal.R.array.live_display_summaries);

        int[] removeIdx = null;
        // Remove outdoor mode from lists if there is no support
        if (!mConfig.hasFeature(MODE_OUTDOOR)) {
            removeIdx = ArrayUtils.appendInt(removeIdx,
                    ArrayUtils.indexOf(mModeValues, String.valueOf(MODE_OUTDOOR)));
        } else if (isNightDisplayAvailable) {
            final int autoIdx = ArrayUtils.indexOf(mModeValues, String.valueOf(MODE_AUTO));
            mModeSummaries[autoIdx] = res.getString(R.string.live_display_outdoor_mode_summary);
        }

        // Remove night display on HWC2
        if (isNightDisplayAvailable) {
            removeIdx = ArrayUtils.appendInt(removeIdx,
                    ArrayUtils.indexOf(mModeValues, String.valueOf(MODE_DAY)));
            removeIdx = ArrayUtils.appendInt(removeIdx,
                    ArrayUtils.indexOf(mModeValues, String.valueOf(MODE_NIGHT)));
        }

        if (removeIdx != null) {
            String[] entriesTemp = new String[mModeEntries.length - removeIdx.length];
            String[] valuesTemp = new String[mModeValues.length - removeIdx.length];
            String[] summariesTemp = new String[mModeSummaries.length - removeIdx.length];
            int j = 0;
            for (int i = 0; i < mModeEntries.length; i++) {
                if (ArrayUtils.contains(removeIdx, i)) {
                    continue;
                }
                entriesTemp[j] = mModeEntries[i];
                valuesTemp[j] = mModeValues[i];
                summariesTemp[j] = mModeSummaries[i];
                j++;
            }
            mModeEntries = entriesTemp;
            mModeValues = valuesTemp;
            mModeSummaries = summariesTemp;
        }

        mLiveDisplay.setEntries(mModeEntries);
        mLiveDisplay.setEntryValues(mModeValues);
        mLiveDisplay.setOnPreferenceChangeListener(this);

        mDisplayTemperature = findPreference(KEY_LIVE_DISPLAY_TEMPERATURE);
        if (isNightDisplayAvailable) {
            if (!mConfig.hasFeature(MODE_OUTDOOR)) {
                getPreferenceScreen().removePreference(mLiveDisplay);
            }
            getPreferenceScreen().removePreference(mDisplayTemperature);
        }

        mOutdoorMode = findPreference(KEY_LIVE_DISPLAY_AUTO_OUTDOOR_MODE);
        if (getPreferenceScreen() != null && mOutdoorMode != null
                // MODE_AUTO implies automatic outdoor mode on HWC2
                && (isNightDisplayAvailable || !mConfig.hasFeature(MODE_OUTDOOR))) {
            getPreferenceScreen().removePreference(mOutdoorMode);
            mOutdoorMode = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateModeSummary();
        updateTemperatureSummary();
        SettingsHelper.get(getActivity()).startWatching(this, DISPLAY_TEMPERATURE_DAY_URI,
                DISPLAY_TEMPERATURE_MODE_URI, DISPLAY_TEMPERATURE_NIGHT_URI);
    }

    @Override
    public void onPause() {
        super.onPause();
        SettingsHelper.get(getActivity()).stopWatching(this);
    }

    private void updateModeSummary() {
        int mode = mLiveDisplayManager.getMode();

        int index = ArrayUtils.indexOf(mModeValues, String.valueOf(mode));
        if (index < 0) {
            index = ArrayUtils.indexOf(mModeValues, String.valueOf(MODE_OFF));
        }

        mLiveDisplay.setSummary(mModeSummaries[index]);
        mLiveDisplay.setValue(String.valueOf(mode));

        if (mDisplayTemperature != null) {
            mDisplayTemperature.setEnabled(mode != MODE_OFF);
        }
        if (mOutdoorMode != null) {
            mOutdoorMode.setEnabled(mode != MODE_OFF);
        }
    }

    private void updateTemperatureSummary() {
        int day = mLiveDisplayManager.getDayColorTemperature();
        int night = mLiveDisplayManager.getNightColorTemperature();

        mDisplayTemperature.setSummary(getResources().getString(
                R.string.live_display_color_temperature_summary,
                mDisplayTemperature.roundUp(day),
                mDisplayTemperature.roundUp(night)));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mLiveDisplay) {
            mLiveDisplayManager.setMode(Integer.valueOf((String)objValue));
        }
        return true;
    }

    @Override
    public void onSettingsChanged(Uri uri) {
        updateModeSummary();
        updateTemperatureSummary();
    }


    public static final Searchable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {

        @Override
        public Set<String> getNonIndexableKeys(Context context) {
            final LiveDisplayConfig config = LiveDisplayManager.getInstance(context).getConfig();
            final Set<String> result = new ArraySet<String>();

            if (!config.hasFeature(MODE_OUTDOOR)) {
                result.add(KEY_LIVE_DISPLAY_AUTO_OUTDOOR_MODE);
            }
            if (ColorDisplayManager.isNightDisplayAvailable(context)) {
                if (!config.hasFeature(MODE_OUTDOOR)) {
                    result.add(KEY_LIVE_DISPLAY);
                }
                result.add(KEY_LIVE_DISPLAY_TEMPERATURE);
            }
            if (!context.getResources().getBoolean(
                    com.evervolv.platform.internal.R.bool.config_enableLiveDisplay)) {
                result.add(KEY_LIVE_DISPLAY_TEMPERATURE);
                result.add(KEY_LIVE_DISPLAY);
            }

            return result;
        }
    };
}
