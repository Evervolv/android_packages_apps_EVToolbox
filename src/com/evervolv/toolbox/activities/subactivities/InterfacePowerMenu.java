package com.evervolv.toolbox.activities.subactivities;

import android.content.ContentResolver;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.SettingsFragment;

public class InterfacePowerMenu extends SettingsFragment {

    private static final String PREF_SHOW_SCREENSHOT = "pref_power_menu_show_screenshot";
    private static final String PREF_SHOW_SOUND = "pref_power_menu_show_sound";
    private static final String PREF_SHOW_AIRPLANE_MODE = "pref_power_menu_show_airplane_mode";
    private static final String PREF_SHOW_REBOOT_MENU = "pref_power_menu_show_reboot_menu";
    
    private ContentResolver mCr;
    private PreferenceScreen mPrefSet;

    private CheckBoxPreference mShowScreenshot;
    private CheckBoxPreference mShowSound;
    private CheckBoxPreference mShowAirplaneMode;
    private CheckBoxPreference mShowRebootMenu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.interface_power_menu);

        mPrefSet = getPreferenceScreen();
        mCr = getContentResolver();

        mShowScreenshot = (CheckBoxPreference) mPrefSet.findPreference(
                PREF_SHOW_SCREENSHOT);
        mShowScreenshot.setChecked(Settings.System.getInt(getContext().getContentResolver(),
                Settings.System.POWER_MENU_SHOW_SCREENSHOT, 1) == 1);

        mShowSound = (CheckBoxPreference) mPrefSet.findPreference(
                PREF_SHOW_SOUND);
        mShowSound.setChecked(Settings.System.getInt(getContext().getContentResolver(),
                Settings.System.POWER_MENU_SHOW_SOUND, 1) == 1);

        mShowAirplaneMode = (CheckBoxPreference) mPrefSet.findPreference(
                PREF_SHOW_AIRPLANE_MODE);
        mShowAirplaneMode.setChecked(Settings.System.getInt(getContext().getContentResolver(),
                Settings.System.POWER_MENU_SHOW_AIRPLANE_MODE, 1) == 1);
        
        mShowRebootMenu = (CheckBoxPreference) mPrefSet.findPreference(
                PREF_SHOW_REBOOT_MENU);
        mShowRebootMenu.setChecked(Settings.System.getInt(getContext().getContentResolver(),
                Settings.System.POWER_MENU_SHOW_REBOOT_MENU, 1) == 1);
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;
        
        if (preference == mShowScreenshot) {
            value = mShowScreenshot.isChecked();
            Settings.System.putInt(mCr, Settings.System.POWER_MENU_SHOW_SCREENSHOT,
                    value ? 1 : 0);
            return true;
        } else if (preference == mShowSound) {
            value = mShowSound.isChecked();
            Settings.System.putInt(mCr, Settings.System.POWER_MENU_SHOW_SOUND,
                    value ? 1 : 0);
            return true;
        } else if (preference == mShowAirplaneMode) {
            value = mShowAirplaneMode.isChecked();
            Settings.System.putInt(mCr, Settings.System.POWER_MENU_SHOW_AIRPLANE_MODE,
                    value ? 1 : 0);
            return true;
        } else if (preference == mShowRebootMenu) {
            value = mShowRebootMenu.isChecked();
            Settings.System.putInt(mCr, Settings.System.POWER_MENU_SHOW_REBOOT_MENU,
                    value ? 1 : 0);
            return true;
        }
        return false;
    }

}
