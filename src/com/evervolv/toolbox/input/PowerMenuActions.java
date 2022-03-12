/*
 * Copyright (C) 2014-2015 The CyanogenMod Project
 *               2017-2022 The LineageOS Project
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

package com.evervolv.toolbox.input;

import android.Manifest;
import android.content.Context;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.service.controls.ControlsProviderService;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;

import com.android.settingslib.applications.ServiceListing;

import evervolv.app.GlobalActionManager;
import evervolv.provider.EVSettings;
import com.evervolv.internal.util.PowerMenuConstants;
import com.evervolv.toolbox.R;
import com.evervolv.toolbox.SettingsPreferenceFragment;
import com.evervolv.toolbox.utils.DeviceUtils;

import java.util.List;

import static com.evervolv.internal.util.PowerMenuConstants.*;

public class PowerMenuActions extends SettingsPreferenceFragment {
    final static String TAG = "PowerMenuActions";

    private static final String CATEGORY_POWER_MENU_ITEMS = "power_menu_items";

    private PreferenceCategory mPowerMenuItemsCategory;

    private CheckBoxPreference mScreenshotPref;
    private CheckBoxPreference mAirplanePref;
    private CheckBoxPreference mUsersPref;
    private CheckBoxPreference mBugReportPref;
    private CheckBoxPreference mEmergencyPref;
    private CheckBoxPreference mDeviceControlsPref;

    private GlobalActionManager mGlobalActionManager;

    Context mContext;
    private UserManager mUserManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.power_menu_settings);
        mContext = getActivity().getApplicationContext();
        mUserManager = UserManager.get(mContext);
        mGlobalActionManager = GlobalActionManager.getInstance(mContext);

        mPowerMenuItemsCategory = findPreference(CATEGORY_POWER_MENU_ITEMS);

        for (String action : PowerMenuConstants.getAllActions()) {
            switch (action) {
                case GLOBAL_ACTION_KEY_SCREENSHOT:
                    mScreenshotPref = findPreference(action);
                    break;
                case GLOBAL_ACTION_KEY_AIRPLANE:
                    mAirplanePref = findPreference(action);
                    break;
                case GLOBAL_ACTION_KEY_USERS:
                    mUsersPref = findPreference(action);
                    break;
                case GLOBAL_ACTION_KEY_BUGREPORT:
                    mBugReportPref = findPreference(action);
                    break;
                case GLOBAL_ACTION_KEY_EMERGENCY:
                    mEmergencyPref = findPreference(action);
                    break;
                case GLOBAL_ACTION_KEY_DEVICECONTROLS:
                    mDeviceControlsPref = findPreference(action);
                    break;
            }
        }

        if (!DeviceUtils.isVoiceCapable(getActivity())) {
            mPowerMenuItemsCategory.removePreference(mEmergencyPref);
            mEmergencyPref = null;
        }

        mGlobalActionManager.getLocalUserConfig();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mScreenshotPref != null) {
            mScreenshotPref.setChecked(mGlobalActionManager.userConfigContains(
                    GLOBAL_ACTION_KEY_SCREENSHOT));
        }

        if (mAirplanePref != null) {
            mAirplanePref.setChecked(mGlobalActionManager.userConfigContains(
                    GLOBAL_ACTION_KEY_AIRPLANE));
        }

        if (mUsersPref != null) {
            if (!UserHandle.MU_ENABLED || !UserManager.supportsMultipleUsers()) {
                mPowerMenuItemsCategory.removePreference(mUsersPref);
                mUsersPref = null;
            } else {
                List<UserInfo> users = mUserManager.getUsers();
                final boolean enabled = (users.size() > 1);
                mUsersPref.setChecked(mGlobalActionManager.userConfigContains(
                        GLOBAL_ACTION_KEY_USERS) && enabled);
                mUsersPref.setEnabled(enabled);
            }
        }

        if (mBugReportPref != null) {
            mBugReportPref.setChecked(mGlobalActionManager.userConfigContains(
                    GLOBAL_ACTION_KEY_BUGREPORT));
        }

        if (mEmergencyPref != null) {
            mEmergencyPref.setChecked(mGlobalActionManager.userConfigContains(
                    GLOBAL_ACTION_KEY_EMERGENCY));
        }

        if (mDeviceControlsPref != null) {
            mDeviceControlsPref.setChecked(mGlobalActionManager.userConfigContains(
                    GLOBAL_ACTION_KEY_DEVICECONTROLS));

            // Enable preference if any device control app is installed
            ServiceListing serviceListing = new ServiceListing.Builder(mContext)
                    .setIntentAction(ControlsProviderService.SERVICE_CONTROLS)
                    .setPermission(Manifest.permission.BIND_CONTROLS)
                    .setNoun("Controls Provider")
                    .setSetting("controls_providers")
                    .setTag("controls_providers")
                    .build();
            serviceListing.addCallback(
                    services -> mDeviceControlsPref.setEnabled(!services.isEmpty()));
            serviceListing.reload();
        }

        updatePreferences();
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePreferences();
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mScreenshotPref) {
            final boolean value = mScreenshotPref.isChecked();
            mGlobalActionManager.updateUserConfig(value, GLOBAL_ACTION_KEY_SCREENSHOT);
        } else if (preference == mAirplanePref) {
            final boolean value = mAirplanePref.isChecked();
            mGlobalActionManager.updateUserConfig(value, GLOBAL_ACTION_KEY_AIRPLANE);
        } else if (preference == mUsersPref) {
            final boolean value = mUsersPref.isChecked();
            mGlobalActionManager.updateUserConfig(value, GLOBAL_ACTION_KEY_USERS);
        } else if (preference == mBugReportPref) {
            final boolean value = mBugReportPref.isChecked();
            mGlobalActionManager.updateUserConfig(value, GLOBAL_ACTION_KEY_BUGREPORT);
            Settings.Global.putInt(getContentResolver(),
                    Settings.Global.BUGREPORT_IN_POWER_MENU, value ? 1 : 0);
        } else if (preference == mEmergencyPref) {
            final boolean value = mEmergencyPref.isChecked();
            mGlobalActionManager.updateUserConfig(value, GLOBAL_ACTION_KEY_EMERGENCY);
        } else if (preference == mDeviceControlsPref) {
            final boolean value = mDeviceControlsPref.isChecked();
            mGlobalActionManager.updateUserConfig(value, GLOBAL_ACTION_KEY_DEVICECONTROLS);
        } else {
            return super.onPreferenceTreeClick(preference);
        }
        return true;
    }

    private void updatePreferences() {
        UserInfo currentUser = mUserManager.getUserInfo(UserHandle.myUserId());
        final boolean developmentSettingsEnabled = Settings.Global.getInt(
                getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1;
        final boolean bugReport = Settings.Global.getInt(
                getContentResolver(), Settings.Global.BUGREPORT_IN_POWER_MENU, 0) == 1;
        final boolean isPrimaryUser = currentUser == null || currentUser.isPrimary();
        if (mBugReportPref != null) {
            mBugReportPref.setEnabled(developmentSettingsEnabled && isPrimaryUser);
            if (!developmentSettingsEnabled) {
                mBugReportPref.setChecked(false);
                mBugReportPref.setSummary(R.string.power_menu_bug_report_devoptions_unavailable);
            } else if (!isPrimaryUser) {
                mBugReportPref.setChecked(false);
                mBugReportPref.setSummary(R.string.power_menu_bug_report_unavailable_for_user);
            } else {
                mBugReportPref.setChecked(bugReport);
                mBugReportPref.setSummary(null);
            }
        }
    }
}
