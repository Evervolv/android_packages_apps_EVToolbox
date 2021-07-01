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
import android.os.Bundle;
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

import java.util.List;
import java.util.Set;

import evervolv.hardware.LiveDisplayConfig;
import evervolv.hardware.LiveDisplayManager;

import static evervolv.hardware.LiveDisplayManager.FEATURE_CABC;
import static evervolv.hardware.LiveDisplayManager.FEATURE_COLOR_ADJUSTMENT;
import static evervolv.hardware.LiveDisplayManager.FEATURE_COLOR_ENHANCEMENT;
import static evervolv.hardware.LiveDisplayManager.FEATURE_PICTURE_ADJUSTMENT;

public class AdvancedDisplaySettings extends SettingsPreferenceFragment implements Searchable {

    private static final String TAG = "LiveDisplay";

    private static final String KEY_LIVE_DISPLAY_LOW_POWER = "display_low_power";
    private static final String KEY_LIVE_DISPLAY_COLOR_ENHANCE = "display_color_enhance";
    private static final String KEY_DISPLAY_COLOR = "color_calibration";
    private static final String KEY_PICTURE_ADJUSTMENT = "picture_adjustment";

    private SwitchPreference mColorEnhancement;
    private SwitchPreference mLowPower;
    private PictureAdjustment mPictureAdjustment;
    private DisplayColor mDisplayColor;

    private LiveDisplayManager mLiveDisplayManager;
    private LiveDisplayConfig mConfig;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLiveDisplayManager = LiveDisplayManager.getInstance(getActivity());
        mConfig = mLiveDisplayManager.getConfig();

        addPreferencesFromResource(R.xml.advanced_display);

        mLowPower = findPreference(KEY_LIVE_DISPLAY_LOW_POWER);
        if (!mConfig.hasFeature(FEATURE_CABC)) {
            getPreferenceScreen().removePreference(mLowPower);
        }

        mColorEnhancement = findPreference(KEY_LIVE_DISPLAY_COLOR_ENHANCE);
        if (!mConfig.hasFeature(FEATURE_COLOR_ENHANCEMENT)) {
            getPreferenceScreen().removePreference(mColorEnhancement);
        }

        mPictureAdjustment = findPreference(KEY_PICTURE_ADJUSTMENT);
        if (!mConfig.hasFeature(FEATURE_PICTURE_ADJUSTMENT)) {
            getPreferenceScreen().removePreference(mPictureAdjustment);
        }

        mDisplayColor = findPreference(KEY_DISPLAY_COLOR);
        if (!mConfig.hasFeature(FEATURE_COLOR_ADJUSTMENT)) {
            getPreferenceScreen().removePreference(mDisplayColor);
        }
    }

    public static final Searchable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {

        @Override
        public Set<String> getNonIndexableKeys(Context context) {
            final LiveDisplayConfig config = LiveDisplayManager.getInstance(context).getConfig();
            final Set<String> result = new ArraySet<String>();

            if (!config.hasFeature(FEATURE_COLOR_ENHANCEMENT)) {
                result.add(KEY_LIVE_DISPLAY_COLOR_ENHANCE);
            }
            if (!config.hasFeature(FEATURE_CABC)) {
                result.add(KEY_LIVE_DISPLAY_LOW_POWER);
            }
            if (!config.hasFeature(FEATURE_COLOR_ADJUSTMENT)) {
                result.add(KEY_DISPLAY_COLOR);
            }
            if (!config.hasFeature(FEATURE_PICTURE_ADJUSTMENT)) {
                result.add(KEY_PICTURE_ADJUSTMENT);
            }

            return result;
        }
    };
}
