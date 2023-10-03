/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod project
 * SPDX-FileCopyrightText: 2017-2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.evervolv.toolbox.notificationlight;

import android.os.Bundle;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.SettingsPreferenceFragment;

public class EdgeLightSettings extends SettingsPreferenceFragment {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.edge_light_settings);
    }
}
