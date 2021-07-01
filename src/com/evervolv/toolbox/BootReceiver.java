/*
 * Copyright (C) 2012 The CyanogenMod Project
 *               2017-2019,2021 The LineageOS project
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

package com.evervolv.toolbox;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

import com.evervolv.toolbox.gestures.TouchscreenGestureSettings;
import com.evervolv.toolbox.input.ButtonSettings;
import com.evervolv.toolbox.livedisplay.AdvancedDisplaySettings;

import evervolv.hardware.LiveDisplayConfig;
import evervolv.hardware.LiveDisplayManager;

import static evervolv.hardware.LiveDisplayManager.FEATURE_CABC;
import static evervolv.hardware.LiveDisplayManager.FEATURE_COLOR_ADJUSTMENT;
import static evervolv.hardware.LiveDisplayManager.FEATURE_COLOR_ENHANCEMENT;
import static evervolv.hardware.LiveDisplayManager.FEATURE_PICTURE_ADJUSTMENT;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";
    private static final String ONE_TIME_TUNABLE_RESTORE = "hardware_tunable_restored";

    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (!hasRestoredTunable(ctx)) {
            /* Restore the hardware tunable values */
            ButtonSettings.restoreKeyDisabler(ctx);
            setRestoredTunable(ctx);
        }

        ButtonSettings.restoreKeySwapper(ctx);
        TouchscreenGestureSettings.restoreTouchscreenGestureStates(ctx);

        final boolean advancedDisplay = isAdvancedDisplaySupported(ctx);
        if (!advancedDisplay) {
            ctx.getPackageManager().setComponentEnabledSetting(
                    new ComponentName(ctx, AdvancedDisplaySettings.class.getName()),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }
    }

    private boolean hasRestoredTunable(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(ONE_TIME_TUNABLE_RESTORE, false);
    }

    private void setRestoredTunable(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean(ONE_TIME_TUNABLE_RESTORE, true).apply();
    }

    private boolean isAdvancedDisplaySupported(Context context) {
        final boolean isAvailable = context.getResources().getBoolean(
                com.evervolv.platform.internal.R.bool.config_enableLiveDisplay);
        if (!isAvailable) {
            return false;
        }

        final LiveDisplayConfig config = LiveDisplayManager.getInstance(context).getConfig();
        return config.hasFeature(FEATURE_CABC) || config.hasFeature(FEATURE_COLOR_ENHANCEMENT) ||
                config.hasFeature(FEATURE_PICTURE_ADJUSTMENT) || config.hasFeature(FEATURE_COLOR_ADJUSTMENT);
    }
}
