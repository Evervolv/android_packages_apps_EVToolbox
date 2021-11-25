/*
 * Copyright (C) 2017 The Android Open Source Project
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
import android.hardware.display.ColorDisplayManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import evervolv.hardware.DisplayMode;
import evervolv.hardware.HardwareManager;

import com.android.settingslib.widget.LayoutPreference;
import com.android.settingslib.widget.RadioButtonPreference;
import com.evervolv.toolbox.R;
import com.evervolv.toolbox.SettingsPreferenceFragment;
import com.evervolv.toolbox.utils.ResourceUtils;

import java.util.Collections;

public class DisplayModePickerFragment extends SettingsPreferenceFragment implements
        RadioButtonPreference.OnClickListener {

    private static final String COLOR_PROFILE_TITLE = "live_display_color_profile_%s_title";
    private static final String COLOR_PROFILE = "color_profile_";

    private HardwareManager mHardware;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);

        addPreferencesFromResource(R.xml.display_mode_settings);

        final PreferenceScreen screen = getPreferenceScreen();
        screen.removeAll();

        Context context = screen.getContext();
        final LayoutPreference preview = new LayoutPreference(context,
                R.layout.color_mode_preview);
        preview.setSelectable(false);
        screen.addPreference(preview);

        mHardware = HardwareManager.getInstance(context);

        final DisplayMode[] modes = mHardware.getDisplayModes();
        if (modes != null && modes.length > 0) {
            for (int i = 0; i < modes.length; i++) {
                RadioButtonPreference pref = new RadioButtonPreference(context);
                bindPreference(pref, modes[i]);
                screen.addPreference(pref);
            }
        }
        mayCheckOnlyRadioButton();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onRadioButtonClicked(RadioButtonPreference selected) {
        final String selectedKey = selected.getKey();
        if (selectedKey.startsWith(COLOR_PROFILE)) {
            String modeId = selectedKey.replaceFirst(COLOR_PROFILE, "");
            for (DisplayMode mode : mHardware.getDisplayModes()) {
                if (mode.id == Integer.valueOf(modeId)) {
                    mHardware.setDisplayMode(mode, true);
                    updateCheckedState(selectedKey);
                }
            }
        }
    }

    private RadioButtonPreference bindPreference(RadioButtonPreference pref, DisplayMode mode) {
        final DisplayMode defaultMode = mHardware.getCurrentDisplayMode() != null
                    ? mHardware.getCurrentDisplayMode() : mHardware.getDefaultDisplayMode();
        pref.setTitle(ResourceUtils.getLocalizedString(
                    getResources(), mode.name, COLOR_PROFILE_TITLE));
        pref.setKey(COLOR_PROFILE + mode.id);
        if (mode.id == defaultMode.id) {
            pref.setChecked(true);
        }
        pref.setOnClickListener(this);
        return pref;
    }

    private void updateCheckedState(String selectedKey) {
        final PreferenceScreen screen = getPreferenceScreen();
        if (screen != null) {
            final int count = screen.getPreferenceCount();
            for (int i = 0; i < count; i++) {
                final Preference pref = screen.getPreference(i);
                if (pref instanceof RadioButtonPreference) {
                    final RadioButtonPreference radioPref = (RadioButtonPreference) pref;
                    final boolean newCheckedState = TextUtils.equals(pref.getKey(), selectedKey);
                    if (radioPref.isChecked() != newCheckedState) {
                        radioPref.setChecked(TextUtils.equals(pref.getKey(), selectedKey));
                    }
                }
            }
        }
    }

    private void mayCheckOnlyRadioButton() {
        final PreferenceScreen screen = getPreferenceScreen();
        // If there is only 1 thing on screen, select it.
        if (screen != null && screen.getPreferenceCount() == 1) {
            final Preference onlyPref = screen.getPreference(0);
            if (onlyPref instanceof RadioButtonPreference) {
                ((RadioButtonPreference) onlyPref).setChecked(true);
            }
        }
    }

    public static final SummaryProvider SUMMARY_PROVIDER = new SummaryProvider() {
        @Override
        public String getSummary(Context context, String key) {
            final HardwareManager mgr = HardwareManager.getInstance(context);
            final DisplayMode mode = mgr.getCurrentDisplayMode() != null
                    ? mgr.getCurrentDisplayMode() : mgr.getDefaultDisplayMode();
            return ResourceUtils.getLocalizedString(
                    context.getResources(), mode.name, COLOR_PROFILE_TITLE);
        }
    };

}
