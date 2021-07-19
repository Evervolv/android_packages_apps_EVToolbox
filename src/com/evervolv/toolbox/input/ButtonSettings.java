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
import static com.evervolv.internal.util.DeviceKeysConstants.*;

import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.ArraySet;
import androidx.preference.SwitchPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import android.util.Log;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;

import evervolv.hardware.HardwareManager;
import evervolv.preference.RemotePreference;
import evervolv.provider.EVSettings;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.SettingsPreferenceFragment;
import com.evervolv.toolbox.search.BaseSearchIndexProvider;
import com.evervolv.toolbox.search.Searchable;
import com.evervolv.toolbox.utils.DeviceUtils;

import java.util.List;
import java.util.Set;

public class ButtonSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener, Searchable {
    private static final String TAG = "ButtonSettings";

    private static final String KEY_BUTTON_BACKLIGHT = "button_backlight";
    private static final String KEY_BACK_WAKE_SCREEN = "back_wake_screen";
    private static final String KEY_CAMERA_LAUNCH = "camera_launch";
    private static final String KEY_CAMERA_SLEEP_ON_RELEASE = "camera_sleep_on_release";
    private static final String KEY_CAMERA_WAKE_SCREEN = "camera_wake_screen";
    private static final String KEY_HOME_LONG_PRESS = "button_home_long_press";
    private static final String KEY_HOME_DOUBLE_TAP = "button_home_double_tap";
    private static final String KEY_HOME_WAKE_SCREEN = "home_wake_screen";
    private static final String KEY_MENU_PRESS = "button_menu_press";
    private static final String KEY_MENU_LONG_PRESS = "button_menu_long_press";
    private static final String KEY_MENU_WAKE_SCREEN = "menu_wake_screen";
    private static final String KEY_ASSIST_PRESS = "button_assist_press";
    private static final String KEY_ASSIST_LONG_PRESS = "button_assist_long_press";
    private static final String KEY_ASSIST_WAKE_SCREEN = "assist_wake_screen";
    private static final String KEY_APP_SWITCH_PRESS = "button_app_switch_press";
    private static final String KEY_APP_SWITCH_LONG_PRESS = "button_app_switch_long_press";
    private static final String KEY_APP_SWITCH_WAKE_SCREEN = "app_switch_wake_screen";
    private static final String KEY_VOLUME_MUSIC_CONTROLS = "volbtn_music_controls";
    private static final String KEY_VOLUME_WAKE_SCREEN = "volume_wake_screen";
    private static final String KEY_ADDITIONAL_BUTTONS = "additional_buttons";

    private static final String KEY_DISABLE_NAV_KEYS = "disable_nav_keys";

    private static final String KEY_SWAP_CAPACITIVE_KEYS = "swap_capacitive_keys";

    private static final String CATEGORY_HOME = "home_key";
    private static final String CATEGORY_BACK = "back_key";
    private static final String CATEGORY_MENU = "menu_key";
    private static final String CATEGORY_ASSIST = "assist_key";
    private static final String CATEGORY_APPSWITCH = "app_switch_key";
    private static final String CATEGORY_VOLUME = "volume_keys";
    private static final String CATEGORY_CAMERA = "camera_key";
    private static final String CATEGORY_EXTRAS = "extras_category";

    private ListPreference mHomeLongPressAction;
    private ListPreference mHomeDoubleTapAction;
    private ListPreference mMenuPressAction;
    private ListPreference mMenuLongPressAction;
    private ListPreference mAssistPressAction;
    private ListPreference mAssistLongPressAction;
    private ListPreference mAppSwitchPressAction;
    private ListPreference mAppSwitchLongPressAction;

    private SwitchPreference mDisableNavigationKeys;
    private Handler mHandler;

    private SwitchPreference mCameraWakeScreen;
    private SwitchPreference mCameraSleepOnRelease;
    private SwitchPreference mCameraLaunch;

    private SwitchPreference mSwapCapacitiveKeys;

    private HardwareManager mHardware;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHardware = HardwareManager.getInstance(getActivity());

        addPreferencesFromResource(R.xml.button_settings);

        final Resources res = getResources();
        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        final int deviceKeys = res.getInteger(
                com.evervolv.platform.internal.R.integer.config_deviceHardwareKeys);
        final int deviceWakeKeys = res.getInteger(
                com.evervolv.platform.internal.R.integer.config_deviceHardwareWakeKeys);

        final boolean hasHomeKey = DeviceUtils.hasHomeKey(getActivity());
        final boolean hasBackKey = DeviceUtils.hasBackKey(getActivity());
        final boolean hasMenuKey = DeviceUtils.hasMenuKey(getActivity());
        final boolean hasAssistKey = DeviceUtils.hasAssistKey(getActivity());
        final boolean hasAppSwitchKey = DeviceUtils.hasAppSwitchKey(getActivity());
        final boolean hasCameraKey = DeviceUtils.hasCameraKey(getActivity());
        final boolean hasVolumeKeys = DeviceUtils.hasVolumeKeys(getActivity());

