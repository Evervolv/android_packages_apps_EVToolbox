/*
 * SPDX-FileCopyrightText: 2023 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.evervolv.toolbox.health;

import android.content.Context;
import android.util.AttributeSet;

import com.evervolv.toolbox.R;

import evervolv.health.HealthInterface;

public class TargetTimePreference extends TimePreference {
    private static final String TAG = TargetTimePreference.class.getSimpleName();

    private HealthInterface mHealthInterface;

    public TargetTimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHealthInterface = HealthInterface.getInstance(context);
    }

    @Override
    protected int getSummaryResourceId() {
        return R.string.charging_control_target_time_summary;
    }

    @Override
    protected int getTimeSetting() {
        return mHealthInterface.getTargetTime();
    }

    @Override
    protected void setTimeSetting(int secondOfDay) {
        mHealthInterface.setTargetTime(secondOfDay);
    }
}
