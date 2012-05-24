package com.evervolv.toolbox.activities;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.SettingsFragment;
import com.evervolv.toolbox.activities.subactivities.InterfaceButtons;
import com.evervolv.toolbox.activities.subactivities.InterfaceRotation;

public class Interface extends SettingsFragment {

    private static final String TAG = "EVToolbox";

    private static final String BUTTONS_PREF = "pref_interface_buttons";
    private static final String ROTATION_PREF = "pref_interface_rotation";

    private PreferenceScreen mPrefSet;
    private PreferenceScreen mButtons;
    private PreferenceScreen mRotation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.interface_settings);

        mPrefSet = getPreferenceScreen();

        mButtons = (PreferenceScreen) mPrefSet.findPreference(BUTTONS_PREF);
        mButtons = (PreferenceScreen) mPrefSet.findPreference(ROTATION_PREF);

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mButtons) {
            startPreferencePanel(mButtons.getFragment(),
                    null, mButtons.getTitleRes(), null, null, -1);
            return true;
        } else if (preference == mRotation) {
            startPreferencePanel(mRotation.getFragment(),
                    null, mRotation.getTitleRes(), null, null, -1);
            return true;
        }
        return false;
    }

    public static class Buttons extends InterfaceButtons { }
    public static class Rotation extends InterfaceRotation { }
}
