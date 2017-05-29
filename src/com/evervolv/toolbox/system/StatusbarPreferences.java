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
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.Toolbox;
import com.evervolv.toolbox.ToolboxPreferenceFragment;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class StatusbarPreferences extends ToolboxPreferenceFragment implements
        OnPreferenceChangeListener, Toolbox.DisabledListener {

    private static final String PREF_BATTERY_ICON_COLOR = "pref_battery_icon_color";

    private ColorPickerPreference mBatteryIconColor;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.statusbar);

        mBatteryIconColor = (ColorPickerPreference) findPreference(PREF_BATTERY_ICON_COLOR);
        mBatteryIconColor.setNewPreviewColor(Settings.Secure.getInt(
                getActivity().getContentResolver(),
                Settings.Secure.STATUS_BAR_CHARGE_COLOR, Color.WHITE));
        mBatteryIconColor.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mBatteryIconColor) {
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.STATUS_BAR_CHARGE_COLOR, ((Integer) newValue).intValue());
            return true;
        }
        return false;
    }
}
