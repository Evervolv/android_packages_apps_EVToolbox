/*
 * SPDX-FileCopyrightText: 2012 The CyanogenMod Project
 * SPDX-FileCopyrightText: 2017-2023 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.evervolv.toolbox.notificationlight;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.ArraySet;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;

import com.evervolv.internal.notification.LightsCapabilities;
import com.evervolv.settingslib.widget.EVSystemSettingMainSwitchPreference;
import com.evervolv.toolbox.R;
import com.evervolv.toolbox.SettingsPreferenceFragment;
import com.evervolv.toolbox.notificationlight.LightSettingsDialog.OnOffType;
import com.evervolv.toolbox.search.BaseSearchIndexProvider;
import com.evervolv.toolbox.search.Searchable;

import evervolv.preference.EVSystemSettingSwitchPreference;
import evervolv.provider.EVSettings;

import java.util.Set;

public class BatteryLightSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Searchable {
    private static final String TAG = "BatteryLightSettings";

    private static final String KEY_BATTERY_LIGHTS = "battery_lights";
    private static final String GENERAL_SECTION = "general_section";
    private static final String COLORS_SECTION = "colors_list";
    private static final String BRIGHTNESS_SECTION = "brightness_section";

    private static final String LOW_COLOR_PREF = "low_color";
    private static final String MEDIUM_COLOR_PREF = "medium_color";
    private static final String FULL_COLOR_PREF = "full_color";
    private static final String LIGHT_ENABLED_PREF = "battery_light_enabled";
    private static final String LIGHT_FULL_CHARGE_DISABLED_PREF =
            "battery_light_full_charge_disabled";
    private static final String PULSE_ENABLED_PREF = "battery_light_pulse";
    private static final String BRIGHTNESS_PREFERENCE = "battery_light_brightness_level";
    private static final String BRIGHTNESS_ZEN_PREFERENCE = "battery_light_brightness_level_zen";

    private static final boolean DEFAULT_LIGHT_ENABLED_PREF = true;
    private static final boolean DEFAULT_LIGHT_FULL_CHARGE_DISABLED_PREF = true;
    private static final boolean DEFAULT_PULSE_ENABLED_PREF = true;

    private ApplicationLightPreference mLowColorPref;
    private ApplicationLightPreference mMediumColorPref;
    private ApplicationLightPreference mFullColorPref;
    private EVSystemSettingMainSwitchPreference mLightEnabledPref;
    private EVSystemSettingSwitchPreference mLightFullChargeDisabledPref;
    private EVSystemSettingSwitchPreference mPulseEnabledPref;
    private BatteryBrightnessPreference mBatteryBrightnessPref;
    private BatteryBrightnessZenPreference mBatteryBrightnessZenPref;
    private int mDefaultLowColor;
    private int mDefaultMediumColor;
    private int mDefaultFullColor;
    private int mBatteryBrightness;
    // liblights supports brightness control
    private boolean mHALAdjustableBrightness;
    private boolean mMultiColorLed;

    private static final int MENU_RESET = Menu.FIRST;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Context context = requireContext();
        final Resources res = getResources();

        // Collect battery led capabilities.
        mMultiColorLed =
                LightsCapabilities.supports(context, LightsCapabilities.LIGHTS_RGB_BATTERY_LED);
        mHALAdjustableBrightness = LightsCapabilities.supports(
                context, LightsCapabilities.LIGHTS_ADJUSTABLE_BATTERY_LED_BRIGHTNESS);
        final boolean blinkingLed = LightsCapabilities.blinks(context);
        final boolean segmentedBatteryLed = LightsCapabilities.supports(context,
                LightsCapabilities.LIGHTS_SEGMENTED_BATTERY_LED);

        addPreferencesFromResource(R.xml.battery_light_settings);
        requireActivity().getActionBar().setTitle(R.string.battery_light_title);

        PreferenceScreen prefSet = getPreferenceScreen();

        PreferenceGroup generalPrefs = (PreferenceGroup) prefSet.findPreference(GENERAL_SECTION);

        mLightEnabledPref = prefSet.findPreference(LIGHT_ENABLED_PREF);
        mLightFullChargeDisabledPref = prefSet.findPreference(LIGHT_FULL_CHARGE_DISABLED_PREF);
        mPulseEnabledPref = prefSet.findPreference(PULSE_ENABLED_PREF);
        mBatteryBrightnessPref = prefSet.findPreference(BRIGHTNESS_PREFERENCE);
        mBatteryBrightnessZenPref = prefSet.findPreference(BRIGHTNESS_ZEN_PREFERENCE);

        mDefaultLowColor = res.getInteger(
                com.android.internal.R.integer.config_notificationsBatteryLowARGB);
        mDefaultMediumColor = res.getInteger(
                com.android.internal.R.integer.config_notificationsBatteryMediumARGB);
        mDefaultFullColor = res.getInteger(
                com.android.internal.R.integer.config_notificationsBatteryFullARGB);

        mBatteryBrightness = mBatteryBrightnessPref.getBrightnessSetting();

        mLightEnabledPref.setDefaultValue(DEFAULT_LIGHT_ENABLED_PREF);
        mLightFullChargeDisabledPref.setDefaultValue(DEFAULT_LIGHT_FULL_CHARGE_DISABLED_PREF);
        mPulseEnabledPref.setDefaultValue(DEFAULT_PULSE_ENABLED_PREF);

        if (!blinkingLed || segmentedBatteryLed) {
            generalPrefs.removePreference(mPulseEnabledPref);
        }

        if (mMultiColorLed) {
            generalPrefs.removePreference(mLightFullChargeDisabledPref);
            setHasOptionsMenu(true);

            // Low, Medium and full color preferences
            mLowColorPref = prefSet.findPreference(LOW_COLOR_PREF);
            mLowColorPref.setOnPreferenceChangeListener(this);
            mLowColorPref.setDefaultValues(mDefaultLowColor, 0, 0);
            mLowColorPref.setBrightness(mBatteryBrightness);

            mMediumColorPref = prefSet.findPreference(MEDIUM_COLOR_PREF);
            mMediumColorPref.setOnPreferenceChangeListener(this);
            mMediumColorPref.setDefaultValues(mDefaultMediumColor, 0, 0);
            mMediumColorPref.setBrightness(mBatteryBrightness);

            mFullColorPref = prefSet.findPreference(FULL_COLOR_PREF);
            mFullColorPref.setOnPreferenceChangeListener(this);
            mFullColorPref.setDefaultValues(mDefaultFullColor, 0, 0);
            mFullColorPref.setBrightness(mBatteryBrightness);

            final BrightnessPreference.OnBrightnessChangedListener brightnessListener =
                    new BrightnessPreference.OnBrightnessChangedListener() {
                @Override
                public void onBrightnessChanged(int brightness) {
                    mLowColorPref.setBrightness(brightness);
                    mMediumColorPref.setBrightness(brightness);
                    mFullColorPref.setBrightness(brightness);
                }
            };
            mBatteryBrightnessPref.setOnBrightnessChangedListener(brightnessListener);
        } else {
            prefSet.removePreference(prefSet.findPreference(COLORS_SECTION));
            resetColors();
        }

        // Remove battery LED brightness controls if we can't support them.
        if (!mMultiColorLed && !mHALAdjustableBrightness) {
            prefSet.removePreference(prefSet.findPreference(BRIGHTNESS_SECTION));
        }

        watch(EVSettings.System.getUriFor(EVSettings.System.BATTERY_LIGHT_ENABLED));
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshColors();
    }

    private void refreshColors() {
        ContentResolver resolver = requireActivity().getContentResolver();

        if (mLowColorPref != null) {
            int lowColor = EVSettings.System.getInt(resolver,
                    EVSettings.System.BATTERY_LIGHT_LOW_COLOR, mDefaultLowColor);
            mLowColorPref.setAllValues(lowColor, 0, 0, OnOffType.TOGGLE);
        }

        if (mMediumColorPref != null) {
            int mediumColor = EVSettings.System.getInt(resolver,
                    EVSettings.System.BATTERY_LIGHT_MEDIUM_COLOR, mDefaultMediumColor);
            mMediumColorPref.setAllValues(mediumColor, 0, 0, OnOffType.TOGGLE);
        }

        if (mFullColorPref != null) {
            int fullColor = EVSettings.System.getInt(resolver,
                    EVSettings.System.BATTERY_LIGHT_FULL_COLOR, mDefaultFullColor);
            mFullColorPref.setAllValues(fullColor, 0, 0, OnOffType.TOGGLE);
            updateBrightnessPrefColor(fullColor);
        }
    }

    private void updateBrightnessPrefColor(int color) {
        // If the user has selected no light (ie black) for
        // full charge, use white for the brightness preference.
        if (color == 0) {
            color = 0xFFFFFF;
        }
        mBatteryBrightnessPref.setLedColor(color);
        mBatteryBrightnessZenPref.setLedColor(color);
    }

    /**
     * Updates the default or application specific notification settings.
     *
     * @param key of the specific setting to update
     */
    protected void updateValues(String key, Integer color) {
        ContentResolver resolver = requireActivity().getContentResolver();
        switch (key) {
            case LOW_COLOR_PREF:
                EVSettings.System.putInt(resolver,
                        EVSettings.System.BATTERY_LIGHT_LOW_COLOR, color);
                break;
            case MEDIUM_COLOR_PREF:
                EVSettings.System.putInt(resolver,
                        EVSettings.System.BATTERY_LIGHT_MEDIUM_COLOR, color);
                break;
            case FULL_COLOR_PREF:
                EVSettings.System.putInt(resolver,
                        EVSettings.System.BATTERY_LIGHT_FULL_COLOR, color);
                updateBrightnessPrefColor(color);
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mMultiColorLed) {
            menu.add(0, MENU_RESET, 0, R.string.reset)
                    .setIcon(R.drawable.ic_settings_backup_restore)
                    .setAlphabeticShortcut('r')
                    .setShowAsActionFlags(
                            MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                resetToDefaults();
                return true;
        }
        return false;
    }

    protected void resetColors() {
        ContentResolver resolver = requireActivity().getContentResolver();

        // Reset to the framework default colors
        EVSettings.System.putInt(resolver, EVSettings.System.BATTERY_LIGHT_LOW_COLOR,
                mDefaultLowColor);
        EVSettings.System.putInt(resolver, EVSettings.System.BATTERY_LIGHT_MEDIUM_COLOR,
                mDefaultMediumColor);
        EVSettings.System.putInt(resolver, EVSettings.System.BATTERY_LIGHT_FULL_COLOR,
                mDefaultFullColor);
        refreshColors();
    }

    protected void resetToDefaults() {
        final Resources res = getResources();
        final boolean batteryLightEnabled = DEFAULT_LIGHT_ENABLED_PREF;
        final boolean batteryLightFullChargeDisabled = DEFAULT_LIGHT_FULL_CHARGE_DISABLED_PREF;
        final boolean batteryLightPulseEnabled = DEFAULT_PULSE_ENABLED_PREF;

        if (mLightEnabledPref != null) mLightEnabledPref.setChecked(batteryLightEnabled);
        if (mLightFullChargeDisabledPref != null) {
            mLightFullChargeDisabledPref.setChecked(batteryLightFullChargeDisabled);
        }
        if (mPulseEnabledPref != null) mPulseEnabledPref.setChecked(batteryLightPulseEnabled);

        resetColors();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ApplicationLightPreference lightPref = (ApplicationLightPreference) preference;
        updateValues(lightPref.getKey(), lightPref.getColor());
        return true;
    }

    public static final SummaryProvider SUMMARY_PROVIDER = new SummaryProvider() {
        @Override
        public String getSummary(Context context, String key) {
            if (EVSettings.System.getInt(context.getContentResolver(),
                    EVSettings.System.BATTERY_LIGHT_ENABLED, 1) == 1) {
                return context.getString(R.string.enabled);
            }
            return context.getString(R.string.disabled);
        }
    };

    public static final Searchable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {

        @Override
        public Set<String> getNonIndexableKeys(Context context) {
            final Set<String> result = new ArraySet<>();

            if (!LightsCapabilities.supports(context, LightsCapabilities.LIGHTS_BATTERY_LED)) {
                result.add(KEY_BATTERY_LIGHTS);
                result.add(LIGHT_ENABLED_PREF);
                result.add(GENERAL_SECTION);
                result.add(COLORS_SECTION);
                result.add(LOW_COLOR_PREF);
                result.add(MEDIUM_COLOR_PREF);
                result.add(FULL_COLOR_PREF);
            } else {
                result.add(COLORS_SECTION);
                result.add(LOW_COLOR_PREF);
                result.add(MEDIUM_COLOR_PREF);
                result.add(FULL_COLOR_PREF);
            }
            if (!LightsCapabilities.supports(context,
                    LightsCapabilities.LIGHTS_ADJUSTABLE_BATTERY_LED_BRIGHTNESS)) {
                result.add(BRIGHTNESS_SECTION);
                result.add(BRIGHTNESS_PREFERENCE);
                result.add(BRIGHTNESS_ZEN_PREFERENCE);
            }
            if (!LightsCapabilities.blinks(context) ||
                    LightsCapabilities.supports(context,
                            LightsCapabilities.LIGHTS_SEGMENTED_BATTERY_LED)) {
                result.add(PULSE_ENABLED_PREF);
            }
            return result;
        }
    };
}
