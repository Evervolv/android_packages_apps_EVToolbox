/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod project
 * SPDX-FileCopyrightText: 2017-2020 The LineageOS project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.evervolv.toolbox.utils;

import static com.evervolv.internal.util.DeviceKeysConstants.*;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

public class DeviceUtils {

    public static int getDeviceKeys(Context context) {
        return context.getResources().getInteger(
                com.evervolv.platform.internal.R.integer.config_deviceHardwareKeys);
    }

    public static int getDeviceWakeKeys(Context context) {
        return context.getResources().getInteger(
                com.evervolv.platform.internal.R.integer.config_deviceHardwareWakeKeys);
    }

    /* returns whether the device has power key or not. */
    public static boolean hasPowerKey() {
        return KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_POWER);
    }

    /* returns whether the device has home key or not. */
    public static boolean hasHomeKey(Context context) {
        return (getDeviceKeys(context) & KEY_MASK_HOME) != 0;
    }

    /* returns whether the device has back key or not. */
    public static boolean hasBackKey(Context context) {
        return (getDeviceKeys(context) & KEY_MASK_BACK) != 0;
    }

    /* returns whether the device has menu key or not. */
    public static boolean hasMenuKey(Context context) {
        return (getDeviceKeys(context) & KEY_MASK_MENU) != 0;
    }

    /* returns whether the device has assist key or not. */
    public static boolean hasAssistKey(Context context) {
        return (getDeviceKeys(context) & KEY_MASK_ASSIST) != 0;
    }

    /* returns whether the device has app switch key or not. */
    public static boolean hasAppSwitchKey(Context context) {
        return (getDeviceKeys(context) & KEY_MASK_APP_SWITCH) != 0;
    }

    /* returns whether the device has camera key or not. */
    public static boolean hasCameraKey(Context context) {
        return (getDeviceKeys(context) & KEY_MASK_CAMERA) != 0;
    }

    /* returns whether the device has volume rocker or not. */
    public static boolean hasVolumeKeys(Context context) {
        return (getDeviceKeys(context) & KEY_MASK_VOLUME) != 0;
    }

    /* returns whether the device can be waken using the home key or not. */
    public static boolean canWakeUsingHomeKey(Context context) {
        return (getDeviceWakeKeys(context) & KEY_MASK_HOME) != 0;
    }

    /* returns whether the device can be waken using the back key or not. */
    public static boolean canWakeUsingBackKey(Context context) {
        return (getDeviceWakeKeys(context) & KEY_MASK_BACK) != 0;
    }

    /* returns whether the device can be waken using the menu key or not. */
    public static boolean canWakeUsingMenuKey(Context context) {
        return (getDeviceWakeKeys(context) & KEY_MASK_MENU) != 0;
    }

    /* returns whether the device can be waken using the assist key or not. */
    public static boolean canWakeUsingAssistKey(Context context) {
        return (getDeviceWakeKeys(context) & KEY_MASK_ASSIST) != 0;
    }

    /* returns whether the device can be waken using the app switch key or not. */
    public static boolean canWakeUsingAppSwitchKey(Context context) {
        return (getDeviceWakeKeys(context) & KEY_MASK_APP_SWITCH) != 0;
    }

    /* returns whether the device can be waken using the camera key or not. */
    public static boolean canWakeUsingCameraKey(Context context) {
        return (getDeviceWakeKeys(context) & KEY_MASK_CAMERA) != 0;
    }

    /* returns whether the device can be waken using the volume rocker or not. */
    public static boolean canWakeUsingVolumeKeys(Context context) {
        return (getDeviceWakeKeys(context) & KEY_MASK_VOLUME) != 0;
    }

    /* returns whether the device supports button backlight adjusment or not. */
    public static boolean hasButtonBacklightSupport(Context context) {
        final boolean buttonBrightnessControlSupported = context.getResources().getInteger(
                com.evervolv.platform.internal.R.integer
                        .config_deviceSupportsButtonBrightnessControl) != 0;

        // All hardware keys besides volume and camera can possibly have a backlight
        return buttonBrightnessControlSupported
                && (hasHomeKey(context) || hasBackKey(context) || hasMenuKey(context)
                || hasAssistKey(context) || hasAppSwitchKey(context));
    }

    /**
     * Returns whether the device is voice-capable (meaning, it is also a phone).
     */
    public static boolean isVoiceCapable(Context context) {
        TelephonyManager telephony = context.getSystemService(TelephonyManager.class);
        return telephony != null && telephony.isVoiceCapable();
    }
}
