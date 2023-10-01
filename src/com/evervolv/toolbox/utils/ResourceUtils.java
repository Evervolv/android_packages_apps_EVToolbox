/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.evervolv.toolbox.utils;

import android.content.res.Resources;
import android.util.Log;

public class ResourceUtils {

    private static final String TAG = ResourceUtils.class.getSimpleName();

    public static String getLocalizedString(final Resources res,
                                            final String stringName,
                                            final String stringFormat) {
        final String name = stringName.toLowerCase().replace(" ", "_");
        final String nameRes = String.format(stringFormat, name);
        return getStringForResourceName(res, nameRes, stringName);
    }

    public static String getStringForResourceName(final Resources res,
                                                  final String resourceName,
                                                  final String defaultValue) {
        final int resId = res.getIdentifier(resourceName, "string", "com.evervolv.toolbox");
        if (resId <= 0) {
            Log.e(TAG, "No resource found for " + resourceName);
            return defaultValue;
        } else {
            return res.getString(resId);
        }
    }
}
