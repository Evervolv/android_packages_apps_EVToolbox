/*
 * Copyright (C) 2016 The CyanogenMod project
 *               2017-2020 The LineageOS project
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
package com.evervolv.toolbox.utils;

import android.content.Context;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import static com.evervolv.internal.util.DeviceKeysConstants.*;

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
}