        final boolean showHomeWake = DeviceUtils.canWakeUsingHomeKey(getActivity());
        final boolean showBackWake = DeviceUtils.canWakeUsingBackKey(getActivity());
        final boolean showMenuWake = DeviceUtils.canWakeUsingMenuKey(getActivity());
        final boolean showAssistWake = DeviceUtils.canWakeUsingAssistKey(getActivity());
        final boolean showAppSwitchWake = DeviceUtils.canWakeUsingAppSwitchKey(getActivity());
        final boolean showCameraWake = DeviceUtils.canWakeUsingCameraKey(getActivity());
        final boolean showVolumeWake = DeviceUtils.canWakeUsingVolumeKeys(getActivity());

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
        final PreferenceGroup extrasCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_EXTRAS);

        mHandler = new Handler();

        // Force Navigation bar related options
        mDisableNavigationKeys = (SwitchPreference) findPreference(KEY_DISABLE_NAV_KEYS);

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
            updateDisableNavkeysCategories(mDisableNavigationKeys.isChecked());
            mDisableNavigationKeys.setDisableDependentsState(true);
        } else {
            prefScreen.removePreference(mDisableNavigationKeys);
        }

        if (hasHomeKey) {
            if (!showHomeWake) {
                homeCategory.removePreference(findPreference(KEY_HOME_WAKE_SCREEN));
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
                menuCategory.removePreference(findPreference(KEY_MENU_WAKE_SCREEN));
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
                assistCategory.removePreference(findPreference(KEY_ASSIST_WAKE_SCREEN));
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
                appSwitchCategory.removePreference(findPreference(KEY_APP_SWITCH_WAKE_SCREEN));
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
            mCameraWakeScreen = (SwitchPreference) findPreference(KEY_CAMERA_WAKE_SCREEN);
            mCameraSleepOnRelease =
                    (SwitchPreference) findPreference(KEY_CAMERA_SLEEP_ON_RELEASE);
            mCameraLaunch = (SwitchPreference) findPreference(KEY_CAMERA_LAUNCH);

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
                SwitchPreference volumeWakeScreen = (SwitchPreference) findPreference(KEY_VOLUME_WAKE_SCREEN);
                if (volumeWakeScreen != null) {
                    SwitchPreference volumeMusicControls = (SwitchPreference) findPreference(KEY_VOLUME_MUSIC_CONTROLS);
                    if (volumeMusicControls != null) {
                        volumeMusicControls.setDependency(KEY_VOLUME_WAKE_SCREEN);
                        volumeWakeScreen.setDisableDependentsState(true);
                    }
                }
            } else {
                volumeCategory.removePreference(findPreference(KEY_VOLUME_WAKE_SCREEN));
            }
        } else {
            prefScreen.removePreference(volumeCategory);
        }

        mSwapCapacitiveKeys = findPreference(KEY_SWAP_CAPACITIVE_KEYS);
        if (mSwapCapacitiveKeys != null && !isKeySwapperSupported(getActivity())) {
            prefScreen.removePreference(mSwapCapacitiveKeys);
        } else {
            mSwapCapacitiveKeys.setOnPreferenceChangeListener(this);
            mSwapCapacitiveKeys.setDependency(KEY_DISABLE_NAV_KEYS);
        }

        final ButtonBacklightBrightness backlight = findPreference(KEY_BUTTON_BACKLIGHT);
        if (!DeviceUtils.hasButtonBacklightSupport(getActivity())
                /*&& !backlight.isKeyboardSupported(getActivity())*/) {
            prefScreen.removePreference(backlight);
        }

        if (mCameraWakeScreen != null) {
            if (mCameraSleepOnRelease != null && !res.getBoolean(
                    com.evervolv.platform.internal.R.bool.config_singleStageCameraKey)) {
                mCameraSleepOnRelease.setDependency(KEY_CAMERA_WAKE_SCREEN);
            }
        }

        final RemotePreference additionalButtons = extrasCategory.findPreference(KEY_ADDITIONAL_BUTTONS);
        if (!additionalButtons.isAvailable()) {
            prefScreen.removePreference(extrasCategory);
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
        } else if (preference == mSwapCapacitiveKeys) {
            mHardware.set(HardwareManager.FEATURE_KEY_SWAP, (Boolean) newValue);
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
        final ButtonBacklightBrightness backlight =
                (ButtonBacklightBrightness) prefScreen.findPreference(KEY_BUTTON_BACKLIGHT);

        /* Toggle backlight control depending on navbar state, force it to
           off if enabling */
        if (backlight != null) {
            backlight.setEnabled(!navbarEnabled);
            backlight.updateSummary();
        }

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

    private static boolean isKeySwapperSupported(Context context) {
        HardwareManager hardware = HardwareManager.getInstance(context);
        return hardware.isSupported(HardwareManager.FEATURE_KEY_SWAP);
    }

    public static void restoreKeyDisabler(Context context) {
        if (!isKeyDisablerSupported(context)) {
            return;
        }

        writeDisableNavkeysOption(context, EVSettings.Secure.getInt(context.getContentResolver(),
                EVSettings.Secure.DEV_FORCE_SHOW_NAVBAR, 0) != 0);
    }

    public static void restoreKeySwapper(Context context) {
        if (!isKeySwapperSupported(context)) {
            return;
        }

        final SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        HardwareManager hardware = HardwareManager.getInstance(context);
        hardware.set(HardwareManager.FEATURE_KEY_SWAP,
                preferences.getBoolean(KEY_SWAP_CAPACITIVE_KEYS, false));
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mDisableNavigationKeys) {
            mDisableNavigationKeys.setEnabled(false);
            writeDisableNavkeysOption(getActivity(), mDisableNavigationKeys.isChecked());
            updateDisableNavkeysOption();
            updateDisableNavkeysCategories(true);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDisableNavigationKeys.setEnabled(true);
                    updateDisableNavkeysCategories(mDisableNavigationKeys.isChecked());
                }
            }, 1000);
        }

        return super.onPreferenceTreeClick(preference);
    }

    public static final Searchable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {

        @Override
        public Set<String> getNonIndexableKeys(Context context) {
            final Set<String> result = new ArraySet<String>();

            if (!DeviceUtils.hasBackKey(context)
                    || !DeviceUtils.canWakeUsingBackKey(context)) {
                result.add(CATEGORY_BACK);
                result.add(KEY_BACK_WAKE_SCREEN);
            }

            if (!DeviceUtils.hasHomeKey(context)) {
                result.add(CATEGORY_HOME);
                result.add(KEY_HOME_LONG_PRESS);
                result.add(KEY_HOME_DOUBLE_TAP);
                result.add(KEY_HOME_WAKE_SCREEN);
            } else if (!DeviceUtils.canWakeUsingHomeKey(context)) {
                result.add(KEY_HOME_WAKE_SCREEN);
            }

            if (!DeviceUtils.hasMenuKey(context)) {
                result.add(CATEGORY_MENU);
                result.add(KEY_MENU_PRESS);
                result.add(KEY_MENU_LONG_PRESS);
                result.add(KEY_MENU_WAKE_SCREEN);
            } else if (!DeviceUtils.canWakeUsingMenuKey(context)) {
                result.add(KEY_MENU_WAKE_SCREEN);
            }

            if (!DeviceUtils.hasAssistKey(context)) {
                result.add(CATEGORY_ASSIST);
                result.add(KEY_ASSIST_PRESS);
                result.add(KEY_ASSIST_LONG_PRESS);
                result.add(KEY_ASSIST_WAKE_SCREEN);
            } else if (!DeviceUtils.canWakeUsingAssistKey(context)) {
                result.add(KEY_ASSIST_WAKE_SCREEN);
            }

            if (!DeviceUtils.hasAppSwitchKey(context)) {
                result.add(CATEGORY_APPSWITCH);
                result.add(KEY_APP_SWITCH_PRESS);
                result.add(KEY_APP_SWITCH_LONG_PRESS);
                result.add(KEY_APP_SWITCH_WAKE_SCREEN);
            } else if (!DeviceUtils.canWakeUsingAppSwitchKey(context)) {
                result.add(KEY_APP_SWITCH_WAKE_SCREEN);
            }

            if (!DeviceUtils.hasCameraKey(context)) {
                result.add(CATEGORY_CAMERA);
                result.add(KEY_CAMERA_LAUNCH);
                result.add(KEY_CAMERA_SLEEP_ON_RELEASE);
                result.add(KEY_CAMERA_WAKE_SCREEN);
            } else if (!DeviceUtils.canWakeUsingCameraKey(context)) {
                result.add(KEY_CAMERA_WAKE_SCREEN);
            }

            if (!DeviceUtils.hasVolumeKeys(context)) {
                result.add(CATEGORY_VOLUME);
                result.add(KEY_VOLUME_MUSIC_CONTROLS);
                result.add(KEY_VOLUME_WAKE_SCREEN);
            } else if (!DeviceUtils.canWakeUsingVolumeKeys(context)) {
                result.add(KEY_VOLUME_WAKE_SCREEN);
            }

            if (!isKeyDisablerSupported(context)) {
                result.add(KEY_DISABLE_NAV_KEYS);
            }

            if (!isKeySwapperSupported(context)) {
                result.add(KEY_SWAP_CAPACITIVE_KEYS);
            }

            if (!DeviceUtils.hasButtonBacklightSupport(context)) {
                result.add(KEY_BUTTON_BACKLIGHT);
            }
            return result;
        }
    };
}
