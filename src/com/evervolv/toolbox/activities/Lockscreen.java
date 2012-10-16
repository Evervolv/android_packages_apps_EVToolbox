package com.evervolv.toolbox.activities;

import android.content.ContentResolver;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.SettingsFragment;
import com.evervolv.toolbox.activities.subactivities.LockscreenStyle;

public class Lockscreen extends SettingsFragment {

    private static final String LOCKSCREEN_STYLE_PREF = "pref_lockscreen_style";
    private static final String LOCKSCREEN_TRANSPARENT_PREF = "pref_lockscreen_transparent";

    private static final String TAG = "EVToolbox";

    private ContentResolver mCr;
    private PreferenceScreen mPrefSet;
    private PreferenceScreen mLockStyle;
    private CheckBoxPreference mLockTransparent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.lockscreen_settings);

        mPrefSet = getPreferenceScreen();
        mCr = getContentResolver();
        mLockStyle = (PreferenceScreen) mPrefSet.findPreference(
                LOCKSCREEN_STYLE_PREF);
        mLockTransparent = (CheckBoxPreference) mPrefSet.findPreference(
                LOCKSCREEN_TRANSPARENT_PREF);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;
        if (preference == mLockStyle) {
            startPreferencePanel(mLockStyle.getFragment(),
                    null, mLockStyle.getTitleRes(), null, null, -1);
            return true;
        } else if (preference == mLockTransparent) {
            value = mLockTransparent.isChecked();
            Settings.System.putInt(mCr, Settings.System.LOCKSCREEN_TRANSPARENT,
                    value ? 1 : 0);
        }
        return false;
    }

    public static class LockStyle extends LockscreenStyle { }
}
