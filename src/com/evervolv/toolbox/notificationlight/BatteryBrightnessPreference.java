/*
 * SPDX-FileCopyrightText: 2017 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.evervolv.toolbox.notificationlight;

import android.content.Context;
import android.os.UserHandle;
import android.util.AttributeSet;

import evervolv.provider.EVSettings;

public class BatteryBrightnessPreference extends BrightnessPreference {
    private static String TAG = "BatteryBrightnessPreference";

    private Context mContext;

    public BatteryBrightnessPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    protected int getBrightnessSetting() {
        return EVSettings.System.getIntForUser(mContext.getContentResolver(),
                EVSettings.System.BATTERY_LIGHT_BRIGHTNESS_LEVEL,
                LIGHT_BRIGHTNESS_MAXIMUM, UserHandle.USER_CURRENT);
    }

    @Override
    protected void setBrightnessSetting(int brightness) {
        EVSettings.System.putIntForUser(mContext.getContentResolver(),
                EVSettings.System.BATTERY_LIGHT_BRIGHTNESS_LEVEL,
                brightness, UserHandle.USER_CURRENT);
    }
}
