/*
 * Copyright (C) 2016 The CyanogenMod project
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

import static android.view.Display.DEFAULT_DISPLAY;

import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.Settings;
import androidx.preference.SwitchPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import android.util.Log;
import android.view.IWindowManager;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.WindowManagerGlobal;

import evervolv.hardware.HardwareManager;
import evervolv.provider.EVSettings;
import com.evervolv.toolbox.R;
import com.evervolv.toolbox.SettingsPreferenceFragment;

import java.util.List;

public class ButtonSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "ButtonSettings";

    private static final String KEY_HOME_LONG_PRESS = "button_home_long_press";
    private static final String KEY_HOME_DOUBLE_TAP = "button_home_double_tap";
    private static final String KEY_MENU_PRESS = "button_menu_press";
    private static final String KEY_MENU_LONG_PRESS = "button_menu_long_press";
    private static final String KEY_ASSIST_PRESS = "button_assist_press";
    private static final String KEY_ASSIST_LONG_PRESS = "button_assist_long_press";
    private static final String KEY_APP_SWITCH_PRESS = "button_app_switch_press";
    private static final String KEY_APP_SWITCH_LONG_PRESS = "button_app_switch_long_press";
    private static final String KEY_VOLUME_PANEL_ON_LEFT = "volume_panel_on_left";

    private static final String DISABLE_NAV_KEYS = "disable_nav_keys";

    private static final String CATEGORY_POWER = "power_key";
    private static final String CATEGORY_HOME = "home_key";
    private static final String CATEGORY_BACK = "back_key";
    private static final String CATEGORY_MENU = "menu_key";
    private static final String CATEGORY_ASSIST = "assist_key";
    private static final String CATEGORY_APPSWITCH = "app_switch_key";
    private static final String CATEGORY_VOLUME = "volume_keys";
    private static final String CATEGORY_NAVBAR = "navigation_bar_category";
    private static final String CATEGORY_CAMERA = "camera_key";

    // Available custom actions to perform on a key press.
    // Must match values for KEY_HOME_LONG_PRESS_ACTION in:
    // vendor/ev/sdk/core/java/evervolv/provider/EVSettings.java
    private enum Action {
        NOTHING,
        MENU,
        APP_SWITCH,
        SEARCH,
        VOICE_SEARCH,
        IN_APP_SEARCH,
        LAUNCH_CAMERA,
        SLEEP,
        LAST_APP,
        SPLIT_SCREEN;

        public static Action fromIntSafe(int id) {
            if (id < NOTHING.ordinal() || id > Action.values().length) {
                return NOTHING;
            }
            return Action.values()[id];
        }

        public static Action fromSettings(ContentResolver cr, String setting, Action def) {
            return fromIntSafe(EVSettings.System.getInt(cr, setting, def.ordinal()));
        }
    }

    // Masks for checking presence of hardware keys.
    // Must match values in frameworks/base/core/res/res/values/config.xml
    public static final int KEY_MASK_HOME = 0x01;
    public static final int KEY_MASK_BACK = 0x02;
    public static final int KEY_MASK_MENU = 0x04;
    public static final int KEY_MASK_ASSIST = 0x08;
    public static final int KEY_MASK_APP_SWITCH = 0x10;
    public static final int KEY_MASK_CAMERA = 0x20;
    public static final int KEY_MASK_VOLUME = 0x40;

    private ListPreference mHomeLongPressAction;
    private ListPreference mHomeDoubleTapAction;
    private ListPreference mMenuPressAction;
    private ListPreference mMenuLongPressAction;
    private ListPreference mAssistPressAction;
    private ListPreference mAssistLongPressAction;
    private ListPreference mAppSwitchPressAction;
    private ListPreference mAppSwitchLongPressAction;

    private SwitchPreference mVolumePanelOnLeft;

    private SwitchPreference mDisableNavigationKeys;
    private PreferenceCategory mNavigationPreferencesCat;
    private Handler mHandler;

    private SwitchPreference mCameraWakeScreen;
    private SwitchPreference mCameraSleepOnRelease;
    private SwitchPreference mCameraLaunch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.button_settings);

        final Resources res = getResources();
        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        final int deviceKeys = res.getInteger(
                com.evervolv.platform.internal.R.integer.config_deviceHardwareKeys);
        final int deviceWakeKeys = res.getInteger(
                com.evervolv.platform.internal.R.integer.config_deviceHardwareWakeKeys);

        final boolean hasPowerKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_POWER);
        final boolean hasHomeKey = (deviceKeys & KEY_MASK_HOME) != 0;
        final boolean hasBackKey = (deviceKeys & KEY_MASK_BACK) != 0;
        final boolean hasMenuKey = (deviceKeys & KEY_MASK_MENU) != 0;
        final boolean hasAssistKey = (deviceKeys & KEY_MASK_ASSIST) != 0;
        final boolean hasAppSwitchKey = (deviceKeys & KEY_MASK_APP_SWITCH) != 0;
        final boolean hasCameraKey = (deviceKeys & KEY_MASK_CAMERA) != 0;
        final boolean hasVolumeKeys = (deviceKeys & KEY_MASK_VOLUME) != 0;

        final boolean showHomeWake = (deviceWakeKeys & KEY_MASK_HOME) != 0;
        final boolean showBackWake = (deviceWakeKeys & KEY_MASK_BACK) != 0;
        final boolean showMenuWake = (deviceWakeKeys & KEY_MASK_MENU) != 0;
        final boolean showAssistWake = (deviceWakeKeys & KEY_MASK_ASSIST) != 0;
        final boolean showAppSwitchWake = (deviceWakeKeys & KEY_MASK_APP_SWITCH) != 0;
        final boolean showCameraWake = (deviceWakeKeys & KEY_MASK_CAMERA) != 0;
        final boolean showVolumeWake = (deviceWakeKeys & KEY_MASK_VOLUME) != 0;

        final PreferenceGroup powerCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_POWER);
        final PreferenceGroup homeCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_HOME);
        final PreferenceGroup backCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_BACK);
        final PreferenceGroup menuCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_MENU);
        final PreferenceGroup assistCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_ASSIST);
        final PreferenceGroup appSwitchCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_APPSWITCH);
        final PreferenceGroup volumeCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_VOLUME);
        final PreferenceGroup cameraCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_CAMERA);

        mHandler = new Handler();

        // Force Navigation bar related options
        mDisableNavigationKeys = (SwitchPreference) findPreference(DISABLE_NAV_KEYS);

        mNavigationPreferencesCat = (PreferenceCategory) findPreference(CATEGORY_NAVBAR);

        Action defaultHomeLongPressAction = Action.fromIntSafe(res.getInteger(
                com.android.internal.R.integer.config_longPressOnHomeBehavior));
        Action defaultHomeDoubleTapAction = Action.fromIntSafe(res.getInteger(
                com.android.internal.R.integer.config_doubleTapOnHomeBehavior));
        Action homeLongPressAction = Action.fromSettings(resolver,
                EVSettings.System.KEY_HOME_LONG_PRESS_ACTION,
                defaultHomeLongPressAction);
        Action homeDoubleTapAction = Action.fromSettings(resolver,
                EVSettings.System.KEY_HOME_DOUBLE_TAP_ACTION,
                defaultHomeDoubleTapAction);

        final HardwareManager hardware = HardwareManager.getInstance(getActivity());

        // Only visible on devices that does not have a navigation bar already
        boolean hasNavigationBar = true;
        boolean supportsKeyDisabler = isKeyDisablerSupported(getActivity());
        try {
            IWindowManager windowManager = WindowManagerGlobal.getWindowManagerService();
            hasNavigationBar = windowManager.hasNavigationBar(getActivity().getDisplayId());
        } catch (RemoteException e) {
            Log.e(TAG, "Error getting navigation bar status");
        }
        if (supportsKeyDisabler) {
            // Remove keys that can be provided by the navbar
            updateDisableNavkeysOption();
            mNavigationPreferencesCat.setEnabled(mDisableNavigationKeys.isChecked());
            updateDisableNavkeysCategories(mDisableNavigationKeys.isChecked());
        } else {
            prefScreen.removePreference(mDisableNavigationKeys);
        }

        boolean advancedReboot = EVSettings.Secure.getInt(
                resolver, EVSettings.Secure.ADVANCED_REBOOT, 0) == 1;
        if (!advancedReboot || !hasPowerKey) {
            prefScreen.removePreference(powerCategory);
        }

        if (hasHomeKey) {
            if (!showHomeWake) {
                homeCategory.removePreference(findPreference(EVSettings.System.HOME_WAKE_SCREEN));
            }
            mHomeLongPressAction = initList(KEY_HOME_LONG_PRESS, homeLongPressAction);
            mHomeDoubleTapAction = initList(KEY_HOME_DOUBLE_TAP, homeDoubleTapAction);
        } else {
            prefScreen.removePreference(homeCategory);
        }

        if (!hasBackKey || !showBackWake) {
            prefScreen.removePreference(backCategory);
        }

        if (hasMenuKey) {
            if (!showMenuWake) {
                menuCategory.removePreference(findPreference(EVSettings.System.MENU_WAKE_SCREEN));
            }

            Action pressAction = Action.fromSettings(resolver,
                    EVSettings.System.KEY_MENU_ACTION, Action.MENU);
            mMenuPressAction = initList(KEY_MENU_PRESS, pressAction);

            Action longPressAction = Action.fromSettings(resolver,
                        EVSettings.System.KEY_MENU_LONG_PRESS_ACTION,
                        hasAssistKey ? Action.NOTHING : Action.SEARCH);
            mMenuLongPressAction = initList(KEY_MENU_LONG_PRESS, longPressAction);
        } else {
            prefScreen.removePreference(menuCategory);
        }

        if (hasAssistKey) {
            if (!showAssistWake) {
                assistCategory.removePreference(findPreference(EVSettings.System.ASSIST_WAKE_SCREEN));
            }

            Action pressAction = Action.fromSettings(resolver,
                    EVSettings.System.KEY_ASSIST_ACTION, Action.SEARCH);
            mAssistPressAction = initList(KEY_ASSIST_PRESS, pressAction);

            Action longPressAction = Action.fromSettings(resolver,
                    EVSettings.System.KEY_ASSIST_LONG_PRESS_ACTION, Action.VOICE_SEARCH);
            mAssistLongPressAction = initList(KEY_ASSIST_LONG_PRESS, longPressAction);
        } else {
            prefScreen.removePreference(assistCategory);
        }

        if (hasAppSwitchKey) {
            if (!showAppSwitchWake) {
                appSwitchCategory.removePreference(findPreference(
                        EVSettings.System.APP_SWITCH_WAKE_SCREEN));
            }

            Action pressAction = Action.fromSettings(resolver,
                    EVSettings.System.KEY_APP_SWITCH_ACTION, Action.APP_SWITCH);
            mAppSwitchPressAction = initList(KEY_APP_SWITCH_PRESS, pressAction);

            Action longPressAction = Action.fromSettings(resolver,
                    EVSettings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION, Action.SPLIT_SCREEN);
            mAppSwitchLongPressAction = initList(KEY_APP_SWITCH_LONG_PRESS, longPressAction);
        } else {
            prefScreen.removePreference(appSwitchCategory);
        }

        if (hasCameraKey) {
            mCameraWakeScreen = (SwitchPreference) findPreference(EVSettings.System.CAMERA_WAKE_SCREEN);
            mCameraSleepOnRelease =
                    (SwitchPreference) findPreference(EVSettings.System.CAMERA_SLEEP_ON_RELEASE);
            mCameraLaunch = (SwitchPreference) findPreference(EVSettings.System.CAMERA_LAUNCH);

            if (!showCameraWake) {
                prefScreen.removePreference(mCameraWakeScreen);
            }
            // Only show 'Camera sleep on release' if the device has a focus key
            if (res.getBoolean(com.evervolv.platform.internal.R.bool.config_singleStageCameraKey)) {
                prefScreen.removePreference(mCameraSleepOnRelease);
            }
        } else {
            prefScreen.removePreference(cameraCategory);
        }

        if (hasVolumeKeys) {
            if (showVolumeWake) {
                SwitchPreference volumeWakeScreen = (SwitchPreference) findPreference(EVSettings.System.VOLUME_WAKE_SCREEN);
                if (volumeWakeScreen != null) {
                    SwitchPreference volumeMusicControls = (SwitchPreference) findPreference(EVSettings.System.VOLBTN_MUSIC_CONTROLS);
                    if (volumeMusicControls != null) {
                        volumeMusicControls.setDependency(EVSettings.System.VOLUME_WAKE_SCREEN);
                        volumeWakeScreen.setDisableDependentsState(true);
                    }
                }
            } else {
                volumeCategory.removePreference(findPreference(EVSettings.System.VOLUME_WAKE_SCREEN));
            }

            final boolean volumePanelOnLeft = EVSettings.Secure.getInt(
                    getContentResolver(), EVSettings.Secure.VOLUME_PANEL_ON_LEFT, 0) != 0;
            mVolumePanelOnLeft = (SwitchPreference)
                    prefScreen.findPreference(KEY_VOLUME_PANEL_ON_LEFT);
            if (mVolumePanelOnLeft != null) {
                mVolumePanelOnLeft.setChecked(volumePanelOnLeft);
            }
        } else {
            prefScreen.removePreference(volumeCategory);
        }

        // Only show the navigation bar category on devices that have a navigation bar
        // or support disabling the hardware keys
        if (!hasNavigationBar && !supportsKeyDisabler) {
            prefScreen.removePreference(mNavigationPreferencesCat);
        }

        if (mCameraWakeScreen != null) {
            if (mCameraSleepOnRelease != null && !res.getBoolean(
                    com.evervolv.platform.internal.R.bool.config_singleStageCameraKey)) {
                mCameraSleepOnRelease.setDependency(EVSettings.System.CAMERA_WAKE_SCREEN);
            }
        }
    }

    private ListPreference initList(String key, Action value) {
        return initList(key, value.ordinal());
    }

    private ListPreference initList(String key, int value) {
        ListPreference list = (ListPreference) getPreferenceScreen().findPreference(key);
        if (list == null) return null;
        list.setValue(Integer.toString(value));
        list.setSummary(list.getEntry());
        list.setOnPreferenceChangeListener(this);
        return list;
    }

    private void handleListChange(ListPreference pref, Object newValue, String setting) {
        String value = (String) newValue;
        int index = pref.findIndexOfValue(value);
        pref.setSummary(pref.getEntries()[index]);
        EVSettings.System.putInt(getContentResolver(), setting, Integer.valueOf(value));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mHomeLongPressAction) {
            handleListChange((ListPreference) preference, newValue,
                    EVSettings.System.KEY_HOME_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mHomeDoubleTapAction) {
            handleListChange((ListPreference) preference, newValue,
                    EVSettings.System.KEY_HOME_DOUBLE_TAP_ACTION);
            return true;
        } else if (preference == mMenuPressAction) {
            handleListChange(mMenuPressAction, newValue,
                    EVSettings.System.KEY_MENU_ACTION);
            return true;
        } else if (preference == mMenuLongPressAction) {
            handleListChange(mMenuLongPressAction, newValue,
                    EVSettings.System.KEY_MENU_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mAssistPressAction) {
            handleListChange(mAssistPressAction, newValue,
                    EVSettings.System.KEY_ASSIST_ACTION);
            return true;
        } else if (preference == mAssistLongPressAction) {
            handleListChange(mAssistLongPressAction, newValue,
                    EVSettings.System.KEY_ASSIST_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mAppSwitchPressAction) {
            handleListChange(mAppSwitchPressAction, newValue,
                    EVSettings.System.KEY_APP_SWITCH_ACTION);
            return true;
        } else if (preference == mAppSwitchLongPressAction) {
            handleListChange(mAppSwitchLongPressAction, newValue,
                    EVSettings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION);
            return true;
        }
        return false;
    }

    private static void writeDisableNavkeysOption(Context context, boolean enabled) {
        EVSettings.Secure.putInt(context.getContentResolver(),
                EVSettings.Secure.DEV_FORCE_SHOW_NAVBAR, enabled ? 1 : 0);
    }

    private void updateDisableNavkeysOption() {
        boolean enabled = EVSettings.Secure.getInt(getActivity().getContentResolver(),
                EVSettings.Secure.DEV_FORCE_SHOW_NAVBAR, 0) != 0;

        mDisableNavigationKeys.setChecked(enabled);
    }

    private void updateDisableNavkeysCategories(boolean navbarEnabled) {
        final PreferenceScreen prefScreen = getPreferenceScreen();

        /* Disable hw-key options if they're disabled */
        final PreferenceCategory homeCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_HOME);
        final PreferenceCategory backCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_BACK);
        final PreferenceCategory menuCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_MENU);
        final PreferenceCategory assistCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_ASSIST);
        final PreferenceCategory appSwitchCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_APPSWITCH);

        /* Toggle hardkey control availability depending on navbar state */
        if (homeCategory != null) {
            homeCategory.setEnabled(!navbarEnabled);
        }
        if (backCategory != null) {
            backCategory.setEnabled(!navbarEnabled);
        }
        if (menuCategory != null) {
            menuCategory.setEnabled(!navbarEnabled);
        }
        if (assistCategory != null) {
            assistCategory.setEnabled(!navbarEnabled);
        }
        if (appSwitchCategory != null) {
            appSwitchCategory.setEnabled(!navbarEnabled);
        }
    }

    private static boolean isKeyDisablerSupported(Context context) {
        HardwareManager hardware = HardwareManager.getInstance(context);
        return hardware.isSupported(HardwareManager.FEATURE_KEY_DISABLE);
    }

    public static void restoreKeyDisabler(Context context) {
        if (!isKeyDisablerSupported(context)) {
            return;
        }

        writeDisableNavkeysOption(context, EVSettings.Secure.getInt(context.getContentResolver(),
                EVSettings.Secure.DEV_FORCE_SHOW_NAVBAR, 0) != 0);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mVolumePanelOnLeft) {
            EVSettings.Secure.putInt(getActivity().getContentResolver(),
                    EVSettings.Secure.VOLUME_PANEL_ON_LEFT, mVolumePanelOnLeft.isChecked() ? 1 : 0);
            return true;
        } else if (preference == mDisableNavigationKeys) {
            mDisableNavigationKeys.setEnabled(false);
            mNavigationPreferencesCat.setEnabled(false);
            writeDisableNavkeysOption(getActivity(), mDisableNavigationKeys.isChecked());
            updateDisableNavkeysOption();
            updateDisableNavkeysCategories(true);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDisableNavigationKeys.setEnabled(true);
                    mNavigationPreferencesCat.setEnabled(mDisableNavigationKeys.isChecked());
                    updateDisableNavkeysCategories(mDisableNavigationKeys.isChecked());
                }
            }, 1000);
        }

        return super.onPreferenceTreeClick(preference);
    }
}
