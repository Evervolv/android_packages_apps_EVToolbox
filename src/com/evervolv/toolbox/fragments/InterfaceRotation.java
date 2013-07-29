package com.evervolv.toolbox.fragments;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.evervolv.toolbox.R;

public class InterfaceRotation extends PreferenceFragment {

    private static final String ROTATION_0_PREF = "pref_rotation_0";
    private static final String ROTATION_90_PREF = "pref_rotation_90";
    private static final String ROTATION_180_PREF = "pref_rotation_180";
    private static final String ROTATION_270_PREF = "pref_rotation_270";

    private static final int ROTATION_0 = 1;
    private static final int ROTATION_90 = 2;
    private static final int ROTATION_180 = 4;
    private static final int ROTATION_270 = 8;

    private CheckBoxPreference mRotation0Pref;
    private CheckBoxPreference mRotation90Pref;
    private CheckBoxPreference mRotation180Pref;
    private CheckBoxPreference mRotation270Pref;

    private ContentResolver mCr;
    private PreferenceScreen mPrefSet;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.interface_rotation);

        mPrefSet = getPreferenceScreen();
        mCr = getActivity().getContentResolver();

        mRotation0Pref = (CheckBoxPreference) mPrefSet.findPreference(ROTATION_0_PREF);
        mRotation90Pref = (CheckBoxPreference) mPrefSet.findPreference(ROTATION_90_PREF);
        mRotation180Pref = (CheckBoxPreference) mPrefSet.findPreference(ROTATION_180_PREF);
        mRotation270Pref = (CheckBoxPreference) mPrefSet.findPreference(ROTATION_270_PREF);

        int defaultAngles = ROTATION_0 | ROTATION_90 | ROTATION_270;
        // 180 is default enabled on tablets, disabled on phones
        if (getResources().getBoolean(com.android.internal.R.bool.config_allowAllRotations)) {
            defaultAngles |= ROTATION_180;
        }


        int angles = Settings.System.getInt(mCr,
                Settings.System.ACCELEROMETER_ROTATION_ANGLES, defaultAngles);
        mRotation0Pref.setChecked((angles & ROTATION_0) != 0);
        mRotation90Pref.setChecked((angles & ROTATION_90) != 0);
        mRotation180Pref.setChecked((angles & ROTATION_180) != 0);
        mRotation270Pref.setChecked((angles & ROTATION_270) != 0);
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;

        if (preference == mRotation0Pref ||
            preference == mRotation90Pref ||
            preference == mRotation180Pref ||
            preference == mRotation270Pref) {
            int angles = 0;
            if (mRotation0Pref.isChecked()) angles |= ROTATION_0;
            if (mRotation90Pref.isChecked()) angles |= ROTATION_90;
            if (mRotation180Pref.isChecked()) angles |= ROTATION_180;
            if (mRotation270Pref.isChecked()) angles |= ROTATION_270;

            Settings.System.putInt(mCr,
                     Settings.System.ACCELEROMETER_ROTATION_ANGLES, angles);
        }

        return true;
    }

}
